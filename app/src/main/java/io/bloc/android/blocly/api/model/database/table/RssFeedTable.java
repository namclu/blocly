package io.bloc.android.blocly.api.model.database.table;

/**
 * Created by namlu on 01-Sep-16.
 */
public class RssFeedTable extends Table {

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
        return "CREATE_TABLE" + getName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_LINK + " TEXT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_FEED_URL + " TEXT)";
    }

    public static String getColumnLink() {
        return COLUMN_LINK;
    }

    public static String getColumnTitle() {
        return COLUMN_TITLE;
    }

    public static String getColumnDescription() {
        return COLUMN_DESCRIPTION;
    }

    public static String getColumnFeedUrl() {
        return COLUMN_FEED_URL;
    }
}
