package com.gio.martino.moonboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Martino on 04/10/2015.
 */
public class UserDbAdapter {

    /*private UserDbHelper userDb = null;
    private Context context;

    UserDbAdapter(Context context)
    {
        this.context = context;
        userDb = new UserDbHelper(context);
    }

    public boolean isProblemDone(int problemId)
    {
        SQLiteDatabase db = userDb.getReadableDatabase();

        String[] projection = { ProblemInfo.ProblemEntry.COLUMN_NAME_DONE };
        String selection = ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(problemId) };

        Cursor c = db.query(
                ProblemInfo.ProblemEntry.TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        if(c.getCount() == 0) {
            db.close();
            return false;
        }

        c.moveToFirst();
        int isDone = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_DONE));

        db.close();

        return isDone == 1;
    }

    public int getProblemVote(int problemId)
    {
        SQLiteDatabase db = userDb.getReadableDatabase();

        String[] projection = { ProblemInfo.ProblemEntry.COLUMN_NAME_VOTE };
        String selection = ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(problemId) };

        Cursor c = db.query(
                ProblemInfo.ProblemEntry.TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        if(c.getCount() == 0) {
            db.close();
            return 0;
        }

        c.moveToFirst();
        int vote = c.getInt(c.getColumnIndexOrThrow(ProblemInfo.ProblemEntry.COLUMN_NAME_VOTE));

        db.close();

        return vote;
    }



    public void voteProblem(int problemId, int vote)
    {
        SQLiteDatabase db = userDb.getWritableDatabase();

        String[] projection = { ProblemInfo.ProblemEntry.COLUMN_NAME_DONE };
        String selection = ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(problemId) };

        Cursor c = db.query(
                ProblemInfo.ProblemEntry.TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        if(c.getCount() == 0)
        {
            ContentValues values = new ContentValues();
            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID, problemId);
            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_DONE, 0);
            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_VOTE, vote);

            db.insert(
                    ProblemInfo.ProblemEntry.TABLE_NAME,
                    ProblemInfo.ProblemEntry.COLUMN_NAME_NULLABLE,
                    values);
        }
        else
        {
            ContentValues values = new ContentValues();
            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_VOTE, vote);

            // Which row to update, based on the ID
            String selection2 = ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs2 = { String.valueOf(problemId) };

            db.update(
                    ProblemInfo.ProblemEntry.TABLE_NAME,
                    values,
                    selection2,
                    selectionArgs2);
        }

        db.close();
    }

    public void markProblemIntoDb(int problemId, boolean done)
    {
        SQLiteDatabase db = userDb.getWritableDatabase();

        String[] projection = { ProblemInfo.ProblemEntry.COLUMN_NAME_DONE };
        String selection = ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(problemId) };

        Cursor c = db.query(
                ProblemInfo.ProblemEntry.TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        if(c.getCount() == 0)
        {
            ContentValues values = new ContentValues();
            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID, problemId);
            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_DONE, done == true ? 1 : 0);
            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_VOTE, 0);

            db.insert(
                    ProblemInfo.ProblemEntry.TABLE_NAME,
                    ProblemInfo.ProblemEntry.COLUMN_NAME_NULLABLE,
                    values);
        }
        else
        {
            ContentValues values = new ContentValues();
            values.put(ProblemInfo.ProblemEntry.COLUMN_NAME_DONE, done == true ? 1 : 0);

            // Which row to update, based on the ID
            String selection2 = ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs2 = { String.valueOf(problemId) };

            db.update(
                    ProblemInfo.ProblemEntry.TABLE_NAME,
                    values,
                    selection2,
                    selectionArgs2);
        }

        db.close();
    }

    public void problemToCircuit(int problemId, String circuitName, boolean add)
    {

    }

    public void problemToWishlist(int problemId, boolean add)
    {

    }

    public Cursor getCircuitProblems(String circuitName)
    {
        return null;
    }

    public Cursor getWishlistProblems()
    {
        return null;
    }

    public boolean isProblemInWishList(int problemId)
    {
        return false;
    }*/
}
