package mq.org.eventure;
import mq.org.eventure.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EventArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final String[] values;


    public EventArrayAdapter(Context context, String[] values) {
        super(context, R.layout.activity_feed, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_feed, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.caption);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.photo);
        textView.setText(values[position]);

        // Change icon based on name
        String s = values[position];

        if (s.equals("Campanile")) {
            imageView.setImageResource(R.drawable.campanile);
        } else if (s.equals("Sproul Protest")) {
            imageView.setImageResource(R.drawable.protest);
        } else if (s.equals("Sather Gate")) {
            imageView.setImageResource(R.drawable.sather);
        }

        return rowView;
    }
}
