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
 * Created by namlu on 21-Dec-16.
 */

public class RobotoTextView extends TextView{

    private static Map<String, Typeface> sTypefaces = new HashMap<>();

    public RobotoTextView(Context context) {
        super(context);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context);
        extractFont(attrs);
    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
        extractFont(attrs);
    }

    void extractFont(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            return;
        }

        // 36.6: Recover a TypedArray, a container for an array of values retrieved with
        //      obtainStyledAttributes(AttributeSet, int[], int, int)
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.Roboto, 0, 0);
        // 36.7: Obtain an integer corresponding to the robotoFont using
        //      getInteger(int index, int defValue)
        int robotoFontIndex = typedArray.getInteger(R.styleable.Roboto_robotoFont, -1);
        // 36.8: Per the documentation for TypedArray, call recycle() when done
        typedArray.recycle();
        // 36.9: Inflate resource string-array into an actual array of Strings
        String[] stringArray = getResources().getStringArray(R.array.roboto_font_file_names);

        if (robotoFontIndex < 0 || robotoFontIndex >= stringArray.length) {
            return;
        }
        String robotoFont = stringArray[robotoFontIndex];
        Typeface robotoTypeFace = null;

        // 36.10: Check for a cached Typeface for given filename, else inflate brand new Typeface
        //      and place in cache
        if (sTypefaces.containsKey(robotoFont)) {
            robotoTypeFace = sTypefaces.get(robotoFont);
        } else {
            // 36.11: Else inflate brand new Typeface and place in cache
            robotoTypeFace = Typeface.createFromAsset(
                    getResources().getAssets(), "fonts/RobotoTTF" + robotoFont);
            sTypefaces.put(robotoFont, robotoTypeFace);
        }
        // 36.12: Set the Typeface as this instance's typeface
        setTypeface(robotoTypeFace);
    }
}
