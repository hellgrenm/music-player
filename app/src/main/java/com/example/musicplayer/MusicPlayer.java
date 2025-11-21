package com.example.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class MusicPlayer {
    MediaPlayer music;

    public MusicPlayer(Context context, Uri uri){
        music = MediaPlayer.create(context, uri);
    }
    public void play(){
        if (music != null) {
            music.start();
        }
    }

    public void pause() {
        if (music == null) return;

        try {
            if (music.isPlaying()) {
                music.pause();
            } else {
                music.start();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }



    public void stop(){
        if (music == null) return;
        try {
            music.stop();
            music.release();

        } catch ( IllegalStateException e){
            e.printStackTrace();
        }
    music = null;
    }

    public boolean isPlaying() {
        return music != null && music.isPlaying();
    }


    public int getCurrentPosition() {
        return music != null ? music.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return music != null ? music.getDuration() : 0;
    }

    public void seekTo(int position) {
        if (music != null) {
            music.seekTo(position);
        }
    }


}