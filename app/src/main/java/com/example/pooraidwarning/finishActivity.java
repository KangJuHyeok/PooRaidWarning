package com.example.pooraidwarning;

import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;

public class finishActivity extends AppCompatActivity {
    int recentStar;
    String recentReview;
    MediaPlayer mediaPlayer;
    EditText editText;

    RatingBar ratingBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        ratingBar=findViewById(R.id.ratingBar);

        editText=findViewById(R.id.editTextComment);

        Button buttonSubmit=findViewById(R.id.buttonSubmit);
        Button buttonPause=findViewById(R.id.buttonPause);

        Intent receivedIntent = getIntent();
        String title=receivedIntent.getStringExtra("title");

        SQLiteHelper sqLiteHelper = new SQLiteHelper(this);

        mediaPlayer=MediaPlayer.create(this,R.raw.music);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // 노래가 종료되면 다시 시작
                mediaPlayer.start();
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    buttonPause.setText("노래 재생");
                }
                else //(mediaPlayer.isPlaying()!=true){
                {
                    mediaPlayer.start();
                    buttonPause.setText("노래 중지");
                }
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recentStar=(int)Math.round(ratingBar.getRating());
                recentReview= editText.getText().toString();
                sqLiteHelper.setReview(title,recentStar,recentReview,0,0);
                finish();
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
