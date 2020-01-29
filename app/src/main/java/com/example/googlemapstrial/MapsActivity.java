package com.example.googlemapstrial;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity
        implements
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        OnMapReadyCallback,
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

        private GoogleMap mMap;

        private Spinner sp_settings;

        static  final int LOCATE_ME = 0;
        static  final int LOCATE_DRONE = 1;
        static  final int SET_DEST = 2;
        static  final int SET_MULTIPLE_DEST = 3;

        private LinearLayout lyt_dest;
        private Button btn_start, btn_stop, btn_clear, btn_add;


        static final LatLng cetec = new LatLng(25.649, -100.2897398);
        private LatLng MyLatLng, droneLatLng; //phone, drone location
        private Marker droneMarker;

        private List<Marker> markers = new ArrayList<Marker>();

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    MyLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                }
            }
        });

        initUI();
    }

    private void initUI() {
        //spinner dropdown
        sp_settings = (Spinner) findViewById(R.id.sp_settings);
        ArrayAdapter<String> sp_adapter = new ArrayAdapter<String>(
                MapsActivity.this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.map_settings));
        sp_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sp_settings.setAdapter(sp_adapter);
        sp_settings.setOnItemSelectedListener(this);

        //Buttons set destination
        lyt_dest = (LinearLayout) findViewById(R.id.lyt_dest);

        btn_add = (Button) findViewById(R.id.add);
        btn_clear = (Button) findViewById(R.id.clear);
        btn_stop = (Button) findViewById(R.id.stop);
        btn_start = (Button) findViewById(R.id.start);

        btn_add.setOnClickListener(this);
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        //init Marker
        MarkerOptions a = new MarkerOptions().position(cetec).title("Drone");
        droneMarker = mMap.addMarker(a);

        /*

        // Move the camera instantly to Sydney with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));

        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(37.35, -122.0),
                        new LatLng(37.45, -122.0),
                        new LatLng(37.45, -122.2),
                        new LatLng(37.35, -122.2),
                        new LatLng(37.35, -122.0));

        // Get back the mutable Polygon
        Polygon polygon = mMap.addPolygon(rectOptions);
        */
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this,
                "Current phone location\n" +
                        "lat: " + location.getLatitude() + "\n" +
                        "long: " + location.getLongitude(),
                Toast.LENGTH_LONG).show();
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add: {
                addMarker();
                break;
            }
            case R.id.clear: {
                clearMarkers();
                break;
            }
        }
    }

    //Spinning selector
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        lyt_dest.setVisibility(View.GONE);
        switch (position) {
            case LOCATE_ME: {
                // Move the camera instantly to My location with a zoom of 17.
                moveToLocation(MyLatLng);
                break;
            }
            case LOCATE_DRONE: {
                moveToLocation(cetec);
                break;
            }
            case SET_DEST: {
                lyt_dest.setVisibility(View.VISIBLE);
                break;
            }
            case SET_MULTIPLE_DEST: {
                lyt_dest.setVisibility(View.VISIBLE);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        moveToLocation(MyLatLng);
    }

    public void moveToLocation(LatLng loc) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,17));
    }

    public void addMarker() {
        int number = markers.size() + 1;
        MarkerOptions a = new MarkerOptions()
                .position(MyLatLng)
                .draggable(true)
                .title("Stage " + number);

        Marker m = mMap.addMarker(a);
        m.setPosition(MyLatLng);

        markers.add(m);
    }

    public void clearMarkers() {
        for (int i = 0; i < markers.size(); i++) {
            Marker aux = markers.get(i);
            aux.remove();
        }

        markers.clear();
    }
}
