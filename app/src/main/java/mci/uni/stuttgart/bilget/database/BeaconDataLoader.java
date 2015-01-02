package mci.uni.stuttgart.bilget.database;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class BeaconDataLoader extends AsyncTaskLoader<LocationInfo> {

	private LocationInfo entryData;
	private BeaconDBHelper beaconDBHelper;
    private String MACAddress;
	private static final String TAG = "BeaconDataLoader";
	
	public BeaconDataLoader(Context context, BeaconDBHelper beaconDBHelper, String MACAddress) {
		super(context);
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
		LocationInfo locationInfo = DatabaseUtil.queryData(beaconDBHelper, MACAddress, null);
        //TODO if the data is not found, then call internet downloading
		return locationInfo;
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
