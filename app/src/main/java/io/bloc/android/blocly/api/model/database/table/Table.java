package io.bloc.android.blocly.api.model.database.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by namlu on 01-Sep-16.
 */
public abstract class Table {

    // Each table must possess an id column
    protected static final String COLUMN_ID = "id";

    // Each Table class must provide a name and a createStatement
    public abstract String getName();

    public abstract String getCreateStatement();

    // Each Table is responsible for executing its own upgrades
    public void onUpgrade(SQLiteDatabase writableDatabase, int oldVersion, int newVersion) {
        // Nothing
    }
}
