package com.gio.martino.moonboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class SetupWizardActivity extends Activity {

    private int backgroundImageId = R.drawable.setup_1_1;
    String[] holdsStrs;
    byte[] holds;
    int currentHoldIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    nextHold();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
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

        char firstChar = holdLetter.charAt(0);
        int value = firstChar - 'A';

        int value2 = Integer.decode(holdLetter.substring(1).split(" ")[0]);

        return value + (value2 - 1) * 11; // 11 = K
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

        ((MoonboardApplication)getApplication()).getMoonboardCommunicationService().send(MoonboardCommunicationService.MESSAGE_TYPE_SET_PROBLEM, data);
    }

    private void drawHolds(byte[] holds)
    {
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //myOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), backgroundImageId);
        Bitmap cachedBitmap = Bitmap.createBitmap(bitmap);

        Bitmap mutableBitmap = cachedBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setAlpha(60);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        //int w = imageView.getMeasuredWidth();
        //int h = imageView.getMeasuredHeight();

        float scaleX = 2;//(float)bitmap.getWidth() / (float)w;
        float scaleY = 2;//(float)bitmap.getHeight() / (float)h;

        float startX = 94 * scaleX;
        float startY = 936 * scaleY;
        float deltaX = 50 * scaleX;
        float deltaY = 50 * scaleY;

        for(byte hold : holds)
        {
            int x = (hold & 0xff) % 11;
            int y = (hold & 0xff) / 11;

            float px = startX + x * deltaX;
            float py = startY - y * deltaY;

            canvas.drawCircle(px, py, 50, paint);
        }

        imageView.setImageBitmap(mutableBitmap);
    }
}
