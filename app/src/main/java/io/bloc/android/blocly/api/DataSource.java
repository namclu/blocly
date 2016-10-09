package io.bloc.android.blocly.api;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.BuildConfig;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.model.database.DatabaseOpenHelper;
import io.bloc.android.blocly.api.model.database.table.RssFeedTable;
import io.bloc.android.blocly.api.model.database.table.RssItemTable;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;

/**
 * Created by namlu on 18-Jun-16.
 *
 * DataSource maintains a list of all the feeds and items in our application.
 * With a single class responsible for data, the remainder of our application will
 * never have to create or destroy data.
 */
public class DataSource {

    // Fields needed for Database
    private DatabaseOpenHelper databaseOpenHelper;
    private RssFeedTable rssFeedTable;
    private RssItemTable rssItemTable;

    private List<RssFeed> feeds;
    private List<RssItem> items;

    public DataSource(){
        // Database tables created
        rssFeedTable = new RssFeedTable();
        rssItemTable = new RssItemTable();

        // Both Table fields are kept w/in DataSource and act as primary access points for models
        // .getSharedInstance() returns an instance of BloclyApplication
        // DatabaseOpenHelper(Context context, Table... tables)
        databaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(),
                rssFeedTable, rssItemTable);

        feeds = new ArrayList<RssFeed>();
        items = new ArrayList<RssItem>();
        // createFakeData();

        // Test the RSS feed request
        // We don't want to block the interface from responding when we make our
        // network request, therefore we place it in a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses;

                // DEBUG property is used to decide whether or not to delete the existing database
                // The 'false' inside of if() ensures database is not lost each time app is launched
                // Switch it to 'true' after modifying the database schema in any way
                if (BuildConfig.DEBUG && false) {
                    BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
                }

                // Until getWritableDatabase() is invoked, our database will not be created nor opened
                // getReadableDatabase() may also be used, however this method will not upgrade
                //      the database even if the versions are mismatched
                SQLiteDatabase writableDatabase = databaseOpenHelper.getWritableDatabase();

                feedResponses = new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml")
                        .performRequest();

