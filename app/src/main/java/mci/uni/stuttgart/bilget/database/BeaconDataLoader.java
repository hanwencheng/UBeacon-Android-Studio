package mci.uni.stuttgart.bilget.database;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

public class BeaconDataLoader extends AsyncTaskLoader<LocationInfo> {

	private LocationInfo entryData;
	private BeaconDBHelper beaconDBHelper;
    private String MACAddress;
	private static final String TAG = "BeaconDataLoader";
	
	public BeaconDataLoader(Context context, BeaconDBHelper beaconDBHelper, String MACAddress) {
		super(context);
        Log.d(TAG,"new data loader is created with macaddress" + MACAddress);
		this.beaconDBHelper = beaconDBHelper;
        this.MACAddress = MACAddress;
	}

	@Override
	public LocationInfo loadInBackground() {
//		LocationInfo hanwensHome = new LocationInfo("E7D38F1CF82E", 
//				"indoor",
//				"desk",
//				"hanwen's desk", 
//				"my beautiful desk!");
//		long firstRowId = DatabaseUtil.insertData(beaconDBHelper, hanwensHome);
//		Log.d(TAG, firstRowId + " is inserted and the table is initialed" +hanwensHome);
        //TODO if the data is not found, then call internet downloading
		return DatabaseUtil.queryData(beaconDBHelper, MACAddress, null);
	}
	
	@Override
	protected void onStartLoading() {
		if(entryData != null){
			deliverResult(entryData);
		}else{
			forceLoad();
		}
	}
	
	@Override
	public void deliverResult(LocationInfo data) {
        if(data == null){
            Log.d(TAG,"query information is null");
        }else{
            Log.d(TAG,"find the location info!");
        }
		entryData = data;
		super.deliverResult(data);
	}
	
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	@Override
	public void onCanceled(LocationInfo data) {
		super.onCanceled(data);
	}
	
	@Override
	protected void onReset() {
		// if we have a cursor, need to be closed.
		super.onReset();
	}

}
