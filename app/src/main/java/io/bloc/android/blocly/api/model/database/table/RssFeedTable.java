package io.bloc.android.blocly.api.model.database.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by namlu on 01-Sep-16.
 */
public class RssFeedTable extends Table {

    // Implementing the Builder class defined in Table.Builder
    public static class Builder implements Table.Builder {

        // each instance of Builder will have its own copy of ContentValues after instantiation
        ContentValues values = new ContentValues();

        // It's best practice to make each method in a Builder-style class return itself
        public Builder setSiteURL(String siteURL) {
            values.put(COLUMN_LINK, siteURL);
            return this;
        }

        public Builder setFeedURL(String feedURL) {
            values.put(COLUMN_FEED_URL, feedURL);
            return this;
        }

        public Builder setTitle(String title) {
            values.put(COLUMN_TITLE, title);
            return this;
        }

        public Builder setDescription(String description) {
            values.put(COLUMN_DESCRIPTION, description);
            return this;
        }

        // .insert() returns the row ID of newly inserted row, or -1 if error occurred
        @Override
        public long insert(SQLiteDatabase writableDB) {
            return writableDB.insert(NAME, null, values);
        }
    }

    /*
     * 54: get methods that help decouple Table and its subclasses.
     * Inquiries will be made by Table and returned as raw data, which will be made into model
     *      objects.
     */
    public static String getTitle(Cursor cursor) {
        return getString(cursor, COLUMN_TITLE);
    }

    public static String getDescription(Cursor cursor) {
        return getString(cursor, COLUMN_DESCRIPTION);
    }

    public static String getSiteURL(Cursor cursor) {
        return getString(cursor, COLUMN_LINK);
    }

    public static String getFeedUrl(Cursor cursor) {
        return getString(cursor, COLUMN_FEED_URL);
    }

    private static final String NAME = "rss_feeds";

    private static final String COLUMN_LINK = "link";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_FEED_URL = "feed_url";

    @Override
    public String getName() {
        return "rss_feeds";
    }

    // "id" is initialized as an INTEGER type and specified as the Primary Key
    // Providing a Primary Key column is required for SQLite tables.
    @Override
    public String getCreateStatement() {
        return "CREATE TABLE " + getName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_LINK + " TEXT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_FEED_URL + " TEXT)";
    }
}
