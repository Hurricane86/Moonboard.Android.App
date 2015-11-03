package com.gio.martino.moonboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 * Created by Martino on 07/10/2015.
 */
public class ProblemUserDbRecord
{
    private int problemId           = 0;
    private int userGrade           = -1;
    private int vote                = 0;
    private int attempts            = 0;
    private long date               = 0;
    private boolean done            = false;
    private boolean wishlist        = false;

    private UserDbHelper dbHelper   = null;
    private boolean exist           = false;

    public ProblemUserDbRecord(UserDbHelper dbHelper, int problemId)
    {
        this.problemId = problemId;
        this.dbHelper = dbHelper;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(problemId) };

        Cursor c = db.query(
                ProblemInfo.ProblemEntry.TABLE_NAME,      // The table to query
                null,                                     // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        if(c.getCount() > 0)
        {
            c.moveToFirst();

            userGrade = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_USER_GRADE));
            vote = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_VOTE));
            done = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_DONE)) == 1;
            wishlist = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_DONE)) == 2;
            attempts = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_ATTEMPTS));
            date = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_DATE));

            exist = true;
        }

        db.close();
        c.close();
    }

    public int getProblemId()       { return problemId; }
    public int getUserGrade()       { return userGrade; }
    public int getVote()            { return vote; }
    public int getAttempts()        { return attempts; }
    public Date getDate()           { return new Date(date); }
    public boolean isDone()         { return done; }
    public boolean isInWishlist()   { return wishlist; }

    public void commit()
    {
        date = System.currentTimeMillis();

        createOrUpdateDbRecord();
    }

    public void setUserGrade(int userGrade)
    {
        this.userGrade = userGrade;
        //createOrUpdateDbRecord();
    }

    public void setVote(int vote)
    {
        this.vote = vote;
        //createOrUpdateDbRecord();
    }

    public void setIsDone(boolean done)
    {
        this.done = done;
        if(done)
            wishlist = false;
        //createOrUpdateDbRecord();
    }

    public void setIsInWishlist(boolean wishlist)
    {
        this.wishlist = wishlist;
        if(wishlist)
            done = false;
        //createOrUpdateDbRecord();
    }

    public void setAttempts(int attempts)
    {
        this.attempts = attempts;
        //createOrUpdateDbRecord();
    }

    private void createOrUpdateDbRecord()
    {
        ContentValues values = new ContentValues();
        values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_DONE, done ? 1 : (wishlist ? 2 : 0));
        values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_VOTE, vote);
        values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_USER_GRADE, userGrade);
        values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_ATTEMPTS, attempts);
        values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_DATE, date);

        if(!exist)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID, problemId);

            db.insert(
                    ProblemInfo.ProblemEntry.TABLE_NAME,
                    ProblemInfo.ProblemEntry.COLUMN_NAME_NULLABLE,
                    values);

            db.close();

            exist = true;
        }
        else
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Which row to update, based on the ID
            String selection = ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(problemId) };

            db.update(
                    ProblemInfo.ProblemEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);

            db.close();
        }
    }

}
