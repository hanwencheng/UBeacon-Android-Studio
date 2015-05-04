package mci.uni.stuttgart.bilget.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import mci.uni.stuttgart.bilget.R;
import mci.uni.stuttgart.bilget.database.BeaconDBHelper;

public class SearchListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SearchListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//TODO

        setHasOptionsMenu(true);//default is false;

        //assign DOM element
        View rootView = inflater.inflate(R.layout.search_fragment_main, container,
                false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.search_recycler_view);
        registerForContextMenu(mRecyclerView);

        initRecyclerView(mRecyclerView);

        adapter = new SearchListAdapter(null, new BeaconDBHelper(getActivity()));
        mRecyclerView.setAdapter(adapter);

        return rootView;


    }

    /**
     * set the focus function and layoutManager of the recycler view
     * @param recyclerView target view
     */
    private void initRecyclerView(RecyclerView recyclerView){
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayout.VERTICAL);
    }
}
