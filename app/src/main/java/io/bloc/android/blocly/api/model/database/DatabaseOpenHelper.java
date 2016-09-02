package io.bloc.android.blocly.api.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.bloc.android.blocly.api.model.database.table.Table;

/**
 * Created by namlu on 01-Sep-16.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    // Database name and version
    // Applications are allowed to use multiple databases
    private static final String NAME = "blocly_db";

    private static final int VERSION = 1;

    private Table[] tables;

    public DatabaseOpenHelper(Context context, Table... tables) {

        // Pass both the version and name of the database to the super constructor
        // SQLiteOpenHelper will compare the VERSION variable to the version stored
        // in the database; if they differ, an upgrade occurs
        super(context, NAME, null, VERSION);
        this.tables = tables;
    }

    // onCreate() happens the first time we open the database
    // Iterate over all Table objects and execute their Create statements
    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Table table : tables) {
            // execSQL() ...
            db.execSQL(table.getCreateStatement());
        }
    }

    // onUpgrade() happens when an upgrade is triggered
    // Iterate over our Table objects and invoke the appropriate upgrade method
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Table table : tables) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }
}
