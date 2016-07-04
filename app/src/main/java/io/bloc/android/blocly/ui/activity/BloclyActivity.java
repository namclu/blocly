package io.bloc.android.blocly.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import io.bloc.android.blocly.R;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;

/**
 * Created by namlu on 14-Jun-16.
 */
public class BloclyActivity extends Activity {

    private ItemAdapter itemAdapter;
    private static String TAG = BloclyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);

        itemAdapter = new ItemAdapter();

        try{
            String myString = null;
            myString.charAt(0);
        }catch (Exception e){
            Log.e(TAG, "called from onCreate()", e);
        }

        // A reference to the inflated RecyclerView instance from activity_blocly.xml
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemAdapter);
    }
}
