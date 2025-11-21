package com.example.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private MusicPlayer mp;
    private ImageButton pauseBtn, stopBtn;
    private Button playBtn;
    private TextView artistText;
    private ImageView cdArt;
    private SeekBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    playBtn = findViewById(R.id.play_btn);
    pauseBtn = findViewById(R.id.pause_btn);
    stopBtn = findViewById(R.id.stop_btn);
    artistText = findViewById(R.id.song_name);
    cdArt = findViewById(R.id.album_art);
    progress = findViewById(R.id.progress_bar);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp == null || !mp.isPlaying()){
                    filePicker.launch("audio/*");
                    setListeners();
                } else if (mp!= null && mp.isPlaying()){
                    mp.stop();
                    filePicker.launch("audio/*");
                }
            }
        });

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mp != null && fromUser) {
                    mp.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mp != null) {
                progress.setProgress(mp.getCurrentPosition());
                if (mp.isPlaying()) {
                    handler.postDelayed(this, 1000);
                }
            }
        }
    };




    public void setListeners(){

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    if (mp.isPlaying()) {
                        mp.pause();
                        pauseBtn.setImageResource(R.drawable.play);
                    } else {
                        mp.play();
                        pauseBtn.setImageResource(R.drawable.pause);
                    }
                }
            }
        });


        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null){
                    mp.stop();
                    artistText.setText("Select a song");
                    cdArt.setImageResource(R.drawable.music);
                    pauseBtn.setImageResource(R.drawable.pause);
                    progress.setProgress(0);
                }
            }
        });

    }

    private final ActivityResultLauncher<String> filePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {

                    mp = new MusicPlayer(this, uri);
                    mp.play();
                    artistText.setText(getFileNameFromUri(this, uri));
                    pauseBtn.setImageResource(R.drawable.pause);
                    progress.setMax(mp.getDuration());
                    handler.post(updateSeekBar);


                    Bitmap albumArt = getAlbumArt(this, uri);

                    if (albumArt != null) {
                        cdArt.setImageBitmap(albumArt);
                    } else {
                        cdArt.setImageResource(R.drawable.ic_launcher_background); // fallback-bild
                    }
                }
            });
    public String getFileNameFromUri(Context context, Uri uri) {
        String result = null;


        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


    public Bitmap getAlbumArt(Context context, Uri uri) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, uri);

            byte[] artBytes = mmr.getEmbeddedPicture();
            if (artBytes != null) {
                return BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            }
            mmr.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }







}