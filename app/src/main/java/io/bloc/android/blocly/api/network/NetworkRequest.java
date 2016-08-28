package io.bloc.android.blocly.api.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by namlu on 27-Aug-16.
 */
public abstract class NetworkRequest<Result> {

    public static final int ERROR_IO = 1;
    public static final int ERROR_MALFORMED_URL = 2;

    public int errorCode;

    // Each request is responsible for reporting errors to its callers
    public int getErrorCode() {
        return errorCode;
    }

    protected void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    // Method which accesses the Internet, retrieves the result and returns it in the anticipated form.
    public abstract Result performRequest();

    // To help subclasses make GET HTTP requests of any URL, the base class provides a convenience method
    protected InputStream openStream(String urlString){
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
            // openStream() method opens a connection to URL and returns an InputStream
            // for reading from that connection.
            inputStream = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            setErrorCode(ERROR_IO);
            return null;
        }
        return inputStream;
    }
}
