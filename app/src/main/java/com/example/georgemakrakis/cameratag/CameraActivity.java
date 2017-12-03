package com.example.georgemakrakis.cameratag;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity
{
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private Bitmap mutableBitmap;
    private String mCurrentPhotoPath;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mImageView = (ImageView) findViewById(R.id.imageview1);

        //Through an exception, in this way the VM ignores the file URI exposure.
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File photoFile = null;
            try
            {
                photoFile = createImageFile("");
            }
            catch (IOException ex)
            {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private File createImageFile(String n) throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" + n;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private String getAddress()
    {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return TODO;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
        if(location!= null)
        {
            double lat = location.getLatitude();
            double lng = location.getLongitude();


            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try
            {
                addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                return city + " " + country + " " + knownName;
            }
            catch (IOException e)
            {
                e.printStackTrace();

            }
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            try
            {
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));

                //create bitmap with a canvas
                Bitmap newPhoto = Bitmap.createBitmap(mImageBitmap);
                mutableBitmap = newPhoto.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitmap);
                canvas.drawBitmap(mutableBitmap,0,0,null);

                //draw the text
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setTextSize(200);
                paint.setTextAlign(Paint.Align.LEFT);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                //Getting the height of the device's screen
                int mHeight= this.getResources().getDisplayMetrics().heightPixels;

                //Drawing text to bottom left corner
                canvas.drawText(timeStamp.toString()+"\n"+getAddress(),0, (mHeight*2)+paint.getTextSize(),paint);

                File image= createImageFile("new");
                FileOutputStream out=new FileOutputStream(image);
                mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                mImageView.setImageBitmap(mutableBitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
