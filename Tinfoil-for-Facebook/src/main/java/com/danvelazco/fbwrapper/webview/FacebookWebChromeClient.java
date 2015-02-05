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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.danvelazco.fbwrapper.R;
import com.danvelazco.fbwrapper.util.Logger;

/**
 * FacebookWebChromeClient.<br/> Extends {@link android.webkit.WebChromeClient}.<br/> Used by {@link FacebookWebView}.
 */
public class FacebookWebChromeClient extends WebChromeClient {

    // Members
    private Context mContext = null;
    private boolean mAllowGeolocation = false;
    private boolean mAllowFileUpload = false;
    private FrameLayout mCustomContentView = null;
    private CustomViewCallback mCustomViewCallback;
    private Bitmap mDefaultVideoPoster = null;
    private View mVideoProgressView = null;
    private WebChromeClientListener mListener = null;

    /**
     * Constructor.
     *
     * @param context
     *         {@link Context}
     */
    public FacebookWebChromeClient(Context context) {
        mContext = context;
    }

    /**
     * Destroy WebChromeClient instance.
     */
    public void destroy() {
        mContext = null;
        mDefaultVideoPoster = null;
        mVideoProgressView = null;
        mCustomContentView = null;
        mCustomViewCallback = null;
        mListener = null;
    }

    /**
     * Allow web applications to access this device's location.<br/> Need
     *
     * @param allow
     *         {@link boolean} flag stating whether or not to allow this web application to see the device's location.
     */
    public void setAllowGeolocation(boolean allow) {
        mAllowGeolocation = allow;
    }

    /**
     * Allow web applications to access this device's location.<br/> Need
     *
     * @param allow
     *         {@link boolean} flag stating whether or not to allow this web application to see the device's location.
     */
    public void setAllowFileUpload(boolean allow) {
        mAllowFileUpload = allow;
    }

    /**
     * Set the custom view that can be used to add other views. For example, this could be used for video playback.
     *
     * @param view
     *         {@link FrameLayout}
     */
    public void setCustomContentView(FrameLayout view) {
        mCustomContentView = view;
    }

    /**
     * Check whether or not the custom view is currently showing.
     *
     * @return {@link boolean}
     */
    public boolean isCustomViewVisible() {
        return mCustomContentView != null && (mCustomContentView.getVisibility() == View.VISIBLE);
    }

