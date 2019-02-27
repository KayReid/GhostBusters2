package edu.stlawu.ghostbusters;

import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ScoreboardActivity extends AppCompatActivity {

    private TextView highscore_1;
    private TextView highscore_2;
    private TextView highscore_3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        highscore_1 = findViewById(R.id.highscore_1);
        highscore_2 = findViewById(R.id.highscore_2);
        highscore_3 = findViewById(R.id.highscore_3);

        Intent intent = getIntent();
        String score = intent.getStringExtra("timescore");

        System.out.println(highscore_1.toString());

        // Check for an opening in the scoreboard
        if(highscore_1.getText().toString().equals("00:00")){
            highscore_1.setText(score);
        } else {
            if (highscore_2.getText().toString().equals("00:00")){
                highscore_2.setText(score);
            } else {
                if (highscore_3.getText().toString().equals("00:00")){
                    highscore_3.setText(score);
                }
            }
        }

    }
}
