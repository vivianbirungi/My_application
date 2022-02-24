package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

public class Camera extends AppCompatActivity {
    private ImageButton camerabtn;
    private ImageButton videobtn;
    private ImageButton btnsend;
    private ImageView imageview;
    private TextInputLayout textInputLayout;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;
    private LocationManager mLocationManagerGPS;
    private LocationListener mLocationListenerGPS;
    private String txtViewLatGPS ="0.0";
    private String txtViewLongGPS ="0.0";
    private String txtViewAltGPS ="0.0";
    private String txtViewLatNetwork;
    private String txtViewLongNetwork;
    private String  txtViewAltNetwork;
    private Uri imageUr;
    private TextView locations;
    private LocationManager mLocationManagerNetwork;
    private LocationListener mLocationListenerNetwork;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Images");
    private StorageReference reference = FirebaseStorage.getInstance().getReference();
    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;
    private Uri imageUri ;
     private byte[] imageUpload;

    private ProgressBar progressBar;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        camerabtn = findViewById(R.id.camerabtn);
        videobtn = findViewById(R.id.videobtn);
        btnsend = findViewById(R.id.btnsend);
        imageview = findViewById(R.id.imageview);
        locations = findViewById(R.id.location);
        progressBar = findViewById(R.id.progressBar);
        textInputLayout = findViewById(R.id.textInputLayout);

        findViewById(R.id.camerabtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filename = "photo";
                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try {
                    File imageFile = File.createTempFile(filename,".jpg", storageDirectory);
                    currentPhotoPath = imageFile.getAbsolutePath();

                     imageUri = FileProvider.getUriForFile(Camera.this, "com.example.myapplication.fileprovider", imageFile);
                    Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent,1);
                    getPositionGPS();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });




    btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPhotoPath != null) {
                    uploadToFirebase(imageUpload);
                } else {
                        Toast.makeText(Camera.this,"upload is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(Camera.this, "com.example.myapplication.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("output", String.valueOf((requestCode)));
        if (requestCode == 1 && resultCode == RESULT_OK) {

//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//
//            imageview.setImageBitmap(photo);
//            camerabtn.setVisibility(View.GONE);
//            videobtn.setVisibility(View.GONE);
//            textInputLayout.setVisibility(View.VISIBLE);
//            btnsend.setVisibility(View.VISIBLE);
//// set url
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
////             imageUri = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//            imageUri = baos.toByteArray();
//            try {
//
//                Bitmap thumbnail = MediaStore.Images.Media.getBitmap(
//                        getContentResolver(), imageUr);
//                imageview.setImageBitmap(thumbnail);
//               imageurl = getRealPathFromURI(imageUri);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);


            ImageView imageView = findViewById(R.id.imageview);
            imageView.setImageBitmap(bitmap);
            camerabtn.setVisibility(View.GONE);
            videobtn.setVisibility(View.GONE);
            textInputLayout.setVisibility(View.VISIBLE);
            btnsend.setVisibility(View.VISIBLE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, baos);
            imageUpload = baos.toByteArray();
        }
    }

    private void getPositionGPS() {
        mLocationManagerGPS = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListenerGPS = new LocationListener() {
            public void onLocationChanged(Location location) {
                txtViewLatGPS= (Double.toString(location.getLatitude()));
                txtViewLongGPS= (Double.toString(location.getLongitude()));
                txtViewAltGPS = (Double.toString(location.getAltitude()));
                locations.setText("lat" + Double.toString(location.getLongitude()) +  "long" +Double.toString(location.getLongitude()));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
                showAlert(R.string.GPS_disabled);
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            } else {

                mLocationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 0, mLocationListenerGPS);
            }
        }
    }

    private void showAlert(int messageId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageId).setCancelable(false).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void requestLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.GPS_permissions).setCancelable(false).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ActivityCompat.requestPermissions(Camera.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }).show();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.GPS_permissions).setCancelable(false).setPositiveButton(R.string.btn_watch_permissions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
                }
            }).setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        }
    }
    private void uploadToFirebase(byte[] uri){
        String uniqueID = UUID.randomUUID().toString();
        StorageReference fileRef = reference.child(System.currentTimeMillis() + "." + uniqueID);
        Long datetime = System.currentTimeMillis();
        Log.d("lat and lon",txtViewLatGPS + txtViewLongGPS );

        Timestamp timestamp = new Timestamp(datetime);
        fileRef.putBytes(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
            Model model = new Model(uri1.toString(), txtViewLatGPS,txtViewLongGPS,timestamp );
            String modelId = root.push().getKey();
            root.child(modelId).setValue(model);
            Toast.makeText(Camera.this, "Uploaaded Sucessfully", Toast.LENGTH_SHORT).show();
        })).addOnProgressListener(snapshot -> progressBar.setVisibility(View.VISIBLE)).addOnFailureListener(e -> Toast.makeText(Camera.this, "uploading Failed !!", Toast.LENGTH_SHORT).show());
    }
    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));

    }

}