package com.ibm.bluelist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class UploadActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 1;

    Bitmap photo;
    Bitmap icon;

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
                EditText itemToAdd = (EditText) findViewById(R.id.todo);
                final String name = itemToAdd.getText().toString();
                // If text was added, continue with normal operations
                if (!name.isEmpty() && photo != null) {
                    ((TabActivity) getParent()).upload(name, getStringFromBitmap(photo), getStringFromBitmap(icon));
                    clear();
                    ((TabActivity) getParent()).goToMap();
                } else {
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Caption Empty")
                            .setMessage("Please enter a caption for your photo!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            }
        });
    }

    public void clear() {
        ((ImageView) findViewById(R.id.image)).setImageBitmap(null);
        ((EditText) findViewById(R.id.todo)).setText("");
        photo = null;
    }

    public void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ImageView photoView = (ImageView) findViewById(R.id.image);
                File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
                photo = decodeSampledBitmapFromFile(file.getAbsolutePath(), 1000, 700);
                photoView.setImageBitmap(photo);
                icon = getResizedBitmap(photo, photo.getWidth() / 4, photo.getHeight() / 4);
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("CANCELED NO PHOTOOOO");
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight)
        {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth)
        {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
     /*
     * This functions converts Bitmap picture to a string which can be
     * JSONified.
     * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }
}
