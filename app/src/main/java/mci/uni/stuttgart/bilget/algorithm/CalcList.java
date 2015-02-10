package mci.uni.stuttgart.bilget.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mci.uni.stuttgart.bilget.BeaconsInfo;

/**
 * a singleton class
 * the helper function to get the stable display of the discovered beacons.
 * Created by Hanwen on 2/1/2015.
 */
public class CalcList {

    private static CalcList instance = null;

    List<BeaconsInfo> outputList;
    Map<String, BeaconsInfo> beaconStorageMap;
    //a map to store the rssi information for the current outputList.
    Map<String, int[]> map;
    //a key set to store the key which is not in the input outputList.
    Set<String> keySet;

    //should be instanced when scan start
    public CalcList(){
        map = new HashMap<>();
        beaconStorageMap = new HashMap<>();
    }

    public static CalcList getInstance(){
        if(instance == null){
            instance = new CalcList();
        }
        return instance;
    }

    public List<BeaconsInfo> calcList(List<BeaconsInfo> inputList){

        outputList = new ArrayList<>();
        keySet = new HashSet<>(map.keySet());

        //iterate all the beacons in the list
        for(BeaconsInfo beaconInfo : inputList){
            String macAddress = beaconInfo.MACaddress;
            //update our data
            beaconStorageMap.put(macAddress, beaconInfo);
            int rssi = Math.abs(beaconInfo.RSSI);

            if(updateMap(macAddress, rssi)){
                outputList.add(beaconInfo);
            }
        }

        calculateOthers(keySet);
        return outputList;
    }

    private boolean updateMap(String macAddress, int rssi){
        int[] rssiArray= map.get(macAddress);
        int rssiReversed = RangeThreshold.FAR - rssi;
        if(rssiArray == null){
            int[] initArray = new int[RangeThreshold.TOTAL];
            //if the beacon is in our range threshold.
            if(rssiReversed > 0 ) {
                initArray[0] =  rssiReversed;//other two value would be 0
                for(int i = 1; i < RangeThreshold.TOTAL ; i++ ){
                    initArray[i] = -1;
                }
                map.put(macAddress, initArray);
            }else{
                return false; // it is so far away so we don't calculate it.
            }
            return true;
        }else{
            //every element sequentially backward, push rssi into it
            popInArray(rssiReversed, rssiArray);
            //delete the key in the keyset. !important step!
            keySet.remove(macAddress);
            return calculateArray(rssiArray, macAddress);
        }
    }

    private void popInArray(int neu, int[] array){
        for (int i = 0; i < RangeThreshold.TOTAL; i++ ){
            int temp = array[i];
            array[i] = neu;
            neu = temp;
        }
    }

    //it is just a simple calculation;
    private boolean calculateArray(int[] array, String macAddress){
        int sum = 0;
        for (int i = 0; i < RangeThreshold.TOTAL ; i ++ ){
            sum = sum + array[i];
        }
        if( sum > RangeThreshold.THRESHOLD){
            return true;
        }else{
            if (checkIsFull(array)) {// if the array is a full array
                //clear until next discovering
                map.remove(macAddress);
                // TODO affect the result significantly
            }
            //else keep this value in map, so next time we may add more rssi info.
            return false;
        }
    }

    //check if the array is full with rssi data, without -1;
    private boolean checkIsFull(int[] array){
        boolean isFull = true;
        for(int i = 0; i < RangeThreshold.TOTAL ; i++){
            if(array[i] < 0){
                isFull = false;
                break;
            }
        }
        return isFull;
    }

    private void calculateOthers(Set<String> addressSet){
        for(String address : addressSet){
            //if it is not in our input list
                int[] rssiArray = map.get(address);
                popInArray(0, rssiArray);

                if (!calculateArray(rssiArray, address)) {
                    beaconStorageMap.remove(address);//delete if not exist here, until next time it be discovered again.
                } else {
                    outputList.add(beaconStorageMap.get(address));//else add it into outputList
                }
        }
    }
}
