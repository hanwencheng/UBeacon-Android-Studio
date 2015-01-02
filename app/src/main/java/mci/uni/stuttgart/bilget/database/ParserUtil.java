package mci.uni.stuttgart.bilget.database;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Json parser for the location data
 * Created by Hanwen on 1/2/2015.
 */
public class ParserUtil {

    private final static String rootObjectName = "data";
    private final static String TAG = "jsonParserUtil";

    public static List parseLocation(InputStream in) throws UnsupportedEncodingException {
        JsonReader jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));


        return null;
    }

    private List readLocationArray(JsonReader jsonReader) {
        List locations = null;
        try {
            jsonReader.beginObject();
            if(jsonReader.hasNext()){ //only do once
                String name = jsonReader.nextName();
                if(name.equals(rootObjectName)){
                    locations = readLocations(jsonReader);
                }
            }
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read the root object");
        }
        return locations;
    }

    private List readLocations(JsonReader jsonReader){
        List<LocationInfo> locations = new ArrayList<>();
        try {
            jsonReader.beginArray();
            while(jsonReader.hasNext()) {
                locations.add(readLocation(jsonReader));
            }
            jsonReader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read the array");
        }
        return locations;
    }

    private LocationInfo readMacAddress(JsonReader jsonReader){
        LocationInfo location;
        try {
            jsonReader.beginObject();
            //TODO
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return
    }

    private LocationInfo readLocation(JsonReader jsonReader){

        try {
            jsonReader.beginObject();
            jsonReader.
            while(jsonReader.hasNext()){
                if (jsonReader.peek() != JsonToken.NULL){

                }
            }

            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read macAddress object");
        }

        return null;
    }
}
