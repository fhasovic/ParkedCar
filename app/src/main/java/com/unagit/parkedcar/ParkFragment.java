package com.unagit.parkedcar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.unagit.parkedcar.Helpers.Helpers;

import static com.unagit.parkedcar.MainActivity.LOG_TAG;


public class ParkFragment extends Fragment  implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap googleMap;

    /**
     * Interface and its object (parkButtonClickListener), which calls method parkButtonPressed,
     * when Park Car button pressed
     */
    public interface OnParkButtonPressedListener {
        // TODO: Update argument type and name
        void onParkButtonPressed(int action, ParkFragment fragment);
    }
    private OnParkButtonPressedListener parkButtonClickListener;

    private final String PARK_BUTTON = "Park Car";
    private final String CLEAR_BUTTON = "Clear";

    private Boolean isParked = false;
    private Float latitude;
    private Float longitude;
    private Long parkedTime;
    private MyDefaultPreferenceManager myDefaultPreferenceManager;

    private Handler handler = new Handler();
    private Runnable runnable;

    private int i=0;


    public ParkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ParkFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParkFragment newInstance(String param1, String param2) {
        ParkFragment fragment = new ParkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myDefaultPreferenceManager = new MyDefaultPreferenceManager(getContext());
        if (context instanceof OnParkButtonPressedListener) {
            parkButtonClickListener = (OnParkButtonPressedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnParkButtonPressedListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();

        if(isParked) {
            startUIUpdate();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUIUpdate();
    }

    private void startUIUpdate() {
        handler.postDelayed(updateUI(), 1 * 1000);
    }

    private Runnable updateUI() {
        runnable = new Runnable() {
            @Override
            public void run() {
                i++;
                TextView parkedTimeTextView = getView().findViewById(R.id.parked_time_textview);
                String timeDifference = Helpers.timeDifference(parkedTime);
                parkedTimeTextView.setText(timeDifference + " ago. " + i);

                handler.postDelayed(this, 1 * 1000);
            }
        };
        return runnable;
    }

    private void stopUIUpdate() {
        if(runnable != null) {
            handler.removeCallbacks(runnable);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_park, container, false);
        setParkButtonOnClickListener(rootView);
        refreshData();
        setMapCallback();
        if(isParked) {
            Button parkButton = rootView.findViewById(R.id.park_car);
            parkButton.setText(CLEAR_BUTTON);
//            parkButtonClickListener.onParkButtonPressed();
        }
        return rootView;
    }

    /**
     * Flip Park Car button between two states:
     * 1. Park Car - change button text, request current location via callback method
     * 2. Clear - change button text, clear park location
     */
    private void setParkButtonOnClickListener(View view) {
        final Button parkButton = view.findViewById(R.id.park_car);
        parkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (parkButton.getText().equals(PARK_BUTTON)) { /* Park Car */
                    //setMarkerOnMap(); <-- we need to set this in onLocationCallback instead, i.e. when we have new location
                    parkButton.setText(CLEAR_BUTTON);
                    startUIUpdate();
                    // Call listener
                    parkButtonClickListener.onParkButtonPressed(Constants.ParkActions.PARK_CAR, ParkFragment.this);
                } else { /* Clear location */
                    clearMap();
                    parkButton.setText(PARK_BUTTON);
                    stopUIUpdate();
                    // Call listener
                    parkButtonClickListener.onParkButtonPressed(Constants.ParkActions.CLEAR_PARKING_LOCATION, ParkFragment.this);
                }
            }
        });

    }

    private void setMapCallback() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            Log.e(LOG_TAG, "mapFragment is null");
        } else {
            Log.d(LOG_TAG, "Map callback is set");
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Get current location from SharedPreferences
        this.googleMap = googleMap;
        if(isParked) {
            parkButtonClickListener.onParkButtonPressed(Constants.ParkActions.PARK_CAR, this);
        } else {
            parkButtonClickListener.onParkButtonPressed(Constants.ParkActions.CLEAR_PARKING_LOCATION, this);
        }
        // Set marker from parking location on the map
        setMarkerOnMap(null);

    }

    public void setMarkerOnMap(@Nullable Location currentLocation) {
        refreshData();
        clearMap();

        // Show current location on a map
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Parking is cleared. Set map camera to current location instead
        if(!isParked && currentLocation != null) {
            // Move camera to current location
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(17 ));
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLng(currentLatLng), 1* 1000 /* 1 sec. */, null);

        } else if(isParked){
            // Set marker on parking location and move camera on it
            LatLng parkingLocation = new LatLng(latitude, longitude);
             MarkerOptions options = new MarkerOptions();
            options.position(parkingLocation)
                    .title("Your Car")
                    .snippet("Parked 23 min ago")
                    .icon(BitmapDescriptorFactory.fromResource(Constants.GoogleMaps.Parking_icon));
            googleMap.addMarker(options)
                    .showInfoWindow(); /* show title (no need to click on marker to show title) */
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(17 ));
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLng(parkingLocation), 1* 1000 /* 1 sec. */, null);
        }
    }

    private void clearMap() {
        googleMap.clear();
    }

    private void refreshData() {
        isParked = myDefaultPreferenceManager.isParked();
        latitude = myDefaultPreferenceManager.getLatitude();
        longitude = myDefaultPreferenceManager.getLongitude();
        parkedTime = myDefaultPreferenceManager.getTimestamp();
    }
}
