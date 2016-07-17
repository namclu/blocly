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

    public static interface NavigationDrawerAdapterDelegate{
        public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationOption navigationOption);
        public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed);
    }

    // NavigationDrawerAdapter will not own the delegate object and therefore when the object
    // is de-referenced elsewhere, NavigationDrawerAdapter should not keep it
    // A WeakReference allows us to use an object as long as a strong reference to it exists somewhere
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

        // When binding each ViewHolder to a position, we recover its RssFeed object
        // if it is found below the three primary navigation elements
        // NavigationOption.values().length = total number of navigational elements, used to
        // offset position into the full RssFeed array because the first RssFeed item is located
        // at position 3 within the adapter but at position 0 in the original RssFeed array.
        if(position >= NavigationOption.values().length){
            int feedPosition = position - NavigationOption.values().length;
            rssFeed = BloclyApplication.getSharedDataSource().getFeeds().get(feedPosition);
        }
        // update(int position, RssFeed rssFeed)
        viewHolder.update(position, rssFeed);
    }

    @Override
    public int getItemCount() {
        // Number of items our Adapter provides is returned by getItemCount()
        // NavigationOption.values().length = total number of navigation options
        // BloclyApplication.getSharedDataSource().getFeeds().size() = with the number of RSS feeds found in DataSource
        // This gives us a total of four items to display.
        return NavigationOption.values().length
                + BloclyApplication.getSharedDataSource().getFeeds().size();
    }

    // Add getter and setter method for delegate
    public NavigationDrawerAdapterDelegate getDelegate(){
        // If the original reference has been removed, this method will return null
        if(delegate == null){
            return null;
        }
        // Use WeakReference.get() to recover the object within
        return delegate.get();
    }

    public void setDelegate(NavigationDrawerAdapterDelegate delegate){
        this.delegate = new WeakReference<NavigationDrawerAdapterDelegate>(delegate);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        View topPadding;
        TextView title;
        View bottomPadding;
        View divider;

        // Track the position and RSS feed for each ViewHolder, similar to ItemAdapterViewHolder.
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
            // Write over fields each time the ViewHolder is updated.
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
            // NavigationDrawerAdapter.this provides a reference to the NavigationDrawerAdapter
            // Including NavigationDrawerAdapter within the interface's methods is done for two reasons:
            // 1) Ensure method uniqueness
            // 2) Provide a reference to the delegating object
            if(getDelegate() == null){
                return;
            }
            if(position < NavigationOption.values().length){
                // .didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationOption navigationOption)
                getDelegate().didSelectNavigationOption(NavigationDrawerAdapter.this,
                        NavigationOption.values()[position]);
            }else {
                // .didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed);
                getDelegate().didSelectFeed(NavigationDrawerAdapter.this, rssFeed);
            }

        }
    }
}
