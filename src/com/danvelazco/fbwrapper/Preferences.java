package com.danvelazco.fbwrapper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity implements Constants {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_preferences);
		
		/* Hide ActionBar preference if device is before Honeycomb */
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			CheckBoxPreference hideAbCheckboxPref = (CheckBoxPreference) findPreference(PREFS_HIDE_AB);
			PreferenceCategory generalCategory = (PreferenceCategory) findPreference(PREFS_CAT_GENERAL);
			generalCategory.removePreference(hideAbCheckboxPref);
		}
		
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		
		String key = preference.getKey();
		
		if (PREFS_ABOUT.equals(key)) {
			showAboutAlert();
		}
		
		return true;
	}
	
	private void showAboutAlert() {
    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.menu_about));
        alertDialog.setMessage(getString(R.string.txt_about));
        alertDialog.setIcon(R.drawable.ic_launcher); 
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.lbl_dialog_close), 
        		new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		return;
        	} 
        });
        alertDialog.show();
    }
}