package io.bloc.android.blocly.api.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by namlu on 10-Feb-17.
 *
 * 49.1: Abstract network request class defines the basic structure for all network request
 * going forward
 */

public abstract class NetworkRequest<Result> {

    // 49.2: Error codes are simplified way of explaining the error
    public static final int ERROR_IO = 1;
    public static final int ERROR_MALFORMED_URL = 2;
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    protected void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    // 49.3: Subclasses required to implement performRequest() method which accesses Internet,
    // retrieves result, and returns in anticipated form
    public abstract Result performRequest();

    // 49.4: Convenience method to help subclasses make GET HTTP requests of a URL
    // An InputStream represents a resource in bytes (eight bits at a time)
    protected InputStream openStream(String urlString) {
        URL url = null;

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            setErrorCode(ERROR_MALFORMED_URL);
            return null;
        }
        InputStream inputStream = null;

        try {
            // 49.5: openStream() method creates the network connection required to recover data found
            // in that address
            inputStream = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            setErrorCode(ERROR_IO);
            return null;
        }
        return inputStream;
    }
}
