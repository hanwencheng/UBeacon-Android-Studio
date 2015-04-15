package mci.uni.stuttgart.bilget;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mci.uni.stuttgart.bilget.algorithm.RangeThreshold;
import mci.uni.stuttgart.bilget.database.BeaconDBHelper;
import mci.uni.stuttgart.bilget.database.BeaconDataLoader;
import mci.uni.stuttgart.bilget.database.LocationInfo;
import mci.uni.stuttgart.bilget.network.JSONLoader;

public class BeaconsAdapter extends Adapter<BeaconsViewHolder> {
	
	private List<BeaconsInfo> beaconsList;//need to be filled
	private static final String TAG = "BeaconsAdapter";
    private static final String NOTFOUND = "";

	//state variable;
	private Context context;
	Fragment contextFragment;
    BeaconDBHelper beaconDBHelper;
    JSONLoader jsonLoader;

    private Map<String, BeaconsViewHolder> viewMap;
    int loaderID;

    public interface OnListHeadChange{
        public void onLabelNameChange(String labelName, int position);
    }
    OnListHeadChange mCallback;

    private int expandedPosition = -1;

    // Provide a suitable constructor (depends on the kind of dataSet)
    public BeaconsAdapter(List<BeaconsInfo> beaconsMap, Fragment fragment, mci.uni.stuttgart.bilget.database.BeaconDBHelper beaconDBHelper) {
    	this.beaconsList = beaconsMap;
    	this.contextFragment = fragment;
    	this.beaconDBHelper = beaconDBHelper;
        this.viewMap = new HashMap<>();
        loaderID = 0;
        mCallback = (OnListHeadChange) contextFragment;
        checkNetwork();
        jsonLoader = JSONLoader.getInstance(beaconDBHelper);
    }

    // Create new views (invoked by the layout manager)
    public BeaconsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
    	context = parent.getContext();
        View v = LayoutInflater.from(context)
                               .inflate(R.layout.beacon_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        BeaconsViewHolder beaconViewHolder = new BeaconsViewHolder(v);
        return beaconViewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(BeaconsViewHolder beaconsViewHolder, int position) {
    	BeaconsInfo beaconInfo = beaconsList.get(position);
    	Log.d(TAG," 0 : beaconsInfo is" + beaconInfo);
    	beaconsViewHolder.vName.setText(beaconInfo.name);
        String rangeHint = readRssi(beaconInfo.RSSI);
    	beaconsViewHolder.vRSSI.setText(rangeHint);
//    	beaconsViewHolder.vLabel.setText(beaconInfo.UUID);
//    	beaconsViewHolder.vMACaddress.setText(beaconInfo.MACaddress);
    	//call the background database query function
        Bundle bundle = new Bundle();
        bundle.putString("mac", beaconInfo.MACaddress);
        bundle.putInt("position", position);
        viewMap.put(beaconInfo.MACaddress, beaconsViewHolder);
    	contextFragment.getLoaderManager().initLoader(loaderID, bundle, new BeaconDataLoaderCallbacks());
        loaderID++;

        if (position == expandedPosition) {
            beaconsViewHolder.vExpandArea.setVisibility(View.VISIBLE);
        } else {
            beaconsViewHolder.vExpandArea.setVisibility(View.GONE);
        }

    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return beaconsList.size();
    }
    
    @Override
    public void onViewAttachedToWindow(BeaconsViewHolder holder) {
    	super.onViewAttachedToWindow(holder);
    	long itemId = holder.getItemId();
    	int itemPosition = holder.getPosition();
    	
    	Log.d("log", "id is" + itemId + "; position is" + itemPosition);
    }
    
//===========================================DataLoader Callback========================================
//======================================================================================================
    
    private class BeaconDataLoaderCallbacks implements LoaderCallbacks<LocationInfo>{
        private String mac;
        private int position;

		@Override
		public Loader<LocationInfo> onCreateLoader(int id, Bundle args) {
            this.mac = args.getString("mac");
            this.position = args.getInt("position");
			return new BeaconDataLoader(context, beaconDBHelper, mac);
		}

		@Override
		public void onLoadFinished(Loader<LocationInfo> loader,
				LocationInfo data) {
            BeaconsViewHolder mBeaconsViewHolder = viewMap.get(this.mac);
			if(data!= null && data.category!=null){
				Log.d(TAG, "3:get location info from the database" + data);
                mBeaconsViewHolder.vName.setText(data.label);
//                mBeaconsViewHolder.vMACaddress.setText(this.mac);
                mBeaconsViewHolder.vDescription.setText(data.description);
                mBeaconsViewHolder.vLabel.setText(data.subcategory);
//                mBeaconsViewHolder.vCategory.setText(data.category);

                Log.i(TAG, "interface is" + mCallback);
                mCallback.onLabelNameChange(data.label, position);
			}else{
                Log.d(TAG, "3:the data itself or the category is null, data: " + data);
                mBeaconsViewHolder.vDescription.setText(NOTFOUND);
                mBeaconsViewHolder.vLabel.setText(NOTFOUND);
//                mBeaconsViewHolder.vCategory.setText(NOTFOUND);
//                mBeaconsViewHolder.vMACaddress.setText(this.mac);//TODO start download action, this UI action may be disabled
                URL testURL = null;
                try {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(contextFragment.getActivity());
                    testURL = new URL(sharedPreferences.getString("prefLink", "http://meschup.hcilab.org/map/"));
                    Log.i(TAG, "our database comes from" + testURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                jsonLoader.download(testURL, true, contextFragment.getActivity());
            }
		}

		@Override
		public void onLoaderReset(Loader<LocationInfo> loader) {
			//Do nothing.
		}
    	
    }

//============================================Network Callback==========================================
//======================================================================================================

    private void checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager)
                contextFragment.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(contextFragment.getActivity(), R.string.network_not_avaliable, Toast.LENGTH_SHORT).show();
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

//=============================================RSSI INDICATOR===========================================
//======================================================================================================

    private String readRssi(int rssi){
        rssi = Math.abs(rssi);//because the origin number is negative
        String hint = "out of range";
        if(rssi < RangeThreshold.NEAR){
            hint = "very close";
        }else if(rssi < RangeThreshold.MIDDLE){
            hint = "near";
        }else if(rssi < RangeThreshold.FAR ){
            hint = "in the range";
        }
        return hint;
    }
}
