package com.gio.martino.moonboard;

/**
 * Created by Martino on 23/09/2015.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AppDbHelper extends SQLiteOpenHelper
{
    private static String TAG = "AppDbHelper"; // Tag just for the LogCat window
    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private static String DB_NAME ="moonboardDB.sqlite";// Database name
    private static int DB_VERSION = 9;
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    public AppDbHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);

        if(android.os.Build.VERSION.SDK_INT >= 17)
        {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }
        else
        {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    public void createDataBase()
    {
        //If database not exists copy it from the assets

        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist)
        {
            this.getReadableDatabase();
            this.close();
            try
            {
                //Copy the database from assets
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private static int getDbVersionFromFile(File file)
    {
        try
        {
            RandomAccessFile fp = new RandomAccessFile(file,"r");
            fp.seek(60);
            byte[] buff = new byte[4];
            fp.read(buff, 0, 4);
            return ByteBuffer.wrap(buff).getInt();
        }
        catch (IOException e)
        {
            return -1;
        }
    }

    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDataBase()
    {
        File dbFile = new File(DB_PATH + DB_NAME);
        Log.v("dbFile", dbFile + "   "+ dbFile.exists());

        if(!dbFile.exists())
            return false;

        int currentVersion = getDbVersionFromFile(dbFile);
        if(currentVersion != DB_VERSION)
            return false;

        return true;
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException
    {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //Open the database, so we can query it
    public boolean openDataBase() throws SQLException
    {
        String mPath = DB_PATH + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close()
    {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
