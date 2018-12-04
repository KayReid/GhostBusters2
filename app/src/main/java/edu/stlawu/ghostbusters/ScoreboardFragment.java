package edu.stlawu.ghostbusters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScoreboardFragment extends Fragment, AppCompatActivity {

    public static final String PREF_NAME = "GhostBusters";
    public static final String NEW_CLICKED = "NEWCLICKED";

    private TextView highscore1;
    private TextView highscore2;
    private TextView highscore3;

    //private MainFragment.OnFragmentInteractionListener mListener;

    public ScoreboardFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        highscore1 = findViewById(R.id.highscore_1);
        highscore2 = findViewById(R.id.highscore_2);
        highscore3 = findViewById(R.id.highscore_3);

        Intent intent = getIntent();
        String time = intent.getStringExtra("final time");
        addHighScore(time);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_scoreboard, container, false);


        View playAgainButton = rootView.findViewById(R.id.playAgain_button);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor pref_ed =
                getActivity().getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE).edit();
                pref_ed.putBoolean(NEW_CLICKED, true).apply();

                Intent intent = new Intent(getActivity(), GameActivity.class);
                getActivity().startActivity(intent);
            }
        });

        View exitButton = rootView.findViewById(R.id.exit_button);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor pref_ed =
                        getActivity().getSharedPreferences(
                                PREF_NAME, Context.MODE_PRIVATE).edit();
                pref_ed.putBoolean(NEW_CLICKED, true).apply();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return rootView;

    }


    public void addHighScore(String timePlayed) {

        // Case 1: If there are vacancies in the 1st, 2nd, or 3rd rankings,
        // add time score here
        if (highscore1.equals("00:00:00")) {
            highscore1.setText(timePlayed);
        } else {
            if (highscore2.equals("00:00:00")) {
                highscore2.setText(timePlayed);
            } else {
                if (highscore3.equals("00:00:00")) {
                    highscore3.setText(timePlayed);
                }
            }
        }

        // Case 2: If spots are already filled, compare time score
        // with current scores








    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
