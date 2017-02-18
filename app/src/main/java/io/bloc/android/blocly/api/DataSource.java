package io.bloc.android.blocly.api;

import android.database.Cursor;
import android.os.Handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.BuildConfig;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.model.database.DatabaseOpenHelper;
import io.bloc.android.blocly.api.model.database.table.RssFeedTable;
import io.bloc.android.blocly.api.model.database.table.RssItemTable;
import io.bloc.android.blocly.api.model.database.table.Table;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;
import io.bloc.android.blocly.api.network.NetworkRequest;

/**
 * Created by namlu on 18-Jun-16.
 *
 * DataSource maintains a list of all the feeds and items in our application.
 * With a single class responsible for data, the remainder of our application will
 * never have to create or destroy data.
 */
public class DataSource {

    // 55.2: Interface provides a means for external classes to make async requests of DataSource
    public static interface Callback<Result>{
        public void onSuccess(Result result);
        public void onError(String errorMessage);
    }

    // Fields needed for Database
    private DatabaseOpenHelper databaseOpenHelper;
    private RssFeedTable rssFeedTable;
    private RssItemTable rssItemTable;
    // 55.3:
    private ExecutorService executorService;

    public DataSource() {
        // Database tables created
        rssFeedTable = new RssFeedTable();
        rssItemTable = new RssItemTable();
        // 55.3: ExecutorService queues tasks and completes them as quickly as possible
        executorService = Executors.newSingleThreadExecutor();

        // Both Table fields are kept w/in DataSource and act as primary access points for models
        // .getSharedInstance() returns an instance of BloclyApplication
        databaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(),
                rssFeedTable, rssItemTable);

