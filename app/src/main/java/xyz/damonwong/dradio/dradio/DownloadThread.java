package xyz.damonwong.dradio.dradio;

import android.os.AsyncTask;

import java.net.URL;

/**
 * Created by damon on 9/26/16.
 */
public class DownloadThread extends AsyncTask<URL,Integer,Long> {

    @Override
    protected Long doInBackground(URL... urls) {

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
    }
}
