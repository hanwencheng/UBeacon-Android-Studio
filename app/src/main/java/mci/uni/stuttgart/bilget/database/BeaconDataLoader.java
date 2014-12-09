package mci.uni.stuttgart.bilget.database;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class BeaconDataLoader extends AsyncTaskLoader<LocationInfo> {

	private LocationInfo entryData;
	private BeaconDBHelper beaconDBHelper;
	private static final String TAG = "BeaconDataLoader";
	
	public BeaconDataLoader(Context context, BeaconDBHelper beaconDBHelper) {
		super(context);
		this.beaconDBHelper = beaconDBHelper;
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
		LocationInfo locationInfo = DatabaseUtil.queryData(beaconDBHelper, "E7D38F1CF82E", null);
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
		// TODO Auto-generated method stub
		super.onCanceled(data);
	}
	
	@Override
	protected void onReset() {
		// if we have a cursor, need to be closed.
		super.onReset();
	}

}
