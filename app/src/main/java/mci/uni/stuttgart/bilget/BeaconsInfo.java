package mci.uni.stuttgart.bilget;


import android.support.annotation.NonNull;

public class BeaconsInfo implements Comparable<BeaconsInfo>{
    public String name;
    public int RSSI;//default value is a negative value.
    public String UUID;
	public String MACaddress;

	public String toString() {
		return "name:" + name + ";RSSI:" + RSSI + ";category:" +UUID;
	}

	public int compareTo(@NonNull BeaconsInfo another) {

		int thisRSSI = this.RSSI;
		int anotherRSSI = another.RSSI;
		if(thisRSSI < anotherRSSI){
			return 1;
		}else if(thisRSSI > anotherRSSI){
			return -1;
		}else{
			return 0;
		}
	}
}
