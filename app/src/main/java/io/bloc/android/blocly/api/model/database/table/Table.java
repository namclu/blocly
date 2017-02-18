package io.bloc.android.blocly.api.model.database.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by namlu on 01-Sep-16.
 */
public abstract class Table {

    // Establish a Builder pattern for the class
    public static interface Builder {
        // each class implementing Builder must be capable of inserting its data into a
        //      SQLiteDatabase object and return the row identifier of the new object
        public long insert(SQLiteDatabase writableDB);
    }

    // Each table must possess an id column
    protected static final String COLUMN_ID = "id";

    // Each Table class must provide a name and a createStatement
    public abstract String getName();

    public abstract String getCreateStatement();

    // Each Table is responsible for executing its own upgrades
    public void onUpgrade(SQLiteDatabase writableDatabase, int oldVersion, int newVersion) {
        // Nothing
    }

    // 54: Returns a Cursor object, which points to a specific row for the given rowId
    public Cursor fetchRow(SQLiteDatabase readOnlyDatabase, long rowId) {
        // query(boolean distinct, String table, String[] columns, String selection,
        //      String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
        return readOnlyDatabase.query(true, getName(), null, COLUMN_ID + " = ?",
                new String[] {String.valueOf(rowId)}, null, null, null, null);
    }

    // 55: Convenience method to get the rowId
    public static long getRowId(Cursor cursor) {
        return getLong(cursor, COLUMN_ID);
    }

    // 54: Returns a String object for the specified column parameter
    protected static String getString(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);

        // Verify whether the column is present in the Cursor before recovering its respective data
        if (columnIndex == -1) {
            return "";
        }
        return cursor.getString(columnIndex);
    }

    // 54: Returns a Long object for the specified column parameter
    protected static long getLong(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);

        // Verify whether the column is present in the Cursor before recovering its respective data
        if (columnIndex == -1) {
            return -1l;
        }
        return cursor.getLong(columnIndex);
    }

    // 54: Databases store boolean values as 1 for true, 0 for false
    // Returns a 1 (true) if the integer associated with the column is equal to 1
    protected static boolean getBoolean(Cursor cursor, String column) {
        return getLong(cursor, column) == 1l;
    }
}
