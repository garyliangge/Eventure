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

    public EventArrayAdapter(Context context, LinkedList<Bitmap> images,
                             LinkedList<String> names, LinkedList<Float> distances) {
        super(context, R.layout.activity_feed, names);
        this.context = context;
        this.images = images;
        this.names = names;
        this.distances = distances;
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
        textView.setText(names.get(position));

        distanceView.setText(distances.get(position) + " m");

        imageView.setImageBitmap(images.get(position));

        return rowView;
    }
}
