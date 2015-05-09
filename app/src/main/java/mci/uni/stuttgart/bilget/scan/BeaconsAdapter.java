package mci.uni.stuttgart.bilget.scan;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mci.uni.stuttgart.bilget.BeaconsInfo;
import mci.uni.stuttgart.bilget.BeaconsViewHolder;
import mci.uni.stuttgart.bilget.R;
import mci.uni.stuttgart.bilget.algorithm.RangeThreshold;
import mci.uni.stuttgart.bilget.database.BeaconDBHelper;
import mci.uni.stuttgart.bilget.database.LocationInfo;
import mci.uni.stuttgart.bilget.network.JSONLoader;

/**
 * beacon's adapter class, when get the list, first query the macAddress in our database,
 * if not exist, then query it through internet.
 */
public class BeaconsAdapter extends Adapter<BeaconsViewHolder> {
	
	private List<BeaconsInfo> beaconsList;//need to be filled
	private static final String TAG = "BeaconsAdapter";
    private static final String NOT_FOUND = "not_found";//TODO

	//state variable;
	private Context context;
	Fragment contextFragment;
    BeaconDBHelper beaconDBHelper;
    private Map<String, BeaconsViewHolder> viewMap;
    int loaderID;
    JSONLoader jsonLoader;

    //callback declared in mainListFragment
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
        // set the view's size, margins, padding and layout parameters
        return new BeaconsViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(BeaconsViewHolder beaconsViewHolder, int position) {
    	BeaconsInfo beaconInfo = beaconsList.get(position);
    	Log.d(TAG," 0 : beaconsInfo is" + beaconInfo);
    	beaconsViewHolder.vName.setText(beaconInfo.name);
        String rangeHint = readRssi(beaconInfo.RSSI);
    	beaconsViewHolder.vRSSI.setText(rangeHint);

    	//call the background database query function, get the macAddress from the bundle
        Bundle bundle = new Bundle();
        bundle.putString("mac", beaconInfo.macAddress);
        bundle.putInt("position", position);
        viewMap.put(beaconInfo.macAddress, beaconsViewHolder);
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
    
    private class BeaconDataLoaderCallbacks implements LoaderManager.LoaderCallbacks<LocationInfo> {
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
			if(data!= null){
				Log.d(TAG, "3:get location info from the database" + data);
                if(data.label !=null)
                    mBeaconsViewHolder.vName.setText(data.label);
                if(data.description !=null)
                    mBeaconsViewHolder.vDescription.setText(data.description);
                if(data.category != null)
                    mBeaconsViewHolder.vCategory.setText(data.category);
                if(data.subcategory != null)
                    mBeaconsViewHolder.vSubcategory.setText(data.subcategory);

                Log.i(TAG, "interface is" + mCallback);
                mCallback.onLabelNameChange(data.label, position);
			}else{
                Log.d(TAG, "3:the data itself is null");
                mBeaconsViewHolder.vCategory.setText(NOT_FOUND);
                mBeaconsViewHolder.vSubcategory.setText(NOT_FOUND);
                mBeaconsViewHolder.vDescription.setText(NOT_FOUND);
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
