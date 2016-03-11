package mq.org.eventure;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class UploadActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 1;
    private int stroke = 10;
    Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        openCamera();

        ((Button)findViewById(R.id.reupload)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
                openCamera();
            }
        });

        ((Button)findViewById(R.id.upload)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TabActivity) getParent()).passMarker(getCroppedBitmap(photo));
                clear();
            }
        });
    }

    public void clear() {
        ((ImageView) findViewById(R.id.image)).setImageBitmap(null);
        photo = null;
    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(stroke, stroke, bitmap.getWidth()-stroke, bitmap.getHeight()-stroke);

        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                photo = (Bitmap)data.getExtras().get("data");
                ((ImageView)findViewById(R.id.image)).setImageBitmap(photo);
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("CANCELED NO PHOTOOOO");
            }
        }
    }
}