    /**
     * Set the listener for this WebChromeClient.
     *
     * @param listener
     *         {@link WebChromeClientListener}.
     */
    public void setListener(WebChromeClientListener listener) {
        // Set the listener
        mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProgressChanged(WebView view, int progress) {
        // Fire off the callback
        if (mListener != null) {
            mListener.onProgressChanged(view, progress);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap getDefaultVideoPoster() {
        if (mDefaultVideoPoster == null) {
            mDefaultVideoPoster = BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.default_video_poster);
        }
        return mDefaultVideoPoster;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getVideoLoadingProgressView() {
        if (mVideoProgressView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
        }
        return mVideoProgressView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (view instanceof FrameLayout) {
            // Add the new view to the view container, make it visible
            mCustomContentView.addView(view);
            mCustomContentView.setVisibility(View.VISIBLE);

            // Keep a reference of the CustomViewCallback
            mCustomViewCallback = callback;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHideCustomView() {

        // [TODO][BUG]: video player won't close sometimes, only toggle media controls
        // [TODO][BUG]: sound from video will keep playing sometimes

        if (mCustomViewCallback != null) {
            // Fire the callback
            mCustomViewCallback.onCustomViewHidden();
        }

        if (mCustomContentView != null) {
            // Remove all views from the container and hide it
            mCustomContentView.removeAllViews();
            mCustomContentView.setVisibility(View.GONE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        // Log console messages in debug
        Logger.d(getClass().getSimpleName(), "onConsoleMessage: ");
        Logger.d(getClass().getSimpleName(), "\tmessage: " + consoleMessage.message());
        Logger.d(getClass().getSimpleName(), "\tlineNumber: " + consoleMessage.lineNumber());
        Logger.d(getClass().getSimpleName(), "\tsourceID: " + consoleMessage.sourceId());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin,
                                                   GeolocationPermissions.Callback callback) {
        // If we are not allowed to use geolocation, show an alert, if possible.
        if (!mAllowGeolocation) {
            if (mListener != null) {
                mListener.showGeolocationDisabledAlert();
            }
        }

        // Invoke the callback stating whether or not geolocation is allowed
        callback.invoke(origin, mAllowGeolocation, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGeolocationPermissionsHidePrompt() {
        if (mListener != null) {
            mListener.hideGeolocationAlert();
        }
    }

    /**
     * Handle file upload. Used for backwards compatibility.
     *
     * @param uploadMsg
     *         A ValueCallback to set the URI of the file to upload. onReceiveValue must be called to wake up the
     *         thread.
     */
    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "", "");
    }

    /**
     * Handle file upload. Used for backwards compatibility.
     * <p/>
     * See {@link #openFileChooser(android.webkit.ValueCallback, String, String)}
     *
     * @param uploadMsg
     *         A ValueCallback to set the URI of the file to upload. onReceiveValue must be called to wake up the
     *         thread.
     * @param acceptType
     *         The value of the 'accept' attribute of the input tag associated with this file picker.
     */
    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, acceptType, "");
    }

    /**
     * Tell the client to open a file chooser.
     *
     * @param uploadMsg
     *         A ValueCallback to set the URI of the file to upload. onReceiveValue must be called to wake up the
     *         thread.
     * @param acceptType
     *         The value of the 'accept' attribute of the input tag associated with this file picker.
     * @param capture
     *         The value of the 'capture' attribute of the input tag associated with this file picker.
     */
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        // Fire off the callback
        if (mListener != null && mAllowFileUpload) {
            mListener.openFileChooser(uploadMsg, acceptType, capture);
        }
    }

    /**
     * Tell the client to open a file chooser.
     *
     * @param filePathCallback
     *         {@link ValueCallback} used to set the list of URI(s) of the file(s) to upload. onReceiveValue must be
     *         called to wake up the thread.
     * @param fileChooserParams
     *         {@link FileChooserParams} parameters used for the file chooser
     * @return {@link boolean} true if the file chooser will be opened, false if not
     */
    @SuppressWarnings("unused")
    public boolean openFileChooser(ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        // Fire off the callback
        return mListener != null && mAllowFileUpload
                && mListener.openFileChooser(filePathCallback, fileChooserParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        return openFileChooser(filePathCallback, fileChooserParams);
    }

    /**
     * Listener interface used to fire callbacks for progress changed and opening a file chooser for file upload.
     */
    public interface WebChromeClientListener {

        /**
         * Called anytime the progress of the page being loaded changes.
         *
         * @param view
         *         {@link WebView} where the page is being loaded.
         * @param progress
         *         {@link int} progress.
         */
        void onProgressChanged(WebView view, int progress);

        /**
         * Called anytime the web site is trying to access geolocation data and this client is not allowing us to use
         * it. Use this to show an alert to the user in the case they want to enable the use of geolocation.
         */
        void showGeolocationDisabledAlert();

        /**
         * Called when the website that had previously requested access to geolocation data no longer needs it.
         */
        void hideGeolocationAlert();

        /**
         * This method will be called anytime the file chooser has to be opened in order to upload a file.
         * <p/>
         * Must call {@link #setAllowFileUpload(boolean)} and set the value to true before trying this.
         *
         * @param uploadMsg
         *         A ValueCallback to set the URI of the file to upload. onReceiveValue must be called to wake up the
         *         thread.
         * @param acceptType
         *         The value of the 'accept' attribute of the input tag associated with this file picker.
         * @param capture
         *         The value of the 'capture' attribute of the input tag associated with this file picker.
         */
        void openFileChooser(ValueCallback<Uri> uploadMsg,
                             String acceptType, String capture);

        /**
         * This method will be called anytime the file chooser has to be opened when uploading a file.
         * <p/>
         * Must call {@link #setAllowFileUpload(boolean)} and set the value to true before trying this.
         *
         * @param filePathCallback
         *         {@link ValueCallback} used to set the list of URI(s) of the file(s) to upload. onReceiveValue must be
         *         called to wake up the thread.
         * @param fileChooserParams
         *         {@link FileChooserParams} parameters used for the file chooser
         * @return {@link boolean} true if the file chooser will be opened, false if not
         */
        boolean openFileChooser(ValueCallback<Uri[]> filePathCallback,
                                FileChooserParams fileChooserParams);

    }

}
