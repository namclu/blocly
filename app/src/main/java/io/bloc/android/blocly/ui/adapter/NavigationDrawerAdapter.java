package io.bloc.android.blocly.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;

/**
 * Created by namlu on 12-Jan-17.
 */

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {


    // 43.3: enums are data types used for organizing related constants
    public enum NavigationOption {
        NAVIGATION_OPTION_INBOX,
        NAVIGATION_OPTION_FAVORITES,
        NAVIGATION_OPTION_ARCHIVED
    }

    // 43.3: View inflate(int resource, ViewGroup root, boolean attachToRoot)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.navigation_item, viewGroup, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        RssFeed rssFeed = null;

        // 43.6: If view position is beyond NavigationOption, show RSS feeds
        //      The feedPosition is determined by taking position - 3 (number of NavigationOption)
        if (position >= NavigationOption.values().length) {
            int feedPosition = position - NavigationOption.values().length;
            rssFeed = BloclyApplication.getSharedDataSource().getFeeds().get(feedPosition);
        }
        viewHolder.update(position, rssFeed);
    }

    // 43.4: getItemCount() will return number of NavigationOption w/ number of RSS feeds found from
    //      DataSource, giving total of four items to display
    @Override
    public int getItemCount() {
        return NavigationOption.values().length
                + BloclyApplication.getSharedDataSource().getFeeds().size();
    }

    // 43.4: ViewHolder describes a single item view and metadata about its place w/in RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View topPadding;
        TextView title;
        View bottomPadding;
        View divider;

        public ViewHolder(View itemView) {
            super(itemView);
            topPadding = itemView.findViewById(R.id.v_nav_item_top_padding);
            title = (TextView) itemView.findViewById(R.id.tv_nav_item_title);
            bottomPadding = itemView.findViewById(R.id.v_nav_item_bottom_padding);
            divider = itemView.findViewById(R.id.v_nav_item_divider);

            itemView.setOnClickListener(this);
        }

        /*
         * OnClickListener
         */
        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "Nothing... yet!", Toast.LENGTH_SHORT).show();
        }

        void update(int position, RssFeed rssFeed) {

            // 43.4: Top padding is shown either at NAVIGATION_OPTION_INBOX (position 0)
            //      or at the first feed item (position 3)
            boolean shouldShowTopPadding = position == NavigationOption.NAVIGATION_OPTION_INBOX.ordinal()
                    || position == NavigationOption.values().length;
            topPadding.setVisibility(shouldShowTopPadding ? View.VISIBLE : View.GONE);

            // 43.4: Bottom padding is shown either at NAVIGATION_OPTION_ARCHIVED (position 2)
            //      or at getItemCount() - 1 (last Rss feed object)
            boolean shouldShowBottomPadding = position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()
                    || position == getItemCount() - 1;
            bottomPadding.setVisibility(shouldShowBottomPadding ? View.VISIBLE : View.GONE);

            // 43.4: Set divider visibility at NAVIGATION_OPTION_ARCHIVED.ordinal()
            divider.setVisibility(position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()
                    ? View.VISIBLE : View.GONE);

            // 43.5: titleTexts[] represents the nav drawer options: Inbox, Favorites, Archived
            if (position < NavigationOption.values().length) {
                int[] titleTexts = new int[]{R.string.navigation_option_inbox,
                        R.string.navigation_option_favorites,
                        R.string.navigation_option_archived};

                title.setText(titleTexts[position]);
            } else {
                title.setText(rssFeed.getTitle());
            }
        }
    }
}
