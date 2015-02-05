/*
 * Copyright (C) 2013 Daniel Velazco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.danvelazco.fbwrapper.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

/**
 * FacebookWebView.<br/>
 * Extends {@link android.webkit.WebView}.<br/>
 */
@SuppressLint("SetJavaScriptEnabled")
public class FacebookWebView extends WebView {

    // Constants, default values for this WebView's configuration
    final boolean DEFAULT_JS_ENABLED = true;
    final WebSettings.PluginState DEFAULT_PLUGIN_STATE = WebSettings.PluginState.ON_DEMAND;
    final boolean DEFAULT_SUPPORT_ZOOM = true;
    final boolean DEFAULT_SAVE_FORM_DATA = false;
    final boolean DEFAULT_SAVE_PASSWORD = false;
    final boolean DEFAULT_DOM_STORAGE_ENABLED = true;
    final boolean DEFAULT_ALLOW_GEOLOCATION = false;
    final boolean DEFAULT_ALLOW_FILE_UPLOAD = true;
    final boolean DEFAULT_WIDE_VIEWPORT = true;
    final boolean DEFAULT_LOAD_WITH_OVERVIEW_MODE = true;
    final int DEFAULT_CACHE_MODE = WebSettings.LOAD_DEFAULT;
    final WebSettings.RenderPriority DEFAULT_RENDER_PRIORITY = WebSettings.RenderPriority.HIGH;
    final int DEFAULT_SCROLLBAR_STYLE = WebView.SCROLLBARS_OUTSIDE_OVERLAY;

    // Members
    private boolean mInitialized = false;
    private Context mContext = null;
    private WebSettings mWebSettings = null;
    private FacebookWebViewClient mWebViewClient = null;
    private FacebookWebChromeClient mWebChromeClient = null;

    /**
     * Constructor.
     *
     * @param context {@link Context}
     */
    public FacebookWebView(Context context) {
        this(context, null);
    }

    /**
     * Constructor.
     *
     * @param context {@link Context}
     * @param attrs   {@link AttributeSet}
     */
    public FacebookWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor.
     *
     * @param context  {@link Context}
     * @param attrs    {@link AttributeSet}
     * @param defStyle {@link int}
     */
    public FacebookWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Do not try to initialize anything if it's in edit mode (layout editor)
        if (isInEditMode()) {
            return;
        }

