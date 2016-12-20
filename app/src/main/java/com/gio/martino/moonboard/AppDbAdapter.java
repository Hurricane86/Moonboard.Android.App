package com.gio.martino.moonboard;

/**
 * Created by Martino on 23/09/2015.
 */
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AppDbAdapter
{
    protected static final String TAG = "DataAdapter";

    private SQLiteDatabase mDb;
    private AppDbHelper mDbHelper;

    public AppDbAdapter(Context context)
    {
        mDbHelper = new AppDbHelper(context);
    }

    public AppDbAdapter createDatabase()
    {
        mDbHelper.createDataBase();
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
            String sql ="SELECT author FROM problems GROUP BY author ORDER BY author ASC";

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

    public int getLastProblemId()
    {
        String sql = "SELECT id FROM problems ORDER BY id DESC LIMIT 1";
        Cursor mCur = mDb.rawQuery(sql, null);
        if (mCur!=null)
        {
            mCur.moveToNext();
            int result = mCur.getInt(0);
            mCur.close();

            return result;
        }

        return 0;
    }

    public Cursor getProblems(int holdsType, int holdsSetup, int fromGrade, int toGrade, String author)
    {
        try
        {
            String sql = "SELECT * FROM problems WHERE " +
                    "holdsType LIKE '" + holdsType +"' AND " +
                    "holdsSetup LIKE '" + holdsSetup +"' AND " +
                    "grade >= " + fromGrade +" AND " +
                    "grade <= " + toGrade;

            if(author != null && !author.isEmpty())
            {
                sql += " AND author LIKE '" + author + "'";
            }

            sql += " ORDER BY grade ASC";

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

    public boolean insertProblem(int id, String name, String author, int grade, int holdType, int holdSetup, String holds)
    {
        String sql = "INSERT INTO problems VALUES(" +
                Integer.toString(id) + ", " +
                "\"" + name + "\", " +
                Integer.toString(holdType) + ", " +
                Integer.toString(holdSetup) + ", " +
                "\"" + author + "\", " +
                Integer.toString(grade) + ", " +
                holds +
                ")";

        mDb.execSQL(sql);

        return true;
    }
}
