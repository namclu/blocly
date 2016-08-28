package io.bloc.android.blocly.api.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by namlu on 27-Aug-16.
 */
public class GetFeedsNetworkRequest extends NetworkRequest {

    // Recover multiple RSS feeds and store each feed's address
    String [] feedUrls;

    public GetFeedsNetworkRequest(String... feedUrls){
        this.feedUrls = feedUrls;
    }

    // Method requests a BufferedReader for each URL found in feedUrls
    @Override
    public Object performRequest() {
        for (String feedUrlString : feedUrls) {
            // openStream(String urlString), a method of NetworkRequest which takes a URL and returns an InputStream
            InputStream inputStream = openStream(feedUrlString);
            if (inputStream == null) {
                return null;
            }
            try {
                // Uses the constructor InputStreamReader(InputStream in)
                // InputStreamReader is a subclass of Reader
                // Uses the constructor BufferedReader(Reader in)
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // After we open a connection to the feed, we begin reading it using a BufferedReader
                // readLine() method recovers one line of characters at a time.
                String line = bufferedReader.readLine();
                while (line != null) {
                    Log.v(getClass().getSimpleName(), "Line: " + line);
                    // readLine() method reads a line of text and returns it as a String
                    line = bufferedReader.readLine();
                }
                // Close the reader and its stream
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            }
        }
        return null;
    }
}
