package com.gio.martino.moonboard;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Martino on 29/04/2016.
 */
public class ProblemDownloader implements IHttpServiceBehaviour
{
    public interface Visitor
    {
        void onTaskCompleted(int newProblems);
    }

    private MoonboardHttpService api = new MoonboardHttpService(this);
    private AppDbAdapter db = null;
    Activity activity = null;
    public Visitor visitor = null;

    public ProblemDownloader(Activity activity)
    {
        db = new AppDbAdapter(activity.getApplicationContext());
        db.createDatabase();
        db.open();

        this.activity = activity;
    }

    public boolean async_run()
    {
        //if(api.getStatus() != AsyncTask.Status.FINISHED)
         //   return false;

        int lastId = db.getLastProblemId();
        api.execute("get_problems_as_json&arg0=" + Integer.toString(lastId));

        return true;
    }

    private int process(String strJson)
    {
        try
        {
            JSONObject jsonRootObject = new JSONObject(strJson);

            JSONArray jsonArray = jsonRootObject.optJSONArray("problems");

            for(int i=0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = Integer.parseInt(jsonObject.optString("id"));
                String name = jsonObject.optString("name");
                String author = jsonObject.optString("author");
                int grade = Integer.parseInt(jsonObject.optString("grade"));
                int holdType = Integer.parseInt(jsonObject.optString("hold type"));
                int holdSetup = Integer.parseInt(jsonObject.optString("hold setup"));
                String holds = jsonObject.optString("holds");
                //int stars = Integer.parseInt(jsonObject.optString("stars"));

                db.insertProblem(id, name, author, grade, holdType, holdSetup, holds);
            }

            return jsonArray.length();
        }
        catch (Exception e)
        {

            return 0;
        }
    }

    @Override
    public void OnTaskCompleted(String strResult)
    {
        int newProblems = process(strResult);
        if(visitor != null)
            visitor.onTaskCompleted(newProblems);
    }
}
