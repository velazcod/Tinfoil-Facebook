package com.danvelazco.fbwrapper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import com.danvelazco.fbwrapper.activity.BaseFacebookWebViewActivity;
import com.danvelazco.fbwrapper.preferences.FacebookPreferences;
import com.danvelazco.fbwrapper.util.Logger;

/**
 * Facebook web wrapper activity.
 */
public class FbWrapper extends BaseFacebookWebViewActivity {

    // Constant
    private final static int MENU_DRAWER_GRAVITY = GravityCompat.END;

    // Members
    private DrawerLayout mDrawerLayout = null;
    private String mDomainToUse = INIT_URL_MOBILE;
    private RelativeLayout mWebViewContainer = null;

    // Preferences stuff
    private SharedPreferences mSharedPreferences = null;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityCreated() {
        Logger.d(getClass().getSimpleName(), "onActivityCreated()");

        // TODO: allow user to customize theme
        // TODO: this will require the  app to be restarted and theme set before setting layout
        // No action bar, right drawer
        //          mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        // Action bar, no right drawer
        //          mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Set the content view layout
        setContentView(R.layout.main_layout);

        // Keep a reference of the DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_main);
        mWebViewContainer = (RelativeLayout) findViewById(R.id.webview_container);

        // Set the click listener interface for the buttons
        setOnClickListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onWebViewInit(Bundle savedInstanceState) {
        Logger.d(getClass().getSimpleName(), "onWebViewInit()");

        // Load the application's preferences
        loadPreferences();

        // Get the Intent data in case we need to load a specific URL
        Intent intent = getIntent();

        // Get a subject and text and check if this is a link trying to be shared
        String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);

        // If we have a valid URL that was shared to us, open the sharer
        if (sharedUrl != null) {
            if (!sharedUrl.equals("")) {
                String formattedSharedUrl = String.format(mDomainToUse + URL_PAGE_SHARE_LINKS,
                        sharedUrl, sharedSubject);
                Logger.d(getClass().getSimpleName(), "Loading the sharer page...");
                loadNewPage(Uri.parse(formattedSharedUrl).toString());
                return;
            }
        }

        // Open the proper URL in case the user clicked on a link that brought us here
        if (intent.getData() != null) {
            Logger.d(getClass().getSimpleName(), "Loading a specific Facebook URL a user " +
                    "clicked on somewhere else");
            loadNewPage(intent.getData().toString());
            return;
        }

        // Attempt to either restore the activity or open the default page
        if (savedInstanceState != null) {
            // Restore the state of the WebView using the saved instance state
            Logger.d(getClass().getSimpleName(), "Restoring the WebView state");
            restoreWebView(savedInstanceState);
        } else {
            // Load the URL depending on the type of device or preference
            Logger.d(getClass().getSimpleName(), "Loading the init Facebook URL");
            loadNewPage(mDomainToUse);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResumeActivity() {
        Logger.d(getClass().getSimpleName(), "onResumeActivity()");

        // This will allow us to check and see if the domain to be used changed
        String previousDomainUsed = mDomainToUse;

        // Reload the preferences in case the user changed something critical
        loadPreferences();

        // If the domain changes, reload the page with the new domain
        if (!mDomainToUse.equalsIgnoreCase(previousDomainUsed)) {
            loadNewPage(mDomainToUse);
        }
    }

    /**
     * Sets the click listener on all the buttons in the activity
     */
    private void setOnClickListeners() {
        // Create a new listener
        MenuDrawerButtonListener buttonsListener = new MenuDrawerButtonListener();

        // Set this listener to all the buttons
        findViewById(R.id.menu_drawer_right).setOnClickListener(buttonsListener);
        findViewById(R.id.menu_item_jump_to_top).setOnClickListener(buttonsListener);
        findViewById(R.id.menu_item_refresh).setOnClickListener(buttonsListener);
        findViewById(R.id.menu_item_newsfeed).setOnClickListener(buttonsListener);
        findViewById(R.id.menu_items_notifications).setOnClickListener(buttonsListener);
        findViewById(R.id.menu_share_this).setOnClickListener(buttonsListener);
        findViewById(R.id.menu_preferences).setOnClickListener(buttonsListener);
        findViewById(R.id.menu_about).setOnClickListener(buttonsListener);
        findViewById(R.id.menu_kill).setOnClickListener(buttonsListener);
    }

    /**
     * Used to open the menu drawer
     */
    private void openMenuDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(MENU_DRAWER_GRAVITY);
        }
    }

