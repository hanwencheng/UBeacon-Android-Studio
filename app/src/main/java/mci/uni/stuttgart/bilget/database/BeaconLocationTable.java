package mci.uni.stuttgart.bilget.database;

import android.provider.BaseColumns;

/**
 * Beacon information saved into database
 * @author Hanwen
 *
 */
public class BeaconLocationTable {
	
	public BeaconLocationTable(){
		
	}
	//base column include the _count and _id property.
	public static abstract class LocationEntry implements BaseColumns{
		public static final String TABLE_NAME = "entry";
		
		public static final String COLUMN_NAME_MACADDRESS = "macaddress";
		public static final String COLUMN_NAME_CATEGORY = "category";
		public static final String COLUMN_NAME_SUBCATEGORY = "subcategory";
		public static final String COLUMN_NAME_LABEL = "label";
		public static final String COLUNM_NAME_DESCRIPTION = "description";
		
		public static final String COLUMN_NAME_NULL = "empty";
	}
}
