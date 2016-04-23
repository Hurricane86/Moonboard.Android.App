package com.gio.martino.moonboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new MoonboardHttpService().execute("update_db");

        Button button = (Button)findViewById(R.id.searchButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = makeIntent(v.getContext(), ProblemsActivity.class);
                startActivity(intent);
            }
        });

        button = (Button)findViewById(R.id.newProblemButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = makeIntent(v.getContext(), CreateProblemActivity.class);
                startActivity(intent);
            }
        });

        button = (Button)findViewById(R.id.setupWizardButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = makeIntent(v.getContext(), SetupWizardActivity.class);
                startActivity(intent);
            }
        });
        button = (Button)findViewById(R.id.statsButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = makeIntent(v.getContext(), StatsActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Spinner holdSpinner = (Spinner)findViewById(R.id.holdSpinner);
        //holdSpinner.setSelection(settings.getInt("hold_spinner_selection", 0), false);
        holdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                int resourceId = 0;
                switch (position) {
                    case 0:
                        resourceId = R.array.holds0_arrays;
                        break;
                    case 1:
                        resourceId = R.array.holds1_arrays;
                        break;
                    case 2:
                        resourceId = R.array.holds2_arrays;
                        break;
                    case 3:
                        resourceId = R.array.holds3_arrays;
                        break;
                    case 4:
                        resourceId = R.array.holds4_arrays;
                        break;
                }

                Spinner setupSpinner = (Spinner) findViewById(R.id.setupSpinner);

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentView.getContext(),
                        resourceId, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                setupSpinner.setAdapter(adapter);

                SharedPreferences.Editor editSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editSettings.putInt("hold_spinner_selection", position);
                editSettings.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        Spinner setupSpinner = (Spinner)findViewById(R.id.setupSpinner);
        //setupSpinner.setSelection(settings.getInt("setup_spinner_selection", 0), false);
        setupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                SharedPreferences.Editor editSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editSettings.putInt("setup_spinner_selection", position);
                editSettings.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner fromGradeSpinner = (Spinner) findViewById(R.id.fromGradeSpinner);
        fromGradeSpinner.setSelection(settings.getInt("from_grade_spinner_selection", 0), false);
        fromGradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner toGradeSpinner = (Spinner) findViewById(R.id.toGradeSpinner);
                if (toGradeSpinner.getSelectedItemPosition() < position)
                    toGradeSpinner.setSelection(position);

                SharedPreferences.Editor editSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editSettings.putInt("from_grade_spinner_selection", position);
                editSettings.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        Spinner toGradeSpinner = (Spinner) findViewById(R.id.toGradeSpinner);
        toGradeSpinner.setSelection(settings.getInt("to_grade_spinner_selection", toGradeSpinner.getAdapter().getCount()-1), false);
        toGradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner fromGradeSpinner = (Spinner) findViewById(R.id.fromGradeSpinner);
                if (fromGradeSpinner.getSelectedItemPosition() > position)
                    fromGradeSpinner.setSelection(position);

                SharedPreferences.Editor editSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editSettings.putInt("to_grade_spinner_selection", position);
                editSettings.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        AppDbAdapter db = new AppDbAdapter(getApplicationContext());
        db.createDatabase();
        db.open();

        Cursor c = db.getAuthors();
        c.moveToFirst();
        String[] authors = new String[c.getCount()+1];
        authors[0] = "All authors";
        int i = 1;
        do
        {
            String s = c.getString(0);
            if(s != null)
                authors[i++] = s;
        }
        while (c.moveToNext());

        Spinner authorSpinner = (Spinner)findViewById(R.id.authorSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, authors);
        authorSpinner.setAdapter(adapter);
        authorSpinner.setSelection(settings.getInt("author_spinner_selection", 0), false);
        authorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l)
            {
                SharedPreferences.Editor editSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editSettings.putInt("author_spinner_selection", position);
                editSettings.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent makeIntent(Context context, Class<?> cls)
    {
        Spinner holdSpinner0 = (Spinner) findViewById(R.id.holdSpinner);
        Spinner setupSpinner0 = (Spinner) findViewById(R.id.setupSpinner);
        Spinner fromGradeSpinner = (Spinner) findViewById(R.id.fromGradeSpinner);
        Spinner toGradeSpinner = (Spinner) findViewById(R.id.toGradeSpinner);
        Spinner author = (Spinner) findViewById(R.id.authorSpinner);

        Intent intent = new Intent(context, cls);
        intent.putExtra("holdsType", holdSpinner0.getSelectedItemPosition());
        intent.putExtra("holdsSetup", setupSpinner0.getSelectedItemPosition());
        intent.putExtra("fromGrade", fromGradeSpinner.getSelectedItemPosition());
        intent.putExtra("toGrade", toGradeSpinner.getSelectedItemPosition());

        if (author.getSelectedItemPosition() > 0)
            intent.putExtra("author", (String) author.getSelectedItem());

        return intent;
    }
}
