package mci.uni.stuttgart.bilget;


public class BeaconsInfo implements Comparable<BeaconsInfo>{
	protected String name;
	protected String RSSI;
	protected String UUID;
	protected String MACaddress;
	
	public String toString() {
		return "name:" + name + ";RSSI:" + RSSI + ";category:" +UUID;
	}

	@Override
	public int compareTo(BeaconsInfo another) {
		String intRSSI = this.RSSI.substring(0,3);
		String anotherRSSI = another.RSSI.substring(0,3);
		long thisRssi = Integer.parseInt(intRSSI);
		long anotherRssi = Integer.parseInt(anotherRSSI);
		if(thisRssi > anotherRssi){
			return 1;
		}else if(thisRssi < anotherRssi){
			return -1;
		}else{
			return 0;
		}
	}
}
