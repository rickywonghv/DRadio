package xyz.damonwong.dradio.dradio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DrawerLayout layDrawer;
    private ListView lstDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    api appclass=new api();
    RequestQueue queue;
    TextView playingText;
    ListView listV;
    List<Programme> programmeList=new ArrayList<Programme>();
    ProgrammeAdapter mainadapter;
    ImageButton ctrlBtn;
    ConnectivityManager cm;
    public ImageView mPlayerControl;
    WifiManager.WifiLock wifiLock;
    TextView currentTime;
    TextView audioDur;
    SeekBar seekBar;
    private double startTime = 0;
    private double finalTime = 0;
    private MusicPlayer musicPlayer;

    int duration;
    Intent sendIntent;
    String ACTION_UPDATE_INFO="";

    BroadcastReceiver actBroadcast;
    Handler mHandler;

    Spinner selector;
    private MusicIntentReceiver myReceiver;
    AudioManager audioManager;
    private RemoteControlReceiver remoteControlReceiver;

    DownloadService downloadService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentTime=(TextView) findViewById(R.id.currentTime);
        audioDur=(TextView) findViewById(R.id.audioDur);
        selector=(Spinner) findViewById(R.id.selector);
        final ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.programme,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selector.setAdapter(adapter);
        selector.setOnItemSelectedListener(this);


        Intent it=new Intent(MainActivity.this,MusicPlayer.class);
        bindService(it,conn,BIND_AUTO_CREATE);
        Intent downloadIntent=new Intent(MainActivity.this,DownloadService.class);
        bindService(downloadIntent,downloadServ,BIND_AUTO_CREATE);
        queue = Volley.newRequestQueue(this);
        volleyInit();
        playerInit();
        listInit();
        playingText=(TextView) findViewById(R.id.playingText);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    musicPlayer.mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        myReceiver = new MusicIntentReceiver();
        remoteControlReceiver=new RemoteControlReceiver();
        ctrl();
    }

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if(musicPlayer.mediaPlayer.isPlaying()){
                            musicPlayer.mediaPlayer.pause();
                            setCtrlPlay();
                        }

                        break;
                    case 1:
                        if(!musicPlayer.isComplete()){
                            musicPlayer.mediaPlayer.start();
                            setCtrlPause();
                        }
                        break;
                    default:
                        //Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }

    public class RemoteControlReceiver extends BroadcastReceiver {
        MediaPlayer mediaPlayer;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                    // Handle key press.
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                    }else{
                        mediaPlayer.pause();
                    }
                }
            }
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        programmeList.clear();
        mainadapter.notifyDataSetInvalidated();
        Log.d("Selected",adapterView.getSelectedItem().toString());
        String Selected=adapterView.getSelectedItem().toString();
        getItem(Selected);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        actBroadcast=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int a= intent.getExtras().getInt("duration");
                if(a!=0){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int currentPosition = 0;
                            int total=0;
                            total = musicPlayer.mediaPlayer.getDuration();
                            while (!musicPlayer.isComplete()) {
                                try {
                                    Thread.sleep(1000);
                                    currentPosition = musicPlayer.mediaPlayer.getCurrentPosition();
                                     //for current song progress
                                } catch (InterruptedException e) {
                                    return;
                                } catch (Exception e) {
                                    return;
                                }

                                final String totalTime =getDurationBreakdown(total);
                                final String curTime = getDurationBreakdown(currentPosition);
                                seekBar.setMax(total); //song duration
                                seekBar.setProgress(currentPosition);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        audioDur.setText(totalTime);
                                        currentTime.setText(curTime);
                                    }
                                });
                            }
                        }
                    }).start();

                    if(musicPlayer.mediaPlayer.isPlaying()){
                        String crt=Integer.toString(musicPlayer.mediaPlayer.getCurrentPosition());
                        setCurrentTime(crt);
                        mPlayerControl.setImageResource(R.drawable.pause);
                    }else {
                        mPlayerControl.setImageResource(R.drawable.play);
                    }
                }
            }
        };
        IntentFilter ifilter = new IntentFilter("xyz.damonwong.dradio.dradio.action.DURATION");
        registerReceiver(actBroadcast, ifilter);

        if(isOnline()){
            //getItem("mystery");

        }else{
            toast("Check your Internet Connection!");
        }

    }


    public void setDuration(String param){
        audioDur.setText(param);
    }
    public void setCurrentTime(String crt){
        currentTime.setText(crt);
    }

    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append(":");
        sb.append(minutes);
        sb.append(":");
        sb.append(seconds);
        sb.append("");

        return(sb.toString());
    }

    public void ctrl(){
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {//global BroadcastReceiver
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (action.equals("android.intent.action.MEDIA_BUTTON")) {
                    Log.e("test", "ok");
                }
            }
        };
        IntentFilter intentfilterTime = null;
        intentfilterTime = new IntentFilter();
        intentfilterTime.addAction("android.intent.action.MEDIA_BUTTON");
        registerReceiver(broadcastReceiver, intentfilterTime);
    }

    private void volleyInit(){
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        queue = new RequestQueue(cache, network);
        queue.start();
    }

    private ServiceConnection downloadServ=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadService=((DownloadService.LocalBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            unbindService(downloadServ);
        }
    };

    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(myReceiver, filter);
            musicPlayer=((MusicPlayer.LocalBinder)iBinder).getService();

            IntentFilter ctrl = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
            registerReceiver(remoteControlReceiver,ctrl);

            if(musicPlayer.isPlaying&&!musicPlayer.isComplete()&&!musicPlayer.getPlayTitle().isEmpty()){
                playingText.setText(musicPlayer.getPlayTitle());
                setCtrlPause();
            }else{
                playingText.setText(musicPlayer.getPlayTitle());
                setCtrlPlay();
            }
            //New Tread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int currentPosition = 0;
                    while (!musicPlayer.isComplete()) {
                        try {
                            Thread.sleep(1000);
                            currentPosition = musicPlayer.mediaPlayer.getCurrentPosition();
                        } catch (InterruptedException e) {
                            return;
                        } catch (Exception e) {
                            return;
                        }
                        final int total = musicPlayer.mediaPlayer.getDuration();
                        final String totalTime =getDurationBreakdown(total);
                        final String curTime = getDurationBreakdown(currentPosition);
                        seekBar.setEnabled(true);
                        seekBar.setMax(total); //song duration
                        seekBar.setProgress(currentPosition);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                audioDur.setText(totalTime);
                                currentTime.setText(curTime);
                            }
                        });
                    }
                }
            }).start();
            //End Current Thread
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            unregisterReceiver(myReceiver);
        }
    };

    private void listInit(){
        listV=(ListView) findViewById(R.id.mainList);
        mainadapter=new ProgrammeAdapter(MainActivity.this,programmeList);
        listV.setAdapter(mainadapter);
        listV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Programme programme = programmeList.get(position);
                musicPlayer.setUrl(programme.getTitle(),programme.getUrl());
                musicPlayer.playUrl();
                seekBar.setProgress(0);
                playingText.setText(musicPlayer.getPlayTitle());
                audioDur.setText("00:00");
                currentTime.setText("00:00");
            }
        });

        listV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Programme programme = programmeList.get(i);
                String url=programme.getUrl();
                String title=programme.getTitle();
                downloadService.download(url,title);
                return false;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void playerInit(){

        ctrlBtn=(ImageButton) findViewById(R.id.ctrlBtn);
        mPlayerControl = (ImageView)findViewById(R.id.ctrlBtn);
        seekBar=(SeekBar) findViewById(R.id.seekBar);


        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicPlayer.isPlaying){
                    setCtrlPlay();
                }else{
                    setCtrlPause();
                }

                musicPlayer.togglePlayPause();

            }
        });
    }

    public void setCtrlPlay(){
        mPlayerControl.setImageResource(R.drawable.play);
    }
    public void setCtrlPause(){
        mPlayerControl.setImageResource(R.drawable.pause);
    }

    public void getItem(String selected){ //Get Items List
        queue.add(appclass.get(selected,new api.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                listMainItems(result);
            }
        }));
    }

    public void listMainItems(JSONObject result){ //Process List Result

        try{

            JSONArray items=result.getJSONArray("items");
            for(int i=0;i<items.length();i++){
                JSONObject object=items.getJSONObject(i);
                JSONObject enclosure=object.getJSONObject("enclosure");

                String title=object.getString("title");
                String url=enclosure.getString("link");
                String date="1-1-2";//object.getString("pubDate");
                String desc="abc";//object.getString("description");
                programmeList.add(new Programme(title,desc,url,date));
            }
            mainadapter.notifyDataSetChanged();
        }catch (Exception error){

        }
    }

    public boolean isOnline() {
        cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void toast(String msg){
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
    }
}
