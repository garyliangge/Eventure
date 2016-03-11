package mq.org.eventure;

import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * Created by Michael on 2/26/16.
 */
public class TabActivity extends ActivityGroup {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
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
    }

    public void passMarker(Bitmap m) {
        MapActivity map = (MapActivity) getLocalActivityManager().getActivity("Map Tab");
        map.addMarker(m);
    }
}
