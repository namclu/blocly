package io.bloc.android.blocly.api.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by namlu on 10-Feb-17.
 */

public class GetFeedsNetworkRequest extends NetworkRequest {

    // 49.6: Stores feed URLs
    String[] feedUrls;

    public GetFeedsNetworkRequest(String... feedUrls) {
        this.feedUrls = feedUrls;
    }

    // 49.7:
    @Override
    public Object performRequest() {
        for (String feedUrlString : feedUrls) {
            InputStream inputStream = openStream(feedUrlString);
            if (inputStream == null) {
                return null;
            }
            try {
                // 49.7: BufferedReader reads text from a character-input steam
                // InputSteamReader reads from bytes streams and decodes them into char streams
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                // 49.8: BufferedReader.readline() reads in a line of text
                String line = bufferedReader.readLine();
                while (line != null) {
                    Log.v(getClass().getSimpleName(), "Line: " + line);
                    line = bufferedReader.readLine();
                }
                // 49.9: Close the steam when done
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
