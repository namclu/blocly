package io.bloc.android.blocly.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.DataSource;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;

/**
 * Created by namlu on 20-Jun-16.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder>{

    @Override
    // Required method which asks us to create and return a ViewHolder, specifically one
    // matching the class we supplied as our typed-parameter, ItemAdapterViewHolder
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index){
        //LayoutInflater - instantiates a layout XML into its corresponding View object
        //inflate(int resource, ViewGroup root, boolean attachToRoot);
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rss_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    // onBindViewHolder(VH holder, int position), requested when an index needs its data mapped
    // to a given ViewHolder
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index){
        DataSource shareDataSource = BloclyApplication.getSharedDataSource();
        itemAdapterViewHolder.update(shareDataSource.getFeeds().get(0), shareDataSource.getItems().get(index));
    }

    @Override
    public int getItemCount(){
        return BloclyApplication.getSharedDataSource().getItems().size();
    }

    // When to use an inner class?
    // Inner class responsible for representing a single View created and returned by an Adapter
    class ItemAdapterViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        TextView feed;
        TextView content;

        public ItemAdapterViewHolder(View itemView){
            //itemView is a reference to inflated version of rss_item.xml
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.tv_rss_item_feed_title);
            feed = (TextView)itemView.findViewById(R.id.tv_rss_item_feed_title);
            content = (TextView)itemView.findViewById(R.id.tv_rss_item_content);
        }

        void update(RssFeed rssFeed, RssItem rssItem){
            feed.setText(rssFeed.getTitle());
            title.setText(rssItem.getTitle());
            content.setText(rssItem.getDescription());
        }
    }

}