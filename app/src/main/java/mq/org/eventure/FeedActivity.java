package mq.org.eventure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Michael on 2/18/16.
 */
public class FeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
    }

    private void openMap(){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.move_in_right, R.anim.move_out_right);
        finish();
    }
}
