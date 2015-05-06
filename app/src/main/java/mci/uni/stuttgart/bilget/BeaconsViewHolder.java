package mci.uni.stuttgart.bilget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BeaconsViewHolder extends RecyclerView.ViewHolder {
	
	public TextView vName;
	public TextView vRSSI;
	public TextView vLabel;
	public TextView vMACaddress;
    public TextView vCategory;
    public TextView vDescription;
    public LinearLayout vExpandArea;

    private static String TAG = "BeaconViewHolder";

	public BeaconsViewHolder(View view) {
		super(view);
		
		vName = (TextView) view.findViewById(R.id.beacon_item_name);
		vRSSI = (TextView) view.findViewById(R.id.beacon_item_RSSI);
		vLabel = (TextView) view.findViewById(R.id.beacon_item_label);
//        vCategory = (TextView) view.findViewById(R.id.beacon_item_category);
        vDescription = (TextView) view.findViewById(R.id.beacon_item_description);
//		vMACaddress = (TextView) view.findViewById(R.id.beacon_item_MACaddress);
        vExpandArea = (LinearLayout)view.findViewById(R.id.expandArea);

	}

}
