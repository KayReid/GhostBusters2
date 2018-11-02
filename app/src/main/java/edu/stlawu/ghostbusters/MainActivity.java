package edu.stlawu.ghostbusters;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.stlawu.ghostbusters.MainFragment.OnFragmentInteractionListener;

public class MainActivity
        extends AppCompatActivity
        implements OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}