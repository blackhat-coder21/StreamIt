package com.example.videoplayer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.videoplayer.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

public class stream_home extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stream_home);
        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_nav);
        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.stream);
        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.audio:
                        startActivity(new Intent(getApplicationContext(), audio_home.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.stream:
                        return true;
                    case R.id.video:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        ImageView streamBtn=findViewById(R.id.btnStream);
        TextInputEditText input = findViewById(R.id.input);
        streamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = input.getText().toString();

                Intent intent = new Intent(stream_home.this, stream.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
//                finish();
            }
        }, 2000);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_folders:
                finish();
                startActivity(getIntent());
                break;
            case R.id.about_us:
                Intent intent = new Intent(this,AboutUs.class);
                startActivity(intent);
                break;
        }
        return true;
    }


}