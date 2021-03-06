package com.twofromkt.ecomap.map_activity.bottom_sheet;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.twofromkt.ecomap.R;
import com.twofromkt.ecomap.map_activity.MapActivity;
import com.twofromkt.ecomap.map_activity.map.MapView;
import com.twofromkt.ecomap.util.CustomViewPager;
import com.twofromkt.ecomap.util.Util;

import java.util.ArrayList;

import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;

public class BottomSheetView extends RelativeLayout {

    ViewPagerBottomSheetBehavior bottomList;
    CustomViewPager listViewPager;
    TrashSett trashSett;
    TabLayout listTabLayout;
    ListViewPagerAdapter listPagerAdapter;

    RelativeLayout categoriesLayout;
    RelativeLayout listLayout;
    RelativeLayout collapsedPart;
    FragmentManager fragmentManager;
    View bottomListView;

    MapActivity parentActivity;

    private BottomSheetAdapter adapter;

    public BottomSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.element_bottom_sheet, this);
    }

    public void attach(FragmentManager fragmentManager, MapActivity parentActivity) {
        this.parentActivity = parentActivity;
        this.fragmentManager = fragmentManager;

        adapter = new BottomSheetAdapter(this);
        initFields();
        setListeners();
    }

    private void initFields() {
        bottomListView = findViewById(R.id.bottom_list);
        bottomList = ViewPagerBottomSheetBehavior.from(bottomListView);

        listPagerAdapter =
                new ListViewPagerAdapter(fragmentManager, MapView.getShownMarkers(), parentActivity);
        listViewPager = (CustomViewPager) findViewById(R.id.list_viewpager);
        listViewPager.setAdapter(listPagerAdapter);

        listTabLayout = (TabLayout) findViewById(R.id.list_tabs);
        listTabLayout.setupWithViewPager(listViewPager);

        trashSett = (TrashSett) findViewById(R.id.bottom_sheet_trash_sett);
        trashSett.attach(parentActivity);

//        settTabLayout.setVisibility(GONE);
        listTabLayout.setVisibility(GONE);

        categoriesLayout = (RelativeLayout) findViewById(R.id.categories_layout);
        collapsedPart = (RelativeLayout) findViewById(R.id.collapsed_part);
        listLayout = (RelativeLayout) findViewById(R.id.list_layout);
    }

    private void setListeners() {
        bottomList.setBottomSheetCallback(adapter);
        collapsedPart.setOnTouchListener(adapter);
        collapsedPart.setOnClickListener(adapter);
    }

    public void focusOnTab(int i) {
        listViewPager.setCurrentItem(i);
    }

    /**
     * Updates the list in the tab number index with newData.
     * Note that newData can be passed by reference because it is
     * being copied inside.
     * @param index Index of tab we need to update
     * @param newData New data to update with
     */
    public void updateList(int index, ArrayList<Util.PlaceWithCoord> newData) {
        listPagerAdapter.updateList(index, newData);
    }

//    public boolean[] getTrashCategories() {
//        return settPagerAdapter.trashSett.chosen;
//    }

    public void collapse() {
        if (isHidden()) {
            parentActivity.map.moveUpLocationButton();
        }
        bottomList.setHideable(false);
        bottomList.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    //TODO that should be removed
    public void show() {
        if (isHidden()) {
            collapse();
        }
    }

    public void expand() {
        if (!isExpanded()) {
            bottomList.setHideable(false);
            bottomList.setState(ViewPagerBottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void hide() {
        if (!isHidden()) {
            parentActivity.map.moveDownLocationButton();
        }
        bottomList.setHideable(true);
        bottomList.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public boolean isExpanded() {
        return bottomList.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    public boolean isCollapsed() {
        return bottomList.getState() == BottomSheetBehavior.STATE_COLLAPSED;
    }

    public boolean isHidden() {
        return bottomList.getState() == BottomSheetBehavior.STATE_HIDDEN;
    }

    public float getPeekHeight() {
        return bottomList.getPeekHeight();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superSaved = super.onSaveInstanceState();
        return new SavedBottomSheet(superSaved, bottomList.getState());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedBottomSheet savedState = (SavedBottomSheet) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        bottomList.setState(savedState.getState());
        if (bottomList.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomList.setHideable(false);
        }
    }

    public boolean isChecked(int i) {
        return trashSett.chosen[i];
    }
}