    /**
     * Used to close the menu drawer
     */
    private void closeMenuDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(MENU_DRAWER_GRAVITY);
        }
    }

    /**
     * Check to see if the menu drawer is currently open
     *
     * @return {@link boolean} true if the menu drawer is open,
     *         false if closed.
     */
    private boolean isMenuDrawerOpen() {
        if (mDrawerLayout != null) {
            return mDrawerLayout.isDrawerOpen(MENU_DRAWER_GRAVITY);
        } else {
            return false;
        }
    }

    /**
     * Set the preferences for this activity by using the
     * {@link PreferenceManager} to load the Default shared preferences.<br />
     * Most preferences will be automatically set for the {@link com.danvelazco.fbwrapper.webview.FacebookWebView}.
     */
    private void loadPreferences() {

        if (mSharedPreferences == null) {
            // Get the default shared preferences instance
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        // Get the URL load and check-in settings
        boolean anyDomain = mSharedPreferences.getBoolean(FacebookPreferences.OPEN_LINKS_INSIDE, false);
        boolean allowCheckins = mSharedPreferences.getBoolean(FacebookPreferences.ALLOW_CHECKINS, false);

        // Set the flags for loading URLs and allowing geolocation
        setAllowCheckins(allowCheckins);
        setAllowAnyDomain(anyDomain);

        // Whether the site should be loaded as the mobile or desktop version
        String mode = mSharedPreferences.getString(FacebookPreferences.SITE_MODE,
                FacebookPreferences.SITE_MODE_AUTO);

        // Force or detect the site mode to load
        if (mode.equalsIgnoreCase(FacebookPreferences.SITE_MODE_MOBILE)) {
            // Force the webview config to mobile
            setupFacebookWebViewConfig(true, true);
        } else if (mode.equalsIgnoreCase(FacebookPreferences.SITE_MODE_DESKTOP)) {
            // Force the webview config to desktop mode
            setupFacebookWebViewConfig(true, false);
        } else {
            // Do not force, allow us to auto-detect what mode to use
            setupFacebookWebViewConfig(false, true);
        }

        // If we haven't shown the new menu drawer to the user, auto open it
        if (!mSharedPreferences.getBoolean(FacebookPreferences.MENU_DRAWER_SHOWED_OPENED, false)) {
            openMenuDrawer();

            // Make sure we don't auto-open the menu ever again
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(FacebookPreferences.MENU_DRAWER_SHOWED_OPENED, true);
            editor.apply();
        }

    }

    /**
     * Configure this {@link com.danvelazco.fbwrapper.webview.FacebookWebView}
     * with the appropriate preferences depending on the device configuration.<br />
     * Use the 'force' flag to force the configuration to either mobile or desktop.
     *
     * @param force  {@link boolean}
     *               whether to force the configuration or not,
     *               if false the 'mobile' flag will be ignored
     * @param mobile {@link boolean}
     *               whether to use the mobile or desktop site.
     */
    private void setupFacebookWebViewConfig(boolean force, boolean mobile) {
        if (force && mobile) {
            // Force the mobile site to load
            mDomainToUse = INIT_URL_MOBILE;
        } else if (force && !mobile) {
            // Force the desktop site to load
            mDomainToUse = INIT_URL_DESKTOP;
        } else {
            // Detect whether this device is a phone or tablet
            // Use respective domain
            mDomainToUse = isDeviceTablet() ? INIT_URL_DESKTOP : INIT_URL_MOBILE;
        }

        // Set the user agent depending on config
        setUserAgent(force, mobile);

    }

    /**
     * Check whether this device is a phone or a tablet.
     *
     * @return {@link boolean} whether this device is a phone
     *         or a tablet
     */
    private boolean isDeviceTablet() {
        boolean isTablet;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // There were no tablet devices before Honeycomb, assume is a phone
            isTablet = false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
            // Honeycomb only allowed tablets, always assume it's a tablet
            isTablet = true;
        } else {
            // If the device's screen width is higher than 600dp,
            // it's a tablet, otherwise, it's a phone
            Configuration config = getResources().getConfiguration();
            isTablet = config.smallestScreenWidthDp >= 600;
        }

        return isTablet;
    }

    /**
     * Menu drawer button listener interface
     */
    private class MenuDrawerButtonListener implements View.OnClickListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_item_jump_to_top:
                    jumpToTop();
                    break;
                case R.id.menu_item_refresh:
                    refreshCurrentPage();
                    break;
                case R.id.menu_item_newsfeed:
                    loadNewPage(mDomainToUse);
                    break;
                case R.id.menu_items_notifications:
                    loadNewPage(mDomainToUse + URL_PAGE_NOTIFICATIONS);
                    break;
                case R.id.menu_share_this:
                    shareCurrentPage();
                    break;
                case R.id.menu_preferences:
                    startActivity(new Intent(FbWrapper.this, FacebookPreferences.class));
                    break;
                case R.id.menu_about:
                    showAboutAlert();
                    break;
                case R.id.menu_kill:
                    mWebViewContainer.removeView(mWebView);
                    mWebView.removeAllViews();
                    mWebView.destroy();
                    finish();
                    break;
            }
            closeMenuDrawer();
        }
    }

    /**
     * Show an alert dialog with the information about the application.
     */
    private void showAboutAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.menu_about));
        alertDialog.setMessage(getString(R.string.txt_about));
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.lbl_dialog_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Don't do anything, simply close the dialog
                    }
                });
        alertDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_drawer:
                openMenuDrawer();
                return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void onBackPressed() {
        // If the back button is pressed while the drawer
        // is open try to close it
        if (isMenuDrawerOpen()) {
            closeMenuDrawer();
        } else {
            super.onBackPressed();
        }
    }

}
