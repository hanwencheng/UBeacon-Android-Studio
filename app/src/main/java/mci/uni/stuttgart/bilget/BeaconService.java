package mci.uni.stuttgart.bilget;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconService extends Service {
	protected static final long LONG_SCAN_PERIOD = 8000;
    protected static final long SHORT_SCAN_PERIOD = 2000;
    protected static final long MIDDLE_SCAN_PERIOD = 5000;
    protected static String SERVICE_IS_RUNNING = "BeaconService.bildgetScanService";
    protected boolean mScanning = false;
    private static String TAG = "beacon service";

	private Handler scanHandler;
	private Map<String, BeaconsInfo> resultsMap;
	private List<BeaconsInfo> resultList;
	private int count;

    //config variable
	private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings scanSettings;
    private Object scanCallback;

    private NotificationManager mNotificationManager;


    private SharedPreferences preferences;
    private long scanPeriod;

    //AIDL functions
    private final IBeacon.Stub mBinder = new IBeacon.Stub() {
	    public int getCount(){
			return count;
	    }
        public void setPeriod(){
            setScanPeriod();
        }
        public String getName() {
			return null;
	    }
        @Override
		public List<BeaconsInfo> getList() throws RemoteException {
			return resultList;
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "a client is bound to service");
        return mBinder;
        
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "a client is unbound");
		return super.onUnbind(intent);
	}
	
	@Override
	public void onCreate() {
		scanHandler = new Handler();
		count = 0;
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        setScanPeriod();
        setRunning(true);

		final BluetoothManager bluetoothManager =
				(BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        resultsMap = new HashMap<>();
        resultList =  new ArrayList<>();
        initScanCallback();
        scanRunnable.run();
		super.onCreate();

        createNotification();

	}

    public void setScanPeriod(){
        String scanFrequecy = preferences.getString("prefFrequency","1");
        int frequency = Integer.parseInt(scanFrequecy);
        switch (frequency){
            case 0: scanPeriod = LONG_SCAN_PERIOD;
                break;
            case 1: scanPeriod = MIDDLE_SCAN_PERIOD;
                break;
            case 2: scanPeriod = SHORT_SCAN_PERIOD;
                break;
            default: scanPeriod = SHORT_SCAN_PERIOD;
        }
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "service is started");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		scanHandler.removeCallbacks(scanRunnable);
		setRunning(false);
		Log.d(TAG, "service is destoryed");

        destroyNotification();
	}
	
	Runnable scanRunnable = new Runnable() {
		@Override
		public void run() {
			scanBLE(true);
			scanHandler.postDelayed(scanRunnable, scanPeriod);
		}
	};

    /**
     * start or stop the bluetooth scanning.
     * @param enable true to start, false to stop
     */
	public void scanBLE(boolean enable) {

        if (Build.VERSION.SDK_INT >= 21) {
		    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            //more setting please check new api
            scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
        }

        //we implemented our own filter instead, which is more flexible
//		List<ScanFilter> uuidFilter = new ScanFilter.Builder()
//				.setRssiRange(-75, 0)
//				.build();
		if (enable) {
            mScanning = true;
            resultsMap.clear();
            startScan();
//			Stops scanning after a pre-defined scan period.
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    Log.d(TAG, "now scan will stop");
                    stopScan();
                    mapToList(resultsMap, resultList);
                    Log.d(TAG, "now the list is" + resultList);
                }
            }, scanPeriod);
        } else {
        	if (mScanning) {
        		mScanning = false;
        		stopScan();
			}
        }
		Log.d(TAG, "now the scanning state is" + mScanning);
	}

    /**
     * Transfer from the map in service to a list, used for our ui
     * @param map raw beacon map
     * @param list raw beacon list
     */
	protected void mapToList(Map<String, BeaconsInfo> map, List<BeaconsInfo> list){
		list.clear();
		for(BeaconsInfo beaconsInfo : map.values()){
            list.add(beaconsInfo);
        }
		// speak out the most close place.
		if(!list.isEmpty()){
            updateNotification();
        }
	}
	
	private void setRunning(boolean running) {
	    SharedPreferences.Editor editor = preferences.edit();

	    editor.putBoolean(SERVICE_IS_RUNNING, running);
	    editor.apply();
	}

