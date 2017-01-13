package io.bloc.android.blocly.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;

/**
 * Created by namlu on 13-Jul-16.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    // Enum is a data type used for organizing related constants, e.g. DaysOfTheWeek
    // Because they are constants, the names of an enum type's fields are in uppercase letters.
    public enum NavigationOption{
        NAVIGATION_OPTION_INBOX,
        NAVIGATION_OPTION_FAVORITES,
        NAVIGATION_OPTION_ARCHIVED
    }

    // 44.1: didSelectNavigationOption() invoked when user selects either Inbox, Favorites, or Archived
    //      didSelectFeed() invoked when user selects an RSS feed
    public static interface NavigationDrawerAdapterDelegate {
        public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationOption navigationOption);
        public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed);
    }

    // 44.1: WeakReference does not guarantee the object within it, leaving the object free to disappear at any moment.
    WeakReference<NavigationDrawerAdapterDelegate> delegate;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        // LayoutInflater - instantiates a layout XML into its corresponding View object
        // .inflate(int resource, ViewGroup root, boolean attachToRoot)
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.navigation_item, viewGroup, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        RssFeed rssFeed = null;

        // 43.6: If view position is beyond NavigationOption, show RSS feeds
        // The feedPosition is determined by taking position - 3 (number of NavigationOption)
        if(position >= NavigationOption.values().length){
            int feedPosition = position - NavigationOption.values().length;
            rssFeed = BloclyApplication.getSharedDataSource().getFeeds().get(feedPosition);
        }
        // update(int position, RssFeed rssFeed)
        viewHolder.update(position, rssFeed);
    }

    // 43.4: getItemCount() will return number of NavigationOption w/ number of RSS feeds found from
    //      DataSource, giving total of four items to display
    @Override
    public int getItemCount() {
        // Number of items our Adapter provides is returned by getItemCount()
        // NavigationOption.values().length = total number of navigation options
        // BloclyApplication.getSharedDataSource().getFeeds().size() = with the number of RSS feeds found in DataSource
        // This gives us a total of four items to display.
        return NavigationOption.values().length
                + BloclyApplication.getSharedDataSource().getFeeds().size();
    }

    // 44.1: Getter and setter for delegate
    // User WeakReference.get() to recover the object within. If original reference has been removed
    //      this method will return null
    public NavigationDrawerAdapterDelegate getDelegate() {
        if (delegate == null) {
            return null;
        }
        return delegate.get();
    }

    public void setDelegate(NavigationDrawerAdapterDelegate delegate) {
        this.delegate = new WeakReference<NavigationDrawerAdapterDelegate>(delegate);
    }

    // 43.4: ViewHolder describes a single item view and metadata about its place w/in RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        View topPadding;
        TextView title;
        View bottomPadding;
        View divider;

        // 44.1: Track the position and RSS feed for each ViewHolder
        int position;
        RssFeed rssFeed;

        public ViewHolder(View itemView) {
            super(itemView);
            topPadding = itemView.findViewById(R.id.v_nav_item_top_padding);
            title = (TextView) itemView.findViewById(R.id.tv_nav_item_title);
            bottomPadding = itemView.findViewById(R.id.v_nav_item_bottom_padding);
            divider = itemView.findViewById(R.id.v_nav_item_divider);
            // .setOnClickListener(onClickListener l)
            itemView.setOnClickListener(this);
        }

        void update(int position, RssFeed rssFeed){

            // Update these fields each time ViewHolder is updated
            this.position = position;
            this.rssFeed = rssFeed;

            // The ordinal() method returns the integer position of the enumerated value,
            // therefore NavigationOption.NAVIGATION_OPTION_INBOX.ordinal() == 0
            // The values() method recovers an array containing each enumerated value in order,
            // it's length is therefore 3
            boolean shouldShowTopPadding = position == NavigationOption.NAVIGATION_OPTION_INBOX.ordinal()
                    || position == NavigationOption.values().length;
            topPadding.setVisibility(shouldShowTopPadding ? View.VISIBLE : View.GONE);

            // NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal() == 1
            // getItemCount = 4
            boolean shouldShowBottomPadding = position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()
                    || position == getItemCount() - 1;
            bottomPadding.setVisibility(shouldShowBottomPadding ? View.VISIBLE: View.GONE);

            divider.setVisibility(position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()
                    ? View.VISIBLE : View.GONE);

            // To map each position to its respective title, we create a local array
            // with our three primary titles arranged in order
            if(position < NavigationOption.values().length){
                int[] titleTexts = new int[]{
                        R.string.navigation_option_inbox,
                        R.string.navigation_option_favorites,
                        R.string.navigation_option_archived
                };
                title.setText(titleTexts[position]);
            } else{
                title.setText(rssFeed.getTitle());
            }
        }

        /*
        * onClickListener
        */

        @Override
        public void onClick(View v) {
            // 44.2: NavigationDrawerAdapter.this provides a reference to the NavigationDrawerAdapter
            // Including NavigationDrawerAdapter w/in interface method 1) ensure method uniqueness
            //      and 2) provides reference to delegating object
            if (getDelegate() == null) {
                return;
            }
            if (position < NavigationOption.values().length) {
                getDelegate().didSelectNavigationOption(NavigationDrawerAdapter.this, NavigationOption.values()[position]);
            } else {
                getDelegate().didSelectFeed(NavigationDrawerAdapter.this, rssFeed);
            }
        }
    }
}
