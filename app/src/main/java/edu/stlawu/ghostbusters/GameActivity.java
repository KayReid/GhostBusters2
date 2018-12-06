package edu.stlawu.ghostbusters;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.media.SoundPool;

import java.util.Observable;
import java.util.Observer;

import static edu.stlawu.ghostbusters.R.drawable.ghost;

public class GameActivity extends AppCompatActivity implements Observer, MainFragment.OnFragmentInteractionListener{

    private final static int PERMISSION_REQUEST_CODE = 999;
    private final static String LOGTAG = MainActivity.class.getSimpleName();
    private View screen;
    private View screenGhost;
    private CameraViewDisplay camera_view;
    private Observable location;
    private LocationHandler handler = null;
    private boolean permissions_granted;
    private boolean withinRange = false;
    private ImageButton flashlightButton;
    private Boolean flashLightStatus = false;
    private Boolean fauxFlashLightStatus = false;
    private GhostManager gm = new GhostManager(500);
    private CountDownTimer countdown;
    private TextView timer = null;
    private TextView ghostGoal = null;
    private int goalNumber_opt1;
    private int goalNumber_opt2;
    private int goalNumber_opt3;
    private int goalNumber_test;
    private int ghostsCaptured;
    private int ghostWithinRange;
    private int distance;

    private int black = 0;
    private SoundPool soundPool = null;
    public AudioAttributes aa = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        this.aa = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
        this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(aa).build();
        this.black = this.soundPool.load(this, R.raw.black,1); 


        // check permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GameActivity.this, new String[] {Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }

        
        //ghost screen
        // TODO: fix this shit
        screenGhost = findViewById(R.id.screenGhost);
        screenGhost.setBackground(getDrawable(ghost));
        screenGhost.getBackground().setAlpha(0);

        // set screen tint
        screen = findViewById(R.id.screen);
        screen.setBackgroundColor(Color.RED);
        screen.getBackground().setAlpha(0);

        flashlightButton = findViewById(R.id.flashlight);
        timer = findViewById(R.id.time_count);
        ghostGoal = findViewById(R.id.ghost_count);

        // TODO: Create Timer Options: 5, 10, or 20 minutes
        CreateTimerOptions();

        if (handler == null) {
            this.handler = new LocationHandler(this);
            this.handler.addObserver(this);
        }

