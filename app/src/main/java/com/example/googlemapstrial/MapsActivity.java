package com.example.googlemapstrial;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnCircleClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
        OnMarkerClickListener,
        OnMarkerDragListener,
        OnCircleClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    //constants
    static  final int LOCATE_ME = 0;
    static  final int LOCATE_DRONE = 1;
    static  final int SET_DEST = 2;
    static  final int SET_DEST_MANUALLY = 3;
    static  final int RETURN_HOME = 4;
    static  final int ADD_GEOFENCE = 5;
    static  final int ADD_GEOFENCE_MANUALLY = 6;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    //UI variables
    private Spinner sp_settings;
    private LinearLayout lyt_dest;
    private Button btn_start, btn_stop, btn_clear, btn_add;

    //Google vars
    private GoogleMap mMap;

    static final LatLng cetec = new LatLng(25.649, -100.2897398);
    private LatLng MyLatLng, droneLatLng; //phone, drone location

    private Marker droneMarker;
    private List<Marker> markers = new ArrayList<Marker>();

    private Boolean isAddManually = false;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private FusedLocationProviderClient fusedLocationClient;

    //Geofence variables
    private GeofencingClient geofencingClient;
    private List<Geofence> geoList = new ArrayList<Geofence>();
    private double GEOFENCE_RADIUS = 100;
    private Marker geofenceMarker;

    private Circle geoFenceLimits;

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
        //geofencingClient = LocationServices.getGeofencingClient(this);
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
        mMap.setOnMarkerDragListener(this);


        enableMyLocation();

        //init Marker
        MarkerOptions a = new MarkerOptions().position(cetec).title("Drone").icon(BitmapDescriptorFactory.fromResource(R.drawable.drone_pin));
        //Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
        droneMarker = mMap.addMarker(a);

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCircleClickListener(this);
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
                if (isAddManually) {
                    openDialogDest();
                } else {
                    addMarker(MyLatLng);
                }
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
                isAddManually = false;
                break;
            }
            case SET_DEST_MANUALLY: {
                lyt_dest.setVisibility(View.VISIBLE);
                isAddManually = true;
                break;
            }
            case ADD_GEOFENCE:{
                removeGeofence();
                addGeoMarker(MyLatLng);
                break;
            }
            case ADD_GEOFENCE_MANUALLY:{
                openDialogGeofence();
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

    public void addMarker(LatLng latlng) {
        int stageNumber = markers.size() + 1;
        MarkerOptions a = new MarkerOptions()
                .position(latlng)
                .draggable(true)
                .title("Stage " + stageNumber);

        Marker m = mMap.addMarker(a);
        m.setPosition(latlng);

        markers.add(m);
        moveToLocation(latlng);
    }

    public void addGeoMarker(LatLng latlng) {
        if(geofenceMarker == null) {
            MarkerOptions a = new MarkerOptions()
                    .position(latlng)
                    .draggable(true)
                    .title("GEOFENCE");

            geofenceMarker = mMap.addMarker(a);
        }

        geofenceMarker.setPosition(latlng);
        moveToLocation(latlng);
    }

    public void clearMarkers() {
        for (int i = 0; i < markers.size(); i++) {
            Marker aux = markers.get(i);
            aux.remove();
        }

        markers.clear();
    }

    public void openDialogDest() {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_destination, null);

        final EditText latText = (EditText) dialogView.findViewById(R.id.latitude);
        final EditText lngText = (EditText) dialogView.findViewById(R.id.longitude);
        final EditText altText = (EditText) dialogView.findViewById(R.id.altitude);
        Button btnAdd = (Button) dialogView.findViewById(R.id.add);

        mBuilder.setView(dialogView);
        final AlertDialog dialog = mBuilder.create();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!latText.getText().toString().isEmpty() && !lngText.getText().toString().isEmpty()) {
                    final Double lat = Double.parseDouble(latText.getText().toString());
                    final Double lng = Double.parseDouble(lngText.getText().toString());
                    final Double alt = Double.parseDouble(altText.getText().toString());
                    addMarker(new LatLng(lat, lng));
                    dialog.cancel();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Set a lat & lng to continue",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        dialog.show();
    }

    public void openDialogGeofence() {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_geofence, null);

        final EditText latText = (EditText) dialogView.findViewById(R.id.latitude);
        final EditText lngText = (EditText) dialogView.findViewById(R.id.longitude);
        final EditText radioText = (EditText) dialogView.findViewById(R.id.radio);
        Button btnAdd = (Button) dialogView.findViewById(R.id.add);

        mBuilder.setView(dialogView);
        final AlertDialog dialog = mBuilder.create();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!latText.getText().toString().isEmpty() && !lngText.getText().toString().isEmpty()) {
                    final Double lat = Double.parseDouble(latText.getText().toString());
                    final Double lng = Double.parseDouble(lngText.getText().toString());

                    if(!radioText.getText().toString().isEmpty()) {
                        final Double radio = Double.parseDouble(radioText.getText().toString());
                        GEOFENCE_RADIUS = radio;
                    }

                    addGeoMarker(new LatLng(lat, lng));
                    addGeofence();
                    dialog.cancel();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Set a lat & lng to continue",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        dialog.show();
    }

    public void openDialogRadioGeofence() {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_radio_geofence, null);

        final EditText radioText = (EditText) dialogView.findViewById(R.id.radio);
        Button btnAdd = (Button) dialogView.findViewById(R.id.add);
        Button btnRemove = (Button) dialogView.findViewById(R.id.remove);

        mBuilder.setView(dialogView);
        final AlertDialog dialog = mBuilder.create();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!radioText.getText().toString().isEmpty()) {
                    final Double radio = Double.parseDouble(radioText.getText().toString());
                    GEOFENCE_RADIUS = radio;
                    addGeofence();
                    dialog.cancel();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Set a valid Radio",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                removeGeofence();
                geofenceMarker.remove();
                geofenceMarker = null;
            }
        });

        dialog.show();
    }

    //Draw the circle for now
    public void addGeofence() {
        Log.d("call", "drawGeofence()");
        removeGeofence();
        addGeofenceCircle();
    }

    //remove circle for now
    private void removeGeofence() {
        if ( geoFenceLimits != null ) {
            Log.d("call", "fence limits not null");
            geoFenceLimits.remove();
        }
    }

    private void addGeofenceCircle() {
        Toast.makeText(this, "pos: " + geofenceMarker.getPosition(), Toast.LENGTH_SHORT).show();
        CircleOptions circleOptions = new CircleOptions()
                .center( geofenceMarker.getPosition() )
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius( GEOFENCE_RADIUS );
        geoFenceLimits = mMap.addCircle( circleOptions );
        geoFenceLimits.setClickable(true);
    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Toast.makeText(this, "pos: " + marker.getPosition(), Toast.LENGTH_SHORT).show();
        if(marker.equals(geofenceMarker)) {
            Toast.makeText(this, "Geo Marker", Toast.LENGTH_SHORT).show();
            addGeofence();
        }

        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onCircleClick(Circle circle) {
        Toast.makeText(this, "Set Radio", Toast.LENGTH_SHORT).show();
        openDialogRadioGeofence();
    }
}