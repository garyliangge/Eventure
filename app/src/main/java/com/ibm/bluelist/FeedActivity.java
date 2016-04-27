package com.ibm.bluelist;

import android.app.ListActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.query.QueryResult;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Created by Michael on 2/18/16.
 */
public class FeedActivity extends ListActivity {
    private BlueListApplication mApplication; // Application holds global variables for working with data

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = BlueListApplication.getInstance();
        sync();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        String selectedValue = (String) ((String) getListAdapter().getItem(position));
        Toast.makeText(this, selectedValue, Toast.LENGTH_SHORT).show();
    }

    private void sync() {
        HashMap<Float, DocumentRevision> distances = new HashMap<>();
        LinkedList<Bitmap> images = new LinkedList<>();
        LinkedList<String> names = new LinkedList<>();
        LinkedList<Float> calculated = new LinkedList<>();

        QueryResult result = mApplication.getAllItems();
        if (result == null) {
            return;
        }
        for (DocumentRevision d : result) {
            double lon = (double) d.getBody().asMap().get(BlueListApplication.TODO_ITEM_LON_KEY);
            double lat = (double) d.getBody().asMap().get(BlueListApplication.TODO_ITEM_LAT_KEY);

            distances.put(roundTwoD(distFrom((float) ((TabActivity) getParent()).getLoc().latitude,
                    (float) ((TabActivity) getParent()).getLoc().longitude, (float) lat, (float) lon)), d);
        }

        SortedSet<Float> keys = new TreeSet<Float>(distances.keySet());
        for (float key : keys) {
            calculated.add(key);
            names.add((String) distances.get(key).getBody().asMap().get(BlueListApplication.TODO_ITEM_NAME_KEY));
            String s = (String) distances.get(key).getBody().asMap().get(BlueListApplication.TODO_ITEM_IMAGE_KEY);
            images.add(getBitmapFromString(s));
        }
        setListAdapter(new EventArrayAdapter(this, images, names, calculated));
    }

    private float roundTwoD(float d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##"); return Float.valueOf(twoDForm.format(d));
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

    private Bitmap getBitmapFromString(String jsonString) {
        /*
        * This Function converts the String back to Bitmap
        * */
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("SYNCING");
        sync();
    }
}
