package com.gio.martino.moonboard;

import android.os.AsyncTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class MoonboardHttpService extends AsyncTask<String, Void, String>
{
    private IHttpServiceBehaviour behaviour;

    MoonboardHttpService(IHttpServiceBehaviour b)
    {
        behaviour = b;
    }

    @Override
    protected String doInBackground(String... str)
    {
        try
        {
            String get_url = "http://moonboard.x10.mx/moonservice/moonboardService.php?api=" + str[0].replace(" ", "%20");
            HttpClient Client = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpGet httpget = new HttpGet(get_url);
            return Client.execute(httpget, responseHandler);
        }
        catch(Exception e)
        {
            System.out.println(e);
            return "";
        }
    }

    @Override
    protected void onPostExecute(String result)
    {
       if(behaviour != null)
           behaviour.OnTaskCompleted(result);
    }
}
