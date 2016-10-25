package xyz.damonwong.dradio.dradio;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends Service{
    private DownloadManager downloadManager;
    private long downloadReference;
    private boolean isDownloading=false;
    private

    api volleyApi=new api();

    StrictMode.ThreadPolicy policy;


    public DownloadService() {
    }

    protected Object doInBackground(String url,String name) {
        download(url,name);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TBD
        return Service.START_FLAG_REDELIVERY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return mLockBin;
    }

    public class LocalBinder extends Binder {
        DownloadService getService(){
            return DownloadService.this;
        }
    }

    private LocalBinder mLockBin=new LocalBinder();

    public void download(String uri, String name){

        if(isExternalStorageWritable()&&isExternalStorageReadable()){
            File dir = new File(getExternalFilesDir(null),"dradio/"+name);
            if(dir.exists() == false){
                dir.mkdirs();
            }
            int count;
            try{
                File f = new File(Environment.getExternalStorageDirectory() + "/dradio");
                if(!f.exists()) {
                    f.mkdir();
                }
                URL url = new URL(uri);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(android.os.Environment.getExternalStorageDirectory()+"/dradio/"+name+".mp3");
                byte data[] = new byte[1024];
                long total = 0;
                forground(name);

                while ((count = input.read(data)) != -1) {
                    total += count;
                    //publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                    isDownloading=true;
                }
                output.flush();
                output.close();
                input.close();
                isDownloading=false;
                if(!isDownloading){
                    stopForeground(true);
                }
                Toast.makeText(this,"Downloaded",Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"notIsExternalStorageWritable",Toast.LENGTH_LONG).show();
        }
    }

    private void forground(String name){
        Intent it=new Intent(this,MainActivity.class);
        PendingIntent pentIt=PendingIntent.getActivity(getApplicationContext(),0,it,PendingIntent.FLAG_CANCEL_CURRENT);
        Notification noti;
        noti=new Notification.Builder(this)
                .setSmallIcon(R.drawable.play)
                .setTicker("Background Play")
                .setContentTitle(getString(R.string.app_name))
                .setContentText(name+" is downloading")
                .setContentIntent(pentIt)
                .build();
        startForeground(1,noti);
    }

    protected void onProgressUpdate(Integer progress) {
        //setProgressPercent(progress[0]);
    }

    public boolean isDownloading(){
        if(isDownloading){
            return true;
        }else{
            return false;
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
