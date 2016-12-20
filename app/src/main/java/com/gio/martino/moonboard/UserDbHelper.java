package com.gio.martino.moonboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Martino on 29/09/2015.
 */
public class UserDbHelper extends SQLiteOpenHelper
{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "ProblemsDone.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String BOOLEAN_TYPE = " INTEGER";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProblemInfo.ProblemEntry.TABLE_NAME + " (" +
                    ProblemInfo.ProblemEntry._ID + " INTEGER PRIMARY KEY," +
                    ProblemInfo.ProblemEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    ProblemInfo.ProblemEntry.COLUMN_NAME_DONE + BOOLEAN_TYPE + COMMA_SEP +
                    ProblemInfo.ProblemEntry.COLUMN_NAME_VOTE + INTEGER_TYPE +" )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProblemInfo.ProblemEntry.TABLE_NAME;

    private Context context;

    public UserDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        makeBackup(oldVersion);

        if(oldVersion == 1 && newVersion == 2)
        {
            db.execSQL("ALTER TABLE entry ADD COLUMN wishlist INTEGER");
            db.execSQL("ALTER TABLE entry ADD COLUMN user_grade INTEGER");
        }
        else if(oldVersion == 2 && newVersion == 3)
        {
            db.execSQL("UPDATE entry SET user_grade = -1");
        }
        else if(oldVersion == 3 && newVersion == 4)
        {
            db.execSQL("ALTER TABLE entry ADD COLUMN attempts INTEGER");
            db.execSQL("ALTER TABLE entry ADD COLUMN date INTEGER");
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void makeBackup(int version)
    {
        File currentDB = context.getApplicationContext().getDatabasePath("ProblemsDone.db");
        File backupDB = new File(context.getApplicationInfo().dataDir + "/databases/", "MoonboardUserDb_v"+version+".sqlite");

        if (currentDB.exists())
        {
            try
            {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();

                dst.transferFrom(src, 0, src.size());

                src.close();
                dst.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
