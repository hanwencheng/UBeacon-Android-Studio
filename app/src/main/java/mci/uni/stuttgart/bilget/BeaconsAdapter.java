package mci.uni.stuttgart.bilget;

import java.util.List;

import mci.uni.stuttgart.bilget.database.BeaconDBHelper;
import mci.uni.stuttgart.bilget.database.BeaconDataLoader;
import mci.uni.stuttgart.bilget.database.LocationInfo;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BeaconsAdapter extends Adapter<BeaconsViewHolder> {
	
	private List<BeaconsInfo> beaconsList;//need to be filled
	private static final String TAG = "BeaconAdapter";
	
	//state variable;
	private Context context;
	private BeaconsViewHolder contextBeaconsViewHolder;
	Fragment conteFragment;
	BeaconDBHelper beaconDBHelper;

    // Provide a suitable constructor (depends on the kind of dataSet)
    public BeaconsAdapter(List<BeaconsInfo> beaconsMap, Fragment fragment, BeaconDBHelper beaconDBHelper) {
    	this.beaconsList = beaconsMap;
    	this.conteFragment = fragment;
    	this.beaconDBHelper = beaconDBHelper;
    }

    // Create new views (invoked by the layout manager)
    public BeaconsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
    	context = parent.getContext();
        View v = LayoutInflater.from(context)
                               .inflate(R.layout.beacon_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        BeaconsViewHolder beaconViewHolder = new BeaconsViewHolder(v);
        this.contextBeaconsViewHolder = beaconViewHolder;
       
        return beaconViewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(BeaconsViewHolder beaconsViewHolder, int position) {
    	BeaconsInfo beaconInfo = beaconsList.get(position);
    	Log.i("@","beaconsInfo is" + beaconInfo);
    	beaconsViewHolder.vName.setText(beaconInfo.name);
    	beaconsViewHolder.vRSSI.setText(beaconInfo.RSSI);
    	beaconsViewHolder.vUUID.setText(beaconInfo.UUID);
    	beaconsViewHolder.vMACaddress.setText(beaconInfo.MACaddress);
    	//call the background database query function
    	conteFragment.getLoaderManager().initLoader(0, null, new BeaconDataLoaderCallbacks());
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

		@Override
		public Loader<LocationInfo> onCreateLoader(int id, Bundle args) {
			return new BeaconDataLoader(context, beaconDBHelper);
		}

		@Override
		public void onLoadFinished(Loader<LocationInfo> loader,
				LocationInfo data) {
			if(data!= null && data.category!=null){
				Log.d(TAG, "get location info from the database" + data);
				contextBeaconsViewHolder.vMACaddress.setText(data.description);
			}
		}

		@Override
		public void onLoaderReset(Loader<LocationInfo> loader) {
			//Do nothing.
		}
    	
    }
}
