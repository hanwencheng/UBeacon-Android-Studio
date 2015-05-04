package mci.uni.stuttgart.bilget.scan;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mci.uni.stuttgart.bilget.BeaconService;
import mci.uni.stuttgart.bilget.BeaconsInfo;
import mci.uni.stuttgart.bilget.BeaconsViewHolder;
import mci.uni.stuttgart.bilget.IBeacon;
import mci.uni.stuttgart.bilget.R;
import mci.uni.stuttgart.bilget.SettingsActivity;
import mci.uni.stuttgart.bilget.Util.RecyclerItemClickListener;
import mci.uni.stuttgart.bilget.Util.SoundPoolPlayer;
import mci.uni.stuttgart.bilget.Util.VibratorBuilder;
import mci.uni.stuttgart.bilget.algorithm.CalcList;
import mci.uni.stuttgart.bilget.database.BeaconDBHelper;
import mci.uni.stuttgart.bilget.scan.BeaconsAdapter;

public class ScanListFragment extends Fragment
                                implements SharedPreferences.OnSharedPreferenceChangeListener,
        BeaconsAdapter.OnListHeadChange
{
    //UI sweeties.
	private RecyclerView mRecyclerView;
	private Adapter<BeaconsViewHolder> mAdapter;
    private List<BeaconsInfo> resultList;
	private SwipeRefreshLayout swipeLayout;
    private Button startServiceButton;
    private Button stopServiceButton;

	//state variables
	private String currentLocation = null;
    private List<BeaconsInfo> savedListInstance;

    //Request code constants
	private final static int REQUEST_ENABLE_BT = 1;
	private final static int MY_DATA_CHECK_CODE = 2;
    private final static int REQUEST_SETTINGS =3;

    //text to speech constants
	private TextToSpeech mSpeech;
	private final static long UPDATE_PERIOD = 5000 ;
	private final static String SPEAK_NAME = "name";//text to speech utteranceId
	private final static String TAG = "Ubeacon";
    private final static String IS_TTS_ENABLE = "isTTSEnabled";

    //preference settings
    private int thresholdPref;
    private String frequencyPref;
    private String audioHintPref;

    private SoundPoolPlayer player;
    private VibratorBuilder vibrator;

    private Handler mHandler;
	private IBeacon beaconInterface;
    private SharedPreferences sharedPreferences;
    private CalcList calcList;


//	========================================Initialization==========================================
//	================================================================================================
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);//TODO

        setHasOptionsMenu(true);//default is false;

        //assign DOM element
		View rootView = inflater.inflate(R.layout.scan_fragment_main, container,
				false);
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.scan_recycler_view);
		registerForContextMenu(mRecyclerView);
		startServiceButton = (Button) rootView.findViewById(R.id.service_start);
		stopServiceButton = (Button) rootView.findViewById(R.id.service_stop);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        calcList = CalcList.getInstance();// init algorithm module

        initRecyclerView(mRecyclerView);

