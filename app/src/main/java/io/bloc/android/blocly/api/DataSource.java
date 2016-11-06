package io.bloc.android.blocly.api;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    // 54: DataSource will form its broadcast Intent by specifying the Intent's action, the static String
    // ACTION_DOWNLOAD_COMPLETED string = io.bloc.android.blocly.api.DataSource.ACTION_DOWNLOAD_COMPLETED
    public static final String ACTION_DOWNLOAD_COMPLETED = DataSource.class.getCanonicalName().concat(".ACTION_DOWNLOAD_COMPLETED");

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
        databaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(),
                rssFeedTable, rssItemTable);

        feeds = new ArrayList<RssFeed>();
        items = new ArrayList<RssItem>();

        // Test the RSS feed request
        // We don't want to block the interface from responding when we make our
        // network request, therefore we place it in a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                // DEBUG property is used to decide whether or not to delete the existing database
                // The 'false' inside of if() ensures database is not lost each time app is launched
                // Switch it to 'true' after modifying the database schema in any way
                if (BuildConfig.DEBUG && true) {
                    BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
                }
                // Until getWritableDatabase() is invoked, our database will not be created nor opened
                // getReadableDatabase() may also be used, however this method will not upgrade
                // the database even if the versions are mismatched
                SQLiteDatabase writableDatabase = databaseOpenHelper.getWritableDatabase();
                /*List<GetFeedsNetworkRequest.FeedResponse> feedResponses =
                        new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml").performRequest();*/
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses =
                        new GetFeedsNetworkRequest("http://www.abc.net.au/news/feed/52498/rss.xml").performRequest();

                // Why do we need to do: feedResponses.get(0)
                GetFeedsNetworkRequest.FeedResponse androidCentral = feedResponses.get(0);

                // One table, "rss_feeds", is meant to have an entry for each website the user
                //      has subscribed to
                // androidCentralFeedID returns the row ID of newly inserted row, or -1 if error occurred
                long androidCentralFeedId = new RssFeedTable.Builder()
                        .setFeedURL(androidCentral.channelFeedURL)
                        .setSiteURL(androidCentral.channelURL)
                        .setTitle(androidCentral.channelTitle)
                        .setDescription(androidCentral.channelDescription)
                        .insert(writableDatabase);

                // 54: New Rss items ArrayList to be used later
                List<RssItem> newRSSItems = new ArrayList<RssItem>();

                // The other table, "rss_items", will feature every item from every subscription
                for (GetFeedsNetworkRequest.ItemResponse itemResponse: androidCentral.channelItems) {
                    // Attempt to convert the downloaded String into Unix time
                    long itemPubDate = System.currentTimeMillis();
                    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.ENGLISH);
                    try {
                        // long getTime() returns the number of milliseconds since
                        //      January 1, 1970, 00:00:00 GMT represented by this Date object
                        itemPubDate = dateFormat.parse(itemResponse.itemPubDate).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // Insert the RSS item into the database
                    // 54: Store the row identifier which results from inserting the RSS item
                    long newItemRowId = new RssItemTable.Builder()
                            .setTitle(itemResponse.itemTitle)
                            .setDescription(itemResponse.itemDescription)
                            .setEnclosure(itemResponse.itemEnclosureURL)
                            .setMIMEType(itemResponse.itemEnclosureMIMEType)
                            .setLink(itemResponse.itemURL)
                            .setGUID(itemResponse.itemGUID)
                            .setPubDate(itemPubDate)
                            // We supply the row identifier for the AndroidCentral feed we inserted earlier.
                            // This forms a relationship between each RSS item and the AndroidCentral feed.
                            .setRSSFeed(androidCentralFeedId)
                            .insert(writableDatabase);

                    // 54: Use the row identifier to query for its corresponding row
                    Cursor itemCursor = rssItemTable.fetchRow(databaseOpenHelper.getReadableDatabase(), newItemRowId);

                    // 54: Cursors initially reference a non-existent row, index -1.
                    // In order to place the Cursor at its first resulting row, we must invoke moveToFirst()
                    itemCursor.moveToFirst();
                    RssItem newRssItem = itemFromCursor(itemCursor);
                    newRSSItems.add(newRssItem);

                    // 54: Close the cursor
                    itemCursor.close();
                }
                // 54: Use the row identifier to query for its corresponding row
                Cursor androidCentralCursor = rssFeedTable.fetchRow(databaseOpenHelper.getReadableDatabase(), androidCentralFeedId);

                androidCentralCursor.moveToFirst();
                RssFeed androidCentralRSSFeed = feedFromCursor(androidCentralCursor);

                // 54: Close the cursor
                androidCentralCursor.close();

                // 54: Add the new newRSSItems and androidCentralRssFeed into items and feeds list respectively
                items.addAll(newRSSItems);
                feeds.add(androidCentralRSSFeed);

                // 54: To send a broadcast, use Context's sendBroadcast(Intent)
                BloclyApplication.getSharedInstance().sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETED));

                /*
                 * Assign 54: Testing new RssItemTable methods
                 */

                // Fetch all archived RSS items.
                //RssItemTable.fetchAllArchived(writableDatabase);

                // Todo: Fetch all archived RSS items from a particular RSS feed.
                RssItemTable.fetchAllArchived(writableDatabase, androidCentralRSSFeed);

                // Todo: Fetch all favorited RSS items.
                // Todo: Fetch all favorited RSS items from a particular RSS feed.
                // Todo: Fetch all items from a particular RSS feed.
                // Todo: Fetch all items from a particular RSS feed with a given OFFSET and LIMIT.


            }
        }).start();
    }

    public List<RssFeed> getFeeds(){
        return feeds;
    }

    public List<RssItem> getItems(){
        return items;
    }

    // 54: Pulls information from the Cursor and places it directly into RssFeed's constructor using
    //      newly created get methods in RssFeedTable.java
    public static RssFeed feedFromCursor(Cursor cursor) {
        return new RssFeed(RssFeedTable.getTitle(cursor), RssFeedTable.getDescription(cursor),
                RssFeedTable.getSiteURL(cursor), RssFeedTable.getFeedUrl(cursor));
    }

    // 54: Pulls information from the Cursor and places it directly into RssItem's constructor using
    //      newly created get methods in RssItemTable.java
    public static RssItem itemFromCursor(Cursor cursor) {
        return new RssItem(RssItemTable.getGUID(cursor), RssItemTable.getTitle(cursor),
                RssItemTable.getDescription(cursor), RssItemTable.getLink(cursor),
                RssItemTable.getEnclosure(cursor), RssItemTable.getRssFeedId(cursor),
                RssItemTable.getPubDate(cursor), RssItemTable.getFavorite(cursor),
                RssItemTable.getArchived(cursor));
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
