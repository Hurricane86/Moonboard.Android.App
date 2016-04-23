package com.gio.martino.moonboard;

import android.os.AsyncTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class MoonboardHttpService extends AsyncTask<String, Void, String>
{
    @Override
    protected String doInBackground(String... str)
    {
        try
        {
            String get_url = "http://moonboard.x10.mx/moonservice/moonboardService.php?api=" + str[0].replace(" ", "%20");
            HttpClient Client = new DefaultHttpClient();
            HttpGet httpget;
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            httpget = new HttpGet(get_url);
            String content = Client.execute(httpget, responseHandler);

            return content;
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return "Cannot Connect";
    }

    protected void onPostExecute(String result)
    {
       // TextView tv = (TextView) findViewById(R.id.show_text);
       // tv.setText(result);
       System.out.println(result);
    }
}
