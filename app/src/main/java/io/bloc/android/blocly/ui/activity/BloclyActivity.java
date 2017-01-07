package io.bloc.android.blocly.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;

import io.bloc.android.blocly.R;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;

/**
 * Created by namlu on 14-Jun-16.
 */
public class BloclyActivity extends AppCompatActivity {

    private ItemAdapter itemAdapter;

    // 42.4:
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);

        // 42.4: Set Toolbar as the ActionBar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activity_blocly);
        setSupportActionBar(toolbar);

        itemAdapter = new ItemAdapter();

        // A reference to the inflated RecyclerView instance from activity_blocly.xml
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemAdapter);

        // 42.5: Get support ActionBar corresponding to this toolbar and
        //      setDisplayHomeAsUpEnabled() enables the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_activity_blocly);

        // 42.6: Create a new ActionBarDrawerToggle and set DrawerLayout's DrawerListener
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    // 42.7a: synchronize the indicator with the state of the linked DrawerLayout after
    //      onRestoreInstanceState has occurred.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    // 42.7b: onConfigurationChanged() called by the system when the device configuration changes
    //      while your activity is running
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    // 42.7c: onOptionsItemSelected() hook is called whenever an item in your options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
