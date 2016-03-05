package mq.org.eventure;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


public class MapActivity extends AppCompatActivity {

    LatLng loc;
    double minDistance, maxDistance, distance; //IN METERS
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        minDistance = 1000;
        maxDistance = 10000;

        distance = minDistance;
        ((TextView)findViewById(R.id.currentRadius)).setText(distance + " meter radius");

        //replace GOOGLE MAP fragment in this Activity
        replaceMapFragment();

        ((SeekBar)findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    distance = 1000 + ((maxDistance - minDistance) / 9) * progress;
                    ((TextView)findViewById(R.id.currentRadius)).setText(distance + " meter radius");
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

    private void replaceMapFragment() {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        // Enable Zoom
        map.getUiSettings().setZoomGesturesEnabled(true);

        //set Map TYPE
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //enable Current location Button
        map.setMyLocationEnabled(true);

        //set "listener" for changing my location
        map.setOnMyLocationChangeListener(myLocationChangeListener());
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener() {
        return new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                loc = new LatLng(location.getLatitude(), location.getLongitude());

                redraw();
            }
        };
    }

    private void redraw(){
        if (map != null && loc != null) {
            map.clear();

            map.addCircle(new CircleOptions()
                    .center(loc)
                    .radius(distance)
                    .strokeColor(Color.parseColor("#A8E9FF"))
                    .fillColor(Color.parseColor("#4DA8E9FF")));
        }
    }
}