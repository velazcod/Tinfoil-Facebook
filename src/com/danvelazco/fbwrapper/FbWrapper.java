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

package com.danvelazco.fbwrapper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Activity with a WebView wrapping facebook.com with its own CookieSyncManager
 * to hold cookies persistently.
 *
 * @author Daniel Velazco
 *
 */
@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
public class FbWrapper extends Activity implements Constants, OnGestureListener {

    // Constants
    protected static final int MSG_AUTO_HIDE_AB = 1;
    protected static final long MSG_AUTO_HIDE_AB_DELAY_MS = 4000;
    protected static final long MSG_QUICK_AUTO_HIDE_AB_DELAY_MS = 700;

    // Members
    private ActionBar mActionBar;
    private int mActionBarHeight;
    private RelativeLayout mFullLayout;
    private Activity mActivity;
    private WebView mFBWrapper;
    private GestureDetector mGestureScanner;
    @SuppressWarnings("deprecation")
    private android.text.ClipboardManager mAncientClipboard;
    private android.content.ClipboardManager mClipboard;
    private ValueCallback<Uri> mUploadMessage;
    private final static int RESULTCODE_PICUPLOAD = 1;
    private boolean mDesktopView = false;
    private String USERAGENT_ANDROID_DEFAULT;
    private ProgressBar mProgressBar;
    private boolean mAutoHideAb = false;
    private boolean mAllowCheckins = false;
    private boolean mOpenLinksInside = false;
    private String mSiteMode;

