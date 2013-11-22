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
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebViewFragment;

import com.danvelazco.fbwrapper.AccessTokenDialogFragment;
import com.danvelazco.fbwrapper.R;
import com.danvelazco.fbwrapper.TagFriendDialogFragment;

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
    public final static String KEY_PROXY_ENABLED = "prefs_enable_proxy";
    public final static String KEY_PROXY_HOST = "prefs_proxy_host";
    public final static String KEY_PROXY_PORT = "prefs_proxy_port";
    public final static String SITE_MODE = "prefs_mobile_site";
    public final static String SITE_MODE_AUTO = "auto";
    public final static String SITE_MODE_MOBILE = "mobile";
    public final static String SITE_MODE_DESKTOP = "desktop";
    public final static String ABOUT = "pref_about";
    public final static String API = "prefs_api";

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
        } else if (API.equals(key)) {
        	if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(API, false)) {
        		FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("api_dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = AccessTokenDialogFragment.newInstance();
                newFragment.show(ft, "api_dialog");
                
                // un-check the preference, until such time as the dialog manages to get an access token
            	((CheckBoxPreference)findPreference("prefs_api")).setChecked(false);
        	} else {
        		PreferenceManager.getDefaultSharedPreferences(this)
        			.edit()
        			.remove("API_KEY")
        			.commit();
        	}
        	return true;
        }
        return false;
    }
    
    public void received_access_token(String token) {
    	Log.d("FP", "access token: " + token);
    	((CheckBoxPreference)findPreference("prefs_api")).setChecked(true);
    	PreferenceManager.getDefaultSharedPreferences(this)
    		.edit()
    		.putString("API_KEY", token)
    		.commit();
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
