package com.twofromkt.ecomap;

import com.google.android.gms.maps.model.LatLng;
import com.twofromkt.ecomap.activities.MapActivity;
import com.twofromkt.ecomap.db.Cafe;
import com.twofromkt.ecomap.db.GetPlaces;
import com.twofromkt.ecomap.db.TrashBox;

import java.util.HashSet;

import static com.twofromkt.ecomap.db.TrashBox.Category.AND;
import static com.twofromkt.ecomap.db.TrashBox.Category.GLASS;

public class Mock {
    public static void putObjects(MapActivity act) {
        GetPlaces.putObject(new Cafe("Кафе 1", new LatLng(60.043175, 30.409615), "Мое первое кафе",
                null, "", "656-68-52", "", "www.vk.com"), 0, act.getApplicationContext());
        GetPlaces.putObject(new Cafe("Кафе 2", new LatLng(60.143175, 30.509615), "Мое второе кафе",
                null, "", "656-68-53", "", "www.vk.ru"), 0, act.getApplicationContext());
        HashSet<TrashBox.Category> h = new HashSet<>();
        h.add(GLASS);
        h.add(AND);
        GetPlaces.putObject(new TrashBox("Урна 1", new LatLng(60.193175, 30.359615), "Моя первая урна",
                null, "", h), 1, act.getApplicationContext());
        h.remove(AND);
        GetPlaces.putObject(new TrashBox("Урна 2", new LatLng(60.163175, 30.359615), "Моя вторая урна",
                null, "", h), 1, act.getApplicationContext());
    }
}