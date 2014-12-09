package mci.uni.stuttgart.bilget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class BeaconsViewHolder extends RecyclerView.ViewHolder {
	
	protected TextView vName;
	protected TextView vRSSI;
	protected TextView vUUID;
	protected TextView vMACaddress;

	public BeaconsViewHolder(View view) {
		super(view);
		
		vName = (TextView) view.findViewById(R.id.beacon_item_name);
		vRSSI = (TextView) view.findViewById(R.id.beacon_item_RSSI);
		vUUID = (TextView) view.findViewById(R.id.beacon_item_UUID);
		vMACaddress = (TextView) view.findViewById(R.id.beacon_item_MACaddress);
	}

}
