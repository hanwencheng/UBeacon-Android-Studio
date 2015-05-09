package mci.uni.stuttgart.bilget.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import mci.uni.stuttgart.bilget.BeaconsInfo;
import mci.uni.stuttgart.bilget.BeaconsViewHolder;
import mci.uni.stuttgart.bilget.R;
import mci.uni.stuttgart.bilget.database.BeaconDBHelper;
import mci.uni.stuttgart.bilget.database.LocationInfo;

/**
 *  The adapter for the Search mode's recycler list
 */
public class SearchListAdapter extends RecyclerView.Adapter <BeaconsViewHolder>{

    Context context;
    List<LocationInfo> resultList;

    public SearchListAdapter(List<LocationInfo> beaconsList){
        this.resultList = beaconsList;
    }

    @Override
    public BeaconsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        context = parent.getContext();
        View v = LayoutInflater.from(context)
                .inflate(R.layout.beacon_layout, parent, false);
        // set the view's size, margins, padding and layout parameters
        return new BeaconsViewHolder(v);}

    @Override
    public void onBindViewHolder(BeaconsViewHolder beaconsViewHolder, int position) {
        inflateView(beaconsViewHolder, position);
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    private void inflateView(BeaconsViewHolder viewHolder, int position){
        if(resultList.isEmpty()) return;
        LocationInfo beaconInfo = resultList.get(position);
        Log.d("search adapter", "beacons info in adapter is" + beaconInfo);
        viewHolder.vRSSI.setText("");
        if(beaconInfo.label !=null)
            viewHolder.vName.setText(beaconInfo.label);
        if(beaconInfo.description !=null)
            viewHolder.vDescription.setText(beaconInfo.description);
        if(beaconInfo.category != null)
            viewHolder.vCategory.setText(beaconInfo.category);
        if(beaconInfo.subcategory != null)
            viewHolder.vSubcategory.setText(beaconInfo.subcategory);
    }
}
