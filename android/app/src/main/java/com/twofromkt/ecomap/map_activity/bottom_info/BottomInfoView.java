package com.twofromkt.ecomap.map_activity.bottom_info;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.telephony.PhoneNumberUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twofromkt.ecomap.place_types.Ecomobile;
import com.twofromkt.ecomap.place_types.Place;
import com.twofromkt.ecomap.place_types.TrashBox;
import com.twofromkt.ecomap.R;
import com.twofromkt.ecomap.map_activity.MapActivity;
import com.twofromkt.ecomap.util.Util;

import static biz.laenger.android.vpbs.R.id.scrollView;
import static biz.laenger.android.vpbs.R.id.time;
import static com.twofromkt.ecomap.Consts.TRASH_TYPES_NUMBER;

public class BottomInfoView extends LinearLayout {


    private static final int DAYS_IN_WEEK = 7;
    private static final String[] DAYS_OF_WEEK = {"Понедельник", "Вторник", "Среда", "Четверг",
            "Пятница", "Суббота", "Воскресенье"};
    BottomSheetBehavior bottomInfo;
    TextView name, information, address;
    NestedScrollView bottomInfoView;
    MapActivity parentActivity;
    ImageView icon;

    Place currPlace;
    TextView[] timetable, dayOfWeek;
    ImageView[] trashTypesIcons;
    LinearLayout trashTypesIconsLayout;
    ImageButton callButton, routeButton, siteButton;

    private BottomInfoAdapter adapter;

