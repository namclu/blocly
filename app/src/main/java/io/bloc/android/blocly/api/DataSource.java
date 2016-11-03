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

    // 55: Interface will provide a means for external classes to make asynchronous requests of DataSource
    public static interface Callback<Result> {
        public void onSuccess(Result result);
        public void onError(String errorMessage);
    }

    // Fields needed for Database
    private DatabaseOpenHelper databaseOpenHelper;
    private RssFeedTable rssFeedTable;
    private RssItemTable rssItemTable;

    // 55: ExecutorService allows the management of tasks to process and can terminate them as well
    private ExecutorService executorService;

    /*
     * 55: Deleted all private List<RssFeed> feeds; and private List<RssItem> items; (and their uses)
     *      so that DataSource will now pass data models to the elements that requested them and
     *      allow that component to own the data.
     */

    public DataSource() {
        // Database tables created
        rssFeedTable = new RssFeedTable();
        rssItemTable = new RssItemTable();

        // 55: Single thread, tasks will be executed in the order they arrive
        executorService = Executors.newSingleThreadExecutor();

        // Both Table fields are kept w/in DataSource and act as primary access points for models
        // .getSharedInstance() returns an instance of BloclyApplication
        databaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(),
                rssFeedTable, rssItemTable);

        // 55: Todo
        if (BuildConfig.DEBUG && true) {
            BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
        }
    }

    // 55: A Runnable is a simple interface which has but one method, run()
    void submitTask(Runnable task) {
        // 55: If executorService is shutdown or terminated, instantiate a new ExecutorService
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        // 55: Submitting a task places it on the queue, it will then be performed on a background thread.
        executorService.submit(task);
    }

    // 55: The method signature indicates which type of Callback is required to recover the new RSS feed.
    // This Callback receives an RssFeed object, the one corresponding to the fetched feed.
    public void fetchNewFeed (final String feedURL, final Callback<RssFeed> callback) {

        // 55: A Handler, when instantiated, associates itself with the Thread on which it is created.
        //      Handlers are capable of executing Runnables on their designated Thread
        final android.os.Handler callbackThreadHandler = new Handler();

        submitTask(new Runnable() {
            @Override
            public void run() {

                // 55: Check whether a row exists for the given RSS feed URL.
                //      If it exists, we recover that row and return it to the Callback.
                Cursor existingFeedCursor = RssFeedTable.fetchFeedWithURL(databaseOpenHelper.getReadableDatabase(), feedURL);
                if (existingFeedCursor.moveToFirst()) {
                    final RssFeed fetchedFeed = feedFromCursor(existingFeedCursor);
                    existingFeedCursor.close();

                    // 55: Handler con't, invoke the Callback object's onSuccess(Result) method
                    //      on the same Thread that made the request
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(fetchedFeed);
                        }
                    });
                    return;
                }

                // 55:
                GetFeedsNetworkRequest getFeedsNetworkRequest = new GetFeedsNetworkRequest(feedURL);
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses = getFeedsNetworkRequest.performRequest();

                // 55:
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

                    // 55:
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(errorMessage);
                        }
                    });
                    return;
                }
                // 55:
                GetFeedsNetworkRequest.FeedResponse newFeedResponse = feedResponses.get(0);
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
                    // 55: update
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

                    // 55:
                    Cursor newFeedCursor = rssFeedTable.fetchRow(databaseOpenHelper.getReadableDatabase(), newFeedId);
                    newFeedCursor.moveToFirst();
                    final RssFeed fetchedFeed = feedFromCursor(newFeedCursor);
                    newFeedCursor.close();

                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(fetchedFeed);
                        }
                    });
                }
            }
        });
    }

    // 55: Parameterized types can include nested types
    // 55: This list will be populated from the results of the query
    public void fetchItemsForFeed(final RssFeed rssFeed, final Callback<List<RssItem>> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                final List<RssItem> resultList = new ArrayList<RssItem>();
                Cursor cursor = RssItemTable.fetchItemsForFeed(
                        databaseOpenHelper.getReadableDatabase(),
                        rssFeed.getRowId());

                // 55: Iterate over each entry, instantiating an RssItem from each row retrieved
                if (cursor.moveToFirst()) {
                    do {
                        resultList.add(itemFromCursor(cursor));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(resultList);
                    }
                });
            }
        });
    }

    // 54: Pulls information from the Cursor and places it directly into RssFeed's constructor using
    //      newly created get methods in RssFeedTable.java
    // 55: Use new Table.getRowId() method to supply required row Id
    static RssFeed feedFromCursor(Cursor cursor) {
        return new RssFeed(Table.getRowId(cursor), RssFeedTable.getTitle(cursor), RssFeedTable.getDescription(cursor),
                RssFeedTable.getSiteURL(cursor), RssFeedTable.getFeedUrl(cursor));
    }

    // 54: Pulls information from the Cursor and places it directly into RssItem's constructor using
    //      newly created get methods in RssItemTable.java
    // 55: Use new Table.getRowId() method to supply required row Id
    static RssItem itemFromCursor(Cursor cursor) {
        return new RssItem(Table.getRowId(cursor), RssItemTable.getGUID(cursor), RssItemTable.getTitle(cursor),
                RssItemTable.getDescription(cursor), RssItemTable.getLink(cursor),
                RssItemTable.getEnclosure(cursor), RssItemTable.getRssFeedId(cursor),
                RssItemTable.getPubDate(cursor), RssItemTable.getFavorite(cursor),
                RssItemTable.getArchived(cursor));
    }
}
