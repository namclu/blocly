package io.bloc.android.blocly.ui.adapter;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.sql.Wrapper;

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

        // Boolean to track expansion state
        boolean contentExpanded;
        boolean imageExpanded;

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

        // References to our hidden Views.
        View expandedContentWrapper;
        TextView expandedContent;
        TextView visitSite;

        public ItemAdapterViewHolder(View itemView){
            //itemView is a reference to inflated version of rss_item.xml
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.tv_rss_item_title);
            feed = (TextView)itemView.findViewById(R.id.tv_rss_item_feed_title);
            content = (TextView)itemView.findViewById(R.id.tv_rss_item_content);
            // Assign the inflated FrameLayout View to a basic View field
            headerWrapper = itemView.findViewById(R.id.fl_rss_item_image_header);
            headerImage = (ImageView) itemView.findViewById(R.id.iv_rss_item_image);

            // References to Checkbox views
            archiveCheckBox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_check_mark);
            favoriteCheckBox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_favorite_star);

            // Extract the hidden views
            expandedContentWrapper = itemView.findViewById(R.id.ll_rss_item_expanded_content_wrapper);
            expandedContent = (TextView) itemView.findViewById(R.id.tv_rss_item_content_full);
            visitSite = (TextView) itemView.findViewById(R.id.tv_rss_item_visit_site);

            // A single itemView maps to a single ViewHolder, and this pair of objects is reused
            // and recycled to accommodate larger sets of data
            // setOnClickListener(onClickListener l);
            itemView.setOnClickListener(this);

            // Set visitSite's onClickListener
            visitSite.setOnClickListener(this);

            // Set ItemAdapterViewHolder as the OnCheckedChangeListener for both check boxes
            // setOnCheckedChangeListener(onCheckChangeListener listener);
            archiveCheckBox.setOnCheckedChangeListener(this);
            favoriteCheckBox.setOnCheckedChangeListener(this);
        }

        void update(RssFeed rssFeed, RssItem rssItem){
            this.rssItem = rssItem;
            title.setText(rssItem.getTitle());
            feed.setText(rssFeed.getTitle());

            // content and expandedContent will present the same text: the RSS item's story
            content.setText(rssItem.getDescription());
            expandedContent.setText(rssItem.getDescription());

            if(rssItem.getImageUrl() != null){
                headerWrapper.setVisibility(View.VISIBLE);
                headerImage.setVisibility(View.INVISIBLE);
                // Attempt to load the image
                // loadImage(String url, ImageLoadingListener listener)
                ImageLoader.getInstance().loadImage(rssItem.getImageUrl(), this);
            } else{
                headerWrapper.setVisibility(View.GONE);
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
            animateImage(!imageExpanded);
            Log.e(TAG, "onLoadingFailed: " + failReason.toString() + " for URL: " + rssItem.getImageUrl());
        }

        // Called when image is loaded successfully (and displayed in View if one was specified)
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if(imageUri.equals(rssItem.getImageUrl())){
                headerImage.setImageBitmap(loadedImage);
                headerImage.setVisibility(View.VISIBLE);
            }
            animateImage(imageExpanded);
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

            if(view == itemView){
                //animateImage(!imageExpanded);
                animateContent(!contentExpanded);
            } else{
                // Clicking visitSite will show a Toast.
                // makeText(Context context, CharSequence text, int duration).show();
                Toast.makeText(view.getContext(), "Visit " + rssItem.getUrl(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonButton, boolean isChecked) {
            Log.v(TAG, "Checked changed to: " + isChecked);
        }

        /*
        * Private methods
        * */

        // The purpose of animateContent is to expand or contract content
        private void animateContent(final boolean expand){

            //  If RSS item is already in the desired state, simply return
            if((expand && contentExpanded) || (!expand && !contentExpanded)){
                return;
            }

            // If we must animate, create initial and final height variables to animate between
            int startingHeight = expandedContentWrapper.getMeasuredHeight();
            int finalHeight = content.getMeasuredHeight();

            if(expand){
                // When expanding, set the starting height to that of the preview content
                // Make full-length content visible but transparent and then animate from
                // full transparency to full opacity.
                startingHeight = finalHeight;
                expandedContentWrapper.setAlpha(0f);
                expandedContentWrapper.setVisibility(View.VISIBLE);

                // To determine the target height of expansion, invoke View's measure(int, int)
                // method, which asks a View to measure itself given the constraints provided
                // We constrain it to the width of content but leave its height unlimited.
                // .measure(int widthMeasureSpec, int heightMeasureSpec)
                // .makeMeasureSpec(int size, int mode)
                expandedContentWrapper.measure(
                        View.MeasureSpec.makeMeasureSpec(content.getWidth(), View.MeasureSpec.EXACTLY),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                // getMeasuredHeight() provides the height (in pixels) that expandedContentWrapper wishes to be
                finalHeight = expandedContentWrapper.getMeasuredHeight();
            } else {
                content.setVisibility(View.VISIBLE);
            }

            //  AnimatorUpdateListener receives updates during an animation
            startAnimator(startingHeight, finalHeight, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                // An animation's progress is between 0.0 and 1.0.
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    // Percent completion used to set the opacity level acts as
                    // a cross-fade from one View to the other
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    float wrapperAlpha = expand ? animatedFraction : 1f - animatedFraction;
                    float contentAlpha = 1f - wrapperAlpha;

                    expandedContentWrapper.setAlpha(wrapperAlpha);
                    content.setAlpha(contentAlpha);
                    // LayoutParams define a View's width and height programmatically
                    // When the animation completes – animatedFraction == 1.0f
                    // we revert the height to WRAP_CONTENT, as defined in rss_item.xml
                    expandedContentWrapper.getLayoutParams().height = animatedFraction == 1f ?
                            ViewGroup.LayoutParams.WRAP_CONTENT :
                            (Integer) valueAnimator.getAnimatedValue();

                    // Once finished altering the View's LayoutParams, we invoke requestLayout(),
                    // which asks the View to redraw itself on screen.
                    expandedContentWrapper.requestLayout();
                    if(animatedFraction == 1f){
                        if(expand){
                            content.setVisibility(View.GONE);
                        } else{
                            expandedContentWrapper.setVisibility(View.GONE);
                        }
                    }
                }
            });
            contentExpanded = expand;
        }

        // The purpose of animateImage is to expand or contract content
        private void animateImage(final boolean expand){

            //  If RSS item is already in the desired state, simply return
            if((expand && imageExpanded) || (!expand && !imageExpanded)){
                return;
            }

            // If we must animate, create initial and final height variables to animate between
            int startingHeight = 0;
            int finalHeight = headerImage.getMeasuredHeight();

            if(expand){
                // When expanding, set the starting height to that of the preview content
                // Make full-length content visible but transparent and then animate from
                // full transparency to full opacity.
                startingHeight = finalHeight;
                headerWrapper.setAlpha(0f);
                headerWrapper.setVisibility(View.VISIBLE);

                // To determine the target height of expansion, invoke View's measure(int, int)
                // method, which asks a View to measure itself given the constraints provided
                // We constrain it to the width of content but leave its height unlimited.
                // .measure(int widthMeasureSpec, int heightMeasureSpec)
                // .makeMeasureSpec(int size, int mode)
                headerWrapper.measure(
                        View.MeasureSpec.makeMeasureSpec(headerImage.getWidth(), View.MeasureSpec.EXACTLY),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                // getMeasuredHeight() provides the height (in pixels) that expandedContentWrapper wishes to be
                finalHeight = headerWrapper.getMeasuredHeight();
            } else {
                headerImage.setVisibility(View.VISIBLE);
            }

            //  AnimatorUpdateListener receives updates during an animation
            startAnimator(startingHeight, finalHeight, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                // An animation's progress is between 0.0 and 1.0.
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    // Percent completion used to set the opacity level acts as
                    // a cross-fade from one View to the other
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    float wrapperAlpha = expand ? animatedFraction : 1f - animatedFraction;
                    float contentAlpha = 1f - wrapperAlpha;

                    headerWrapper.setAlpha(wrapperAlpha);
                    headerImage.setAlpha(contentAlpha);
                    // LayoutParams define a View's width and height programmatically
                    // When the animation completes – animatedFraction == 1.0f
                    // we revert the height to WRAP_CONTENT, as defined in rss_item.xml
                    headerWrapper.getLayoutParams().height = animatedFraction == 1f ?
                            ViewGroup.LayoutParams.WRAP_CONTENT :
                            (Integer) valueAnimator.getAnimatedValue();

                    // Once finished altering the View's LayoutParams, we invoke requestLayout(),
                    // which asks the View to redraw itself on screen.
                    headerWrapper.requestLayout();
                    if(animatedFraction == 1f){
                        if(expand){
                            headerImage.setVisibility(View.GONE);
                        } else{
                            headerWrapper.setVisibility(View.GONE);
                        }
                    }
                }
            });
            imageExpanded = expand;
        }

        private void startAnimator(int start, int end, ValueAnimator.AnimatorUpdateListener animatorUpdateListener){
            ValueAnimator valueAnimator =  ValueAnimator.ofInt(start, end);
            valueAnimator.addUpdateListener(animatorUpdateListener);

            // Set the duration of animation
            // Android is bundled with its own value resources, any application may access them
            // by referring to android.R...
            valueAnimator.setDuration(itemView.getResources().getInteger(android.R.integer.config_mediumAnimTime));

            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.start();
        }
    }

}