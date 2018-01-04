package com.example.hl4350hb.inspirationapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * This Class defines a custom ImageView widget. This ImageView scales the image
 * to the proportional size of the whole screen. The displayer.xml layout
 * utilizes this widget to show the full size of the passed image.
 */

public class ScaleImageView extends android.support.v7.widget.AppCompatImageView {

    // Varying Constructors.
    public ScaleImageView(Context context) {
        super(context);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Gets image from widget and calculates the scaling factor.
        Drawable d = getDrawable();
        if (d != null) {
            int w = MeasureSpec.getSize(widthMeasureSpec);
            int h = w * d.getIntrinsicHeight() / d.getIntrinsicWidth();
            setMeasuredDimension(w, h);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
