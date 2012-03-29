package com.danvelazco.fbwrapper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_preferences);
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		
		String key = preference.getKey();
		
		if (Constants.PREFS_ABOUT.equals(key)) {
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