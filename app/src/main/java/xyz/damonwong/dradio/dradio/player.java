package xyz.damonwong.dradio.dradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by damon on 9/13/16.current time update
 */
public class player extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener{
    public MediaPlayer mMediaPlayer;
    private static final String ACTION_PLAY = "PLAY";
    private static player mInstance = null;


    NotificationManager mNotificationManager;
    Notification mNotification = null;
    final int NOTIFICATION_ID = 1;
    Integer length;
    String Url;
    String playName;

    @Override
    public void onCreate() {
        mInstance = this;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
    }

    public int getDur(){
        return mMediaPlayer.getDuration();
    }

    public int curPosit(){
        return mMediaPlayer.getCurrentPosition();
    }

    public void init(){
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                togglePlayPause();
            }
        });

    }

    public void destroy(){
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void togglePlayPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();

        } else {
            mMediaPlayer.start();
        }
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public void playUrl(String title,String url){
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            //destroy();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        try {
            //mMediaPlayer=new MediaPlayer();

            init();
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            //mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void noti(String songName){
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.play)
                .setTicker(songName)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(songName);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

}
