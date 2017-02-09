package io.bloc.android.blocly.ui.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import java.util.ArrayList;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;
import io.bloc.android.blocly.ui.adapter.NavigationDrawerAdapter;

/**
 * Created by namlu on 14-Jun-16.
 */
public class BloclyActivity extends AppCompatActivity
        implements
        NavigationDrawerAdapter.NavigationDrawerAdapterDelegate,
        ItemAdapter.DataSource,
        ItemAdapter.Delegate{

    // Add external reference to RecyclerView
    private RecyclerView recyclerView;
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
        // Set BloclyActivity as ItemAdapter's delegate and data source.
        itemAdapter.setDataSource(this);
        itemAdapter.setDelegate(this);

        navigationDrawerAdapter = new NavigationDrawerAdapter();

        // Set BloclyActivity (this) as NavigationDrawerAdapter's delegate
        navigationDrawerAdapter.setDelegate(this);

        // A reference to the inflated RecyclerView instance from activity_blocly.xml
        recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);
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

                    // 48.0: If the item equals Share icon and no other items are expanded,
                    // the Share icon will not be enabled (by skipping its iteration using 'continue')
                    if (item.getItemId() == R.id.action_share
                            && itemAdapter.getExpandedItem() == null) {
                        continue;
                    }

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

                    // 48.0: If the item equals Share icon and no other items are expanded,
                    // the Share icon will not be enabled (by skipping its iteration using 'continue')
                    if (item.getItemId() == R.id.action_share
                            && itemAdapter.getExpandedItem() == null) {
                        continue;
                    }

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
        // 48.3:
        if (item.getItemId() == R.id.action_share) {
            RssItem itemToShare = itemAdapter.getExpandedItem();
            if (itemToShare == null) {
                return false;
            }
            // 48.4: To share content, create implicit intent, where receiving activity is not know
            // beforehand
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            // 48.5: Pass additional information to receiving intent
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    String.format("%s (%s)", itemToShare.getTitle(), itemToShare.getUrl()));
            // 48.6: Set the MIME type to text/plan to exclude application that don't deal w text
            shareIntent.setType("text/plain");
            // 48.7: Create a chooser dialog with presents users w/ all Activities on their devices
            // capable of receiving the intent
            Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_chooser_title));
            // 48.8: Launch the activity
            startActivity(chooser);
        } else {
            Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        }
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

        //48.0: Animate the item as soon as its created
        animateShareItem(itemAdapter.getExpandedItem() != null);

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

    /*
    * ItemAdapter.DataSource
    * */

    @Override
    public RssItem getRssItem(ItemAdapter itemAdapter, int position) {
        // getSharedDataSource() returns a DataSource
        // getItems() returns a List<RssItem>
        // get() returns a RssItem
        return BloclyApplication.getSharedDataSource().getItems().get(position);
    }

    @Override
    public RssFeed getRssFeed(ItemAdapter itemAdapter, int position) {
        // getFeeds() returns a List<RssFeed>
        // get() returns a RssFeed
        return BloclyApplication.getSharedDataSource().getFeeds().get(0);
    }

    @Override
    public int getItemCount(ItemAdapter itemAdapter) {
        return BloclyApplication.getSharedDataSource().getItems().size();
    }

    /*
    * ItemAdapter.Delegate
    * */

    @Override
    public void onItemClicked(ItemAdapter itemAdapter, RssItem rssItem) {
        int positionToExpand = -1;
        int positionToContract = -1;

        // Checks if ItemAdapter has an expanded item
        // getExpandedItem() returns an RssItem
        if(itemAdapter.getExpandedItem() != null){
            // If ItemAdapter was previously expanded, contract it
            // Recover its position within the list by invoking List's indexOf(Object) method
            // getSharedDataSource() returns a DataSource
            // getItems() returns a List<RssItem>
            // indexOf() returns an int
            positionToContract = BloclyApplication.getSharedDataSource().getItems().indexOf(itemAdapter.getExpandedItem());

            // Only check the edge-case condition when both expanding and contracting Views are visible on screen
            // findViewByPosition() returns 'position' if View is currently onscreen, 'null' otherwise
            View viewToContract = recyclerView.getLayoutManager().findViewByPosition(positionToContract);
            if(viewToContract == null){
                positionToContract = -1;
            }
        }

        // When a new item is clicked, recover its position within the list and set it as the expanded item
        if(itemAdapter.getExpandedItem() != rssItem){
            positionToExpand = BloclyApplication.getSharedDataSource().getItems().indexOf(rssItem);
            itemAdapter.setExpandedItem(rssItem);
        }else{
            // If user clicks on the expanded item, contract it by resetting ItemAdapter's expanded item to null
            itemAdapter.setExpandedItem(null);
        }

        // Once the position of either item is known, notify the ItemAdapter of a change
        // notifyItemChanged(int position) will notify ItemAdapter that item at position has changed
        // ItemAdapterViewHolder's update(RssFeed, RssItem) will execute which invokes the line we added previously
        if(positionToContract > -1){
            itemAdapter.notifyItemChanged(positionToContract);
        }

        if(positionToExpand > -1){
            itemAdapter.notifyItemChanged(positionToExpand);
            // 48.0: If item View is expanded, animate it to full opacity
            animateShareItem(true);
        } else{
            // 48.0: Else animate it to full transparency
            animateShareItem(false);
            // The list should only scroll if and when the user expands a new item.
            return;
        }
        // Variable to determine how much less we need to scroll when expanding
        int lessToScroll = 0;

        // To get the lessToScroll value, verify that:
        // contracting View is visible &&
        // contracting View resides above newly expanding View
        if(positionToContract > -1 && positionToContract < positionToExpand){
            lessToScroll = itemAdapter.getExpandedItemHeight() - itemAdapter.getCollapsedItemHeight();
        }

        // findViewByPosition() returns 'position' if view is currently onscreen, 'null' otherwise
        View viewToExpand = recyclerView.getLayoutManager().findViewByPosition(positionToExpand);
        // smoothScrollBy(int dx, int dy)
        // getTop() returns distance between top of View and top of its parent, in pixels
        recyclerView.smoothScrollBy(0, viewToExpand.getTop() - lessToScroll);
    }

    // 48.8: Implement onVisitClicked delegate method
    @Override
    public void onVisitClicked(ItemAdapter itemAdapter, RssItem rssItem) {
        // 48.9: Create intent to launch web browser for URL
        Intent visitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rssItem.getUrl()));
        startActivity(visitIntent);
    }

    /*
     * 48.0: Private methods
     */

    private void animateShareItem(final boolean enabled) {
        MenuItem shareItem = menu.findItem(R.id.action_share);

        if (shareItem.isEnabled() == enabled) {
            return;
        }

        shareItem.setEnabled(enabled);
        final Drawable shareIcon = shareItem.getIcon();

        // 48.0: Animates to either full opacity or full transparency based on desired enabled state
        ValueAnimator valueAnimator = ValueAnimator.ofInt(enabled ? new int[]{0, 255} : new int[]{255, 0});
        valueAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // 48.0: A listener to the set of listeners that are sent update events through
        // the life of an animation.
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                shareIcon.setAlpha((Integer) valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }
}
