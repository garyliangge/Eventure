package mq.org.eventure;

import android.app.ActivityGroup;
import android.content.Intent;
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

        TabHost.TabSpec mapTab = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec feedTab = tabHost.newTabSpec("Second Tab");

        mapTab.setIndicator("Map");
        mapTab.setContent(new Intent(this, MapActivity.class));

        feedTab.setIndicator("Feed");
        feedTab.setContent(new Intent(this, FeedActivity.class));

        tabHost.addTab(mapTab);
        tabHost.addTab(feedTab);
    }
}
