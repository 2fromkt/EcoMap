package com.twofromkt.ecomap.map_activity.bottom_sheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.twofromkt.ecomap.R;
import com.twofromkt.ecomap.map_activity.MapActivity;

public abstract class Sett extends android.support.v4.app.Fragment {
    MapActivity mapActivity;

    public void setMapActivity(MapActivity act) {
        this.mapActivity = act;
    }

    public static class CafeSett extends Sett {

        public CafeSett() {
        }

        @Override
        public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstance) {
            return li.inflate(R.layout.cafe_sett_fragment, null);
        }
    }

    public static class TrashSett extends Sett {

        public TrashSett() {
            trashCategoryButtons = new Button[TRASH_N];
            chosen = new boolean[TRASH_N];
        }

        boolean[] chosen;
        Button[] trashCategoryButtons;
        static final int TRASH_N = 3;
        final static float[] ALPHAS = new float[]{(float) 0.6, 1};

        @Override
        public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstance) {
            View view = li.inflate(R.layout.categories_fragment, null);
            for (int i = 0; i < TRASH_N; i++) {
                try {
                    trashCategoryButtons[i] = (Button) view.findViewById((Integer) R.id.class.getField("trash" + (i + 1)).get(null));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                trashCategoryButtons[i].setAlpha(ALPHAS[chosen[i] ? 1 : 0]);
                final int fi = i;
                trashCategoryButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chosen[fi] = !chosen[fi];
                        trashCategoryButtons[fi].setAlpha(ALPHAS[chosen[fi] ? 1 : 0]);
//                    if (mapActivity.chosenCheck[TRASH_NUM]) {
//                        mapActivity.searchTrashes();
//                    }
                    }
                });
            }
            return view;
        }
    }

    public static class OtherSett extends Sett {
        public OtherSett() {
        }

        @Override
        public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstance) {
            return li.inflate(R.layout.other_sett_fragment, null);
        }
    }
}