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

package com.danvelazco.fbwrapper.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import com.danvelazco.fbwrapper.R;

/**
 * Preferences activity
 *
 * TODO: use fragments-based preferences activity
 */
public class FacebookPreferences extends PreferenceActivity {

    // Custom preferences
    public final static String MENU_DRAWER_SHOWED_OPENED = "drawer_shown_opened";

    // Shared preference keys
    public final static String CAT_GENERAL = "pref_cat_general";
    public final static String ALLOW_CHECKINS = "prefs_allow_checkins";
    public final static String OPEN_LINKS_INSIDE = "prefs_open_links_inside";
    public final static String BLOCK_IMAGES = "prefs_block_images";
    public final static String KEY_PROXY_ENABLED = "prefs_enable_proxy";
    public final static String KEY_PROXY_HOST = "prefs_proxy_host";
    public final static String KEY_PROXY_PORT = "prefs_proxy_port";
    public final static String SITE_MODE = "prefs_mobile_site";
    public final static String SITE_MODE_AUTO = "auto";
    public final static String SITE_MODE_MOBILE = "mobile";
    public final static String SITE_MODE_DESKTOP = "desktop";
    public final static String SITE_MODE_BASIC = "basic";
    public final static String SITE_MODE_ZERO = "zero";
    public final static String SITE_MODE_ONION = "onion";
    public final static String ABOUT = "pref_about";

    // Preferences
    private EditTextPreference mPrefProxyHost = null;
    private EditTextPreference mPrefProxyPort = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.main_preferences);

        mPrefProxyHost = (EditTextPreference) findPreference(KEY_PROXY_HOST);
        mPrefProxyHost.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newProxyHostValue = (String) newValue;
                mPrefProxyHost.setSummary(newProxyHostValue);
                return true;
            }
        });

        mPrefProxyPort = (EditTextPreference) findPreference(KEY_PROXY_PORT);
        mPrefProxyPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newProxyPortValue = (String) newValue;
                mPrefProxyPort.setSummary(newProxyPortValue);
                return true;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mPrefProxyHost != null) {
            mPrefProxyHost.setSummary(mPrefProxyHost.getText());
        }
        if (mPrefProxyPort != null) {
            mPrefProxyPort.setSummary(mPrefProxyPort.getText());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // Allow us to open the about dialog in the case the user
        // presses the About preference item
        String key = preference.getKey();
        if (ABOUT.equals(key)) {
            showAboutAlert();
            return true;
        }
        return false;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
