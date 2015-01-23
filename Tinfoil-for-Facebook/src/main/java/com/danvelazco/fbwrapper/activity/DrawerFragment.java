package com.danvelazco.fbwrapper.activity;

import android.app.ListFragment;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.danvelazco.fbwrapper.FbWrapper;
import com.danvelazco.fbwrapper.R;
import com.danvelazco.fbwrapper.util.MySimpleArrayAdapter;

/**
 * A ListFragment that replaces the million OnClickListeners previously used in the Drawer.
 */
public class DrawerFragment extends ListFragment {
    private String[] mPlanetTitles;
    private int[] mIcons;



    public DrawerFragment() {}
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflater.inflate(R.layout.drawer_fragment, container, false);

        mPlanetTitles = getResources().getStringArray(R.array.drawer_items);
        TypedArray ar = getResources().obtainTypedArray(R.array.drawer_item_icons);
        int len = ar.length();
        mIcons = new int[len];
        for (int i = 0; i < len; i++)
            mIcons[i] = ar.getResourceId(i, 0);

        ar.recycle();
        // Set the adapter for the list view
        setListAdapter(new MySimpleArrayAdapter(getActivity(), mPlanetTitles, mIcons));
        // Set the list's click listener

        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        getListView().setDividerHeight(0);
        getListView().setDivider(new ColorDrawable(android.R.color.transparent));
    }
    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        ((FbWrapper)getActivity()).drawerClick(position);
    }

}
