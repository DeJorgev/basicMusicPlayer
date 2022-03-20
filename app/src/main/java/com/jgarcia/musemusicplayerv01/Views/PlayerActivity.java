package com.jgarcia.musemusicplayerv01.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.jgarcia.musemusicplayerv01.R;

public class PlayerActivity extends AppCompatActivity {

    private Button buttonSearchLyrics;

    private String currentSongSecond;
    private TextView textViewSongName;
    private ExoPlayer exoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        String songName = getIntent().getStringExtra("songName");

        textViewSongName = findViewById(R.id.TextViewSongName);
        textViewSongName.setText(songName);

        exoPlayer = new ExoPlayer.Builder(this).build();
        exoPlayer.setMediaItem(createMediaItem());

        if (savedInstanceState != null)
            exoPlayer.seekTo(savedInstanceState.getLong(currentSongSecond));

        exoPlayer.prepare();
        exoPlayer.play();

        buttonSearchLyrics = findViewById(R.id.buttonBuscarLetras);
        buttonSearchLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + songName + " lyrics"));
                startActivity(browserIntent);
            }


        });

    }

    private MediaItem createMediaItem() {
        return new MediaItem.Builder()
                .setUri(Uri.parse(getIntent().getStringExtra("songUri")))
                .build();
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("lastSongName", textViewSongName.getText());

        setResult(0, data);
        finish();
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(currentSongSecond, exoPlayer.getCurrentPosition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.stop();
    }
}