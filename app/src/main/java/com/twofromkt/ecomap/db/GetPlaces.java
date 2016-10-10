package com.twofromkt.ecomap.db;

import com.android.internal.util.Predicate;
import com.google.android.gms.maps.model.LatLng;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.twofromkt.ecomap.Util.*;

public class GetPlaces {
    private static final String[] FILE_NAMES = new String[]{"cafes", "trashes"};
    void putObject(Place p, int category) {
        try {
            FileOutputStream out = new FileOutputStream(FILE_NAMES[category]);
            ObjectOutputStream outO = new ObjectOutputStream(out);
            outO.writeObject(p);
            outO.flush();
            outO.close();
        } catch (IOException e) { //TODO: do something good
            e.printStackTrace();
        }
    }

    private static <T extends Place> ArrayList<T> getPlaces(Predicate<T> pr) {
        ArrayList<T> ans = new ArrayList<>();
        try {
            FileInputStream in = new FileInputStream(FILE_NAMES[0]);
            ObjectInputStream inO = new ObjectInputStream(in);
            T x;
            while ((x = (T)inO.readObject()) != null) {
                if (pr.apply(x))
                    ans.add(x);
            }
        } catch (IOException | ClassNotFoundException e) { //TODO: do something good
            e.printStackTrace();
        }
        return ans;
    }

    static ArrayList<Cafe> getCafes(final LatLng x, final double radii) {
        return getPlaces(new Predicate<Cafe>() {
            @Override
            public boolean apply(Cafe o) {
                return distanceLatLng(x, o.location) < radii;
            }
        });
    }

    public static ArrayList<TrashBox> getTrashes(final LatLng x, final double radii, boolean[] arr) {
        Set<TrashBox.Category> s = new HashSet<>();
        for (int i = 0; i < arr.length; i++)
            if (arr[i])
                s.add(TrashBox.Category.fromIndex(i));
        final Set<TrashBox.Category> finalS = s;
        return getPlaces(new Predicate<TrashBox>() {
            @Override
            public boolean apply(TrashBox o) {
                return distanceLatLng(x, o.location) < radii && finalS.contains(o.category);
            }
        });
    }



}
