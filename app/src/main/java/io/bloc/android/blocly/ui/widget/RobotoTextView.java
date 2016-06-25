package io.bloc.android.blocly.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import io.bloc.android.blocly.R;

/**
 * Created by namlu on 24-Jun-16.
 */
public class RobotoTextView extends TextView{

    // Allocate a static map to track existing Typeface
    private static Map<String, Typeface> sTypefaces = new HashMap<String, Typeface>();

    //Required public constructor #1
    public RobotoTextView(Context context) {
        super(context);
    }

    //Required public constructor #3
    public RobotoTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        extractFont(attrs);
    }

    //Required public constructor #3
    public RobotoTextView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        extractFont(attrs);
    }

    void extractFont(AttributeSet attrs) {

        //Checks to see if RobotoTextview is being previewed in a WYSIWYG editor
        if(isInEditMode()){
            return;
        }
        if(attrs == null){
            return;
        }

        // TypedArray - Container for an array of values that were retrieved with
        // obtainStyledAttributes(AttributeSet, int[], int, int);
        // Be sure to call recycle() when done with them
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.Roboto, 0, 0);

        // Retrieves an integer from typedArray corresponding to robotoFont
        // which was declared in attrs.xml
        // getInteger(int index, int defValue);
        int robotoFontIndex = typedArray.getInteger(R.styleable.Roboto_robotoFont, -1);

        typedArray.recycle();

        // Inflate string-array from arrays.xml into actual array of Strings
        // getStringArray(int id)
        String[] stringArray = getResources().getStringArray(R.array.roboto_font_file_names);
        if(robotoFontIndex< 0 || robotoFontIndex >= stringArray.length){
            return;
        }
        String robotoFont = stringArray[robotoFontIndex];
        // What is this variable used for?
        Typeface robotoTypeface = null;

        // Check for a cached Typeface for the given filename.
        // If Typeface exists - return the reference  to robotoFont
        // else - inflate a brand new Typeface object
        // Typeface.createFromAsset(AssetManager mgr, String path)
        if(sTypefaces.containsKey(robotoFont)){
            robotoTypeface = sTypefaces.get(robotoFont);
        } else{
            robotoTypeface = Typeface.createFromAsset(
                    getResources().getAssets(), "fonts/RobotoTTF/" + robotoFont);
            sTypefaces.put(robotoFont, robotoTypeface);
        }
        // Set the Typeface, inflated or cached, as this instance's typeface
        setTypeface(robotoTypeface);
    }
}
