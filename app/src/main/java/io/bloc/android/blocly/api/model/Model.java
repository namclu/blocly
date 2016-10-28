package io.bloc.android.blocly.api.model;

/**
 * Created by namlu on 28-Oct-16.
 *
 * 55: The Model class is the superclass which RssFeed and RssItem are based on.
 * Currently its only field is rowId.
 */

public abstract class Model {

    // 55: Adds ability to retain rowId data for RssFeed and RssItem
    private final long rowId;


    public Model(long rowId) {
        this.rowId = rowId;
    }

    public long getRowId(){
        return rowId;
    }

}
