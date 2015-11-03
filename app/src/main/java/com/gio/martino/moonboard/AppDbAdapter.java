package com.gio.martino.moonboard;

/**
 * Created by Martino on 23/09/2015.
 */
import java.io.IOException;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AppDbAdapter
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private AppDbHelper mDbHelper;

    public AppDbAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new AppDbHelper(mContext);
    }

    public AppDbAdapter createDatabase() throws SQLException
    {
        try
        {
            mDbHelper.createDataBase();
        }
        catch (IOException mIOException)
        {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public AppDbAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }

    public Cursor getAuthors()
    {
        try
        {
            String sql ="SELECT author FROM problems GROUP BY author";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getAuthors >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor getProblems(int holdsType, int holdsSetup, int fromGrade, int toGrade, String author)
    {
        try
        {
            String sql ="SELECT * FROM problems WHERE " +
                    "holdsType LIKE '" + holdsType +"' AND " +
                    "holdsSetup LIKE '" + holdsSetup +"' AND " +
                    "grade >= " + fromGrade +" AND " +
                    "grade <= " + toGrade;

            if(author != null && !author.isEmpty())
            {
                sql += " AND author LIKE '" + author + "'";
            }

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getProblems >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }
}
