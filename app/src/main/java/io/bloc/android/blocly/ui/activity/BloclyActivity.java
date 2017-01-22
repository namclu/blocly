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
    // Add to use NavigationDrawer
    private NavigationDrawerAdapter navigationDrawerAdapter;
    // 45.6: Add fields to track Menu object and represent overflow button
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

        // 44.3:
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
        // 45.5: Add anonymous classes onDrawerClosed() and onDrawerOpened() so if left nav drawer
        //      is open, right actionbar items will not be visible. invalidateOptionsMenu() tears
        //      down current menu structure and invokes onCreateOptionsMenu() again.
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0){

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (overflowButton != null) {
                    // 45.7a: When the drawer opens, enable both the menu items and overflow buttons
                    overflowButton.setEnabled(false);
                }
                if (menu == null) {
                    return;
                }
                for (int i = 0; i < menu.size(); i++) {
                    // 45.7b: When the drawer opens, enable both the menu items and overflow buttons
                    menu.getItem(i).setEnabled(false);
                }
            }

            // 45.8: no direct way of recovering a reference to the overflow button as it is not
            //      included in either the Menu or ActionBar API.
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (overflowButton == null) {
                    // 45.8:
                    ArrayList<View> foundViews = new ArrayList<View>();
                    getWindow().getDecorView().findViewsWithText(foundViews,
                            getString(R.string.abc_action_menu_overflow_description),
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                    if (foundViews.size() > 0) {
                        overflowButton = foundViews.get(0);
                    }
                }
                // 45.9a: onDrawerSlide() invoked by DrawerLayout as drawer moves. Use this proerpty
                //      to set opacity level of action item and overflow buttons
                if (overflowButton != null) {
                    overflowButton.setAlpha(1f - slideOffset);
                }
                if (menu == null) {
                    return;
                }
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    Drawable icon = item.getIcon();
                    if (icon != null) {
                        // 45.9b: see 45.9a
                        icon.setAlpha((int) ((1f - slideOffset) * 255));
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
        // 45.4: Display Toast each time Actionbar menu is pressed
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

    /*
     * 45.4: Inflating the menu
     * Menus belong to controllers, which are normally Activity classes. Each Activity is
     *      responsible for declaring its menu items.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 45.6: Removed drawer check and save a reference to the Menu object
        getMenuInflater().inflate(R.menu.blocly, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * 44.3: NavigationDrawerAdapterDelegate
     */

    // 44.3: DrawerLayout is now managed by BloclyActivity, not NavigationDrawerAdapter
    @Override
    public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationDrawerAdapter.NavigationOption navigationOption) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, "Show the " + navigationOption.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, "Show RSS items from " + rssFeed.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
