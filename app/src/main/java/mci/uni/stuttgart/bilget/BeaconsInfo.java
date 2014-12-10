package mci.uni.stuttgart.bilget;


import android.support.annotation.NonNull;

public class BeaconsInfo implements Comparable<BeaconsInfo>{
	protected String name;
	protected String RSSI;
	protected String UUID;
	protected String MACaddress;

	public String toString() {
		return "name:" + name + ";RSSI:" + RSSI + ";category:" +UUID;
	}

	public int compareTo(@NonNull BeaconsInfo another) {

		String intRSSI = this.RSSI.replace("db","");
		String anotherRSSI = another.RSSI.replace("db","");
		long thisRssi = Integer.parseInt(intRSSI);
		long anotherRssi = Integer.parseInt(anotherRSSI);
		if(thisRssi < anotherRssi){
			return 1;
		}else if(thisRssi > anotherRssi){
			return -1;
		}else{
			return 0;
		}
	}
}