//====================================backward-compatible functions========================================
//=========================================================================================================

    @SuppressWarnings("deprecation")
    private void startScan(){
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
        } else {
            mBluetoothLeScanner.startScan(null,scanSettings,(ScanCallback) scanCallback);
            mBluetoothLeScanner.flushPendingScanResults((ScanCallback) scanCallback);
        }
    }

    @SuppressWarnings("deprecation")
    private void stopScan(){
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback)scanCallback);
        } else {
            mBluetoothLeScanner.flushPendingScanResults((ScanCallback) scanCallback);
            mBluetoothLeScanner.stopScan((ScanCallback) scanCallback);
        }
    }


    private void initScanCallback(){
    // Device scan callback in API 21.
        if (Build.VERSION.SDK_INT >= 21) {
            scanCallback = new ScanCallback() {
                        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                            addBeaconToMap(result, resultsMap);
                        }

                        public void onScanFailed(int errorCode) {
                            Log.d(TAG, "scan error code is:" + errorCode);
                        }

                        public void onBatchScanResults(java.util.List<android.bluetooth.le.ScanResult> results) {
                            Log.d(TAG, "event listener is called!!!!");
                            Log.d(TAG, "batch result are:" + results);
                            //mAdapter.notifyDataSetChanged();
                            for (int i = 0; i < results.size(); i++) {
                                ScanResult result = results.get(i);
                                Log.d(TAG, "add item" + result + "to list");
                                addBeaconToMap(result, resultsMap);
                            }
                        }
                    };
        }else{
            // Device scan callback from API 18.
            scanCallback =
                    new BluetoothAdapter.LeScanCallback() {
                        @Override
                        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                            addBeaconToMap(device, rssi, scanRecord, resultsMap);
                        }
                    };
        }

    }

    //collect beacon to map, which aimed at new api.
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addBeaconToMap(ScanResult result, Map<String,BeaconsInfo> map){
        int receiveRSSI = result.getRssi();
        BluetoothDevice receiveBeacon = result.getDevice();

        String deviceMAC = receiveBeacon.getAddress();
        Log.d(TAG, "macAddress from API 21 is" + deviceMAC);
        ScanRecord receiveRecord = result.getScanRecord();
        //name inspector
        String deviceName = "NULL NAME";
        String mServiceName = receiveBeacon.getName();
        if(mServiceName != null){
            deviceName = mServiceName;
        }
        //uuid inspector
        List <ParcelUuid> mServiceUUID =  receiveRecord.getServiceUuids();
        String bleUUID ="NULL UUID";
        if(mServiceUUID != null){
            bleUUID = receiveRecord.getServiceUuids().toString();
        }
        Log.d("recordInfo", receiveRecord.toString());

        BeaconsInfo beaconInfo = new BeaconsInfo();
        beaconInfo.name = deviceName;
        beaconInfo.RSSI = receiveRSSI;
        beaconInfo.macAddress = deviceMAC;
        beaconInfo.UUID = bleUUID;

        map.put(deviceMAC, beaconInfo);
    }

    //collect beacon to map, which aimed at old api.
    private void addBeaconToMap(BluetoothDevice device, int rssi, byte[] scanRecord, Map<String,BeaconsInfo> map ){
        BeaconsInfo beaconInfo = new BeaconsInfo();
        beaconInfo.RSSI = rssi;
        beaconInfo.macAddress = device.getAddress();

        String recordInfo = new String(scanRecord);
        String recordUUID ="NULL UUID";
        if(!recordInfo.equals("")){
            Log.d(TAG, "the scan record string is" + recordInfo);
            recordUUID = recordInfo;
        }
        beaconInfo.UUID = recordUUID;

        String deviceName = "NULL NAME";
        String mServiceName = device.getName();
        if(mServiceName != null){
            deviceName = mServiceName;
        }
        beaconInfo.name = deviceName;
        map.put(beaconInfo.macAddress, beaconInfo);
    }


//	=======================================Notification=======================================
//	==========================================================================================

    public void createNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Ubeacon")
                        .setContentText("Service Created");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        // mId allows you to update the notification later on.
        int mId = 0;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private void destroyNotification(){
        mNotificationManager.cancel(0);
    }

    private void updateNotification(){
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Sets an ID for the notification, so it can be updated
        int notifyID = 0;
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Ubeacon")
                .setContentText("Continuing moving")
                .setSmallIcon(R.drawable.ic_launcher)
//                .setSound(Uri.parse("android.resource://mci.uni.stuttgart.bilget/raw/scanning"))
                ;

        mNotificationManager.notify(
                notifyID,
                mNotifyBuilder.build());
    }

}
