package com.ibm.bluelist;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Michael on 2/26/16.
 */
public class TabActivity extends ActivityGroup {
    double lon;
    double lat;
    CustomTabHost tabHost;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        lon = 0.0;
        lat = 0.0;

        tabHost = (CustomTabHost)findViewById(R.id.tabHost);
        tabHost.setup(this.getLocalActivityManager());

        TabHost.TabSpec mapTab = tabHost.newTabSpec("Map Tab");
        TabHost.TabSpec cameraTab = tabHost.newTabSpec("Camera Tab");
        TabHost.TabSpec feedTab = tabHost.newTabSpec("Feed Tab");

        mapTab.setIndicator("Map");
        mapTab.setContent(new Intent(this, MapActivity.class));

        feedTab.setIndicator("Feed");
        feedTab.setContent(new Intent(this, FeedActivity.class));

        cameraTab.setIndicator("Upload");
        cameraTab.setContent(new Intent(this, UploadActivity.class));

        tabHost.addTab(mapTab);
        tabHost.addTab(cameraTab);
        tabHost.addTab(feedTab);

        tabHost.setCurrentTab(0);
        tabHost.setLocationFound(false);
    }

    public void setLoc(double lo, double la) {
        lon = lo;
        lat = la;
        tabHost.setLocationFound(true);
        MapActivity map = (MapActivity) getLocalActivityManager().getActivity("Map Tab");
        map.setLoc(new LatLng(lat, lon));
    }

    public LatLng getLoc() {
        return new LatLng(lat, lon);
    }

    public void upload(String name, String bitmap, String icon) {
        MapActivity map = (MapActivity) getLocalActivityManager().getActivity("Map Tab");
        map.uploadAtLocation(name, bitmap, icon);
    }

    public void goToMap() {
        tabHost.setCurrentTab(0);
    }
}
