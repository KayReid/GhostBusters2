package edu.stlawu.ghostbusters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScoreboardFragment extends Fragment {

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

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_scoreboard, container, false);


        View playAgainButton = rootView.findViewById(R.id.playAgain_button);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SharedPreferences.Editor pref_ed =
                //getActivity().getSharedPreferences(
                //PREF_NAME, Context.MODE_PRIVATE).edit();
                //pref_ed.putBoolean(NEW_CLICKED, true).apply();

                Intent intent = new Intent(getActivity(), GameActivity.class);
                getActivity().startActivity(intent);
            }
        });

        View exitButton = rootView.findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                SharedPreferences.Editor pref_ed =
                getActivity().getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE).edit();
                pref_ed.putBoolean(NEW_CLICKED, true).apply();*/

                Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return rootView;

    }
}
