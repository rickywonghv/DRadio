package xyz.damonwong.dradio.dradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

public class audioBroadcast extends BroadcastReceiver {
    MusicPlayer musicPlayer;
    KeyEvent event;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            event= (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                // Handle key press.
                Log.d("KeyEvent",Integer.toString(event.getKeyCode()));

            }
        }
    }
}
