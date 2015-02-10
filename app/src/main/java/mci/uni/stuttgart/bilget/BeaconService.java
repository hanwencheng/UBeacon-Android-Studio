package mci.uni.stuttgart.bilget;

import android.annotation.TargetApi;
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
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconService extends Service {
	private static String TAG = "beacon service";
	protected static final long SCAN_PERIOD = 3000;//TODO
	protected static String SERVICE_IS_RUNNING = "bildgetScanService";
	
	protected boolean mScanning = false;
	
	private Handler scanHandler;
	private Map<String, BeaconsInfo> resultsMap;
	private List<BeaconsInfo> resultList;
	private int count;

    //config variable
	private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings scanSettings;
    private Object scanCallback;

    private final IBeacon.Stub mBinder = new IBeacon.Stub() {
	    public int getCount(){
			return count;
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
		setRunning(true);
		
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        resultsMap = new HashMap<>();
        resultList =  new ArrayList<>();
        initScanCallback();
        scanRunnable.run();
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "service is started");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		scanHandler.removeCallbacks(scanRunnable);
		setRunning(false);
//		scanHandler.removeCallbacks(updateUI);
		Log.d(TAG, "service is destoryed");
	}
	
	Runnable scanRunnable = new Runnable() {
		@Override
		public void run() {
			scanBLE(true);
			scanHandler.postDelayed(scanRunnable, SCAN_PERIOD);
		}
	};
	
	public void scanBLE(boolean enable) {
//		final List<BeaconsInfo> mList = new ArrayList<BeaconsInfo>();

        if (Build.VERSION.SDK_INT >= 21) {
		    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
    //				.setReportDelay(10001)
                    .build();
        }

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
            }, SCAN_PERIOD);
        } else {
        	if (mScanning) {
        		mScanning = false;
        		stopScan();
			}
        }
		Log.d(TAG, "now the scanning state is" + mScanning);
	}

	protected void mapToList(Map<String, BeaconsInfo> map, List<BeaconsInfo> list){
		list.clear();
		for(BeaconsInfo beaconsInfo : map.values()){
            list.add(beaconsInfo);
        }
		// speak out the most close place.
		if(!list.isEmpty()){
			Collections.sort(list);
//			mSpeech.speak(list.get(0).name, TextToSpeech.QUEUE_FLUSH, null, SPEAK_NAME); //TODO should be set when transfer list
		}
	}
	
	private void setRunning(boolean running) {
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    SharedPreferences.Editor editor = pref.edit();

	    editor.putBoolean(SERVICE_IS_RUNNING, running);
	    editor.apply();
	}

//====================================backward-compatible functions========================================
//=========================================================================================================

    private void startScan(){
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
        } else {
            mBluetoothLeScanner.startScan(null,scanSettings,(ScanCallback) scanCallback);
            mBluetoothLeScanner.flushPendingScanResults((ScanCallback) scanCallback);
        }
    }

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
                            Log.d(TAG, "event linstener is called!!!!");
                            Log.d(TAG, "batch result are:" + results);
//			mAdapter.notifyDataSetChanged();
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
                            BeaconsInfo beaconInfo = new BeaconsInfo();
                            beaconInfo.RSSI = rssi;
                            beaconInfo.MACaddress = device.getAddress();

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
                            resultsMap.put(beaconInfo.MACaddress, beaconInfo);
                        }
                    };
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addBeaconToMap(ScanResult result, Map<String,BeaconsInfo> map){
        int receiveRSSI = result.getRssi();
        BluetoothDevice receiveBeacon = result.getDevice();

        String deviceMAC = receiveBeacon.getAddress();
        Log.d(TAG, "macaddress from API 21 is" + deviceMAC);
        ScanRecord receiveRecord = result.getScanRecord();
        String bleName = receiveRecord.getDeviceName();
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
        beaconInfo.MACaddress = deviceMAC;
        beaconInfo.UUID = bleUUID;

        map.put(deviceMAC, beaconInfo);
    }

}
