package com.twofromkt.ecomap.map_activity.map;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.Loader;
import android.util.Pair;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twofromkt.ecomap.db.GetPlaces;
import com.twofromkt.ecomap.PlaceTypes.Place;
import com.twofromkt.ecomap.db.ResultType;
import com.twofromkt.ecomap.map_activity.MapActivity;

import java.util.ArrayList;

import static com.twofromkt.ecomap.Consts.CAFE_ID;
import static com.twofromkt.ecomap.Consts.CATEGORIES_NUMBER;
import static com.twofromkt.ecomap.Consts.TRASH_ID;
import static com.twofromkt.ecomap.db.GetPlaces.ANIMATE_MAP;
import static com.twofromkt.ecomap.util.LocationUtil.getLatLng;

class MapUtil {

    private MapView map;

    static volatile ArrayList<ArrayList<Pair<MapClusterItem, ? extends Place>>> activeMarkers;

    static {
        activeMarkers = new ArrayList<>();
        for (int i = 0; i < CATEGORIES_NUMBER; i++) {
            MapView.getActiveMarkers().add(new ArrayList<Pair<MapClusterItem, ? extends Place>>());
        }
    }

    MapUtil(MapView map) {
        this.map = map;
    }

    void searchNearCafe(boolean animateMap) {
        Bundle b = createBundle(animateMap);
        Loader<ResultType> loader;
        b.putInt(GetPlaces.WHICH_PLACE, CAFE_ID);
        loader = map.parentActivity.getSupportLoaderManager()
                .restartLoader(MapActivity.LOADER, b, map.parentActivity.adapter);
        loader.onContentChanged();
        map.parentActivity.bottomSheet.collapse();
    }

    void searchNearTrashes(boolean animateMap) {
        Bundle bundle = createBundle(animateMap);
        Loader<ResultType> loader;
        bundle.putInt(GetPlaces.WHICH_PLACE, TRASH_ID);
        bundle.putInt(GetPlaces.ANY_MATCH_KEY, GetPlaces.ONE_MATCH);
        bundle.putBooleanArray(GetPlaces.CHOSEN, map.parentActivity.bottomSheet.getTrashCategories());
        loader = map.parentActivity.getSupportLoaderManager()
                .restartLoader(MapActivity.LOADER, bundle, map.parentActivity.adapter);
        loader.onContentChanged();
        if (map.parentActivity.bottomInfo.isHidden()) {
            map.parentActivity.bottomSheet.collapse();
        }
    }

    void focusOnMarker(Pair<MapClusterItem, ? extends Place> a) {
        map.parentActivity.bottomSheet.hide();
        map.parentActivity.bottomInfo.collapse();
        map.parentActivity.bottomInfo.addInfo(a.second.getName(), a.second.getClass().getName());
//        moveMap(act.mMap, fromLatLngZoom(a.second.location.val1, a.second.location.val2, MAPZOOM));
    }

    void addMarker(Place place, int type) {
        MapClusterItem clusterItem = new MapClusterItem(place);
        map.clusterManager.addItem(clusterItem);
        activeMarkers.get(type).add(new Pair<>(clusterItem, place));
    }

    <T extends Place> void addMarkers(ArrayList<T> p, CameraUpdate cu, int num, boolean animate) {
        clearMarkers(num);
        ArrayList<LatLng> pos = new ArrayList<>();
        for (Place place : p) {
            addMarker(place, num);
            pos.add(getLatLng(place.getLocation()));
        }
        if (pos.size() > 0 && animate) {
            map.mMap.animateCamera(cu);
        }
        map.parentActivity.bottomSheet.notifyChange();

    }

    void clearMarkers(int num) {
        if (num == -1) {
            return;
        }
        for (Pair<MapClusterItem, ? extends Place> m : activeMarkers.get(num)) {
            map.clusterManager.removeItem(m.first);
        }
        activeMarkers.get(num).clear();
        map.parentActivity.bottomSheet.notifyChange();
    }

    void addLocationSearch(MapActivity act) {
        if (ActivityCompat.checkSelfPermission(act,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.mMap.setMyLocationEnabled(true);
        map.mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private Bundle createBundle(boolean animateMap) {
        Bundle bundle = new Bundle();
        bundle.putInt(GetPlaces.MODE, GetPlaces.IN_BOUNDS);
        LatLng x = map.mMap.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng y = map.mMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        bundle.putDouble(GetPlaces.LAT_MINUS, y.latitude);
        bundle.putDouble(GetPlaces.LNG_MINUS, y.longitude);
        bundle.putDouble(GetPlaces.LAT_PLUS, x.latitude);
        bundle.putDouble(GetPlaces.LNG_PLUS, x.longitude);
        bundle.putBoolean(ANIMATE_MAP, animateMap);
        return bundle;
    }

}