        final boolean hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        // click the flashlight button and get either the flash or a faux flash to work
        flashlightButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (hasCameraFlash) {
                    if (flashLightStatus) {
                        flashLightOff();
                    } else {
                        flashLightOn();
                        if (withinRange) {
                            capture(ghostWithinRange);
                        }
                    }
                } else {
                    Toast.makeText(GameActivity.this, "No flashlight available on your device", Toast.LENGTH_SHORT).show();
                    // TODO: create faux flash, YOU ARE CURRENTLY USING THE GHOST VIEW
                    if (fauxFlashLightStatus) {
                        screen.setBackgroundColor(Color.RED);
                        screen.getBackground().setAlpha(0);
                        fauxFlashLightStatus = false;
                    } else {
                        screen.setBackgroundColor(Color.WHITE);
                        screen.getBackground().setAlpha(180);
                        fauxFlashLightStatus = true;
                        if (withinRange && distance < 20) {
                            capture(ghostWithinRange);
                        }
                    }
                }
            }
        });

        // TODO: if flashlight has been on for more than 3 seconds, turn it off

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

        // TODO: put this on a new thread
        @Override
        public void run() {
            CameraView camera = new CameraView(GameActivity.this);
            preview.addView(camera);
        }
    }

    // TODO: Create Popup Window for Timer Options
    AlertDialog chooseTimerDialog;
    //CharSequence[] values = {" 10 Minutes "," 15 Minutes "," 20 Minutes"};
    CharSequence[] values = {" 10 Minutes "," 15 Minutes "," 20 Minutes", " Test: 1 Minute"};
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
                        ghostGoal.setText(String.valueOf(goalNumber_opt1));
                        ghostsCaptured = 0;

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
                                GameOver(ghostsCaptured, goalNumber_opt1);
                            }}.start();
                        break;

                    case 1:
                        // 15 MINUTE, GOAL GHOSTS = 10

                        // Set the Goal Number
                        goalNumber_opt2 = 10;
                        ghostGoal.setText(String.valueOf(goalNumber_opt2));
                        ghostsCaptured = 0;

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
                                GameOver(ghostsCaptured, goalNumber_opt2);
                            }
                        }.start();
                        break;

                    case 2:
                        // 20 MINUTE, GOAL GHOSTS = 15

                        // Set the Goal Number
                        goalNumber_opt3 = 15;
                        ghostGoal.setText(String.valueOf(goalNumber_opt3));
                        ghostsCaptured = 0;

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
                                GameOver(ghostsCaptured, goalNumber_opt3);
                            }
                        }.start();
                        break;

                    case 3:
                        // TEST: 1 MINUTE, GOAL GHOSTS = 1

                        // Set the Goal Number
                        goalNumber_test = 1;
                        ghostGoal.setText(String.valueOf(goalNumber_test));
                        ghostsCaptured = 0;

                        // Set the 20-Min Timer
                        countdown = new CountDownTimer(60000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                int seconds = (int) (millisUntilFinished/1000);
                                int minutes = seconds / 60;
                                seconds = seconds % 60;
                                timer.setText("" + String.format("%02d:%02d", minutes, seconds));
                            }

                            @Override
                            public void onFinish() {
                                GameOver(ghostsCaptured, goalNumber_test);
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

    // Create a Game Over alert box
    public void GameOver(int ghostsCaptured, int ghostgoal) {
        AlertDialog gameOverDialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle("GAME OVER")
                //.setMessage("Number of Ghosts You Captured: " + ghostsCaptured)
                .setCancelable(false);


        if(ghostsCaptured < ghostgoal) {
            String loseMessage = "Number of Ghosts You Captured: " + ghostsCaptured +
                    "\n\nGhost Goal: " + ghostgoal +
                    "\n\nYOU LOSE";
            builder.setMessage(loseMessage).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
        }

        if(ghostsCaptured >= ghostgoal) {
            String winMessage = "Number of Ghosts You Captured: " + ghostsCaptured +
                    "\n\nGhost Goal: " + ghostgoal +
                    "\n\nYOU WIN";
            builder.setMessage(winMessage).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
        }

        gameOverDialog = builder.create();
        gameOverDialog.show();
    }

    // TODO: distinguish between camera and location permissions (use a switch)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // we have only asked for FINE LOCATION
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.permissions_granted = true;
                Log.i(LOGTAG, "Fine location and camera permission granted.");
            } else {
                this.permissions_granted = false;
                Log.i(LOGTAG, "Fine location and camera permission not granted.");
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
            compareGhostLocations(l);
        }
    }

    // check ghost locations
    public boolean compareGhostLocations(Location userLocation){
        // comparing player's location with the ghost locations
        for (int i = 0; i < gm.getGhostList().size(); i++) {
            Location ghostLocation = gm.getGhostList().get(i);

            // gets distance away from a ghost
            distance = (int) userLocation.distanceTo(ghostLocation);

            // if the ghost is within 45 meters, the screen tints and the method returns
            if (distance < 45){
                tint(distance);
                withinRange = true;
                ghostWithinRange = i;
                return true;
            }
        }
        // It should be unreachable in range of a ghost
        screen.getBackground().setAlpha(0);
        withinRange = false;
        return false;
    }


    public void ghostAnimate() {
        screenGhost.getBackground().setAlpha(255);
        soundPool.play(black, 1f, 1f, 1, 0, 1f);
    }

    // tints the screen relative to distance away from a ghost
    // darker red when it is closer
    public void tint(int distance){
        if (!fauxFlashLightStatus) {
            screen.getBackground().setAlpha(120 - distance);
            Log.i(LOGTAG, "WHERE IS TINT?");
            //TODO: add ghost sound effects, ghost animation
            if(distance < 20) {
                ghostAnimate();
                Log.i(LOGTAG, "WHERE R U?");
            }else{
                screenGhost.getBackground().setAlpha(0);
            }
        }
    }

    // ghost interaction
    // takes index of ghost, removes it, and adds a new one
    public void capture(int capturedGhost){
        gm.getGhostList().remove(capturedGhost);
        gm.addGhost();
        //TODO: add capture sound effects and animation

        // then update the screen
        // TODO: call update here?
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}