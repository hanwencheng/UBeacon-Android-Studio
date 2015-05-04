package mci.uni.stuttgart.bilget.database;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

/**
 * background loader used in adpater
 */
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
        String macAddressWithoutColon = MACAddress.replace(":","");
        //TODO if the data is not found, then call internet downloading
		LocationInfo location =  DatabaseUtil.querySingleData(beaconDBHelper, macAddressWithoutColon, null);
        Log.d(TAG, "1:the callback of location query is" +MACAddress + "::" + location);
        return location;
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
            Log.d(TAG,"2:query information is null" + MACAddress);
        }else{
            Log.d(TAG,"2:find the location info!" + MACAddress);
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
