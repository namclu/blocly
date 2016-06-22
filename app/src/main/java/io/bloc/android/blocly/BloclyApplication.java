package io.bloc.android.blocly;

import android.app.Application;

import io.bloc.android.blocly.api.DataSource;

/**
 * Created by namlu on 18-Jun-16.
 *
 * Custom singleton class used to ...
 */
public class BloclyApplication extends Application {

    //Singleton instance of BloclyApplication
    private static BloclyApplication sharedInstance;
    private DataSource dataSource;

    public static BloclyApplication getSharedInstance(){
        return sharedInstance;
    }

    //What does this method return?
    //Called by ItemAdapter.getItemCount()
    public static DataSource getSharedDataSource(){
        return BloclyApplication.getSharedInstance().getDataSource();
    }

    //What does this method return?
    //Called by BloclyApplication.getSharedDataSource()
    public DataSource getDataSource(){
        return dataSource;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedInstance = this;
        dataSource = new DataSource();
    }
}
