package mq.org.eventure;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    LinkedList<MarkerOptions> markers;
    LatLng loc;
    double minDistance, maxDistance, distance; //IN METERS
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
<<<<<<< HEAD
        markers = new LinkedList<>();
        minDistance = 1000;
        maxDistance = 10000;

        distance = minDistance;
        ((TextView)findViewById(R.id.currentRadius)).setText(distance + " meter radius");

        ((SeekBar)findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = 1000 + ((maxDistance - minDistance) / 9) * progress;
                ((TextView) findViewById(R.id.currentRadius)).setText(distance + " meter radius");
                redraw();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
=======

//        minDistance = 1000;
//        maxDistance = 10000;
//
//        distance = minDistance;
//        ((TextView)findViewById(R.id.currentRadius)).setText(distance + " meter radius");
//
//        //replace GOOGLE MAP fragment in this Activity
//        replaceMapFragment();
//
//        ((SeekBar)findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    distance = 1000 + ((maxDistance - minDistance) / 9) * progress;
//                    ((TextView)findViewById(R.id.currentRadius)).setText(distance + " meter radius");
//                    redraw();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
>>>>>>> cb7fb7dc9ccc5be6da3c4e0159245467846f975b
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){

            buildGoogleApiClient();
            mGoogleApiClient.connect();

        }

        if (map == null) {
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap retMap) {

        map = retMap;

        setUpMap();

    }

    public void setUpMap(){
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                markers.add(new MarkerOptions().position(point));
                map.addMarker(new MarkerOptions().position(point));
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

       // mLocationRequest.setSmallestDisplacement(0.1F);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        loc = new LatLng(location.getLatitude(), location.getLongitude());
        redraw();

    }

    public void addMarker(Bitmap icon) {
        if (loc != null) {
            markers.add(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(icon)));
            redraw();
        }
    }

    private void redraw(){
        if (map != null && loc != null) {
            map.clear();

            for (MarkerOptions o: markers) {
                map.addMarker(o);
            }

            map.addCircle(new CircleOptions()
                    .center(loc)
                    .radius(distance)
                    .strokeColor(Color.parseColor("#A8E9FF"))
                    .fillColor(Color.parseColor("#4DA8E9FF")));
        }
    }
}