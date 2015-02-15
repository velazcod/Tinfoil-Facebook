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

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.KeyEvent;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.danvelazco.fbwrapper.util.Logger;

/**
 * FacebookWebViewClient.<br/>
 * Extends {@link android.webkit.WebViewClient}.<br/>
 * Used by {@link FacebookWebView}.
 */
public class FacebookWebViewClient extends WebViewClient {

    // Members
    private WebViewClientListener mListener = null;
    private boolean mAllowAnyUrl = false;

    /**
     * Set the listener for this WebViewClient.
     *
     * @param listener {@link WebViewClientListener}. It must be
     *                 in the Activity context.
     */
    public void setListener(WebViewClientListener listener) {
        mListener = listener;
    }

    /**
     * Destroy WebViewClient instance.
     */
    public void destroy() {
        mListener = null;
    }

    /**
     * Whether this WebViewClient should load any domain without
     * checking the internal domain list.
     *
     * @param allow {@link boolean}
     */
    public void setAllowAnyDomain(boolean allow) {
        mAllowAnyUrl = allow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceivedError(WebView view, int errorCod, String description, String failingUrl) {
        Logger.e(getClass().getSimpleName(), "This WebView has received an error while trying to load:");
        Logger.e(getClass().getSimpleName(), "\tError code: " + errorCod);
        Logger.e(getClass().getSimpleName(), "\tDescription: " + description);
        Logger.e(getClass().getSimpleName(), "\tFailed URL: " + failingUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResourceResponse shouldInterceptRequest (final WebView view, String url) {
        // We are currently not intercepting any resources
        return super.shouldInterceptRequest(view, url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        // Allow the WebView to handle all KeyEvents for now
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Logger.d(getClass().getSimpleName(), "shouldOverrideUrlLoading? " + url);

        // Do not override any type of loading if we can load any URL
        if (mAllowAnyUrl) {
            Logger.d(getClass().getSimpleName(), "The user is allowing us to open any URL in this WebView, let it load.");
            return false;
        }

        // Avoid NPEs when clicking on weird blank links
        if (url.equals("about:blank")) {
            Logger.d(getClass().getSimpleName(), "Blank page, let it load");
            return false;
        }

        // Get the URL's domain name
        String domain = Uri.parse(url).getHost();

        Logger.d(getClass().getSimpleName(), "Checking URL: " + url);
        Logger.d(getClass().getSimpleName(), "\tDomain: " + domain);

        if (domain != null) {
            // Let this WebView open the URL
            // TODO: Check the proper domain names that facebook uses or find another way
            if (domain.contains("facebook") || domain.contains("fb")) {
                Logger.d(getClass().getSimpleName(), "This URL should be loaded internally. Let it load.");
                view.loadUrl(url);
                return false;
            }
        }

        // Otherwise, fire the listener to open the URL by
        // any app that can handle it
        Logger.d(getClass().getSimpleName(), "This URL should be loaded by a 3rd party. Override.");
        fireOpenExternalSiteListener(url);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        // Fire the callback
        fireOnPageStartedListener(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        // Fire the callback
        fireOnPageFinishedListener(url);
    }

    /**
     * Fire off the onPageLoadStarted callback.
     *
     * @param url {@link String} The url that just started loading.
     */
    private void fireOnPageStartedListener(String url) {
        if (mListener != null) {
            mListener.onPageLoadStarted(url);
        }
    }

    /**
     * Fire off the onPageLoadFinished callback.
     *
     * @param url {@link String} The url that just finished loading.
     */
    private void fireOnPageFinishedListener(String url) {
        if (mListener != null) {
            mListener.onPageLoadFinished(url);
        }
    }

    /**
     * Fire off the openExternalSite callback.
     * The listener {@link Activity} should just hand off this
     * url as an intent to make sure some other app can handle it.
     */
    private void fireOpenExternalSiteListener(String url) {
        if (mListener != null) {
            mListener.openExternalSite(url);
        }
    }

    /**
     * Listener interface used to fire callbacks for page load
     * started, finished as well as handing off opening an external url
     */
    public interface WebViewClientListener {

        /**
         * Notify the host activity that a page has started loading.
         *
         * @param url {@link String} The url that just started loading.
         */
        void onPageLoadStarted(String url);

        /**
         * Notify the host activity that a page has finished loading.
         *
         * @param url {@link String} The url that just finished loading.
         */
        void onPageLoadFinished(String url);

        /**
         * Notify the host activity that it should hand off this URL
         * as an intent to any application that can open it since it
         * won't be handled by us.
         *
         * @param url {@link String} The url to be loaded.
         */
        void openExternalSite(String url);

    }
}