    public BottomInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.element_bottom_info, this);
    }

    public void attach(MapActivity parentActivity) {
        this.parentActivity = parentActivity;
        bottomInfoView = (NestedScrollView) findViewById(R.id.bottom_info_scroll_view);
        bottomInfo = BottomSheetBehavior.from(bottomInfoView);
        name = (TextView) findViewById(R.id.bottom_info_name_text);
        address = (TextView) findViewById(R.id.bottom_info_address_text);
        information = (TextView) findViewById(R.id.bottom_info_info_text);
        adapter = new BottomInfoAdapter(this);
        trashTypesIcons = new ImageView[TRASH_TYPES_NUMBER];
        trashTypesIconsLayout = (LinearLayout) findViewById(R.id.bottom_info_trash_icons_layout);
        callButton = (ImageButton) findViewById(R.id.bottom_info_call_button);
        routeButton = (ImageButton) findViewById(R.id.bottom_info_route_button);
        siteButton = (ImageButton) findViewById(R.id.bottom_info_site_button);
        timetable = new TextView[DAYS_IN_WEEK];
        dayOfWeek = new TextView[DAYS_IN_WEEK];
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            try {
                LinearLayout l = (LinearLayout) findViewById(
                        (Integer) R.id.class.getField("timetable" + i).get(null));
                timetable[i] = (TextView) l.findViewById(R.id.time_textview);
                dayOfWeek[i] = (TextView) l.findViewById(R.id.day_of_week_text);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < TRASH_TYPES_NUMBER; i++) {
            ImageView icon = new ImageView(parentActivity);
            try {
                icon.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.class.getField("trash" + (i + 1) + "selected").getInt(null)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            LinearLayout.LayoutParams layoutParams = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int marginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
            layoutParams.setMargins(0, 0, marginRight, 0);
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, metrics);
            layoutParams.width = layoutParams.height = size;
            trashTypesIconsLayout.addView(icon, layoutParams);
            icon.setVisibility(VISIBLE);
            trashTypesIcons[i] = icon;
        }
        icon = (ImageView) findViewById(R.id.bottom_info_type_icon);
        routeButton.setVisibility(GONE);
        setListeners();
    }

    private void setListeners() {
        bottomInfo.setBottomSheetCallback(adapter);
        callButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currPlace == null) {
                    return;
                }
                if (!PhoneNumberUtils.isGlobalPhoneNumber(currPlace.getPhone())) {
                    Toast.makeText(parentActivity, currPlace.getPhone() + " -- сюда нельзя позвонить",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String uri = "tel:" + currPlace.getPhone();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                if (ActivityCompat.checkSelfPermission(parentActivity,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BOTTOM_INFO", "phone calls are not permitted");
                    ActivityCompat.requestPermissions(parentActivity,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MapActivity.PHONE_REQUEST);
                    return;
                }
                parentActivity.startActivity(intent);
            }
        });
        siteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currPlace == null) {
                    return;
                }
                if (!URLUtil.isValidUrl(currPlace.getWebsite())) {
                    Toast.makeText(parentActivity, currPlace.getWebsite() + " -- нет такого адреса",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(currPlace.getWebsite()));
                parentActivity.startActivity(intent);
            }
        });
    }

    public void collapse() {
        bottomInfo.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void hide() {
        bottomInfo.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public boolean isExpanded() {
        return bottomInfo.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    public boolean isCollapsed() {
        return bottomInfo.getState() == BottomSheetBehavior.STATE_COLLAPSED;
    }

    public boolean isHidden() {
        return bottomInfo.getState() == BottomSheetBehavior.STATE_HIDDEN;
    }

    //TODO: make different setPlace for Cafe and TrashBox
    public void setPlace(Place place) {
        if (place == null) {
            return;
        }
        this.currPlace = place;
        if (place instanceof Ecomobile) {
            showCategoriesEcomobile();
            icon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ecomobile_icon));
            name.setText(place.getAddress());
            information.setText("Мы ничего не знаем про это место :(");
            dayOfWeek[0].setText(((Ecomobile) place).getPeriod());
            timetable[0].setText("");
            for (int i = 1; i < 7; i++) {
                dayOfWeek[i].setText("");
                timetable[i].setText("");
            }
        } else {
            icon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.trashbox_icon));
            name.setText(place.getName());
            information.setText(place.getInformation());
            showCategories((TrashBox) place);
            showTimetable(place);
        }
        address.setText(place.getAddress());
        callButton.setVisibility(place.getPhone() == null ? GONE : VISIBLE);
        siteButton.setVisibility(place.getWebsite() == null ? GONE : VISIBLE);
    }

    private void showCategories(TrashBox trashBox) {
        for (int i = 0; i < TRASH_TYPES_NUMBER; i++) {
            if (trashBox.isOfCategory(i)) {
                trashTypesIcons[i].setVisibility(VISIBLE);
            } else {
                trashTypesIcons[i].setVisibility(GONE);
            }
        }
    }

    private void showCategoriesEcomobile() {
        boolean[] can = {false, false, false, false, false, true, true, true, true, true, false};
        for (int i = 0; i < TRASH_TYPES_NUMBER; i++) {
            if (can[i]) {
                trashTypesIcons[i].setVisibility(VISIBLE);
            } else {
                trashTypesIcons[i].setVisibility(GONE);
            }
        }
    }

    //TODO hide icon if timetable is not available
    private void showTimetable(Place place) {
        Util.Timetable workTime = place.getWorkTime();
        if (workTime == null) {
            return;
        }
        if (!workTime.checkTimetables()) {
            dayOfWeek[0].setText(workTime.getTable()[0].getTime());
            clearTexts();
            return;
        }
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            dayOfWeek[i].setText(DAYS_OF_WEEK[i]);
            if (!workTime.getTable()[i].isItTimetable()) {
                timetable[i].setText(workTime.getTable()[i].getTime());
            } else {
                timetable[i].setText(workTime.getTable()[i].getTimeString());
            }
        }
    }

    private void clearTexts() {
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            if (i > 0) {
                dayOfWeek[i].setText("");
            }
            timetable[i].setText("");
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superSaved = super.onSaveInstanceState();
        return new SavedBottomInfo(superSaved, bottomInfo.getState(), currPlace);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedBottomInfo savedState = (SavedBottomInfo) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        bottomInfo.setState(savedState.getState());
        currPlace = savedState.getPlace();
        setPlace(currPlace);
    }

}