package com.ibm.bluelist;

/**
 * Created by Michael on 4/19/16.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.query.QueryResult;
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
        LocationListener
{
    LinkedList<MarkerOptions> markers;
    LatLng loc;
    double minDistance, maxDistance, distance; //IN METERS
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private int stroke = 10;
    private BlueListApplication mApplication; // Application holds global variables for working with data


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mApplication = BlueListApplication.getInstance();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){

            buildGoogleApiClient();
            mGoogleApiClient.connect();

        }

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
    }

    public void setLoc(LatLng l) {
        loc = l;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (map == null) {
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
        }
        redraw();
    }

    @Override
    public void onMapReady(GoogleMap retMap) {

        map = retMap;

        setUpMap();

    }

    public void setUpMap(){
        map.setMyLocationEnabled(true);
//        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//
//            @Override
//            public void onMapClick(LatLng point) {
//                markers.add(new MarkerOptions().position(point));
//                map.addMarker(new MarkerOptions().position(point));
//            }
//        });
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
        ((TabActivity) getParent()).setLoc(loc.longitude, loc.latitude);
        redraw();
    }

    public void uploadAtLocation(String name, String image, String icon) {
        mApplication.addTodoItem(name, image, icon, loc.longitude, loc.latitude);
        redraw();
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

            QueryResult result = mApplication.getAllItems();

            if (result != null) {
                for (DocumentRevision todoItem : result) {
                    String s = (String) todoItem.getBody().asMap().get(BlueListApplication.TODO_ITEM_ICON_KEY);
                    Bitmap image = getCroppedBitmap(getBitmapFromString(s));

                    double lon = (Double) todoItem.getBody().asMap().get(BlueListApplication.TODO_ITEM_LON_KEY);
                    double lat = (Double) todoItem.getBody().asMap().get(BlueListApplication.TODO_ITEM_LAT_KEY);

                    if (image != null && distFrom((float) loc.latitude, (float) loc.longitude, (float) lat, (float) lon) <= distance) {
                        markers.add(new MarkerOptions().position(
                                new LatLng(lat, lon)).icon(BitmapDescriptorFactory.fromBitmap(image)));
                    }
                }
            }
        }
    }

    private float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(stroke, stroke, bitmap.getWidth()-stroke, bitmap.getHeight()-stroke);

        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2 - stroke, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2 - stroke / 2, paint);

//        paint.setStyle(Paint.Style.FILL);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false)
        //return _bmp;
        return output;
    }

    private Bitmap getBitmapFromString(String jsonString) {
        /*
        * This Function converts the String back to Bitmap
        * */
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }
}