        mContext = context;
        initializeWebView();
    }

    /**
     * Destroy this WebView
     */
    @Override
    public void destroy() {
        mInitialized = false;

        if (mWebChromeClient != null) {
            mWebChromeClient.destroy();
        }

        if (mWebViewClient != null) {
            mWebViewClient.destroy();
        }

        mContext = null;
        mWebSettings = null;
        mWebViewClient = null;
        mWebChromeClient = null;

        super.destroy();
    }

    /**
     * Initialize this WebView and set default values
     */
    private void initializeWebView() {
        // Create a new instance of FutebolWebViewClient and keep a reference to it
        mWebViewClient = new FacebookWebViewClient();
        setWebViewClient(mWebViewClient);

        // New instance of FutebolWebChromeClient and keep a reference to it
        mWebChromeClient = new FacebookWebChromeClient(mContext);
        setWebChromeClient(mWebChromeClient);

        // Get a reference of this WebView's WebSettings
        mWebSettings = getSettings();

        // We can consider this WebView initialized
        mInitialized = true;

        // Set default values
        setDefaults();
    }

    /**
     * Default settings and configuration for this WebView.
     */
    private void setDefaults() {
        // Default WebView style
        setScrollBarStyle(DEFAULT_SCROLLBAR_STYLE);

        // Default WebSettings
        mWebSettings.setJavaScriptEnabled(DEFAULT_JS_ENABLED);
        mWebSettings.setPluginState(DEFAULT_PLUGIN_STATE);
        mWebSettings.setSupportZoom(DEFAULT_SUPPORT_ZOOM);
        mWebSettings.setDisplayZoomControls(DEFAULT_SUPPORT_ZOOM);
        mWebSettings.setBuiltInZoomControls(DEFAULT_SUPPORT_ZOOM);
        mWebSettings.setSaveFormData(DEFAULT_SAVE_FORM_DATA);
        mWebSettings.setSavePassword(DEFAULT_SAVE_PASSWORD);
        mWebSettings.setDomStorageEnabled(DEFAULT_DOM_STORAGE_ENABLED);
        mWebSettings.setUseWideViewPort(DEFAULT_WIDE_VIEWPORT);
        mWebSettings.setLoadWithOverviewMode(DEFAULT_LOAD_WITH_OVERVIEW_MODE);
        mWebSettings.setCacheMode(DEFAULT_CACHE_MODE);
        mWebSettings.setRenderPriority(DEFAULT_RENDER_PRIORITY);

        // Default WebChromeClient settings
        mWebChromeClient.setAllowGeolocation(DEFAULT_ALLOW_GEOLOCATION);
        mWebChromeClient.setAllowFileUpload(DEFAULT_ALLOW_FILE_UPLOAD);
    }

    /**
     * Most of the {@link WebSettings} are being handled by the WebView
     * but this allows flexibility for the activity to furthermore
     * change other {@link WebSettings}.
     *
     * @return {@link WebSettings}
     */
    public WebSettings getWebSettings() {
        return mWebSettings;
    }

    /**
     * Set the custom view that can be used to add other views.
     * For example, this could be used for video playback.
     *
     * @param view {@link android.widget.FrameLayout}
     */
    public void setCustomContentView(FrameLayout view) {
        if (!mInitialized || (mWebChromeClient == null)) {
            throw new IllegalStateException("The WebView must be initialized first.");
        }
        mWebChromeClient.setCustomContentView(view);
    }

    /**
     * Set the listener for this WebChromeClient.
     *
     * @param listener {@link FacebookWebChromeClient.WebChromeClientListener}. It must be
     *                 in the Activity context.
     */
    public void setWebChromeClientListener(FacebookWebChromeClient.WebChromeClientListener listener) {
        if (!mInitialized || (mWebChromeClient == null)) {
            throw new IllegalStateException("The WebView must be initialized first.");
        }
        mWebChromeClient.setListener(listener);
    }

    /**
     * Allow web applications to access this device's location.<br/>
     * Need
     *
     * @param allow {@link boolean} flag stating whether or not to allow
     *              this web application to see the
     *              device's location.
     */
    public void setAllowGeolocation(boolean allow) {
        if (!mInitialized || (mWebChromeClient == null)) {
            throw new IllegalStateException("The WebView must be initialized first.");
        }
        mWebChromeClient.setAllowGeolocation(allow);
    }

    /**
     * Set the listener for this WebViewClient.
     *
     * @param listener {@link FacebookWebViewClient.WebViewClientListener}. It must be
     *                 in the Activity context.
     */
    public void setWebViewClientListener(FacebookWebViewClient.WebViewClientListener listener) {
        if (!mInitialized || (mWebViewClient == null)) {
            throw new IllegalStateException("The WebView must be initialized first.");
        }
        mWebViewClient.setListener(listener);
    }

    /**
     * Whether this WebViewClient should load any domain without
     * checking the internal domain list.
     *
     * @param allow {@link boolean}
     */
    public void setAllowAnyDomain(boolean allow) {
        if (!mInitialized || (mWebViewClient == null)) {
            throw new IllegalStateException("The WebView must be initialized first.");
        }
        mWebViewClient.setAllowAnyDomain(allow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Override the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // If the fullscreen view is showing, hide it
            if (mWebChromeClient != null) {
                if (mWebChromeClient.isCustomViewVisible()) {
                    mWebChromeClient.onHideCustomView();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
