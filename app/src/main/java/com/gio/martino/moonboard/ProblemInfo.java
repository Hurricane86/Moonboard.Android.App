package com.gio.martino.moonboard;

import android.provider.BaseColumns;

/**
 * Created by Martino on 29/09/2015.
 */
public final class ProblemInfo {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ProblemInfo() {}

    /* Inner class that defines the table contents */
    public static abstract class ProblemEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_DONE = "done";
        public static final String COLUMN_NAME_VOTE = "vote";
        public static final String COLUMN_NAME_WISHLIST = "wishlist";
        public static final String COLUMN_NAME_USER_GRADE = "user_grade";
        public static final String COLUMN_NAME_ATTEMPTS = "attempts";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_NULLABLE = "null";
    }
}
