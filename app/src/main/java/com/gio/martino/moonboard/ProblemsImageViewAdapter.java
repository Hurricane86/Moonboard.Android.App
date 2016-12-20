package com.gio.martino.moonboard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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

        return ProblemBitmap.get(context, backgroundImageId, holds, done, wishlist, vote);
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
