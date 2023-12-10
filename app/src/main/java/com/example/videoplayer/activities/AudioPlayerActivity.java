package com.example.videoplayer.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoplayer.R;
import com.example.videoplayer.models.MediaFiles;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AudioPlayerActivity extends AppCompatActivity {
    ArrayList<MediaFiles> mAudioFiles = new ArrayList<>();
    private ExoPlayer player;
    private boolean isResume;
    ConcatenatingMediaSource concatenatingMediaSource;
    String audioFile,audioName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        audioFile = getIntent().getStringExtra("audioFile");
        audioName = getIntent().getStringExtra("audioName");
        Uri uri = Uri.parse(audioFile);
        ImageButton btn = findViewById(R.id.imageButton);
        TextView name=findViewById(R.id.audio_name);
        name.setText(audioName);


//        StyledPlayerView playerView = findViewById(R.id.player_view);
//        player = new ExoPlayer.Builder(AudioPlayerActivity.this).build();
//        playerView.setPlayer(player);
//        Uri uri = Uri.parse(audioFile);
//        MediaItem mediaItem = MediaItem.fromUri(uri);
//        player.setMediaItem(mediaItem);
//        player.prepare();
//        player.play();

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(String.valueOf(uri));
            mediaPlayer.prepare();
            mediaPlayer.start();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isResume)
                {
                    isResume=true;
                    btn.setImageDrawable(getResources().getDrawable(R.drawable.baseline_play_arrow_24er));
                    mediaPlayer.pause();
                }
                else {
                    isResume=false;
                    btn.setImageDrawable(getResources().getDrawable(R.drawable.baseline_pause_2467));
                    mediaPlayer.start();
                }

            }
        });
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
//                finish();
            }
        }, 2000);


    }



}