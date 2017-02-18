package io.bloc.android.blocly.api.model;

/**
 * Created by namlu on 15-Feb-17.
 *
 * abstract class Model will give each Model the ability to retain its originating row identifier
 */

public abstract class Model {

    private final long rowId;

    public Model(long rowId) {
        this.rowId = rowId;
    }

    public long getRowId() {
        return rowId;
    }
}
