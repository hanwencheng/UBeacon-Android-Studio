package mci.uni.stuttgart.bilget;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import mci.uni.stuttgart.bilget.database.BeaconDBHelper;
import mci.uni.stuttgart.bilget.database.BeaconDataLoader;
import mci.uni.stuttgart.bilget.database.DatabaseUtil;
import mci.uni.stuttgart.bilget.database.LocationInfo;
import mci.uni.stuttgart.bilget.network.ParserUtil;

public class BeaconsAdapter extends Adapter<BeaconsViewHolder> {
	
	private List<BeaconsInfo> beaconsList;//need to be filled
	private static final String TAG = "BeaconAdapter";
    private static final String NOTFOUND = "-";

	//state variable;
	private Context context;
	private BeaconsViewHolder contextBeaconsViewHolder;
	Fragment contextFragment;
	BeaconDBHelper beaconDBHelper;

    BeaconDataLoaderCallbacks mDataCallbacks;
    int loaderID;

    // Provide a suitable constructor (depends on the kind of dataSet)
    public BeaconsAdapter(List<BeaconsInfo> beaconsMap, Fragment fragment, BeaconDBHelper beaconDBHelper) {
    	this.beaconsList = beaconsMap;
    	this.contextFragment = fragment;
    	this.beaconDBHelper = beaconDBHelper;
        mDataCallbacks = new BeaconDataLoaderCallbacks();
        loaderID = 0;
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
        String rangeHint = readRssi(beaconInfo.RSSI);
    	beaconsViewHolder.vRSSI.setText(rangeHint);
    	beaconsViewHolder.vLabel.setText(beaconInfo.UUID);
    	beaconsViewHolder.vMACaddress.setText(beaconInfo.MACaddress);
    	//call the background database query function
        Bundle bundle = new Bundle();
        bundle.putString("mac", beaconInfo.MACaddress);
    	contextFragment.getLoaderManager().initLoader(loaderID, bundle, mDataCallbacks);
        loaderID++;
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
            String mac = args.getString("mac");
			return new BeaconDataLoader(context, beaconDBHelper, mac);
		}

		@Override
		public void onLoadFinished(Loader<LocationInfo> loader,
				LocationInfo data) {
			if(data!= null && data.category!=null){
				Log.i(TAG, "3:get location info from the database" + data);
				contextBeaconsViewHolder.vMACaddress.setText(data.subcategory);
                contextBeaconsViewHolder.vDescription.setText(data.description);
                contextBeaconsViewHolder.vLabel.setText(data.label);
                contextBeaconsViewHolder.vCategory.setText(data.category);
			}else{
                Log.i(TAG, "3:the data itself or the category is null" + data);
                contextBeaconsViewHolder.vDescription.setText(NOTFOUND);
                contextBeaconsViewHolder.vLabel.setText(NOTFOUND);
                contextBeaconsViewHolder.vCategory.setText(NOTFOUND);
                contextBeaconsViewHolder.vMACaddress.setText("Not Found");//TODO start download action
                URL testURL = null;
                try {
                    testURL = new URL("http://meschup.hcilab.org/map/");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                new downloadJSON().execute(testURL);
            }
		}

		@Override
		public void onLoaderReset(Loader<LocationInfo> loader) {
			//Do nothing.
		}
    	
    }

//============================================Network Callback==========================================
//======================================================================================================

    private class downloadJSON extends AsyncTask<URL, Integer, Boolean>{

        @Override
        protected Boolean doInBackground(URL... params) {
            boolean result = false;
            for (URL param : params) {
                try {
                    result = downloadURL(param);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(isCancelled()) break;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Log.d(TAG, "the download result is" + aBoolean);
            super.onPostExecute(aBoolean);
        }
    }

    private boolean downloadURL( URL url) throws IOException{
        InputStream inputStream = null;
        int len = 100;

        url =  new URL("http://meschup.hcilab.org/map/");//TODO

        if(!DatabaseUtil.queryURL(beaconDBHelper , url.toString())) {
            try {
                Log.i(TAG, "has not visited , start download");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                List<LocationInfo> locationInfos = ParserUtil.parseLocation(inputStream);
                for (LocationInfo locationInfo : locationInfos) {
                    long rowNum = DatabaseUtil.insertData(beaconDBHelper, locationInfo);
                    Log.d(TAG, "insert a new row, row number is" + rowNum);
                }
                return true; //TODO

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }else{
            Log.i(TAG, "url is already visited once");
            return false;
        }
    }

    private void checkNetwork() throws MalformedURLException {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            URL testURL = new URL("http://meschup.hcilab.org/map/");
            new downloadJSON().execute(testURL);
        } else {
            Toast.makeText(context, R.string.ble_is_supported, Toast.LENGTH_SHORT).show();
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
        rssi = Math.abs(rssi);
        String hint = "out of range";
        if(rssi < 45){
            hint = "very close";
        }else if(rssi < 60){
            hint = "near";
        }else if(rssi < 45 ){
            hint = "in the range";
        }
        return hint;
    }
}
