package com.twofromkt.ecomap.map_activity.map;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.twofromkt.ecomap.place_types.Place;
import com.twofromkt.ecomap.map_activity.MapActivity;
import com.twofromkt.ecomap.util.LocationUtil;

import java.util.ArrayList;

import static com.twofromkt.ecomap.Consts.TRASH_ID;
import static com.twofromkt.ecomap.util.LocationUtil.fromLatLngZoom;

class MapAdapter implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<MapClusterItem>,
        ClusterManager.OnClusterItemClickListener<MapClusterItem>,
        FloatingActionButton.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private MapView map;

    MapAdapter(MapView map) {
        this.map = map;
    }

    @Override
    public boolean onClusterClick(Cluster<MapClusterItem> cluster) {
        map.moveMap(LocationUtil.fromLatLngZoom(cluster.getPosition(),
                map.mMap.getCameraPosition().zoom + 1));
        return true;
    }

    @Override
    public boolean onClusterItemClick(MapClusterItem clusterItem) {
        map.parentActivity.bottomSheet.hide();
        map.parentActivity.bottomInfo.collapse();
        Place p = null;
        for (ArrayList<Pair<MapClusterItem, ? extends Place>> ac : MapView.getAllMarkers()) {
            for (Pair<MapClusterItem, ? extends Place> x : ac) {
                if (x.first.equals(clusterItem)) {
                    p = x.second;
                    break;
                }
            }
        }
        if (p == null) {
            Toast.makeText(map.parentActivity, "Unable to find place", Toast.LENGTH_SHORT).show();
            return false;
        }
        map.loadPlace(p.getId(), p.getCategoryNumber());
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map.mMap = googleMap;
        if (map.startPos != null) {
            map.moveMap(map.startPos);
        }
        if (ActivityCompat.checkSelfPermission(
                map.parentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(map.parentActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MapActivity.GPS_REQUEST);
            return;
        }
        map.mMap.setMyLocationEnabled(true);
        map.mMap.getUiSettings().setMyLocationButtonEnabled(false);
        Location location = map.getLocation();
        if (location != null) {
            CameraPosition position = LocationUtil.fromLatLngZoom(location.getLatitude(),
                    location.getLongitude(), MapView.MAPZOOM - 4);
            if (!map.hasCustomLocation) {
                map.moveMap(position);
                map.hasCustomLocation = true;
            }
        }
        MapMultiListener listener = new MapMultiListener();
        map.clusterManager = new ClusterManager<>(map.parentActivity, map.mMap);
        listener.addListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (map.parentActivity.typePanel.isChosen(TRASH_ID)) {
                    map.showTrashMarkers();
                }
//                element_map.util.searchNearCafe();
            }
        });
//        listener.addListener(map.clusterManager);
        map.mMap.setOnCameraIdleListener(listener);
        map.mMap.setOnMarkerClickListener(map.clusterManager);
        map.clusterManager.setOnClusterItemClickListener(this);
        map.clusterManager.setOnClusterClickListener(this);
        IconRenderer renderer = new IconRenderer(map.parentActivity, map.mMap, map.clusterManager);
        map.clusterManager.setRenderer(renderer);
        if (!map.placesLoaded) {
            map.util.loadAllPlaces();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == map.locationButton) {
            Location location = map.getLocation();
            if (location != null) {
                map.moveMap(fromLatLngZoom(location.getLatitude(), location.getLongitude(), MapView.MAPZOOM));
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("MAP", "google api connected");
        Location location = map.getLocation();
        if (location != null && !map.hasCustomLocation) {
            CameraPosition position = LocationUtil.fromLatLngZoom(location.getLatitude(),
                    location.getLongitude(), MapView.MAPZOOM - 4);
            map.moveMap(position);
            map.hasCustomLocation = true;
        }
    }

    //TODO do something with that
    @Override
    public void onConnectionSuspended(int i) {
        Log.d("MAP", "connection suspended");
        Toast.makeText(map.parentActivity, "Connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MAP", "connection failed");
        Toast.makeText(map.parentActivity, "Connection failed", Toast.LENGTH_SHORT).show();
    }
}
