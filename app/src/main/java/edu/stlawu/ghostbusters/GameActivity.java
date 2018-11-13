package edu.stlawu.ghostbusters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class GameActivity extends AppCompatActivity implements Observer, MainFragment.OnFragmentInteractionListener{

    private final static int PERMISSION_REQUEST_CODE = 999;
    private final static String LOGTAG = MainActivity.class.getSimpleName();
    private View screen;
    private Observable location;
    private LocationHandler handler = null;
    private boolean permissions_granted;
    private Location ghost;
    private Button flashlightButton;
    private Boolean flashLightStatus = false;
    //private ArrayList<Location> ghostList = MapsActivity.getInstance().getGhostList();
    private GhostManager gm = new GhostManager(100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        screen = findViewById(R.id.screen);
        flashlightButton = findViewById(R.id.flashlight);

        if (handler == null) {
            this.handler = new LocationHandler(this);
            this.handler.addObserver(this);
        }

        // check permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }

        // TODO: does your phone or tablet have a flashlight? What to do if it doesn't?
        final boolean hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        // check camera permissions
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GameActivity.this, new String[] {Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }


        /**
        Double labGhostLat = 44.587783573;
        Double labGhostLong = -75.160687667;

        // set location of example ghost
        ghost = new Location("labGhost");
        ghost.setLatitude(labGhostLat);
        ghost.setLongitude(labGhostLong);*/

        flashlightButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO: check if camera has flashlight
                if (hasCameraFlash) {
                    if (flashLightStatus)
                        flashLightOff();
                    else
                        flashLightOn();
                } else {
                    Toast.makeText(GameActivity.this, "No flash available on your device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // camera view
        // TODO: does doing this not help lessen the work being done on the main thread?
        // look at example on running on a different thread in stopWatch
        final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        GameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CameraView camera = new CameraView(GameActivity.this);
                preview.addView(camera);
            }
        });

        // TODO: check ghostList, but it isn't f****** working :)
        //MapsActivity map = new MapsActivity();
        //ArrayList<Location> ghosts = new ArrayList<>(map.getGhostList());

        Log.i(LOGTAG, "Size of list: " + gm.getGhostList().size());
        //Log.i(LOGTAG, "Size of ghost list: " + ghosts.size());
        Log.i(LOGTAG, "List of ghost locations:" + gm.getGhostList());
        //Log.i(LOGTAG, "Init: " + MapsActivity.getInstance().getInit());
    }

    public boolean isPermissions_granted() {
        return permissions_granted;
    }

    // TODO: distinguish between camera and location permissions(switch?)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // we have only asked for FINE LOCATION
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.permissions_granted = true;
                Log.i(LOGTAG, "Fine location permisssion granted.");
            } else {
                this.permissions_granted = false;
                Log.i(LOGTAG, "Fine location permisssion not granted.");
            }
        }
    }

    // TODO: make flashlight function
    // https://medium.com/@ssaurel/create-a-torch-flashlight-application-for-android-c0b6951855c
    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
            // imageFlashlight.setImageResource(R.drawable.btn_switch_on);
        } catch (CameraAccessException e) {
            // TODO: error message
        }
    }

    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
            // imageFlashlight.setImageResource(R.drawable.btn_switch_off);
        } catch (CameraAccessException e) {
            // TODO: error message
        }
    }

    @Override
    public void update(Observable observable, Object o) {

        for (int i = 0; i < gm.getGhostList().size(); i++){
            Location ghostLocation = gm.getGhostList().get(i);

            if (observable instanceof LocationHandler) {
                Location l = (Location) o;
                // don't even need these variables rn
                //final double lat = l.getLatitude();
                //final double lon = l.getLongitude();

                // do a gradual color change using rgb values, something like (255,255,255) when outside of zero, then (255-distance*5,0,0)
                int distance = (int) findDistance(l,ghostLocation);

                // TODO: change location of this?
                if(distance < 45){
                    screen.setBackgroundColor(Color.rgb(255,255-distance*5,255-distance*5));
                }else{
                    screen.setBackgroundColor(Color.WHITE);
                }
            }
        }


    }

    // returns distance in meters
    public double findDistance(Location location1, Location location2){
        return location1.distanceTo(location2);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}