package mci.uni.stuttgart.bilget.database;

import android.provider.BaseColumns;

/**
 * Beacon information saved into database
 * @author Hanwen
 *
 */
public class BeaconLocationData {
	
	public BeaconLocationData(){
		
	}
	//base column include the _count and _id property.
	public static abstract class LocationEntry implements BaseColumns{
		public static final String TABLE_NAME = "entry";
		
		public static final String COLUMN_NAME_ENTRY_ID = "entryid";
		public static final String COLUNM_NAME_DEVICE_TITLE = "name";
		public static final String COLUMN_NAME_LOCATION = "location";
		public static final String COLUMN_NAME_MACADDRESS = "macaddress";
		public static final String COLUMN_NAME_UUID = "uuid";
		public static final String COLUMN_NAME_CATEGORY = "category";
		public static final String COLUMN_NAME_SUBCATEGORY = "subcategory";
		public static final String COLUMN_NAME_LABEL = "label";
		public static final String COLUNM_NAME_DESCRIPTION = "description";
		
		public static final String COLUMN_NAME_NULL = "empty";
	}
}
