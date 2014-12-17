package mci.uni.stuttgart.bilget.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import mci.uni.stuttgart.bilget.database.BeaconLocationData.LocationEntry;

public class BeaconDBHelper extends SQLiteOpenHelper {
	
	public static final int DATABASE_VERSION = 2;
	private static final String TAG = "DB open helper";
	public static final String DATABASE_NAME = "BeaconLocation.db";
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SPACE = " ";
	private static final String SINGLE_QUOTE = "'";
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " 
				+ LocationEntry.TABLE_NAME
//				+ " (" + LocationEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP 
//				+ LocationEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP
				+ " ("  + LocationEntry.COLUMN_NAME_MACADDRESS + TEXT_TYPE + " PRIMARY KEY" +  COMMA_SEP 
//				+ LocationEntry.COLUNM_NAME_DEVICE_TITLE + TEXT_TYPE + COMMA_SEP 
				+ LocationEntry.COLUMN_NAME_CATEGORY + TEXT_TYPE + COMMA_SEP 
//				+ LocationEntry.COLUMN_NAME_UUID + TEXT_TYPE + COMMA_SEP 
				+ LocationEntry.COLUMN_NAME_SUBCATEGORY + TEXT_TYPE + COMMA_SEP
				+ LocationEntry.COLUNM_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP
				+ LocationEntry.COLUMN_NAME_LABEL + TEXT_TYPE
				+ " )";
	
	public BeaconDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);//the third parameter is factory.
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//drop the database table if the one existed.
		db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
		db.execSQL(SQL_CREATE_ENTRIES);
		//init test value
		db.execSQL("INSERT INTO " + LocationEntry.TABLE_NAME 
				+ " ( " + LocationEntry.COLUMN_NAME_MACADDRESS + COMMA_SEP 
				+ LocationEntry.COLUMN_NAME_CATEGORY + COMMA_SEP
				+ LocationEntry.COLUMN_NAME_SUBCATEGORY + COMMA_SEP
				+LocationEntry.COLUMN_NAME_LABEL + COMMA_SEP
				+ LocationEntry.COLUNM_NAME_DESCRIPTION  + ") "
				+ " VALUES ( 'E7D38F1CF82E', 'indoor','desk','hanwen''s desk','my beautiful desk!')");
//		long firstRowId = DatabaseUtil.insertData(this, hanwensHome);
//		Log.d(TAG, firstRowId + " is inserted and the table is initialed");
	} 

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
		Log.w(TAG, "data base upgraded, table is cleared");
	}

	@Override
	public void onOpen(SQLiteDatabase db) {

		String count  = "select count(*) from " + LocationEntry.TABLE_NAME;
		Cursor mcursor = db.rawQuery(count, null);
		mcursor.moveToFirst();
		int icount = mcursor.getCount();
		Log.d(TAG, "total row number in database is" + icount);
	}
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onDowngrade(db, oldVersion, newVersion);
	}
}
