package io.bloc.android.blocly.ui.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.DataSource;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;

/**
 * Created by namlu on 20-Jun-16.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder> {

    private static String TAG = ItemAdapter.class.getSimpleName();

    // RssItem ArrayList to keep track of which objects have been checked
    private List<RssItem> myRssItems = new ArrayList<>();

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

    // Extends RecyclerView.ViewHolder as RecyclerView.ViewHolder is an abstract class
    // Inner class responsible for representing a single View created and returned by an Adapter
    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements ImageLoadingListener,
            View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        TextView title;
        TextView feed;
        TextView content;
        View headerWrapper;
        ImageView headerImage;
        // Reference to RssItem to act on the data associated with each ItemAdapterViewHolder
        RssItem rssItem;
        // References to Checkbox objects
        CheckBox archiveCheckBox;
        CheckBox favoriteCheckBox;

        public ItemAdapterViewHolder(View itemView){
            //itemView is a reference to inflated version of rss_item.xml
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.tv_rss_item_feed_title);
            feed = (TextView)itemView.findViewById(R.id.tv_rss_item_feed_title);
            content = (TextView)itemView.findViewById(R.id.tv_rss_item_content);
            // Assign the inflated FrameLayout View to a basic View field
            headerWrapper = itemView.findViewById(R.id.fl_rss_item_image_header);
            headerImage = (ImageView) itemView.findViewById(R.id.iv_rss_item_image);
            // References to Checkbox views
            archiveCheckBox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_check_mark);
            favoriteCheckBox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_favorite_star);
            // A single itemView maps to a single ViewHolder, and this pair of objects is reused
            // and recycled to accommodate larger sets of data
            // setOnClickListener(onClickListener l);
            itemView.setOnClickListener(this);
            // Set ItemAdapterViewHolder as the OnCheckedChangeListener for both check boxes
            // setOnCheckedChangeListener(onCheckChangeListener listener);
            archiveCheckBox.setOnCheckedChangeListener(this);
            favoriteCheckBox.setOnCheckedChangeListener(this);
        }

        void update(RssFeed rssFeed, RssItem rssItem){
            this.rssItem = rssItem;
            feed.setText(rssFeed.getTitle());
            title.setText(rssItem.getTitle());
            content.setText(rssItem.getDescription());
            if(rssItem.getImageUrl() != null){
                headerWrapper.setVisibility(View.VISIBLE);
                headerImage.setVisibility(View.INVISIBLE);
                // Attempt to load the image
                // loadImage(String url, ImageLoadingListener listener)
                ImageLoader.getInstance().loadImage(rssItem.getImageUrl(), this);
            } else{
                headerWrapper.setVisibility(View.GONE);
            }
            // If rssItem.isArchived() is true, then set archiveCheckbox to checked state
            if(rssItem.isArchived()){
                archiveCheckBox.setSelected(true);
            }
            // If rssItem.isFavorite() is true, then set favoriteCheckbox to checked state
            if(rssItem.isFavorite()){
                favoriteCheckBox.setSelected(true);
            }
        }

         /*
        * ImageLoadingListener starts here
        * */

        // Called when image loading task was started
        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        // Called when an error has occurred during image loading
        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            // .e(String tag, String msg)
            Log.e(TAG, "onLoadingFailed: " + failReason.toString() + " for URL: " + rssItem.getImageUrl());
        }

        // Called when image is loaded successfully (and displayed in View if one was specified)
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if(imageUri.equals(rssItem.getImageUrl())){
                headerImage.setImageBitmap(loadedImage);
                headerImage.setVisibility(View.VISIBLE);
            }
        }

        // Call when image loading task was cancelled because View for image was reused in newer task
        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            // Attempt a retry
            ImageLoader.getInstance().loadImage(imageUri, this);
        }

        /*
        * onClickListener starts here
        * */

        @Override
        // Abstract and only method of View.OnClickListener
        public void onClick(View view) {
            // makeText(Context context, CharSequence text, int duration).show();
            Toast.makeText(view.getContext(), rssItem.getTitle(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonButton, boolean isChecked) {
            Log.v(TAG, "Checked changed to: " + isChecked);
            // If button pressed is archiveCheckbox && isChecked is true then
            // setArchived = true
            if(buttonButton.equals(archiveCheckBox) && isChecked){
                // rssItem.setArchived(isChecked);
                rssItem.setArchived(isChecked);
            }
            // If button pressed is favoriteCheckbox && isChecked is true then
            // setFavorite = true
            if(buttonButton.equals(favoriteCheckBox) && isChecked){
                // rssItem.setFavorite(isChecked);
                rssItem.setFavorite(isChecked);
            }
        }
    }

}