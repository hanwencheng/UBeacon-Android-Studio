package mci.uni.stuttgart.bilget;

import android.app.Activity;
import android.app.Fragment;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mci.uni.stuttgart.bilget.database.BeaconDBHelper;

public class MainListFragment extends Fragment {

	private RecyclerView mRecyclerView;
	private Adapter<BeaconsViewHolder> mAdapter;
    private List<BeaconsInfo> resultList;
	private SwipeRefreshLayout swipeLayout;

	//state variables
	private boolean mScanning;
	private Handler mHandler;
	private String currentLocation = null;
	
	private final static int REQUEST_ENABLE_BT = 1;
	private final static int MY_DATA_CHECK_CODE = 2;
    private final static int REQUEST_SETTINGS =3;
	
	private TextToSpeech mSpeech;
	private final static long UPDATE_PERIOD = BeaconService.SCAN_PERIOD ; 
	private final static String SPEAK_NAME = "name";//text to speech utteranceId
	private final static String TAG = "Ubeacon";
    private final static String IS_TTS_ENABLE = "isTTSEnabled";

	private Button startServiceButton;
	private Button stopServiceButton;
	
	private IBeacon beaconInteface;
	
//	========================================Initialization==========================================
//	================================================================================================
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        setHasOptionsMenu(true);//default is false;
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
		registerForContextMenu(mRecyclerView);//TODO to be checked

		startServiceButton = (Button) rootView.findViewById(R.id.service_start);
		stopServiceButton = (Button) rootView.findViewById(R.id.service_stop);
		
//		======================set UI event listener======================
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				swipeLayout.setRefreshing(true);
//				scanBLE(true); TODO
				mHandler.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                	swipeLayout.setRefreshing(false);
	                }
	            }, 1000);
			}
		});

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean enable = false;
                if(mRecyclerView != null && mRecyclerView.getChildCount() > 0){
                    enable = mRecyclerView.getChildAt(0).getTop() == 0 ;//TODO
                }
                swipeLayout.setEnabled(enable);
            }
        });
		
		startServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "start button clicked");
				Intent intent = new Intent(getActivity(), BeaconService.class);
				getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
			}
		});
		
		stopServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "stop button clicked");
				doUnBindService();
				//unBind service and end the UI update runnable.
				getActivity().unbindService(mServiceConnection);
			}
		});
		
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        
        // use a linear layout manager
        LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayout.VERTICAL);

        resultList =  new ArrayList<BeaconsInfo>();
        // specify an adapter (see also next example)
        mAdapter = new BeaconsAdapter(resultList, this, new BeaconDBHelper(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        
        mHandler = new Handler();
//        mSpeech = new TextToSpeech(getActivity(), null);
//        enableTTS();

        checkBLE(getActivity());
        
        checkBluetooth(getActivity());
        
		return rootView;
	}

//		======================action bar callback======================
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

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
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                pref.edit().putBoolean(IS_TTS_ENABLE, item.isChecked()).apply();
                return true;
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
    
    private void updateList(){
    	try {
			Log.i(TAG, "get List" + beaconInteface.getList());
//			@SuppressWarnings("unchecked")
//			List<BeaconsInfo> beaconsInfo = Collections.checkedList( beaconInteface.getList(), BeaconsInfo.class);
			@SuppressWarnings("unchecked")
			List<BeaconsInfo> beaconsInfo = beaconInteface.getList();
			resultList.clear();
			resultList.addAll(beaconsInfo);
			if (!resultList.isEmpty() && mSpeech!=null && !resultList.get(0).name.equals(currentLocation)) {
				currentLocation = resultList.get(0).name;
				//TODO should be set when transfer list
                if (Build.VERSION.SDK_INT < 21) {
                    mSpeech.speak("you are now approaching" + currentLocation, TextToSpeech.QUEUE_FLUSH, null);
                } else {
				    mSpeech.speak("you are now approaching" + currentLocation, TextToSpeech.QUEUE_FLUSH, null, SPEAK_NAME);
                }
			}
			mAdapter.notifyDataSetChanged();
		} catch (RemoteException e) {
			e.printStackTrace();
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        StringBuilder builder = new StringBuilder();

        builder.append("\n audio: "
                + preferences.getString("prefAudio", "default"));

        builder.append("\n Send report:"
                + preferences.getBoolean("prefGuideSwitch", false));

        builder.append("\n Sync Frequency: "
                + preferences.getString("prefThreshold", "default"));

        builder.append("\n Sync Frequency: "
                + preferences.getString("prefFrequency", "default"));

        return builder.toString();
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
			beaconInteface = IBeacon.Stub.asInterface(service);//IBeacon
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
		beaconInteface = null;
		mHandler.removeCallbacks(updateUI);
	}
	
	public static boolean isRunning(Context context) {
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	    return pref.getBoolean(BeaconService.SERVICE_IS_RUNNING, false);
	}
	
//	=====================================LifeCycle EventListeners=============================
//	==========================================================================================
	@Override
	public void onStop() {
        disableTTS();
		super.onStop();
	}
	
	@Override
	public void onStart() {
        if(mSpeech == null){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(pref.getBoolean(IS_TTS_ENABLE, true)) {  //TODO default value is true.
                enableTTS();
            }
        }
		super.onStart();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
//		scanBLE(false);
	};
	
	@Override
	public void onResume() {
		super.onResume();
		if(!isRunning(getActivity())){
			setStartButtonEnable(true);
		}else{
			setStartButtonEnable(false);
			if(beaconInteface != null){
				updateUI.run();
			}else{
				Intent intent = new Intent(getActivity(), BeaconService.class);
				getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
				Toast.makeText(getActivity(), "service is still running, bind again", Toast.LENGTH_SHORT).show();
			}
			
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (beaconInteface != null) {
			mHandler.removeCallbacks(updateUI);
		}
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
                //TODO
            }else{
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
    
}
