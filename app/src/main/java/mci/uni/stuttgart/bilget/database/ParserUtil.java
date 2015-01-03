package mci.uni.stuttgart.bilget.database;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Json parser for the location data
 * Created by Hanwen on 1/2/2015.
 */
public class ParserUtil {

    private final static String rootObjectName = "data";
    private final static String TAG = "jsonParserUtil";

    public static List<LocationInfo> parseLocation(InputStream in) throws IOException {
        JsonReader jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readRootObject(jsonReader);
        }finally{
            jsonReader.close();
        }

    }

    private static List<LocationInfo> readRootObject(JsonReader jsonReader) {
        List<LocationInfo> locations = null;
        try {
            jsonReader.beginObject();
            if(jsonReader.hasNext()){ //only do once
                String name = jsonReader.nextName();
                if(name.equals(rootObjectName)){
                    locations = readLocationsArray(jsonReader);
                }
            }
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read the root object");
        }
        return locations;
    }

    //read the location array
    private static List<LocationInfo> readLocationsArray(JsonReader jsonReader){
        List<LocationInfo> locations = new ArrayList<>();
        try {
            jsonReader.beginArray();
            while(jsonReader.hasNext()) {
                locations.add(readMacAddress(jsonReader));
            }
            jsonReader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read the array");
        }
        return locations;
    }

    //read the object with only one property : macAddress
    private static LocationInfo readMacAddress(JsonReader jsonReader){
        LocationInfo location = null;
        try {
            jsonReader.beginObject();
            if(jsonReader.peek() != JsonToken.NULL) {
                String macAddress = jsonReader.nextName();
                Log.d(TAG, "read new location info with mac address" + macAddress);
                location = readLocation(jsonReader, macAddress);
            }
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }

    //read the location information properties.
    private static LocationInfo readLocation(JsonReader jsonReader, String macAddress){
        String category = null, subcategory = null, label = null, description = null;
        LocationInfo locationInfo = null;
        try {
            jsonReader.beginObject();
            while(jsonReader.hasNext()){
                if (jsonReader.peek() != JsonToken.NULL){
                    String name = jsonReader.nextName();
                    switch (name){
                        case "category" : category = jsonReader.nextString();
                            break;
                        case "subcategory" : subcategory = jsonReader.nextString();
                            break;
                        case "label" : label = jsonReader.nextString();
                            break;
                        case "description" : description = jsonReader.nextString();
                            break;
                        default: jsonReader.skipValue();
                    }
                }else{
                    jsonReader.skipValue();
                }
            }
            locationInfo = new LocationInfo(macAddress, category, subcategory, label, description);
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read macAddress object");
        }
        return locationInfo;
    }
}
