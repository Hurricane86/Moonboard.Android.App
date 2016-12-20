package com.gio.martino.moonboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class SetupWizardActivity extends Activity
{

    private int backgroundImageId = R.drawable.setup_1_1;
    String[] holdsStrs;
    byte[] holds;
    int currentHoldIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_wizard);

        Intent intent = getIntent();

        int holdsType = intent.getIntExtra("holdsType", 0);
        int holdsSetup = intent.getIntExtra("holdsSetup", 0);
        backgroundImageId = getResources().getIdentifier("setup_" + (holdsType + 1) + "_" + (holdsSetup + 1), "drawable", getPackageName());
        int holdsValuesId = getResources().getIdentifier("setup_" + (holdsType + 1) + "_" + (holdsSetup + 1), "array", getPackageName());

        if(backgroundImageId != 0 && holdsValuesId != 0 )
        {
            holdsStrs = getResources().getStringArray(holdsValuesId);
            holds = getAllHoldsFromSetup(holdsStrs);

            drawHolds(holds);
            sendToMoonboard(holds);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    nextHold();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    prevHold();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void nextHold()
    {
        if(holds == null)
            return;

        currentHoldIndex = (currentHoldIndex + 1) % holds.length;

        update();
    }

    private void prevHold()
    {
        if(holds == null)
            return;

        --currentHoldIndex;
        if(currentHoldIndex < 0)
            currentHoldIndex = holds.length - 1;

        update();
    }

    private void update()
    {
        byte holdIndex = holds[currentHoldIndex];

        drawHolds(new byte[]{holdIndex});
        sendToMoonboard(new byte[]{holdIndex});

        // update UI stuff...
        String holdInfo = holdsStrs[currentHoldIndex];
        TextView holdInfoTextView = (TextView)findViewById(R.id.holdInfoTextView);
        holdInfoTextView.setText(holdInfo);

        /*TouchImageView imageView = (TouchImageView)findViewById(R.id.imageView);
        float scaleX = 2;
        float scaleY = 2;

        float startX = 94 * scaleX;
        float startY = 936 * scaleY;
        float deltaX = 50 * scaleX;
        float deltaY = 50 * scaleY;

        int x = (holdIndex & 0xff) % 11;
        int y = (holdIndex & 0xff) / 11;

        float focusX = (startX + x * deltaX) /
        imageView.getWidth();
        float focusY = (startY - y * deltaY) / imageView.getHeight();
        imageView.setZoom(1.5f, focusX, focusY);*/
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

    private void sendToMoonboard(byte[] holds)
    {
        byte[] data = new byte[holds.length*2];
        int i = 0;
        for(byte h : holds)
        {
            data[i++] = h;
            data[i++] = 0; // type 0
        }

        ((MoonboardApplication)getApplication()).getMoonboardCommunicationService()
                .send(MoonboardCommunicationService.MESSAGE_TYPE_SET_PROBLEM, data);
    }

    private void drawHolds(byte[] holds)
    {
        byte[] data = new byte[holds.length*2];
        int i = 0;
        for(byte h : holds)
        {
            data[i++] = h;
            data[i++] = 0; // type 0
        }

        Bitmap image = ProblemBitmap.get(getBaseContext(), backgroundImageId, data, false, false, 0);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(image);
    }
}
