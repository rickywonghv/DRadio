package xyz.damonwong.dradio.dradio;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by damon on 9/13/16.
 */
public class api{

    public interface VolleyCallback{
        void onSuccess(JSONObject result);
    }

    public JsonObjectRequest get(String req,final VolleyCallback callback){
        //final String url = "http://rss2json.com/api.json?rss_url=http://hkr2.netpodcast.net/hkpeanut3.php?channelid=nosurprise";
        final String basedUrl="https://dradio.damonwong.xyz/";
        String url;
        final String mystery ="mystery";
        final String mysterybackup ="mystery2";
        final String digi="digi";
        final String hkpug="hkpug";
        final String randgad="randgad";
        final String warrior="warrior";
        final String mikeleefile="mikeleefile";

        switch (req){
            case "digi":
                url=basedUrl+digi;
                break;
            case "mystery":
                url=basedUrl+mystery;
                break;
            case "mystery2":
                url=basedUrl+mysterybackup;
                break;
            case "hkpug":
                url=basedUrl+hkpug;
                break;
            case "randgad":
                url=basedUrl+randgad;
                break;
            case "warrior":
                url=basedUrl+warrior;
                break;
            case "mikeleefile":
                url=basedUrl+mikeleefile;
                break;
            default:
                url=basedUrl+mystery;
        }

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public JSONObject onResponse(JSONObject response) {
                        callback.onSuccess(response);
                        return response;
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }

        );
        getRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return getRequest;
    }

}
