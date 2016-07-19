package io.bloc.android.blocly.ui.activity;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;
import io.bloc.android.blocly.ui.adapter.NavigationDrawerAdapter;

/**
 * Created by namlu on 14-Jun-16.
 */
public class BloclyActivity extends AppCompatActivity implements NavigationDrawerAdapter.NavigationDrawerAdapterDelegate{

    private ItemAdapter itemAdapter;
    // Add to use DrawerLayout
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    // Add an instance of NavigationDrawerAdapter
    private NavigationDrawerAdapter navigationDrawerAdapter;
    // Add fields to track Menu object and Overflow button
    private Menu menu;
    private View overflowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);

        // Assign ToolBar as our ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activity_blocly);
        setSupportActionBar(toolbar);

        itemAdapter = new ItemAdapter();
        navigationDrawerAdapter = new NavigationDrawerAdapter();

        // Set BloclyActivity (this) as NavigationDrawerAdapter's delegate
        navigationDrawerAdapter.setDelegate(this);

        // A reference to the inflated RecyclerView instance from activity_blocly.xml
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);
        RecyclerView navigationRecyclerView = (RecyclerView) findViewById(R.id.rv_nav_activity_blocly);

        // Set the layout, animator, and adapter for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemAdapter);

        // Set the layout, animator, and adapter for navigationRecyclerView
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(navigationDrawerAdapter);

        // Recover the instance of ActionBar associated with ToolBar
        // and invoke setDisplayHomeAsUpEnabled(boolean) to allow this behavior
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_activity_blocly);

        // ActionBarDrawerToggle(activity, drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes)
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0){
            // Anonymous classes to override ActionBarDrawerToggle's default behaviors.
            // When the drawer opens, enable both Menu item and Overflow button
            @Override
            public void onDrawerClosed(View drawerView){
                super.onDrawerClosed(drawerView);
                // If drawer is closed, set overflowButton to visible and enabled
                if(overflowButton != null){
                    overflowButton.setAlpha(1f);
                    overflowButton.setEnabled(true);
                }
                if(menu == null){
                    return;
                }
                for(int i = 0; i < menu.size(); i++){
                    MenuItem item = menu.getItem(i);
                    item.setEnabled(true);
                    Drawable icon = item.getIcon();
                    if(icon != null){
                        icon.setAlpha(255);
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                if(overflowButton != null){
                    // Even though we don't see the buttons, they would still be clickable if we
                    // did not disable them
                    overflowButton.setEnabled(false);
                }
                if(menu == null){
                    return;
                }
                for(int i = 0; i < menu.size(); i ++){
                    menu.getItem(i).setEnabled(false);
                }
            }

            @Override
            public void onDrawerSlide(View drawView, float slideOffset){
                // As the drawer moves, slideOffset ranges from 0f at the edge of the screen to
                // 1f at the drawer's maximum width
                super.onDrawerSlide(drawView, slideOffset);
                if(overflowButton == null){
                    // The List that contains the matching views
                    ArrayList<View> foundViews = new ArrayList<View>();
                    // Traverse the view-hierarchy and locate the overflow button
                    getWindow().getDecorView().findViewsWithText(foundViews,
                            getString(R.string.abc_action_menu_overflow_description),
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                    if (foundViews.size() > 0) {
                        overflowButton = foundViews.get(0);
                    }
                }
                if(overflowButton != null){
                    overflowButton.setAlpha(1f - slideOffset);
                }
                if(menu == null){
                    return;
                }
                for(int i = 0; i < menu.size(); i++){
                    MenuItem item = menu.getItem(i);
                    Drawable icon = item.getIcon();
                    if(icon != null){
                        icon.setAlpha((int) (1f - slideOffset) * 255);
                    }
                }
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    // Call syncState() to synchronize the indicator with the state of the linked DrawerLayout
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    // Called when the device configuration changes while your activity is running
    // This will only be called if you have selected configurations you would like
    // to handle with the configChanges attribute in your manifest
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    // This hook is called whenever an item in your options menu is selected
    // Default implementation simply returns false to have the normal processing happen
    // (calling the item's Runnable or sending a message to its Handler as appropriate)
    public boolean onOptionsItemSelected(MenuItem item){
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        // A Toast is displayed each time an Overflow menu item is pressed
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    // Menus belong to controllers, which is handled by BloclyActivity
    // Method to inflate the menu items programmatically
    public boolean onCreateOptionsMenu(Menu menu){
        // .inflate(int menuRes, Menu menu)
        getMenuInflater().inflate(R.menu.blocly, menu);
        // Saves a reference to the menu object we just created
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    /*NavigationDrawerAdapter Delegate*/

    @Override
    // DrawerLayout is managed by BloclyActivity, not NavigationDrawerAdapter, therefore
    // BloclyActivity is responsible for closing the DrawerLayout
    public void didSelectNavigationOption(NavigationDrawerAdapter adapter,
                                          NavigationDrawerAdapter.NavigationOption navigationOption) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, "Show the " + navigationOption.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, "Show RSS feed from " + rssFeed.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