//		======================set UI event listener======================
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				swipeLayout.setRefreshing(true);
                //do magic stuff here.
				mHandler.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                	swipeLayout.setRefreshing(false);
	                }
	            }, 1000);
			}
		});

        //enable swipe view only when we reach the top of the list
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean enable = false;
                if(mRecyclerView != null && mRecyclerView.getChildCount() > 0){
                    enable = mRecyclerView.getChildAt(0).getTop() == 0 ;
                }
                swipeLayout.setEnabled(enable);
            }
        });

        //expand when item is clicked
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    private int mOriginalHeight = 0;
                    private boolean mIsViewExpanded = false;
                    private LinearLayout vExpandArea;
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d(TAG, "now touched on the view");
                        if (mOriginalHeight == 0) {
                            mOriginalHeight = view.getHeight();
                        }
                        ValueAnimator valueAnimator;
                        if (!mIsViewExpanded) {
                            mIsViewExpanded = true;
                            valueAnimator = ValueAnimator.ofInt(mOriginalHeight, mOriginalHeight + (int) (mOriginalHeight * 1.5));
                            view.findViewById(R.id.expandArea).setVisibility(View.VISIBLE);
                        } else {
                            mIsViewExpanded = false;
                            valueAnimator = ValueAnimator.ofInt(mOriginalHeight + (int) (mOriginalHeight * 1.5), mOriginalHeight);
                            view.findViewById(R.id.expandArea).setVisibility(View.GONE);
                        }
                        valueAnimator.setDuration(200);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        final View theView = view;
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                theView.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                                theView.requestLayout();
                            }
                        });
                        valueAnimator.start();
                    }
                })
        );
		
		startServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "start button clicked");
                speakOut("scanning start");
				Intent intent = new Intent(getActivity(), BeaconService.class);
				getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
			}
		});
		
		stopServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "stop button clicked");
                speakOut("scanning stops");
				doUnBindService();
				//unBind service and end the UI update runnable.
				getActivity().unbindService(mServiceConnection);
			}
		});

        resultList =  new ArrayList<BeaconsInfo>();
        // specify an adapter (see also next example)
        mAdapter = new BeaconsAdapter(resultList, this, new BeaconDBHelper(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        
        mHandler = new Handler();

        checkBLE(getActivity());
        
        checkBluetooth(getActivity());

        player = SoundPoolPlayer.getInstance(getActivity());
        vibrator = VibratorBuilder.getInstance(getActivity());

        if(savedInstanceState!=null){
//            resultList = savedInstanceState.getParcelableArrayList("list");
//            mAdapter.notifyDataSetChanged();
        }
        
		return rootView;
	}

//		======================action bar callback======================
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //TTS quick setting when setting button is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_enableTTS:
                if(item.isChecked()){
                    disableTTS();
                    item.setChecked(false);
                }else{
                    enableTTS();
                    item.setChecked(true);
                }
                sharedPreferences.edit().putBoolean(IS_TTS_ENABLE, item.isChecked()).apply();
                return true;
            //open more settings page
            case R.id.action_setting_activity:
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(i, REQUEST_SETTINGS);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        //initial menu here.
        menuInflater.inflate(R.menu.options, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info =  item.getMenuInfo();
        //TODO to be add the actions
        return super.onContextItemSelected(item);
    }

//	========================================Helper Functions========================================
//	================================================================================================
	
	private void checkBLE(Context context) {
		// Use this check to determine whether BLE is supported on the device. Then
		// you can selectively disable BLE-related features.
		if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
		    Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
		    getActivity().finish();
		}else{
			 Toast.makeText(context, R.string.ble_is_supported, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void checkBluetooth(Context context){
	     // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
     // Ensures Bluetooth is available on the device and it is enabled. If not,
     // displays a dialog requesting user permission to enable Bluetooth.
	     if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
	         Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	         startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	     }else{
	    	 Toast.makeText(context, R.string.bluetooth_is_supported, Toast.LENGTH_SHORT).show();
	     }
	}

	private void enableTTS(){
        if(mSpeech == null) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        }
	}

    private void disableTTS(){
        if(mSpeech!= null){
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
            Log.i(TAG, "application stops, text to speech is shut down");
        }
    }
	
    
    private void setStartButtonEnable(boolean isEnable) {
		startServiceButton.setEnabled(isEnable);
		stopServiceButton.setEnabled(!isEnable);
	}

    //get the beacons raw information from service.
    private void updateList(){
    	try {
			Log.i(TAG, "get List" + beaconInterface.getList());
			@SuppressWarnings("unchecked")
			List<BeaconsInfo> beaconsInfo = beaconInterface.getList();
            List<BeaconsInfo> newList = calcList.calcList(beaconsInfo);//TODO
            savedListInstance = newList;
            //ordered by their distance (RSSI value)
            Collections.sort(newList);
			resultList.clear();
            if(!newList.isEmpty()){
                if(sharedPreferences.getBoolean("prefGuideSwitch", true)){
//                    player.play(R.raw.scanning);
                }
                if(sharedPreferences.getBoolean("prefVibrationSwitch", true)) {
//                    vibrator.vibrate(VibratorBuilder.SHORT1PATTERN);
                }
            }
			resultList.addAll(newList);

			mAdapter.notifyDataSetChanged();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }


    //give audio hint and vibration when the first object changed (with data in BeaconsAdapter)
    @Override
    public void onLabelNameChange(String labelName, int position) {
        Log.i(TAG,"get label name is" + labelName);
        String audioHint = sharedPreferences.getString("prefAudio", "");
        if(position == 0 && labelName != null){
            if(currentLocation == null ||!currentLocation.equals(labelName)){
                currentLocation = labelName;

                if(audioHint.equals("")){
                    speakOut(currentLocation);
                }else {
                    speakOut(audioHint + currentLocation);
                }
                if(sharedPreferences.getBoolean("prefGuideSwitch", true)){
                    player.play(R.raw.new_direction);
                }
                if(sharedPreferences.getBoolean("prefVibrationSwitch", true)) {
                    vibrator.vibrate(VibratorBuilder.SHORT1PATTERN);
                }
            }
        }
    }

    //speak something with android TTS.
    private void speakOut(String words){
        if (Build.VERSION.SDK_INT < 21) {//TODO get the information from preference
            mSpeech.speak(words, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            mSpeech.speak(words, TextToSpeech.QUEUE_FLUSH, null, SPEAK_NAME);
        }
    }
     
	Runnable updateUI = new Runnable() {
		@Override
		public void run() {
			updateList();
			mHandler.postDelayed(updateUI, UPDATE_PERIOD);
		}
	};

    //show the preference
    private String getPreference(){

        StringBuilder builder = new StringBuilder();

        builder.append("\n Send report:"
                + sharedPreferences.getBoolean("prefGuideSwitch", false));

        builder.append("\n Sync Frequency: "
                + sharedPreferences.getString("prefThreshold", "default"));

        builder.append("\n Sync Frequency: "
                + sharedPreferences.getString("prefFrequency", "default"));

        builder.append("\n Sync Frequency: "
                + sharedPreferences.getString("prefLink", "http://meschup.hcilab.org/map/"));

        return builder.toString();
    }

    /**
     * set the focus function and layoutManager of the recycler view
     * @param recyclerView target view
     */
    private void initRecyclerView(RecyclerView recyclerView){
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayout.VERTICAL);
    }
	
//	=====================================Service Function=====================================
//	==========================================================================================
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e(TAG, "Service has disconnected");
			doUnBindService();
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "Service has connected");
			beaconInterface = IBeacon.Stub.asInterface(service);//IBeacon
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					doBindService();
				}
			}, 500);// in case the the start delay of the service.
		}
	};
	
	private void doBindService(){
		setStartButtonEnable(false);
		updateUI.run();
	}
	
	private void doUnBindService(){
		setStartButtonEnable(true);
		beaconInterface = null;
		mHandler.removeCallbacks(updateUI);
	}
	
	public static boolean isRunning(Context context) {
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	    return pref.getBoolean(BeaconService.SERVICE_IS_RUNNING, false);
	}
	
