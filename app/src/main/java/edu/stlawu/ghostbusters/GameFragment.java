package edu.stlawu.ghostbusters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class GameFragment extends Fragment implements Observer {

    private GameFragment.OnFragmentInteractionListener mListener;
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
    private long initial_milliseconds;
    private int ghostsCaptured = 0;
    private int ghostWithinRange;
    private int distance;
    AlertDialog chooseTimerDialog;

    // initialize ghost sound
    private int black = 0;
    private SoundPool soundPool = null;
    public AudioAttributes aa = null;

    // constructor
    public GameFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // sound
        this.aa = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
        this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(aa).build();
        this.black = this.soundPool.load(getActivity(), R.raw.black, 1);

    }

    // displays a cameraView on the view
    class CameraViewDisplay{

        CameraViewDisplay(View view) {
            final SurfaceView surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);

            SurfaceHolder surfaceHolder = surfaceView.getHolder();

            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                private Camera mCamera;

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    mCamera = Camera.open();

                    try {
                        mCamera.setPreviewDisplay(holder);
                        mCamera.setDisplayOrientation(90);
                    } catch (IOException exception) {
                        mCamera.release();
                        mCamera = null;
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                           int height) {
                    mCamera.startPreview();
                }
            });
        }
    }

    // Goal and Timer method for each option
    public void goaltimer(int goalnum, int time) {
        goalNumber = goalnum;
        ghostGoal.setText(String.valueOf(goalNumber));
        chosenTime = time;
        if(ghostsCaptured == goalNumber){
            countdown.cancel();
            countdown.onFinish();
        }
    }

    public void CreateTimerOptions() {
        CharSequence[] values = {" 10 Minutes "," 15 Minutes "," 20 Minutes", "TEST: 1 Minute"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Timer");
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

            // TODO: get rid of the duplicates in the switch
            @Override
            public void onClick(DialogInterface dialog, int item) {

                switch(item) {
                    case 0:
                        // set the ghost goal and timer (10 mins)
                        goaltimer(5, 600000);
                        break;

                    case 1:
                        // 15 mins, goal ghosts = 10
                        goaltimer(10, 900000);
                        break;

                    case 2:
                        // 20 mins, goal ghosts = 15
                        goaltimer(15, 1200000);
                        break;

                    case 3:
                        // TEST: 1 min, goal ghost = 1
                        goaltimer(1, 60000);
                        break;
                }

                // setup timer and change the text
                ghostGoal.setText(String.valueOf(goalNumber));


                countdown = new CountDownTimer(chosenTime, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        initial_milliseconds = millisUntilFinished;
                        int seconds = (int) (millisUntilFinished/1000);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;
                        timer.setText("" + String.format("%02d:%02d", minutes, seconds));
                    }

                    @Override
                    public void onFinish() {
                        String finaltime = timer.getText().toString();

                        long min = Integer.parseInt(finaltime.substring(0, 2));
                        long sec = Integer.parseInt(finaltime.substring(3));

                        long t = (min * 60L) + sec;

                        long resultmillis = initial_milliseconds - TimeUnit.SECONDS.toMillis(t);

                        int newsec = (int) (resultmillis/1000);
                        int newmin = newsec / 60;
                        newsec = newsec % 60;

                        String timeplayed = String.format("%02d:%02d", newmin, newsec);

                        GameOver(ghostsCaptured,goalNumber,timeplayed);
                    }}.start();

                chooseTimerDialog.dismiss();
            }
        });
        chooseTimerDialog = builder.create();
        chooseTimerDialog.show();
    }

    // Create a Game Over alert box
    public void GameOver(int ghostsCaptured, int ghostgoal, final String timeplayed) {
        AlertDialog gameOverDialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("GAME OVER").setCancelable(false);


        if(ghostsCaptured < ghostgoal) {
            String loseMessage="Number of Ghosts You Captured:" + ghostsCaptured + "\n\nGhost Goal:" + ghostgoal + "\n\nTime:" + timeplayed + "\n\nYOU LOSE";
            builder.setMessage(loseMessage).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    launchScoreboardActivity(timeplayed);
                }
            });
        }

        if(ghostsCaptured >= ghostgoal) {
            String winMessage = "Number of Ghosts You Captured: " + ghostsCaptured + "\n\nGhost Goal: " + ghostgoal + "\n\nTime:" + timeplayed + "\n\nYOU WIN";
            builder.setMessage(winMessage).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    launchScoreboardActivity(timeplayed);
                }
            });
        }
        gameOverDialog = builder.create();
        gameOverDialog.show();
    }

    public void launchScoreboardActivity(String timeplayed) {
        Intent intent = new Intent(getActivity(), ScoreboardActivity.class);
        intent.putExtra("timescore", timeplayed);
        startActivity(intent);
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
        if(!withinRange){
            // screen.setAlpha(0);
            Log.i(LOGTAG, "NOT HERE IF TINT");
        }
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
            // screen.setAlpha(1);
            Log.i(LOGTAG, "WHERE IS TINT?");
            if(distance < 20) {
                ghostAnimate();
                Log.i(LOGTAG, "WHERE R U?");
            }else{
                screenGhost.setAlpha(0);
                soundPool.autoPause();
            }
        }
    }

    // ghost interaction
    // takes index of ghost, removes it, and adds a new one
    public void capture(int capturedGhost){
        gm.getGhostList().remove(capturedGhost);
        screenGhost.setAlpha(0);
        gm.addGhost();
        ghostsCaptured += 1;
        //TODO: add capture sound effects and animation
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        // create Timer Options: 5, 10, or 20 minutes
        CreateTimerOptions();

        //ghost screen
        screenGhost = rootView.findViewById(R.id.screenGhost);
        //screenGhost.setBackground(getDrawable(ghost));
        screenGhost.setAlpha(0);

        // set screen tint
        screen = rootView.findViewById(R.id.screen);
        // screen.setBackgroundColor(Color.RED);
        screen.setAlpha(0);

        flashlightButton = rootView.findViewById(R.id.flashlight);
        timer = rootView.findViewById(R.id.time_count);
        ghostGoal = rootView.findViewById(R.id.ghost_count);

        // initialize the location feature
        if (handler == null) {
            this.handler = new LocationHandler(getActivity());
            this.handler.addObserver(this);
        }

        // click the flashlight button and get flash to work
        flashlightButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO: if flashlight has been on for more than 3 seconds, turn it off
                if (flashLightStatus) {
                    // screen.setBackgroundColor(Color.RED);
                    screen.setAlpha(0);
                    flashLightStatus = false;
                    Log.i(LOGTAG, "Flash off.");
                } else {
                    // screen.setBackgroundColor(Color.WHITE);
                    screen.setAlpha(0.5f);
                    flashLightStatus = true;
                    if (withinRange && distance < 20) {
                        capture(ghostWithinRange);
                    }
                    Log.i(LOGTAG, "Flash on." + screen.getAlpha());
                }
            }
        });

        // TODO: fix camera stuff
        // camera view
        camera_view = new CameraViewDisplay(rootView);

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GameFragment.OnFragmentInteractionListener) {
            mListener = (GameFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
