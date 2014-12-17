package mci.uni.stuttgart.bilget.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import mci.uni.stuttgart.bilget.database.BeaconLocationData.LocationEntry;

public class DatabaseUtil {
	private static final String SPACE = " ";
	private static final String SINGLE_QUOTE = "'";
	private static final String TAG = "Database Util";
	
	protected static long insertData(BeaconDBHelper mDbHelper, LocationInfo locationInfo){
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
//		values.put(LocationEntry.COLUMN_NAME_CATEGORY, 1);
		values.put(LocationEntry.COLUMN_NAME_MACADDRESS, locationInfo.MACAddress);
		values.put(LocationEntry.COLUMN_NAME_CATEGORY, locationInfo.category);
		values.put(LocationEntry.COLUMN_NAME_SUBCATEGORY, locationInfo.subcategory);
		values.put(LocationEntry.COLUMN_NAME_LABEL, locationInfo.label);
		values.put(LocationEntry.COLUNM_NAME_DESCRIPTION, locationInfo.description);
		
		long newRowId;
		newRowId = db.insertWithOnConflict(LocationEntry.TABLE_NAME,
				null,
//				LocationEntry.COLUMN_NAME_NULL, 
				values,
				SQLiteDatabase.CONFLICT_REPLACE);
		return newRowId;
	}

	protected static LocationInfo queryData(BeaconDBHelper mDbHelper, String queryColumnValue, String queryColumnName){
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		if(queryColumnName == null)
			queryColumnName = LocationEntry.COLUMN_NAME_MACADDRESS;
		
		//because we are using a custom cursor, this may be set to null TODO performance to be test
		String[] projection = {
			LocationEntry.COLUMN_NAME_MACADDRESS,
			LocationEntry.COLUMN_NAME_CATEGORY,
			LocationEntry.COLUMN_NAME_SUBCATEGORY,
			LocationEntry.COLUMN_NAME_LABEL,
			LocationEntry.COLUNM_NAME_DESCRIPTION,
		};
//		String [] projection = null;
		String selection = SPACE + queryColumnName  +  " = " + SINGLE_QUOTE + queryColumnValue + SINGLE_QUOTE;
//					+ SINGLE_QUOTE + "?" + SINGLE_QUOTE;//TODO no need for "where clause"
//		String sortOrder = LocationEntry._ID + " DESC"; TODO
		String sortOrder = null;
		
		Cursor cursor = db.query(
				LocationEntry.TABLE_NAME,
				projection, 
				selection, 
				null, 
				null,
				null, 
				sortOrder);
		Log.d(TAG,"get the cursor" + DatabaseUtils.dumpCursorToString(cursor) + cursor.getCount());
		LocationCursor locationCursor =  new LocationCursor(cursor);
		locationCursor.moveToFirst();
		LocationInfo location = null;
		if(!locationCursor.isAfterLast()){
			location = locationCursor.getLocationInfo();
//			locationCursor.moveToNext();//to be used if we want to get a group of information
		}
		locationCursor.close();
		return location;
	}

	/**
	 *  here using selectionArgs and selection instead of only using selection;
	 * @param mDbHelper
	 * @param deleteColumnName
	 * @param deleteColumnValue
	 * @return the affected row number
	 */
	protected static int deleteData(BeaconDBHelper mDbHelper, String deleteColumnName, String deleteColumnValue){
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// Define 'where' part of query.
		String selection = deleteColumnName + " LIKE ?";
		// Specify arguments in placeholder order.
		String[] selectionArgs = { deleteColumnValue };
		// Issue SQL statement.
		return db.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
	}
	
	/**
	 * here only using selection;
	 * @param mDbHelper
	 * @param queryMacAddress
	 * @param updateColumnName
	 * @param updateColumnValue
	 * @return the affected row number
	 */
	protected static int updateData(BeaconDBHelper mDbHelper, String queryMacAddress, 
			String updateColumnName, String updateColumnValue){
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
	
		// New value for one column
		ContentValues values = new ContentValues();
		values.put(updateColumnName, updateColumnValue);
	
		// Which row to update, based on the ID
		String selection = "WHERE" + SPACE +  updateColumnName + " = " 
				+ SINGLE_QUOTE + queryMacAddress + SINGLE_QUOTE;
		String[] selectionArgs = null;
	
		int count = db.update(
		    LocationEntry.TABLE_NAME,
		    values,
		    selection,
		    selectionArgs);
		return count;
	}
	
	private static class LocationCursor extends CursorWrapper{

		public LocationCursor(Cursor cursor) {
			super(cursor);
			// TODO Auto-generated constructor stub
		}
		
		public LocationInfo getLocationInfo(){
			if(isBeforeFirst() || isAfterLast()){
				return null;
			}
			String category = getString(getColumnIndex(LocationEntry.COLUMN_NAME_CATEGORY));
			String subcategory = getString(getColumnIndex(LocationEntry.COLUMN_NAME_SUBCATEGORY));
			String label = getString(getColumnIndex(LocationEntry.COLUMN_NAME_LABEL));
			String description = getString(getColumnIndex(LocationEntry.COLUNM_NAME_DESCRIPTION));
			String macAddres = getString(getColumnIndex(LocationEntry.COLUMN_NAME_MACADDRESS));
			LocationInfo locationInfo = new LocationInfo(macAddres, category, subcategory, label, description);
			Log.d(TAG,"query result is" + locationInfo);
			return locationInfo;
		}
	};

}