//	=====================================LifeCycle EventListeners=============================
//	==========================================================================================
//  lifecycle is : onCreate -> onStart -> onResume -> onPause -> onStop -> onDestroy
	@Override
	public void onStop() {
        disableTTS();
		super.onStop();
	}
	
	@Override
	public void onStart() {
        if(mSpeech == null){
            if(!sharedPreferences.getBoolean(IS_TTS_ENABLE, false)) {
                enableTTS();
            }
        }
		super.onStart();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
		super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		if(!isRunning(getActivity())){
			setStartButtonEnable(true);
		}else{
			setStartButtonEnable(false);
			if(beaconInterface != null){
				updateUI.run();
			}else{
				Intent intent = new Intent(getActivity(), BeaconService.class);
				getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
//                speakOut("scanning continues");
				Toast.makeText(getActivity(), "service is still running, bind again", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (beaconInterface != null) {
			mHandler.removeCallbacks(updateUI);
		}
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

//	=======================================Intent Callback====================================
//	==========================================================================================
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if(requestCode == REQUEST_ENABLE_BT){
    		if(resultCode == Activity.RESULT_OK){
    			Toast.makeText(getActivity(), R.string.ble_is_enabled, Toast.LENGTH_SHORT).show();
    		}
    		else{
    			Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
    			getActivity().finish();
    		}
    	}

        if (requestCode ==REQUEST_SETTINGS){
                Toast.makeText(getActivity(), "let's change the settings" + getPreference(), Toast.LENGTH_SHORT).show();
            if(resultCode == Activity.RESULT_OK){
                Log.d(TAG, "change finished with setting:" + getPreference());
            }
        }
        
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
	            mSpeech = new TextToSpeech(getActivity(),new TextToSpeech.OnInitListener() {
					@Override
					public void onInit(int status) {
						Log.d(TAG, "speech engine init");
					}
				});
                mSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onError(String utteranceId) {
                        Log.d(SPEAK_NAME, "speech error");
                    }

                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(SPEAK_NAME, "speech start");
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(SPEAK_NAME, "speech done");
                    }
                });
            }
            else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                Log.d(TAG, "speech engine installed");
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    //this listener is override by the listener in setting activity.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG,"preference !" + key);
        if (key.equals("prefThreshold")) {
            Log.i(TAG,"preference threshold changed!");
            //TODO callback in service
        }
        if( key.equals("prefFrequency")) {
            Log.i(TAG,"preference frequency changed!");
            if(beaconInterface != null){
                try {
                    //user MUST restart to see the effect
                    beaconInterface.setPeriod();
                } catch (RemoteException e) {
                    Log.e(TAG, "error in set the scanning period");
                    e.printStackTrace();
                }
            }
        }
        if( key.equals("prefAudio")) {
            Log.i(TAG,"preference audioHint changed!");
        }
    }

    //save the instance in case of screen rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        if(!resultList.isEmpty()){
//            outState.putParcelableArrayList("list", (ArrayList<? extends android.os.Parcelable>) savedListInstance);
//        }
    }
}
