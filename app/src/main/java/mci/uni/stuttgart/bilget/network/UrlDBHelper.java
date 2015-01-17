package mci.uni.stuttgart.bilget.network;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Hanwen on 1/9/2015.
 */
public class UrlDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    private static final String TAG = "URL DB open helper";
    public static final String DATABASE_NAME = "URL.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SPACE = " ";
    private static final String SINGLE_QUOTE = "'";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE ";

    public UrlDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
