package io.bloc.android.blocly.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;

/**
 * Created by namlu on 14-Jun-16.
 */
public class BloclyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);
        Toast.makeText(this, BloclyApplication.getSharedDataSource().getFeeds().get(0).getTitle(),
                Toast.LENGTH_LONG).show();
        TextView textView = (TextView)findViewById(R.id.hello_text_view);
        textView.setText("RSS Feed");
    }
}
