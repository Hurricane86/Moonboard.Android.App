package com.gio.martino.moonboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Martino on 04/10/2015.
 */
public class ProblemsImageViewAdapter extends PagerAdapter
{
    private Context context;
    private Cursor problemsCursor = null;
    private Bitmap cachedBitmap = null;
    private int backgroundImageId = 0;
    private UserDbHelper userDbHelper;
    SparseArray<View> views = new SparseArray<>();

    ProblemsImageViewAdapter(Context context, Intent intent)
    {
        this.context = context;

        int holdsType = intent.getIntExtra("holdsType", 0);
        int holdsSetup = intent.getIntExtra("holdsSetup", 0);
        int fromGrade = intent.getIntExtra("fromGrade", 0);
        int toGrade = intent.getIntExtra("toGrade", 0);
        String author = intent.getStringExtra("author");

        AppDbAdapter db = new AppDbAdapter(context.getApplicationContext());
        db.createDatabase();
        db.open();

        userDbHelper = new UserDbHelper(context.getApplicationContext());

        problemsCursor = db.getProblems(holdsType, holdsSetup, fromGrade, toGrade, author);

        db.close();

        backgroundImageId = context.getResources().getIdentifier("setup_" + (holdsType+1) + "_" + (holdsSetup+1), "drawable", context.getPackageName());
    }

    @Override
    public int getCount()
    {
        return problemsCursor.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == object;
    }

    private Bitmap getProblemImage(int position)
    {
        problemsCursor.moveToPosition(position);

        int currentProblemId = problemsCursor.getInt(problemsCursor.getColumnIndex("id"));
        byte[] holds = problemsCursor.getBlob(problemsCursor.getColumnIndex("holds"));

        ProblemUserDbRecord problemUserDbRecord = new ProblemUserDbRecord(userDbHelper, currentProblemId);

        int vote = problemUserDbRecord.getVote();
        boolean done = problemUserDbRecord.isDone();
        boolean wishlist = problemUserDbRecord.isInWishlist();

        return getProblemImage(holds, done, wishlist, vote);
    }

    private Bitmap getProblemImage(byte[] holds, boolean done, boolean wishlist, int vote)
    {
        if(cachedBitmap == null)
        {
            BitmapFactory.Options myOptions = new BitmapFactory.Options();
            myOptions.inDither = true;
            myOptions.inScaled = false;
            myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            //myOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), backgroundImageId);
            cachedBitmap = Bitmap.createBitmap(bitmap);
        }

        Bitmap mutableBitmap = cachedBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        //paint.setColor(Color.RED);
        paint.setAlpha(60);

        Canvas canvas = new Canvas(mutableBitmap);

        float scaleX = 2;
        float scaleY = 2;

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

    public void updateView(int position)
    {
        ImageView imageView = (ImageView)views.get(position);
        if(imageView != null)
        {
            Bitmap image = getProblemImage(position);
            imageView.setImageBitmap(image);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        Bitmap image = getProblemImage(position);

        ImageView imageView = new ImageView(context);
        int padding = 0;//context.getResources().getDimensionPixelSize(R.dimen.padding_medium);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageBitmap(image);
        container.addView(imageView);
        views.put(position, imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView((ImageView) object);
        views.remove(position);
    }
}
