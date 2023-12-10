package com.example.videoplayer.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videoplayer.activities.AudioPlayerActivity;
import com.example.videoplayer.activities.audio_home;
import com.example.videoplayer.models.MediaFiles;
import com.example.videoplayer.R;
import com.example.videoplayer.models.Utility;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;

public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.ViewHolder> {
    private ArrayList<MediaFiles> audioList;
    private Context context;
    BottomSheetDialog bottomSheetDialog;
    private int viewType;

    public AudioFilesAdapter(ArrayList<MediaFiles> audioList, Context context,int viewType) {
        this.audioList = audioList;
        this.context = context;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioFilesAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.audioName.setText(audioList.get(position).getDisplayName());
        String size = audioList.get(position).getSize();
        holder.audioSize.setText(android.text.format.Formatter.formatFileSize(context,
                Long.parseLong(size)));
        double milliSeconds = Double.parseDouble(audioList.get(position).getDuration());
        holder.audioDuration.setText(Utility.timeConversion((long) milliSeconds));


        if (viewType == 0) {
            holder.menu_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
                    View bsView = LayoutInflater.from(context).inflate(R.layout.video_bs_layout,
                            v.findViewById(R.id.bottom_sheet));
                    bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Rename to");
                            EditText editText = new EditText(context);
                            String path = audioList.get(position).getPath();
                            final File file = new File(path);
                            String audioName = file.getName();
                            audioName = audioName.substring(0, audioName.lastIndexOf("."));
                            editText.setText(audioName);
                            alertDialog.setView(editText);
                            editText.requestFocus();

                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (TextUtils.isEmpty(editText.getText().toString())) {
                                        Toast.makeText(context, "Can't rename empty file", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String onlyPath = file.getParentFile().getAbsolutePath();
                                    String ext = file.getAbsolutePath();
                                    ext = ext.substring(ext.lastIndexOf("."));
                                    String newPath = onlyPath + "/" + editText.getText().toString() + ext;
                                    File newFile = new File(newPath);
                                    boolean rename = file.renameTo(newFile);
                                    if (rename) {
                                        ContentResolver resolver = context.getApplicationContext().getContentResolver();
                                        resolver.delete(MediaStore.Files.getContentUri("external"),
                                                MediaStore.MediaColumns.DATA + "=?", new String[]
                                                        {file.getAbsolutePath()});
                                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        intent.setData(Uri.fromFile(newFile));
                                        context.getApplicationContext().sendBroadcast(intent);

                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Audio Renamed", Toast.LENGTH_SHORT).show();

                                        SystemClock.sleep(200);
                                        ((Activity) context).recreate();
                                    } else {
                                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.create().show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse(audioList.get(position).getPath());
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("audio/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            context.startActivity(Intent.createChooser(shareIntent, "Share Audio via"));
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Delete");
                            alertDialog.setMessage("Do you want to delete this audio");
                            alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri contentUri = ContentUris
                                            .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                    Long.parseLong(audioList.get(position).getId()));
                                    File file = new File(audioList.get(position).getPath());
                                    boolean delete = file.delete();
                                    if (delete) {
                                        context.getContentResolver().delete(contentUri, null, null);
                                        audioList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, audioList.size());
                                        Toast.makeText(context, "Audio Deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Can't Delete", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Properties");

                            String one = "File: " + audioList.get(position).getDisplayName();

                            String path = audioList.get(position).getPath();
                            int indexOfPath = path.lastIndexOf("/");
                            String two = "Path: " + path.substring(0, indexOfPath);

                            String three = "Size: " + android.text.format.Formatter
                                    .formatFileSize(context, Long.parseLong(audioList.get(position).getSize()));

                            String four = "Length: " + Utility.timeConversion((long) milliSeconds);

                            String namewithFormat = audioList.get(position).getDisplayName();
                            int index = namewithFormat.lastIndexOf(".");
                            String format = namewithFormat.substring(index + 1);
                            String five = "Format: " + format;

                            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                            metadataRetriever.setDataSource(audioList.get(position).getPath());


                            alertDialog.setMessage(one + "\n\n" + two + "\n\n" + three + "\n\n" + four +
                                    "\n\n" + five);
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bottomSheetDialog.setContentView(bsView);
                    bottomSheetDialog.show();

                }
            });
        } else {
            holder.menu_more.setVisibility(View.GONE);
            holder.audioName.setTextColor(Color.WHITE);
            holder.audioSize.setTextColor(Color.WHITE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = audioList.get(position).getPath();
                int indexOfPath = path.lastIndexOf("/");
                String two = path.substring(0, indexOfPath);
                String one =  audioList.get(position).getDisplayName();
                String audioFile=two+"/"+one;
                Intent intent = new Intent(context, AudioPlayerActivity.class);
                intent.putExtra("audioFile",audioFile);
                intent.putExtra("audioName",one);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail,menu_more;
        TextView audioName,audioSize,audioDuration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            menu_more = itemView.findViewById(R.id.video_menu_more);
            audioName = itemView.findViewById(R.id.video_name);
            audioSize = itemView.findViewById(R.id.video_size);
            audioDuration = itemView.findViewById(R.id.video_duration);
        }
    }

    public void  updateAudioFiles(ArrayList<MediaFiles> files) {
        audioList = new ArrayList<>();
        audioList.addAll(files);
        notifyDataSetChanged();
    }
}
