package edu.stlawu.ghostbusters;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

public class GameActivity extends AppCompatActivity implements Observer, MainFragment.OnFragmentInteractionListener{

    private final static int PERMISSION_REQUEST_CODE = 999;
    private final static String LOGTAG = MainActivity.class.getSimpleName();
    private View screen;
    private CameraViewDisplay camera_view;
    private Observable location;
    private LocationHandler handler = null;
    private boolean permissions_granted;
    private ImageButton flashlightButton;
    private Boolean flashLightStatus = false;
    private GhostManager gm = new GhostManager(100);
    private CountDownTimer countdown;
    private TextView timer = null;
    private TextView ghostgoal = null;
    private int goalNumber_opt1;
    private int goalNumber_opt2;
    private int goalNumber_opt3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // screen tint
        screen = findViewById(R.id.screen);
        screen.setBackgroundColor(Color.RED);
        screen.getBackground().setAlpha(0);

        flashlightButton = findViewById(R.id.flashlight);
        timer = findViewById(R.id.time_count);
        ghostgoal = findViewById(R.id.ghost_count);
        
        // TODO: Create Timer Options: 5, 10, or 20 minutes
        CreateTimerOptions();

        if (handler == null) {
            this.handler = new LocationHandler(this);
            this.handler.addObserver(this);
        }

        // check permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GameActivity.this, new String[] {Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }

        final boolean hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        flashlightButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (hasCameraFlash) {
                    if (flashLightStatus) {
                        flashLightOff();
                    } else {
                        flashLightOn();
                    }
                } else {
                    Toast.makeText(GameActivity.this, "No flash available on your device", Toast.LENGTH_SHORT).show();
                    // TODO: create faux flash, YOU ARE CURRENTLY USING THE GHOST VIEW
                    // In theory, we could create some kind of view that is white or yellow and only make it visible when the flashlight is pressed?
                    // Make it visible for a few seconds, and add some kind of sound effect
                    if (flashLightStatus) {
                        screen.setBackgroundColor(Color.RED);
                        screen.getBackground().setAlpha(0);
                        flashLightStatus = false;
                    } else {
                        screen.setBackgroundColor(Color.WHITE);
                        screen.getBackground().setAlpha(180);
                        flashLightStatus = true;
                    }
                }
            }
        });

        // camera view
        camera_view = new CameraViewDisplay();
        camera_view.run();
    }

    public boolean isPermissions_granted() {
        return permissions_granted;
    }

    // displays a cameraView on the view
    class CameraViewDisplay implements Runnable{

        final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        @Override
        public void run() {
            CameraView camera = new CameraView(GameActivity.this);
            preview.addView(camera);
        }
    }

    // TODO: Create Popup Window for Timer Options
    AlertDialog chooseTimerDialog;
    CharSequence[] values = {" 10 Minutes "," 15 Minutes "," 20 Minutes"};
    public void CreateTimerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle("Choose Timer");
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                //final TextView timerView = findViewById(R.id.timerView);

                switch(item) {
                    case 0:
                        // 10 MINUTE, GOAL GHOSTS = 5

                        // Set the Goal Number
                        goalNumber_opt1 = 5;
                        ghostgoal.setText(String.valueOf(goalNumber_opt1));

                        // Set the 5-Min Timer
                        countdown = new CountDownTimer(600000, 1000) {

                            @Override
                            public void onTick(long millisUntilFinished) {
                                int seconds = (int) (millisUntilFinished/1000);
                                int minutes = seconds / 60;
                                seconds = seconds % 60;
                                timer.setText("" + String.format("%02d:%02d", minutes, seconds));
                            }

                            @Override
                            public void onFinish() {
                                timer.setText("GAME OVER");
                            }}.start();
                        break;

                    case 1:
                        // 15 MINUTE, GOAL GHOSTS = 10

                        // Set the Goal Number
                        goalNumber_opt2 = 10;
                        ghostgoal.setText(String.valueOf(goalNumber_opt2));

                        // Set the 15-Min Timer
                        countdown = new CountDownTimer(900000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
//                              timer.setText("" + String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished), TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                                int seconds = (int) (millisUntilFinished/1000);
                                int minutes = seconds / 60;
                                seconds = seconds % 60;
                                timer.setText("" + String.format("%02d:%02d", minutes, seconds));
                            }

                            @Override
                            public void onFinish() {
                                timer.setText("GAME OVER");
                            }
                        }.start();
                        break;

                    case 2:
                        // 20 MINUTE, GOAL GHOSTS = 15

                        // Set the Goal Number
                        goalNumber_opt3 = 15;
                        ghostgoal.setText(String.valueOf(goalNumber_opt3));

                        // Set the 20-Min Timer
                        countdown = new CountDownTimer(1200000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                int seconds = (int) (millisUntilFinished/1000);
                                int minutes = seconds / 60;
                                seconds = seconds % 60;
                                timer.setText("" + String.format("%02d:%02d", minutes, seconds));
                            }

                            @Override
                            public void onFinish() {
                                timer.setText("GAME OVER");
                            }
                        }.start();
                        break;
                }
                chooseTimerDialog.dismiss();
            }
        });
        chooseTimerDialog = builder.create();
        chooseTimerDialog.show();
    }

    // TODO: distinguish between camera and location permissions(use a switch)
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

        if (observable instanceof LocationHandler) {
            Location l = (Location) o;

            // comparing player's location with the ghost locations
            for (int i = 0; i < gm.getGhostList().size(); i++) {
                Location ghostLocation = gm.getGhostList().get(i);

                int distance = (int) findDistance(l, ghostLocation);

                // tint screen if necessary
                tint(distance);
            }
        }
    }

    public void tint(int distance){
        if (!flashLightStatus) {
            if (distance < 45) {
                screen.getBackground().setAlpha(120 - distance);
                // TODO: add ghost sound effects, ghost animation
            } else {
                screen.getBackground().setAlpha(0);
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