        // 55.4:
        if (BuildConfig.DEBUG && true) {
            BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
        }
    }

    // 55.5: Callback receives an RssFeed object corresponding to the fetched feed
    public void fetchNewFeed(final String feedURL, final Callback<RssFeed> callback) {

        // 55.6a: A Handler, when instantiated, associates itself w the thread on which it is created
        final android.os.Handler callbackThreadHandler = new android.os.Handler();

        // Test the RSS feed request
        // We don't want to block the interface from responding when we make our
        // network request, therefore we place it in a background thread
        submitTask(new Runnable() {
            @Override
            public void run() {

                // 55.7: fetchFeedWithUrl() checks whether a row exists for a given RSS feed URL. If
                // exists, recover that row and return it to callback.
                Cursor existingFeedCursor = RssFeedTable.fetchFeedWithUrl(databaseOpenHelper.getReadableDatabase(), feedURL);
                if (existingFeedCursor.moveToFirst()) {
                    final RssFeed fetchedFeed = feedFromCursor(existingFeedCursor);
                    existingFeedCursor.close();

                    // 55.6b: Handlers are capable of executing Runnables on their designated thread
                    // so we invoke the Callback object's onSuccess(Result) method on the same Thread
                    // that made the request
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(fetchedFeed);
                        }
                    });
                    return;
                }

                // 55.7:
                GetFeedsNetworkRequest getFeedsNetworkRequest = new GetFeedsNetworkRequest(feedURL);
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses =
                        getFeedsNetworkRequest.performRequest();

                // 55.8a: If the error code does not equal zero, then an error has occurred
                if (getFeedsNetworkRequest.getErrorCode() != 0) {
                    final String errorMessage;

                    if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_IO) {
                        errorMessage = "Network error";
                    } else if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_MALFORMED_URL) {
                        errorMessage = "Malformed URL error";
                    } else if (getFeedsNetworkRequest.getErrorCode() == GetFeedsNetworkRequest.ERROR_PARSING) {
                        errorMessage = "Error parsing feed";
                    } else {
                        errorMessage = "Error unknown";
                    }
                    // 55.8b: Invoke Callback's onError(String)
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(errorMessage);
                        }
                    });
                    return;
                }

                // Why do we need to do: feedResponses.get(0)
                GetFeedsNetworkRequest.FeedResponse newFeedResponse = feedResponses.get(0);

                // One table, "rss_feeds", is meant to have an entry for each website the user
                //      has subscribed to
                // androidCentralFeedID returns the row ID of newly inserted row, or -1 if error occurred
                long newFeedId = new RssFeedTable.Builder()
                        .setFeedURL(newFeedResponse.channelFeedURL)
                        .setSiteURL(newFeedResponse.channelURL)
                        .setTitle(newFeedResponse.channelTitle)
                        .setDescription(newFeedResponse.channelDescription)
                        .insert(databaseOpenHelper.getWritableDatabase());

                // The other table, "rss_items", will feature every item from every subscription
                for (GetFeedsNetworkRequest.ItemResponse itemResponse : newFeedResponse.channelItems) {
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
                    // 55.9:
                    new RssItemTable.Builder()
                            .setTitle(itemResponse.itemTitle)
                            .setDescription(itemResponse.itemDescription)
                            .setEnclosure(itemResponse.itemEnclosureURL)
                            .setMIMEType(itemResponse.itemEnclosureMIMEType)
                            .setLink(itemResponse.itemURL)
                            .setGUID(itemResponse.itemGUID)
                            .setPubDate(itemPubDate)
                            // We supply the row identifier for the AndroidCentral feed we inserted earlier.
                            // This forms a relationship between each RSS item and the AndroidCentral feed.
                            .setRSSFeed(newFeedId)
                            .insert(databaseOpenHelper.getWritableDatabase());
                }
                // 54: Use the row identifier to query for its corresponding row
                // 55.9: Removed broadcast BloclyApplication.getSharedInstance().sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETED));
                Cursor newFeedCursor = rssFeedTable.fetchRow(databaseOpenHelper.getReadableDatabase(), newFeedId);

                newFeedCursor.moveToFirst();
                final RssFeed fetchedFeed = feedFromCursor(newFeedCursor);

                // 54: Close the cursor
                newFeedCursor.close();

                // 55.9:
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(fetchedFeed);
                    }
                });
            }
        });
    }

    // 55.11: This Callback results in a list of RssItem objects. The List will be populated from the
    // results of the query
    public void fetchItemsForFeed(final RssFeed rssFeed, final Callback<List<RssItem>> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                final List<RssItem> resultList = new ArrayList<RssItem>();
                Cursor cursor = RssItemTable.fetchItemsForFeed(
                        databaseOpenHelper.getReadableDatabase(),
                        rssFeed.getRowId());

                // 55.12: Use a do/while loop to iterate over each entry, instantiating an RssItem
                // for each row retrieved
                if (cursor.moveToFirst()) {
                    do {
                        resultList.add(itemFromCursor(cursor));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            }
        });
    }

    // 54: Pulls information from the Cursor and places it directly into RssFeed's constructor using
    //      newly created get methods in RssFeedTable.java
    // 55: Supply rowID by using the Table.getRowId convenience method
    static RssFeed feedFromCursor(Cursor cursor) {
        return new RssFeed(Table.getRowId(cursor), RssFeedTable.getTitle(cursor), RssFeedTable.getDescription(cursor),
                RssFeedTable.getSiteURL(cursor), RssFeedTable.getFeedUrl(cursor));
    }

    // 54: Pulls information from the Cursor and places it directly into RssItem's constructor using
    //      newly created get methods in RssItemTable.java
    // 55: Supply rowID by using the Table.getRowId convenience method
    static RssItem itemFromCursor(Cursor cursor) {
        return new RssItem(Table.getRowId(cursor), RssItemTable.getGUID(cursor), RssItemTable.getTitle(cursor),
                RssItemTable.getDescription(cursor), RssItemTable.getLink(cursor),
                RssItemTable.getEnclosure(cursor), RssItemTable.getRssFeedId(cursor),
                RssItemTable.getPubDate(cursor), RssItemTable.getFavorite(cursor),
                RssItemTable.getArchived(cursor));
    }

    // 55.3 Helper method to make use of ExecutorService
    void submitTask(Runnable task){
        // 55.4: Check if the service has been shutdown or terminated. In such cases, instantiate a
        // new ExecutorService, then submit the new task
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.submit(task);
    }
}