                // Store the FeedResponse objects into appropriate SQLite db field
                for (GetFeedsNetworkRequest.FeedResponse feedResponse: feedResponses) {

                    // ContentValues - used to store a set of values that the ContentResolver can process
                    ContentValues feedValues = new ContentValues();

                    // .put(String key, String value)
                    feedValues.put(RssFeedTable.getColumnLink(), feedResponse.channelURL);
                    feedValues.put(RssFeedTable.getColumnTitle(), feedResponse.channelTitle);
                    feedValues.put(RssFeedTable.getColumnDescription(), feedResponse.channelDescription);
                    // Identifies a unique RssFeed table value
                    feedValues.put(RssFeedTable.getColumnFeedUrl(), feedResponse.channelFeedURL);

                    // Set up checks for handling duplicate RssFeed entries
                    long feedId = -1;

                    // Cursor provides access to result set returned by database query
                    // .query() returns a Cursor object, which represents the result of a query,
                    //      which points to one row of the query result.
                    // Cursor query(String table, String[] columns, String selection,
                    //      String[] selectionArgs, String groupBy, String having, String orderBy)
                    Cursor cursor = writableDatabase.query(rssFeedTable.getName(), null, RssFeedTable.getColumnFeedUrl() + " = ?",
                            new String [] {feedResponse.channelFeedURL}, null, null, null);

                    // If cursor contains an RssFeed table object, get the index value for that object
                    if (cursor.moveToFirst()) {
                        // getColumnIndex(String columnName) returns zero-based index for given
                        //      column name, or -1 if column doesn't exist.
                        int index = cursor.getColumnIndex("id");
                        feedId = cursor.getLong(index);
                    }

                    // If feed URL already exists, then .update() database, else .insert() database
                    if (feedId >= 0) {
                        // int update(String table, ContentValues values, String whereClause, String[] whereArgs)
                        writableDatabase.update(rssFeedTable.getName(), feedValues, "id = ?", new String [] {Long.toString(feedId)});
                    } else {
                        // long insert(String table, String nullColumnHack, ContentValues values)
                        writableDatabase.insert(rssFeedTable.getName(), null, feedValues);
                    }

                    // Store the ItemResponse objects into appropriate SQLite db field
                    for (GetFeedsNetworkRequest.ItemResponse itemResponse : feedResponse.channelItems) {

                        ContentValues itemValues = new ContentValues();

                        itemValues.put(RssItemTable.getColumnLink(), itemResponse.itemURL);
                        itemValues.put(RssItemTable.getColumnTitle(), itemResponse.itemTitle);
                        itemValues.put(RssItemTable.getColumnDescription(), itemResponse.itemDescription);
                        // Identifies a unique RssItem table value
                        itemValues.put(RssItemTable.getColumnGuid(), itemResponse.itemGUID);
                        itemValues.put(RssItemTable.getColumnPubDate(), itemResponse.itemPubDate);
                        itemValues.put(RssItemTable.getColumnEnclosure(), itemResponse.itemEnclosureURL);
                        itemValues.put(RssItemTable.getColumnMimeType(), itemResponse.itemEnclosureMIMEType);
                        itemValues.put(RssItemTable.getColumnRssFeed(), feedId);
                        itemValues.put(RssItemTable.getColumnFavorite(), 0);
                        itemValues.put(RssItemTable.getColumnArchived(), 0);

                        // Set up checks for handling duplicate RssItem entries
                        long itemId = -1;

                        // Cursor provides access to result set returned by database query
                        // .query() returns a Cursor object, which represents the result of a query,
                        //      which points to one row of the query result.
                        // Cursor query(String table, String[] columns, String selection,
                        //      String[] selectionArgs, String groupBy, String having, String orderBy)
                        cursor = writableDatabase.query(rssItemTable.getName(), null, RssItemTable.getColumnGuid() + " = ?",
                                new String []{itemResponse.itemGUID}, null, null, null);

                        // If cursor contains an RssItem table object, get the index value for that object
                        if (cursor.moveToFirst()) {
                            // getColumnIndex(String columnName) returns zero-based index for given
                            //      column name, or -1 if column doesn't exist.
                            int index = cursor.getColumnIndex("id");
                            itemId = cursor.getLong(index);
                        }

                        // If feed URL already exists, then .update() database, else .insert() database
                        if (itemId >= 0) {
                            // int update(String table, ContentValues values, String whereClause, String[] whereArgs)
                            writableDatabase.update(rssItemTable.getName(), itemValues, "id = ?", new String [] {Long.toString(feedId)});
                        } else {
                            // long insert(String table, String nullColumnHack, ContentValues values)
                            writableDatabase.insert(rssItemTable.getName(), null, itemValues);
                        }
                    }
                    // Close cursor
                    cursor.close();
                }
                // Close database
                writableDatabase.close();
            }
        }).start();
    }

    public List<RssFeed> getFeeds(){
        return feeds;
    }

    public List<RssItem> getItems(){
        return items;
    }

    void createFakeData() {
        feeds.add(new RssFeed("My Favorite Feed",
                "This feed is just incredible, I can't even begin to tell you…",
                "http://favoritefeed.net", "http://feeds.feedburner.com/favorite_feed?format=xml"));
        for (int i = 0; i < 10; i++) {
            items.add(new RssItem(String.valueOf(i),
                    BloclyApplication.getSharedInstance().getString(R.string.placeholder_headline) + " " + i,
                    BloclyApplication.getSharedInstance().getString(R.string.placeholder_content),
                    "http://favoritefeed.net?story_id=an-incredible-news-story",
                    "https://bloc-global-assets.s3.amazonaws.com/images-android/foundation/silly-dog.jpg",
                    0, System.currentTimeMillis(), false, false));
        }
    }
}
