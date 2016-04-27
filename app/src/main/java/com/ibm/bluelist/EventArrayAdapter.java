package com.ibm.bluelist;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class EventArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final LinkedList<Bitmap> images;
    private final LinkedList<String> names;
    private final LinkedList<Float> distances;
    private final LinkedList<Long> times;

    public EventArrayAdapter(Context context, LinkedList<Bitmap> images,
                             LinkedList<String> names, LinkedList<Float> distances, LinkedList<Long> times) {
        super(context, R.layout.activity_feed, names);
        this.context = context;
        this.images = images;
        this.names = names;
        this.distances = distances;
        this.times = times;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position  >= images.size()) {
            return null;
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_feed, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.caption);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.photo);
        TextView distanceView = (TextView) rowView.findViewById(R.id.distance);
        TextView timeView = (TextView) rowView.findViewById(R.id.time);

        timeView.setText(timeToString(times.get(position)));

        textView.setText(names.get(position));

        distanceView.setText(distances.get(position) + " meters");

        imageView.setImageBitmap(images.get(position));

        return rowView;
    }

    private String timeToString(long time) {
        int seconds = (int) time / 1000;
        int minutes = seconds / 60;
        if (minutes >= 60) {
            int hours = minutes / 60;
            if (hours >= 24) {
                int days = hours / 24;
                if (days >= 7) {
                    int weeks = days / 7;
                    if (weeks >= 4) {
                        int months = weeks / 4;
                        if (months >= 12) {
                            int years = months / 12;
                            return years + "y";
                        } else {
                            return months + "m";
                        }
                    } else {
                        return weeks + "w";
                    }
                } else {
                    return days + "d";
                }
            } else {
                return hours + "h";
            }
        } else if (minutes > 0){
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }
}
