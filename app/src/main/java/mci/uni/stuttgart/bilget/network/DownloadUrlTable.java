package mci.uni.stuttgart.bilget.network;

import android.provider.BaseColumns;

/**
 * This url table is mainly reserved for further extend function.
 * Created by Hanwen on 1/17/2015.
 */
public class DownloadUrlTable {

    public DownloadUrlTable(){
    }

    //base column include the _count and _id property.
    public static abstract class URLEntry implements BaseColumns {
        public static final String TABLE_NAME = "url";

        public static final String COLUNM_NAME_URL = "urlname";

        public static final String COLUMN_NAME_NULL = "empty";
    }
}
