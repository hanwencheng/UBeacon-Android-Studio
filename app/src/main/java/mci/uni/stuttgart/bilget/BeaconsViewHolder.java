package mci.uni.stuttgart.bilget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class BeaconsViewHolder extends RecyclerView.ViewHolder {
	
	protected TextView vName;
	protected TextView vRSSI;
	protected TextView vLabel;
	protected TextView vMACaddress;
    protected TextView vCategory;
    protected TextView vDescription;

	public BeaconsViewHolder(View view) {
		super(view);
		
		vName = (TextView) view.findViewById(R.id.beacon_item_name);
		vRSSI = (TextView) view.findViewById(R.id.beacon_item_RSSI);
		vLabel = (TextView) view.findViewById(R.id.beacon_item_label);
//        vCategory = (TextView) view.findViewById(R.id.beacon_item_category);
        vDescription = (TextView) view.findViewById(R.id.beacon_item_description);
//		vMACaddress = (TextView) view.findViewById(R.id.beacon_item_MACaddress);
	}

}
