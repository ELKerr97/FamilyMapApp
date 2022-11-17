package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import model.Event;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private GoogleMap map;
    private final float BIRTH_COLOR = BitmapDescriptorFactory.HUE_BLUE;
    private final float MARRIAGE_COLOR = BitmapDescriptorFactory.HUE_ORANGE;
    private final float DEATH_COLOR = BitmapDescriptorFactory.HUE_RED;
    private final float DEFAULT_COLOR = BitmapDescriptorFactory.HUE_GREEN;

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        View view = layoutInflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set google map
        map = googleMap;
        map.setOnMapLoadedCallback(this);

        // Default login location (arbitrary)
        LatLng location = new LatLng(50, 50);
        map.animateCamera(CameraUpdateFactory.newLatLng(location));

        // Get events from data cache and add them to map
        DataCache dataCache = DataCache.getInstance();
        for(Event event : dataCache.getEvents()){
            float color;
            if(event.getEventType().equalsIgnoreCase("birth")){
                color = BIRTH_COLOR;
            } else if (event.getEventType().equalsIgnoreCase("marriage")){
                color = MARRIAGE_COLOR;
            } else if (event.getEventType().equalsIgnoreCase("death")){
                color = DEATH_COLOR;
            } else {
                color = DEFAULT_COLOR;
            }
            LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
            Marker eventMarker = map.addMarker(
                            new MarkerOptions().
                            position(eventLocation).
                            icon(BitmapDescriptorFactory.defaultMarker(color)));
            assert eventMarker != null;
            eventMarker.setTag(event);

            Context context = this.getContext();
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    // Do stuff with marker
                    Event markerEvent = (Event) marker.getTag();
                    Toast eventToast = Toast.makeText(
                            context,
                            markerEvent.getEventType(),
                            Toast.LENGTH_LONG);
                    eventToast.show();
                    return false;
                }
            });
        }
    }

    @Override
    public void onMapLoaded() {
        // You probably don't need this callback. It occurs after onMapReady and I have seen
        // cases where you get an error when adding markers or otherwise interacting with the map in
        // onMapReady(...) because the map isn't really all the way ready. If you see that, just
        // move all code where you interact with the map (everything after
        // map.setOnMapLoadedCallback(...) above) to here.
    }
}