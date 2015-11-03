package com.gio.martino.moonboard;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;

import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;

import java.util.Random;

public class StatsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        BarChartView chart = (BarChartView)findViewById(R.id.linechart);

        chart.addData(getGradeStats());
        chart.show();
    }

    private BarSet getGradeStats()
    {
        UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
        AppDbHelper appDbHelper = new AppDbHelper(getApplicationContext());
        SQLiteDatabase userDb = userDbHelper.getReadableDatabase();
        SQLiteDatabase appDb = appDbHelper.getReadableDatabase();

        BarSet dataset = new BarSet();

        String selection = ProblemInfo.ProblemEntry.COLUMN_NAME_DONE + " LIKE ?";
        String[] selectionArgs = { String.valueOf(1) };

        Cursor c = userDb.query(
                ProblemInfo.ProblemEntry.TABLE_NAME,      // The table to query
                null,                                     // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        c.moveToFirst();

        String grades[] = getResources().getStringArray(R.array.grade_arrays);
        int gradeCount = grades.length;
        int problemsCount[] = new int[gradeCount];

        if(c.getCount() > 0)
        {
            do
            {
                int problemId = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID));

                String selection2 = "id LIKE ?";
                String[] selectionArgs2 = { String.valueOf(problemId) };

                Cursor c2 = appDb.query(
                        "problems",                               // The table to query
                        null,                                     // The columns to return
                        selection2,                               // The columns for the WHERE clause
                        selectionArgs2,                           // The values for the WHERE clause
                        null,                                     // don't group the rows
                        null,                                     // don't filter by row groups
                        null                                      // The sort order
                );

                c2.moveToFirst();

                if(c2.getCount() > 0)
                {
                    int grade = c2.getInt(c2.getColumnIndexOrThrow("grade"));
                    problemsCount[grade]++;
                }

            }
            while (c.moveToNext());
        }

        userDb.close();
        appDb.close();

        c.close();

        Random rnd = new Random();
        for(int i = 0; i < gradeCount; ++i)
        {
            Bar bar = new Bar(grades[i], problemsCount[i]);
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            bar.setColor(color);
            dataset.addBar(bar);
        }

        return dataset;
    }

}
