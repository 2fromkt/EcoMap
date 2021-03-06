package com.twofromkt.ecomap.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.twofromkt.ecomap.map_activity.map.MapClusterItem;
import com.twofromkt.ecomap.place_types.Place;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Util {

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static class PlaceWithCoord {
        public MapClusterItem coordinates;
        public Place place;

        public PlaceWithCoord(Place place, MapClusterItem clusterItem) {
            this.place = place;
            this.coordinates = clusterItem;
        }


        public boolean equals(Object o) {
            return (o instanceof PlaceWithCoord && equals((PlaceWithCoord) o));
        }

        public boolean equals(PlaceWithCoord o) {
            return coordinates.equals(o.coordinates) && place.equals(o.place);
        }
    }

    public static class Timetable implements Serializable {
        private static final String prefix = "0 ";
        private static final int DAYS_IN_WEEK = 7;
        private Period[] table;

        public Timetable(Timetable rhs) {
            System.arraycopy(rhs.table, 0, table, 0, table.length);
        }

        public Timetable(String s) {
            table = new Period[DAYS_IN_WEEK];
            if (s.startsWith(prefix)) {
                table[0] = new Period(s.substring(prefix.length()), false);
            } else if (s.equals("")) {
                table[0] = new Period(s, false);
            } else {
                try {
                    String[] arr = s.split("[\\$]");
                    for (int i = 0; i < DAYS_IN_WEEK; i++) {
                        try {
                            table[i] = new Period(arr[i], true);
                        } catch (Exception e) {
                            table[i] = new Period("Закрыто", false);
                        }
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                if (!checkTimetables()) { // in case of empty timetable
                    for (int i = 0; i < DAYS_IN_WEEK; i++) {
                        table[i].setTime("");
                    }
                }
            }
        }

        public Period[] getTable() {
            return table;
        }

        public boolean checkTimetables() {
            boolean ans = false;
            for (int i = 0; i < DAYS_IN_WEEK; i++) {
                ans |= (table[i] != null && table[i].isItTimetable());
            }
            return ans;
        }
    }

    public static class Period implements Serializable, Comparable<Period> {
        private Time open, close;
        private String time = "";
        private boolean isTimetable;

        Period(String s, boolean isTimetable) {
            this.isTimetable = isTimetable;
            if (!isTimetable) {
                time = s;
            } else {
                String[] arr = s.split("[#]");
                open = new Time(arr[0]);
                close = new Time(arr[1]);
            }
        }

        public Period(String s) {
            String[] a = s.split("[-]"), b = a[0].split("[.]"), c = a[1].split("[.]");
            open = new Time(Integer.parseInt(b[0]), Integer.parseInt(b[1]));
            close = new Time(Integer.parseInt(c[0]), Integer.parseInt(c[1]));
        }

        public Time getOpen() {
            return open;
        }

        public Time getClose() {
            return close;
        }

        public String getTime() {
            return time;
        }

        public String getTimeString() {
            return open.getTimeString() + "-" + close.getTimeString();
        }

        public boolean isItTimetable() {
            return isTimetable;
        }

        public void setTime(String s) {
            time = s;
        }

        @Override
        public int compareTo(Period o) {
            return open.compareTo(o.open);
        }

        public static class Time implements Serializable, Comparable<Time> {
            private int h, m;

            Time(int h, int m) {
                this.h = h;
                this.m = m;
            }

            Time(String s) {
                String[] ar = s.split("[:]");
                h = Integer.parseInt(ar[0]);
                m = Integer.parseInt(ar[1]);
            }

            public boolean before(Time t) {
                return (h < t.h || (h == t.h && m <= t.m));
            }

            public boolean after(Time t) {
                return !before(t);
            }

            public int getH() {
                return h;
            }

            public int getM() {
                return m;
            }

            @SuppressLint("DefaultLocale")
            String getTimeString() {
                return String.format("%02d", h) + ":" + String.format("%02d", m);
            }

            @Override
            public int compareTo(Time o) {
                if (h != o.h)
                    return h - o.h;
                return m - o.m;
            }

            public String toString() {
                return getTimeString();
            }
        }

        public String toString() {
            return open + "-" + close;
        }
    }

    public static class AppendingObjectOutputStream extends ObjectOutputStream {

        public AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }

    }

    @Nullable
    public static LatLngBounds includeAll(ArrayList<? extends Place> data) {
        ArrayList<LatLng> pos = new ArrayList<>();
        for (Place x : data) {
            pos.add(LocationUtil.getLatLng(x.getLocation()));
        }
        if (pos.size() == 0) {
            return null;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        final double margin = .05;
        LatLng top, bottom, left, right;
        top = bottom = left = right = pos.get(0);
        for (LatLng ll : pos) {
            if (top.latitude < ll.latitude) top = ll;
            if (bottom.latitude > ll.latitude) bottom = ll;
            if (left.longitude < ll.longitude) left = ll;
            if (right.longitude > ll.longitude) right = ll;
        }
        top = new LatLng(top.latitude + margin, top.longitude);
        bottom = new LatLng(bottom.latitude - margin, bottom.longitude);
        left = new LatLng(left.latitude, left.longitude - margin);
        right = new LatLng(right.latitude, right.longitude + margin);
        builder.include(top);
        builder.include(bottom);
        builder.include(left);
        builder.include(right);
        return builder.build();
    }

}
