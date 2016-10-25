package xyz.damonwong.dradio.dradio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.support.v4.media.session.MediaButtonReceiver;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOException;

public class MusicPlayer extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,AudioManager.OnAudioFocusChangeListener {
    String playTitle;
    String playUrl;
    Boolean isPlaying;
    Boolean isComplete;
    MediaPlayer mediaPlayer;
    Boolean isPause;
    Notification noti;
    WifiManager.WifiLock wifiLock;
    PendingIntent pplayIntent,pPauseIntent;
    int duration=0;
    AudioManager audioManager;


    public static final String ACTION_PLAY="xyz.damonwong.dradio.dradio.action.PLAY";
    public static final String ACTION_PAUSE="xyz.damonwong.dradio.dradio.action.PAUSE";
    public static final String ACTION_MUSIC_DURATION = "xyz.damonwong.dradio.dradio.action.DURATION";


    public class LocalBinder extends Binder{
        MusicPlayer getService(){
            return MusicPlayer.this;
        }
    }

    private LocalBinder mLockBin=new LocalBinder();

    @Override
    public void onCreate() {
        isPlaying=false;
        isPause=false;
        isComplete=true;
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setBluetoothScoOn(true);
        int result = audioManager.requestAudioFocus(afChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("Au","AUDIOFOCUS_REQUEST_GRANTED");
        }

        super.onCreate();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        toast("Url Error!");
        return false;
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        if(!isComplete()&&mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        // Resume playback
                        mediaPlayer.start();
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        audioManager.abandonAudioFocus(afChangeListener);
                        // Stop playback
                    }
                }
            };

    public class RemoteControlReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                    // Handle key press.
                }
            }
        }
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch (i){
            case AudioManager.AUDIOFOCUS_GAIN:
                mediaPlayer.setVolume(0.7f,0.7f);
                mediaPlayer.start();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stopSelf();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if(!isComplete()&&mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.setVolume(0.1f,0.1f);
                }
                break;
        }
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        togglePlayPause();
        Intent it=new Intent(this,MainActivity.class);
        PendingIntent pentIt=PendingIntent.getActivity(getApplicationContext(),0,it,PendingIntent.FLAG_CANCEL_CURRENT);

        Intent playIntent = new Intent(this, MusicPlayer.class);
        playIntent.setAction(ACTION_PLAY);
        pplayIntent = PendingIntent.getService(this, 0,playIntent, 0);

        Intent pauseIntent = new Intent(this, MusicPlayer.class);
        pauseIntent .setAction(ACTION_PAUSE);
        pPauseIntent = PendingIntent.getService(this, 0,pauseIntent, 0);

        isComplete=false;
        Intent intent = new Intent("xyz.damonwong.dradio.dradio.action.DURATION");
        duration =mediaPlayer.getDuration();
        intent.putExtra("duration", duration);
        sendBroadcast(intent);

        noti=new Notification.Builder(this)
                .setSmallIcon(R.drawable.play)
                .setTicker("Background Play")
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getPlayTitle()+" is playing")
                .setContentIntent(pentIt)
                .addAction(android.R.drawable.ic_media_play, "Play",pplayIntent)
                .addAction(android.R.drawable.ic_media_pause, "Pause",pPauseIntent)
                .build();
        startForeground(1,noti);
    }

    public boolean isComplete(){
        if(isComplete){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equals(ACTION_PLAY)){
            mediaPlayer.start();
            isPlaying=true;
            isPause=false;
        }else if(intent.getAction().equals(ACTION_PAUSE)){
            mediaPlayer.pause();
            isPause=true;
            isPlaying=false;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.release();
        stopForeground(true);
        return super.onUnbind(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(mediaPlayer.isPlaying()){

        }else{
            isPause=false;
            isPlaying=false;
            isComplete=true;
            mediaPlayer.stop();
            mediaPlayer.reset();
            stopForeground(true);
            toast(getPlayTitle()+" is ended");
        }
    }

    public MusicPlayer() {}

    @Override
    public IBinder onBind(Intent arg0) {
        mediaPlayer=new MediaPlayer ();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        return mLockBin;
    }

    public String getPlayTitle() {
        return playTitle;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setUrl(String title,String url){
        playTitle=title;
        playUrl=url;
    }

    public void togglePlayPause(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            isPause=true;
            isPlaying=false;
        }else if(mediaPlayer!=null){
            mediaPlayer.start();
            isPlaying=true;
            isPause=false;
        }
    }

    public int getCurt(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public void playUrl(){
        isComplete=true;
        if(getPlayTitle().isEmpty()&&getPlayUrl().isEmpty()){
            isPlaying=false;
            toast("Not Valid");
        }else{
            if (mediaPlayer.isPlaying()||isPause) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                isPlaying=false;
                isComplete=true;
            }

            try {
                mediaPlayer.setDataSource(getPlayUrl());
                mediaPlayer.prepareAsync();
                isPlaying=true;
                isPause=false;
                isComplete=false;
                toast(getPlayTitle());
            } catch (IOException e) {
                mediaPlayer.reset();
                isComplete=true;
                mediaPlayer.release();
                e.printStackTrace();
            }
        }
    }

    public void toast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }


}
