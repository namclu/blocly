package io.bloc.android.blocly.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;

/**
 * Created by namlu on 18-Jun-16.
 *
 * DataSource maintains a list of all the feeds and items in our application.
 * With a single class responsible for data, the remainder of our application will
 * never have to create or destroy data.
 */
public class DataSource {
    private List<RssFeed> feeds;
    private List<RssItem> items;

    public DataSource(){
        feeds = new ArrayList<RssFeed>();
        items = new ArrayList<RssItem>();
        //createFakeData();

        // Test the RSS feed request
        // We don't want to block the interface from responding when we make our
        // network request, therefore we place it in a background thread
        // List<FeedResponse> performRequest()
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses;

                // SimpleDateFormat is a class for formatting and parsing dates in a
                // locale-sensitive manner. It allows for formatting (date -> text), parsing (text -> date)
                // <pubDate>Tue, 18 Nov 2014 09:00:00 GMT</pubDate>
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

                // A single FeedResponse returns FeedResponse(String channelFeedURL, String channelTitle,
                //      String channelURL, String channelDescription, List<ItemResponse> channelItems)
                feedResponses = new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml")
                        .performRequest();

                // Loop through feedResponses to get the RssFeed objects
                // RssFeed(String title, String description, String siteUrl, String feedUrl)
                for (GetFeedsNetworkRequest.FeedResponse feedResponse : feedResponses) {
                    feeds.add(new RssFeed(feedResponse.channelTitle,
                            feedResponse.channelDescription,
                            feedResponse.channelURL,
                            feedResponse.channelFeedURL));
                    // Loop through itemResponse and get the RssItem objects
                    // RssItem(String guid, String title, String description, String url, String imageUrl,
                    //      long rssFeedId, long datePublished, boolean favorite, boolean archived)
                    for (GetFeedsNetworkRequest.ItemResponse itemResponse : feedResponse.channelItems) {
                        try {
                            items.add(new RssItem(itemResponse.itemURL,
                                    itemResponse.itemTitle,
                                    itemResponse.itemDescription,
                                    itemResponse.itemGUID,
                                    itemResponse.itemEnclosureURL,
                                    0,
                                    dateFormat.parse(itemResponse.itemPubDate).getTime(),
                                    false,
                                    false));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    // public RssFeed(String title, String description, String siteUrl, String feedUrl)
    public List<RssFeed> getFeeds(){
        return feeds;
    }

    // public RssItem(String guid, String title, String description, String url, String imageUrl,
    // long rssFeedId, long datePublished, boolean favorite, boolean archived)
    public List<RssItem> getItems(){
        return items;
    }

    void createRealData() {

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
