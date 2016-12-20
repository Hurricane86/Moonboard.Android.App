package com.gio.martino.moonboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;

/**
 * Created by Martino on 23/04/2016.
 */
public class ProblemBitmap
{
    private static Bitmap cachedBitmap = null;
    private static int currBackgroundImageId = -1;

    public static Bitmap get(Context context, int backgroundImageId, byte[] holds, boolean done, boolean wishlist, int vote)
    {
        if(cachedBitmap == null || currBackgroundImageId != backgroundImageId)
        {
            BitmapFactory.Options myOptions = new BitmapFactory.Options();
            myOptions.inDither = true;
            myOptions.inScaled = false;
            myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            //myOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), backgroundImageId);
            cachedBitmap = Bitmap.createBitmap(bitmap);
            currBackgroundImageId = backgroundImageId;
        }

        Bitmap mutableBitmap = cachedBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        //paint.setColor(Color.RED);
        paint.setAlpha(60);

        Canvas canvas = new Canvas(mutableBitmap);

        float scaleX = 2.62f; //@TODO: should be calculated based on screen resolution
        float scaleY = 2.62f; //@TODO: should be calculated based on screen resolution

        float startX = 94 * scaleX;
        float startY = 936 * scaleY;
        float deltaX = 50 * scaleX;
        float deltaY = 50 * scaleY;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int color_0 = settings.getInt("normal_hold_color", SettingsActivity.DEFAULT_NORMAL_HOLD_COLOR);
        int color_1 = settings.getInt("start_hold_color", SettingsActivity.DEFAULT_START_HOLD_COLOR);
        int color_2 = settings.getInt("top_hold_color", SettingsActivity.DEFAULT_NORMAL_HOLD_COLOR);

        for(int i = 0; i < holds.length / 2; ++i)
        {
            int holdIndex = holds[i*2] & 0xFF;
            int holdType = holds[i*2+1] & 0xFF;

            int x = holdIndex % 11;
            int y = holdIndex / 11;

            float px = startX + x * deltaX;
            float py = startY - y * deltaY;

            switch(holdType)
            {
                case 0:
                    paint.setColor(Color.argb(Color.alpha(color_0),
                            Color.red(color_0),
                            Color.green(color_0),
                            Color.blue(color_0)));
                    break;
                case 1:
                    paint.setColor(Color.argb(Color.alpha(color_1),
                            Color.red(color_1),
                            Color.green(color_1),
                            Color.blue(color_1)));
                    break;
                case 2:
                    paint.setColor(Color.argb(Color.alpha(color_2),
                            Color.red(color_2),
                            Color.green(color_2),
                            Color.blue(color_2)));
                    break;
            }

            paint.setAlpha(60);
            canvas.drawCircle(px, py, 50, paint);
        }

        if(done)
        {
            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.done);
            canvas.drawBitmap(b, 750, 1550, paint);
        }
        else if(wishlist)
        {
            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.wishlist);
            canvas.drawBitmap(b, 300, 700, paint);
        }

        if(vote != 0)
        {
            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.star);
            paint.setAlpha(120);

            for(int i = 0; i < vote; ++i)
                canvas.drawBitmap(b, b.getWidth() * i, 1800, paint);
        }

        return mutableBitmap;
    }
}
