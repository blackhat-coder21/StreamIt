package com.example.videoplayer.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.example.videoplayer.R;
import com.example.videoplayer.adapters.AudioFilesAdapter;
import com.example.videoplayer.adapters.VideoFilesAdapter;
import com.example.videoplayer.models.MediaFiles;

import java.util.ArrayList;

public class AudioFilesActivity  extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String MY_PREF = "my pref";
    RecyclerView recyclerView;
    private ArrayList<MediaFiles> audioFilesArrayList = new ArrayList<>();
    static AudioFilesAdapter audioFilesAdapter;
    String folder_name;
    SwipeRefreshLayout swipeRefreshLayout;
    String sortOrder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_files);
        folder_name = getIntent().getStringExtra("folderName");
        getSupportActionBar().setTitle(folder_name);
        recyclerView = findViewById(R.id.audio_rv);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_audio);

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREF, MODE_PRIVATE).edit();
        editor.putString("playlistFolderName", folder_name);
        editor.apply();

        showAudioFiles();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showAudioFiles();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showAudioFiles() {
        audioFilesArrayList = fetchMedia(folder_name);
        audioFilesAdapter = new AudioFilesAdapter(audioFilesArrayList, this,0);
        recyclerView.setAdapter(audioFilesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        audioFilesAdapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia(String folderName) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        String sort_value = preferences.getString("sort","abcd");

        ArrayList<MediaFiles> audioFiles = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        if (sort_value.equals("sortName")) {
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+" ASC";
        } else if (sort_value.equals("sortSize_asc")) {
            sortOrder = MediaStore.MediaColumns.SIZE+" ASC";
        }else if (sort_value.equals("sortSize_desc")) {
            sortOrder = MediaStore.MediaColumns.SIZE+" DESC";
        }
        else if (sort_value.equals("sortLength_desc")) {
            sortOrder = MediaStore.Audio.Media.DURATION+" DESC";
        }else if (sort_value.equals("sortLength_asc")) {
            sortOrder = MediaStore.Audio.Media.DURATION+" ASC";
        } else {
            sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        }

        String selection = MediaStore.Audio.Media.DATA+" like?";
        String[] selectionArg = new String[]{"%"+folderName+"%"};
        Cursor cursor = getContentResolver().query(uri, null,
                selection, selectionArg, sortOrder);
        if (cursor != null && cursor.moveToNext()) {
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path,
                        dateAdded);
                audioFiles.add(mediaFiles);
            }while (cursor.moveToNext());
        }
        return audioFiles;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_files:
                finish();
                startActivity(getIntent());
                break;
            case R.id.sort_by:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Sort By");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.apply();
                        finish();
                        startActivity(getIntent());
                        dialog.dismiss();
                    }
                });
                String[] items = {"Name ▲","Size ▲","Size ▼","Recently Added",
                        "Length ▲","Length ▼"};
                alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                editor.putString("sort","sortName");
                                break;
                            case 1:
                                editor.putString("sort","sortSize_asc");
                                break;
                            case 2:
                                editor.putString("sort","sortSize_desc");
                                break;
                            case 3:
                                editor.putString("sort","sortDate");
                                break;
                            case 4:
                                editor.putString("sort","sortLength_asc");
                                break;
                            case 5:
                                editor.putString("sort","sortLength_desc");
                                break;
                        }
                    }
                });
                alertDialog.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String inputs = newText.toLowerCase();
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        for (MediaFiles media : audioFilesArrayList) {
            if (media.getTitle().toLowerCase().contains(inputs)) {
                mediaFiles.add(media);
            }
        }
        AudioFilesActivity.audioFilesAdapter.updateAudioFiles(mediaFiles);
        return true;
    }
}