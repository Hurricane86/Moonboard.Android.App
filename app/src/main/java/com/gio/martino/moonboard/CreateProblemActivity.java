package com.gio.martino.moonboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.Set;

public class CreateProblemActivity extends Activity
{
    class Hold implements Comparable<Hold>
    {
        public int index;
        public int type;

        Hold(int index, int type)
        {
            this.index = index;
            this.type = type;
        }

        @Override
        public int compareTo(Hold o)
        {
            if(index == o.index)
                return 0;

            if(index < o.index)
                return -1;
            else
                return 1;
        }

        @Override
        public boolean equals(Object obj)
        {
            return index == ((Hold)obj).index;
        }

        @Override
        public int hashCode()
        {
            return index;
        }
    }

    private Set<Hold> holds = new HashSet<>();
    private byte[] setupHolds = null;
    private int selectedHoldType = 0;
    private int backgroundImageId = R.drawable.setup_1_1;
    private boolean freeSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_problem);

        Intent intent = getIntent();

        int holdsType = intent.getIntExtra("holdsType", 0);
        int holdsSetup = intent.getIntExtra("holdsSetup", 0);
        backgroundImageId = getResources().getIdentifier("setup_" + (holdsType + 1) + "_" + (holdsSetup + 1), "drawable", getPackageName());
        int holdsValuesId = getResources().getIdentifier("setup_" + (holdsType + 1) + "_" + (holdsSetup + 1), "array", getPackageName());

        String[] holdsStrs = getResources().getStringArray(holdsValuesId);
        setupHolds = getAllHoldsFromSetup(holdsStrs);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() != MotionEvent.ACTION_UP)
                    return false;

                float eventX = motionEvent.getX();
                float eventY = motionEvent.getY();
                float[] eventXY = new float[]{eventX, eventY};

                Matrix invertMatrix = new Matrix();
                ((ImageView) view).getImageMatrix().invert(invertMatrix);

                invertMatrix.mapPoints(eventXY);
                int x = (int) eventXY[0];
                int y = (int) eventXY[1];

                Drawable imgDrawable = ((ImageView) view).getDrawable();
                Bitmap bitmap = ((BitmapDrawable) imgDrawable).getBitmap();

                //Limit x, y range within bitmap
                if (x < 0)
                {
                    x = 0;
                }
                else if (x > bitmap.getWidth() - 1)
                {
                    x = bitmap.getWidth() - 1;
                }

                if (y < 0)
                {
                    y = 0;
                }
                else if (y > bitmap.getHeight() - 1)
                {
                    y = bitmap.getHeight() - 1;
                }

                int boardRow = Math.min(((1872 - y) / 100), 18);
                int boardColumn = Math.min((x - 188) / 100, 11);

                int holdIndex = boardColumn + boardRow * 11;
                if(!freeSelectionMode)
                {
                    holdIndex = getNearestHold(boardRow, boardColumn);
                }

                addOrRemoveHold(holdIndex);
                drawHolds();
                sendToMoonboard();

                return true;
            }
        });

        drawHolds();
        sendToMoonboard();
    }

    public static int holdLetter2Number(String holdLetter)
    {
        if (holdLetter == null || holdLetter.length() == 0)
            return 0;

        // old format : <item>E18 / 91 / N</item>
        try
        {
            char firstChar = holdLetter.charAt(0);
            int value = firstChar - 'A';

            int value2 = Integer.decode(holdLetter.substring(1).split(" ")[0]);

            return value + (value2 - 1) * 11; // 11 = K
        }
        // new format: <item>1/H7/SE</item>
        catch (java.lang.NumberFormatException ex)
        {
            String[] strs = holdLetter.split("/");

            char firstChar = strs[1].charAt(0);
            int value = firstChar - 'A';
            int value2 = Integer.decode(strs[1].substring(1));

            return value + (value2 - 1) * 11; // 11 = K
        }
    }

    private int getNearestHold(int boardRow, int boardColumn)
    {
        int bestHold = 0;
        float minDistance = Float.MAX_VALUE;
        for(byte hold : setupHolds)
        {
            float d = (Math.abs(((hold& 0xff) / 11) - boardRow)) + Math.abs((((hold & 0xff) % 11) - boardColumn));
            if(d < minDistance)
            {
                bestHold = hold & 0xff;
                minDistance = d;
            }
        }

        return bestHold;
    }

    private static byte[] getAllHoldsFromSetup(String[] holdsStrs)
    {
        byte[] holds = new byte[holdsStrs.length];

        int i = 0;
        for(String holdStr : holdsStrs)
        {
            holds[i++] = (byte)(holdLetter2Number(holdStr) & 0xFF);
        }

        return holds;
    }

    private void sendToMoonboard()
    {
        byte[] data = new byte[holds.size()*2];
        int i = 0;
        for(Hold h : holds)
        {
            data[i++] = Integer.valueOf(h.index).byteValue();
            data[i++] = Integer.valueOf(h.type).byteValue();
        }

        ((MoonboardApplication)getApplication()).getMoonboardCommunicationService().send(MoonboardCommunicationService.MESSAGE_TYPE_SET_PROBLEM, data);
    }

    private void addOrRemoveHold(int holdIndex)
    {
        Hold h = new Hold(holdIndex, selectedHoldType);

        if(!holds.contains(h))
            holds.add(h);
        else
            holds.remove(h);
    }

    private void drawHolds()
    {
        byte[] data = new byte[holds.size()*2];
        int i = 0;
        for(Hold h : holds)
        {
            data[i++] = Integer.valueOf(h.index).byteValue();
            data[i++] = Integer.valueOf(h.type).byteValue();
        }

        Bitmap image = ProblemBitmap.get(getBaseContext(), backgroundImageId, data, false, false, 0);
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_problem, menu);
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
            startActivityForResult(intent, R.id.action_settings);

            return true;
        }
        else if(id == R.id.action_selectHoldType)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_dialog_selectHoldType)
                    .setSingleChoiceItems(R.array.hold_types, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            selectedHoldType = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });


            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if(id == R.id.action_freeSelectionMode)
        {
            freeSelectionMode = !freeSelectionMode;
            if(freeSelectionMode)
                item.setTitle(R.string.action_freeSelectionModeOff);
            else
                item.setTitle(R.string.action_freeSelectionModeOn);

        }
        else if(id == R.id.action_clear)
        {
            holds.clear();

            drawHolds();
            sendToMoonboard();
        }

        return super.onOptionsItemSelected(item);
    }
}
