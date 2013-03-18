package com.danvelazco.fbwrapper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

// TODO: write javadocs
public class FacebookWebView extends WebView {

    public FacebookWebView(Context context) {
        this(context, null);
    }

    public FacebookWebView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public FacebookWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initialize() {

        // Enable scrollbar fading
        setScrollbarFadingEnabled(true);

    }

}