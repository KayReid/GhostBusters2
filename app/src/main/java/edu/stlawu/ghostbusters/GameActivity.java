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

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import static edu.stlawu.ghostbusters.R.drawable.ghost;

public class GameActivity extends AppCompatActivity implements Observer, MainFragment.OnFragmentInteractionListener{

    private final static String LOGTAG = MainActivity.class.getSimpleName();
    private View screen;
    private View screenGhost;
    private CameraViewDisplay camera_view;
    private LocationHandler handler = null;
    private boolean withinRange = false;
    private ImageButton flashlightButton;
    private Boolean flashLightStatus = false;
    private GhostManager gm = new GhostManager(500);
    private CountDownTimer countdown;
    private TextView timer = null;
    private TextView ghostGoal = null;
    private int goalNumber;
    private int chosenTime;
    private int ghostsCaptured;
    private int ghostWithinRange;
    private int distance;

    // initialize ghost sound
    private int black = 0;
    private SoundPool soundPool = null;
    public AudioAttributes aa = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // sound
        this.aa = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
        this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(aa).build();
        this.black = this.soundPool.load(this, R.raw.black,1); 


        // check permissions
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        // TODO: Create Timer Options: 5, 10, or 20 minutes
        CreateTimerOptions();

        //ghost screen
        // TODO: if we cannot get both screen to work, we will simplify it to just have a ghost pop up
        // TODO: CANNOT DISPLAY THE IMAGE AT ALL, TRY REARRANGING THE XML, TRY REMOVING THE CAMERA
        screenGhost = findViewById(R.id.screenGhost);
        //screenGhost.setBackground(getDrawable(ghost));
        // screenGhost.setAlpha(0);

        // set screen tint
        screen = findViewById(R.id.screen);
        screen.setBackgroundColor(Color.RED);
        screen.setAlpha(0);

        flashlightButton = findViewById(R.id.flashlight);
        timer = findViewById(R.id.time_count);
        ghostGoal = findViewById(R.id.ghost_count);

        // initialize the location feature
        if (handler == null) {
            this.handler = new LocationHandler(this);
            this.handler.addObserver(this);
        }

        // click the flashlight button and get flash to work
        flashlightButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO: in faux flash, YOU ARE CURRENTLY USING THE GHOST VIEW
                // TODO: if flashlight has been on for more than 3 seconds, turn it off
                if (flashLightStatus) {
                    screen.setBackgroundColor(Color.RED);
                    screen.setAlpha(0);
                    flashLightStatus = false;
                    Log.i(LOGTAG, "Flash off.");
                } else {
                    screen.setBackgroundColor(Color.WHITE);
                    screen.setAlpha(0.5f);
                    flashLightStatus = true;
                    if (withinRange && distance < 20) {
                        capture(ghostWithinRange);
                    }
                    Log.i(LOGTAG, "Flash on.");
                }
            }
        });
        // camera view
        // camera_view = new CameraViewDisplay();
        // camera_view.run();
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


    AlertDialog chooseTimerDialog;
    public void CreateTimerOptions() {
        CharSequence[] values = {" 10 Minutes "," 15 Minutes "," 20 Minutes"};

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle("Choose Timer");
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                switch(item) {
                    case 0:
                        // set the ghost goal and timer (10 mins)
                        goalNumber = 5;
                        chosenTime = 600000;
                        break;

                    case 1:
                        // 15 mins, goal ghosts = 10
                        goalNumber = 10;
                        chosenTime = 900000;
                        break;

                    case 2:
                        // 20 mins, goal ghosts = 15
                        goalNumber = 15;
                        chosenTime = 1200000;
                        break;
                }

                // setup timer and change the text
                ghostGoal.setText(String.valueOf(goalNumber));
                ghostsCaptured = 0;

                countdown = new CountDownTimer(chosenTime, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        int seconds = (int) (millisUntilFinished/1000);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;
                        timer.setText("" + String.format("%02d:%02d", minutes, seconds));
                    }

                    @Override
                    public void onFinish() {
                        GameOver(ghostsCaptured, goalNumber);
                    }}.start();

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
            String loseMessage = "Number of Ghosts You Captured: " + ghostsCaptured + "\n\nGhost Goal: " + ghostgoal + "\n\nYOU LOSE";
            builder.setMessage(loseMessage).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
        }

        if(ghostsCaptured >= ghostgoal) {
            String winMessage = "Number of Ghosts You Captured: " + ghostsCaptured + "\n\nGhost Goal: " + ghostgoal + "\n\nYOU WIN";
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

    @Override
    public void update(Observable observable, Object o) {

        if (observable instanceof LocationHandler) {
            Location l = (Location) o;
            compareGhostLocations(l);
        }
    }

    // check ghost locations against user
    public void compareGhostLocations(Location userLocation){
        // comparing player's location with the ghost locations
        withinRange = false;

        for (int i = 0; i < gm.getGhostList().size(); i++) {
            Location ghostLocation = gm.getGhostList().get(i);

            // gets distance away from a ghost
            distance = (int) userLocation.distanceTo(ghostLocation);

            // if the ghost is within 45 meters, the screen tints and the method returns
            if (distance < 45){
                tint(distance);
                withinRange = true;
                ghostWithinRange = i;
                return;
            }
        }
        // unreachable in range of a ghost
        screen.setAlpha(0);
    }

    // puts ghost on screen and adds sound
    public void ghostAnimate() {
        screenGhost.setAlpha(1);
        soundPool.play(black, 1f, 1f, 1, 0, 1f);
    }

    // tints the screen relative to distance away from a ghost
    // darker red when it is closer
    public void tint(int distance){
        if (!flashLightStatus) {
            // screen.getBackground().setAlpha(120 - distance);
            screen.setAlpha(1);
            Log.i(LOGTAG, "WHERE IS TINT?");
            if(distance < 20) {
                ghostAnimate();
                Log.i(LOGTAG, "WHERE R U?");
            }else{
                // screenGhost.setAlpha(0);
                soundPool.autoPause();
            }
        }
    }

    // ghost interaction
    // takes index of ghost, removes it, and adds a new one
    public void capture(int capturedGhost){
        gm.getGhostList().remove(capturedGhost);
        gm.addGhost();
        //TODO: add capture sound effects and animation
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}