    private SharedPreferences mSharedPrefs;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    @SuppressWarnings("deprecation")
    @SuppressLint({"ServiceCast", "NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only mess with ActionBar if device is on honeycomb or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActionBar();
            if (mActionBar != null) {
                mActionBar.setTitle(R.string.app_name_short);
                mActionBarHeight = mActionBar.getHeight();
            }
            mClipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        } else {
            mAncientClipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        }

        mActivity = this;

        setContentView(R.layout.webview);

        mGestureScanner = new GestureDetector(this, this);

        // Load shared preferences
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Hide ActionBar based on user's preferences
        mAutoHideAb = mSharedPrefs.getBoolean(PREFS_HIDE_AB, false);

        // Whether the site should be loaded as the mobile or desktop version
        mSiteMode = mSharedPrefs.getString(PREFS_SITE_MODE,
                PREFS_SITE_MODE_AUTO);

        // Creates new CookieSyncManager instance that will manage cookies
        CookieSyncManager.createInstance(this);

        // Declare ProgressBar in order for it to be used later
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mFullLayout = (RelativeLayout) findViewById(R.id.layout_full);

        // Configure WebView
        mFBWrapper = (WebView) findViewById(R.id.webview);
        mFBWrapper.setWebViewClient(new FbWebViewClient());
        mFBWrapper.setWebChromeClient(new FbWebChromeClient());
        mFBWrapper.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        mFBWrapper.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureScanner.onTouchEvent(event);
            }
        });

        // Apply settings for WebView
        WebSettings webSettings = mFBWrapper.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginState(PluginState.ON);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(true);
        webSettings.setSupportZoom(true);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setDomStorageEnabled(true);

        mFBWrapper.clearFormData();

        // Load default User Agent
        USERAGENT_ANDROID_DEFAULT = webSettings.getUserAgentString();

        // Allow us to load a custom URL
        Intent intent = getIntent();
        Uri urlToLoad = null;

        // Check to see if we receive Intent Data
        if (intent.getData() != null) {
            urlToLoad = intent.getData();
        }

        // Get a subject and text and check if this is a link trying to be
        // shared
        String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (sharedUrl != null) {
            if (!sharedUrl.equals("")) {
                String formattedSharedUrl = String.format(URL_SHARE_LINKS,
                        sharedUrl, sharedSubject);
                urlToLoad = Uri.parse(formattedSharedUrl);
            }
        }

        if (savedInstanceState != null) {
            // Restore the state of the WebView using the saved instance state
            mFBWrapper.restoreState(savedInstanceState);
        } else {
            // Loads proper URL depending on device type
            initSession(urlToLoad);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the state of the WebView as a Bundle to the Instance State
        mFBWrapper.saveState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Handle orientation configuration changes
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start synchronizing the CookieSyncManager
        CookieSyncManager.getInstance().startSync();

        // Re-load these preferences in case some of them were changed
        mAutoHideAb = mSharedPrefs.getBoolean(PREFS_HIDE_AB, false);
        mAllowCheckins = mSharedPrefs.getBoolean(PREFS_ALLOW_CHECKINS, false);

        // TODO: fix this
        if (mActionBar != null) {
            if (mAutoHideAb) {
                mFullLayout.setPadding(0, 0, 0, 0);
            } else {
                mFullLayout.setPadding(0, mActionBarHeight, 0, 0);
            }
        }

        mOpenLinksInside = mSharedPrefs.getBoolean(PREFS_OPEN_LINKS_INSIDE,
                false);

        // Check to see if the Site mode preference was just changed
        if (!mSiteMode.equals(mSharedPrefs.getString(PREFS_SITE_MODE,
                PREFS_SITE_MODE_AUTO))) {

            // Store the new changes on the global field
            mSiteMode = mSharedPrefs.getString(PREFS_SITE_MODE,
                    PREFS_SITE_MODE_AUTO);

            // Loads proper URL depending on device type
            initSession(null);
        }

        //
        delayedQuickAutoHideActionBar(MSG_QUICK_AUTO_HIDE_AB_DELAY_MS);

    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop synchronizing the CookieSyncManager
        CookieSyncManager.getInstance().stopSync();
    }

    @SuppressWarnings("deprecation")
    private void copyToClipboard(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipData clip = ClipData.newPlainText("label", text);
            mClipboard.setPrimaryClip(clip);
        } else {
            mAncientClipboard.setText(text);
        }
    }

    private void shareCurrentPage() {
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Facebook page");
        i.putExtra(Intent.EXTRA_TEXT, mFBWrapper.getUrl());
        startActivity(Intent.createChooser(i, "Share URL"));
    }

    private void destroyWebView() {
        // Avoid an NPE
        if (mFBWrapper != null) {

            /** Free memory and destroy WebView */
            mFBWrapper.freeMemory();
            mFBWrapper.destroy();
            mFBWrapper = null;
        }

        // Avoid an NPE
        if (mSharedPrefs != null) {

            /** Clean up shared preferences */
            mSharedPrefs = null;
        }
    }

    // handles counting down
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTO_HIDE_AB:
                    autoHideActionBar();
                    break;

            }
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showActionBar() {
        if (mActionBar != null) {
            mActionBar.show();

            // Cancel current message to auto hide action bar
            mHandler.removeMessages(MSG_AUTO_HIDE_AB);

            if (mAutoHideAb) {
                // Schedule message to hide action bar
                mHandler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_AB,
                        MSG_AUTO_HIDE_AB_DELAY_MS);
            }
        }
    }

    private void delayedQuickAutoHideActionBar(long delay) {
        if (mAutoHideAb) {
            // Cancel current message to auto hide action bar
            mHandler.removeMessages(MSG_AUTO_HIDE_AB);

            // Schedule message to hide action bar
            mHandler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_AB,
                    delay);
        }
    }

    private void autoHideActionBar() {
        if (mAutoHideAb) {
            hideActionBar();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideActionBar() {

        // Cancel current message to auto hide action bar
        mHandler.removeMessages(MSG_AUTO_HIDE_AB);

        if (mActionBar != null) {
            mActionBar.hide();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        if (requestCode == RESULTCODE_PICUPLOAD) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
    }

    private class FbWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {

            // Posts current progress to the ProgressBar
            mProgressBar.setProgress(progress);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);

            if (!mAllowCheckins) {
                showCheckinsDisabledAlert();
            }

            callback.invoke(origin, mAllowCheckins, false);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            mActivity.startActivityForResult(
                    Intent.createChooser(i, "File Chooser"),
                    RESULTCODE_PICUPLOAD);
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                String acceptType) {
            openFileChooser(uploadMsg, acceptType, "");
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "", "");
        }

    }

    private class FbWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // Avoid NPEs when clicking on some weird links on facebook.com
            if (url.equals("about:blank"))
                return false;

            if (!mOpenLinksInside) {
                if (url.startsWith("https://m.facebook.com/l.php")
                        || url.startsWith("http://m.facebook.com/l.php")
                        || url.startsWith("https://www.facebook.com/l.php")
                        || url.startsWith("http://www.facebook.com/l.php")
                        || url.startsWith("https://facebook.com/l.php")
                        || url.startsWith("http://facebook.com/l.php")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);

                    // Facebook uses a linker helper, it's blank when coming back to app
                    // from an outside link, make sure we attempt to go back to avoid this
                    if (mFBWrapper.canGoBack()) {
                        mFBWrapper.goBack();
                    }

                    return true;
                }
            }

            // Get the URL's domain name
            String domain = Uri.parse(url).getHost();

            // Avoid an NPE
            if (domain != null) {
                // Let this WebView load the page.
                if (domain.equals("m.facebook.com")) {
                    return false;
                } else if (domain.equals("facebook.com")) {
                    return false;
                } else if (domain.equals("touch.facebook.com")) {
                    return false;
                } else if (domain.equals("www.facebook.com")) {
                    return false;
                } else if (domain.equals("fb.html5isready.com")) {
                    return false;
                }
            }

            // This link is not for a page on my site, launch another Activity
            // that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);

            // Facebook uses a linker helper, it's blank when coming back to app
            // from an outside link, make sure we attempt to go back to avoid this
            if (mFBWrapper.canGoBack()) {
                mFBWrapper.goBack();
            }

            return true;
        }

        @SuppressLint("NewApi")
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            // We just started loading new content, show ProgressBar
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @SuppressLint("NewApi")
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            // See if we should auto-hide the action bar
            autoHideActionBar();

            // We just finished loading the new content, hide ProgressBar
            mProgressBar.setVisibility(View.INVISIBLE);

            // Tell the CookieSyncManager to synchronize
            CookieSyncManager.getInstance().sync();
        }

    }

    /**
     * Sets the user agent to the default user agent and load the mobile site.
     */
    private void setDefaultUserAgent(Uri urlToLoad) {
        mDesktopView = false;
        mFBWrapper.getSettings().setUserAgentString(USERAGENT_ANDROID_DEFAULT);

        if (urlToLoad != null) {
            mFBWrapper.loadUrl(urlToLoad.toString());
        } else {
            mFBWrapper.loadUrl(URL_MOBILE_SITE);
        }
    }

    /**
     * Sets the user agent to the default user agent and load the mobile site.
     */
    private void setMobileUserAgent(Uri urlToLoad) {
        mDesktopView = false;
        mFBWrapper.getSettings().setUserAgentString(USER_AGENT_MOBILE);

        if (urlToLoad != null) {
            mFBWrapper.loadUrl(urlToLoad.toString());
        } else {
            if (PREFS_SITE_MODE_FASTBOOK.equals(mSiteMode)) {
                mFBWrapper.loadUrl(URL_FASTBOOK);
            } else {
                mFBWrapper.loadUrl(URL_MOBILE_SITE);
            }
        }
    }

    /**
     * Sets the user agent to the desktop one and load the desktop site
     */
    private void setDesktopUserAgent(Uri urlToLoad) {
        mDesktopView = true;
        mFBWrapper.getSettings().setUserAgentString(USER_AGENT_DESKTOP);

        if (urlToLoad != null) {
            mFBWrapper.loadUrl(urlToLoad.toString());
        } else {
            mFBWrapper.loadUrl(URL_DESKTOP_SITE);
        }
    }

    private void setupConfigForTablets() {
        mFBWrapper.getSettings().setBuiltInZoomControls(true);
        mFBWrapper.getSettings().setLoadWithOverviewMode(true);
        mFBWrapper.getSettings().setUseWideViewPort(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mFBWrapper.getSettings().setDisplayZoomControls(false);
    }

    private void setupConfigForPhones() {
        mFBWrapper.getSettings().setBuiltInZoomControls(false);
        mFBWrapper.getSettings().setLoadWithOverviewMode(false);
        mFBWrapper.getSettings().setUseWideViewPort(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mFBWrapper.getSettings().setDisplayZoomControls(false);
    }

    /**
     * Determines whether to load the mobile or desktop version depending on
     * screen configuration.
     */
    private void initSession(Uri urlToLoad) {
        if (mSiteMode.equals(PREFS_SITE_MODE_AUTO)) {
            // Automatically check the proper site to load depending on screen
            // size
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // ICS allows phones AND tablets
                Configuration config = getResources().getConfiguration();
                if (config.smallestScreenWidthDp >= 600) {
                    // For tablets
                    setupConfigForTablets();
                    setDesktopUserAgent(urlToLoad);
                } else {
                    // For phones
                    setupConfigForPhones();
                    setDefaultUserAgent(urlToLoad);
                }

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                    && Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
                // Honeycomb only allowed tablets, always assume it's a tablet
                setupConfigForTablets();
                setDesktopUserAgent(urlToLoad);
            } else {
                // There were no tablets before Honeycomb, assume it's a phone
                setupConfigForPhones();
                setDefaultUserAgent(urlToLoad);
            }
        } else if (mSiteMode.equals(PREFS_SITE_MODE_DESKTOP)) {
            // Force the desktop version to load
            setupConfigForTablets();
            setDesktopUserAgent(urlToLoad);
        } else if (mSiteMode.equals(PREFS_SITE_MODE_MOBILE)) {
            // Force the mobile version to load
            setupConfigForPhones();
            setMobileUserAgent(urlToLoad);
        } else if (mSiteMode.equals(PREFS_SITE_MODE_FASTBOOK)) {
            // Force the experimental/demo mobile fastbook site to load
            setupConfigForPhones();
            setMobileUserAgent(urlToLoad);
        } else {
            // Otherwise force the mobile version to load
            setupConfigForPhones();
            setDefaultUserAgent(urlToLoad);
        }

    }

    /**
     * Load the notification page view via the menu button
     */
    private void loadNotificationsView() {

        if (!mDesktopView)
            mFBWrapper.loadUrl(URL_MOBILE_NOTIFICATIONS);
        else
            mFBWrapper.loadUrl(URL_DESKTOP_NOTIFICATIONS);
    }

    private void webViewJumpTop() {
        mFBWrapper.loadUrl("javascript:window.scrollTo(0,0);");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the BACK key and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mFBWrapper.canGoBack()) {
            mFBWrapper.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private void showCheckinsDisabledAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.lbl_dialog_alert));
        alertDialog.setMessage(getString(R.string.txt_checkins_disables));
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.lbl_dialog_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // TODO; message
        showActionBar();

        switch (item.getItemId()) {
        case R.id.menu_jump_top:
            webViewJumpTop();
            autoHideActionBar();
            return true;
        case R.id.menu_news_feed:
            initSession(null);
            return true;
        case R.id.menu_copy_url:
            copyToClipboard(mFBWrapper.getUrl());
            return true;
        case R.id.menu_share:
            shareCurrentPage();
            return true;
        case R.id.menu_refresh:
            mFBWrapper.reload();
            return true;
        case R.id.menu_notifications:
            loadNotificationsView();
            return true;
        case R.id.menu_preferences:
            startActivity(new Intent(this, Preferences.class));
            return true;
        case R.id.menu_exit:
            destroyWebView();
            finish();
            return true;
        }

        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return false;
    }

    @SuppressLint("NewApi")
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {

        if (mActionBar == null || !mAutoHideAb) {
            return false;
        }

        if (e1.getRawY() > e2.getRawY()) {

            // TODO: only hide it if scroll movement > action bar height

            // Scrolling up
            hideActionBar();

        } else if (e1.getRawY() < e2.getRawY()) {
            // Scrolling down
            showActionBar();
        }

        return false;
    }

    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public void onShowPress(MotionEvent e) {
    }

}