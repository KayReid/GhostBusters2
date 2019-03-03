package edu.stlawu.ghostbusters;

import android.net.Uri;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener, GameFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}