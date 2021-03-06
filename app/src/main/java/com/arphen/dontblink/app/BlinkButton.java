package com.arphen.dontblink.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageButton;


/**
 * TODO: document your custom view class.
 */
public class BlinkButton extends ImageButton {

    private Runnable hide;
    private Runnable show;
    private boolean onTop = true;

    public BlinkButton(Context context) {
        super(context);
        init(null, 0);
    }

    public BlinkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }
    public BlinkButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void hide() {
        removeCallbacks(hide);
        postDelayed(hide, 3000);
        onTop = false;
    }

    public void showperm() {
        onTop = true;
        removeCallbacks(hide);
        postDelayed(show, 0);
    }

    public void show() {
        if (!onTop) {
            removeCallbacks(hide);
            postDelayed(show, 0);
            hide();
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        hide = new Runnable() {
            @Override
            public void run() {
                animate().alpha(0f).setDuration(1000).setListener(null);
            }
        };
        show = new Runnable() {
            @Override
            public void run() {
                animate().alpha(1f).setDuration(1000).setListener(null);
            }
        };
    }

}
