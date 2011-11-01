package com.danvelazco.fbwrapper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Activity with a WebView wrapping facebook.com with its
 * own CookieSyncManager to hold cookies persistently.
 * 
 * @author Daniel Velazco
 *
 */
public class FbWrapper extends Activity {
	
	private WebView fbWrapper;
	
	private boolean desktopView = false;
	private String USERAGENT_ANDROID_DEFAULT;
	
	private ProgressBar mProgressBar;
	
	private boolean V = false;
	private boolean ALLOW_CHECKINS = false;
	private boolean OPEN_LINKS_INSIDE = false;
	private String SITE_MODE;
	
	private SharedPreferences mSharedPrefs;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.webview);
        
        /** Load shared preferences */
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        /** Logcat verbose */
        V = mSharedPrefs.getBoolean(Constants.PREFS_LOGCAT_ENABLED, false);
        
        /** Whether the site should be loaded as the mobile or desktop version */
        SITE_MODE = mSharedPrefs.getString(Constants.PREFS_SITE_MODE, Constants.PREFS_SITE_MODE_AUTO);
        
        /** Creates new CookieSyncManager instance that will manage cookies */
        CookieSyncManager.createInstance(this);
        
        /** Declare ProgressBar in order for it to be used later */
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        
        /** Configure WebView */
        fbWrapper = (WebView) findViewById(R.id.webview);
        fbWrapper.setWebViewClient(new FbWebViewClient());
        fbWrapper.setWebChromeClient(new FbWebChromeClient());
        fbWrapper.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        
        /** Apply settings for WebView */
        WebSettings webSettings = fbWrapper.getSettings(); 
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        
        /** Load default User Agent */
        USERAGENT_ANDROID_DEFAULT = webSettings.getUserAgentString();
        
        if (savedInstanceState != null) {
        	/** Restore the state of the WebView using the saved instance state */
            fbWrapper.restoreState(savedInstanceState);
        } else {
        	/** Loads proper URL depending on device type */
        	initSession();
        }
        
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	/** Save the state of the WebView as a Bundle to the Instance State */
    	fbWrapper.saveState(outState);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){
    	/** Handle orientation configuration changes */
    	super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	/** Start synchronizing the CookieSyncManager */
    	CookieSyncManager.getInstance().startSync();
    	
    	/** Re-load these preferences in case some of them were changed */
    	V = mSharedPrefs.getBoolean(Constants.PREFS_LOGCAT_ENABLED, false);
    	ALLOW_CHECKINS = mSharedPrefs.getBoolean(Constants.PREFS_ALLOW_CHECKINS, false);
    	
    	OPEN_LINKS_INSIDE = mSharedPrefs.getBoolean(Constants.PREFS_OPEN_LINKS_INSIDE, false);
    	
    	/** Check to see if the Site mode preference was just changed */
    	if (!SITE_MODE.equals(mSharedPrefs.getString(Constants.PREFS_SITE_MODE, Constants.PREFS_SITE_MODE_AUTO))) {
    	
    		/** Store the new changes on the global field */
    		SITE_MODE = mSharedPrefs.getString(Constants.PREFS_SITE_MODE, Constants.PREFS_SITE_MODE_AUTO);
    		
    		/** Loads proper URL depending on device type */
        	initSession();
    	}
    	
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	/** Stop synchronizing the CookieSyncManager */
    	CookieSyncManager.getInstance().stopSync();
    }
 
    private class FbWebChromeClient extends WebChromeClient {
    	@Override
    	public void onProgressChanged(WebView view, int progress) {
    		
    		/** Posts current progress to the ProgressBar */
    		mProgressBar.setProgress(progress);
    	}
    	
    	@Override
    	public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
    		super.onGeolocationPermissionsShowPrompt(origin, callback);
    		
    		callback.invoke(origin, ALLOW_CHECKINS, false);
    	}
    }
    
    private class FbWebViewClient extends WebViewClient {
        
    	@Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	
    		/** Avoid NPEs when clicking on some weird links on facebook.com */
    		if (url.equals("about:blank")) return false;
    		
    		if (!OPEN_LINKS_INSIDE) {
	    		if (url.startsWith("https://m.facebook.com/l.php") ||  
	    				url.startsWith("http://m.facebook.com/l.php")) {
	    			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	                startActivity(intent);
	                return true;
	    		}
    		}
    		
    		/** Get the URL's domain name */
        	String domain = Uri.parse(url).getHost();
        	
        	/** Avoid an NPE */
        	if (domain != null) {
        	
	        	/** Output URL */
	        	if (V) Log.d(Constants.TAG, "Loading URL: " + url);
	        	
	        	/** Let this WebView load the page. */
	            if (domain.equals("m.facebook.com")) {
	                return false;
	            } else if (domain.equals("facebook.com")) {
	            	return false;
	            } else if (domain.equals("www.facebook.com")) {
	            	return false;
	            }
	            
        	}
            
            /** This link is not for a page on my site, launch another Activity that handles URLs */
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
        
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
    		super.onPageStarted(view, url, favicon);
    		
    		/** We just started loading new content, show ProgressBar */
    		mProgressBar.setVisibility(View.VISIBLE);
    	}
    	
    	@Override
    	public void onPageFinished (WebView view, String url) {
    		super.onPageFinished(view, url);
    		
    		/** We just finished loading the new content, hide ProgressBar */
    		mProgressBar.setVisibility(View.INVISIBLE);
    		
    		/** Tell the CookieSyncManager to synchronize */
    		CookieSyncManager.getInstance().sync();
    	}
    	
    }
    
    /**
     * Sets the user agent to the default user agent
     * and load the mobile site.
     */
    private void setMobileUserAgent() {
    	desktopView = false;
    	fbWrapper.getSettings().setUserAgentString(USERAGENT_ANDROID_DEFAULT);
    	fbWrapper.loadUrl(Constants.URL_MOBILE_SITE);
    }
    
    /**
     * Sets the user agent to the desktop one
     * and load the desktop site
     */
    private void setDesktopUserAgent() {
    	desktopView = true;
    	fbWrapper.getSettings().setUserAgentString(Constants.USER_AGENT_DESKTOP);
    	fbWrapper.loadUrl(Constants.URL_DESKTOP_SITE);
    }
    
    /**
     * Determines whether to load the mobile or desktop version
     * depending on screen configuration. 
     */
    private void initSession() {
    	
    	/** Automatically check the proper site to load depending on screen size */
    	if (SITE_MODE.equals(Constants.PREFS_SITE_MODE_AUTO)) {
    	
	    	/** ICS allows phones AND tablets */
	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	        	
		    	Configuration config = getResources().getConfiguration();
		    	if (config.smallestScreenWidthDp >= 600) {
		    		/** For tablets */
		    		setDesktopUserAgent();
		    	} else {
		    		/** For phones */
		    		setMobileUserAgent();
		    	}
		   
		    /** Honeycomb only allowed tablets, always assume it's a tablet */
	    	} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
	    			&& Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
	    		setDesktopUserAgent();
	    	
	    	/** There were no tablets before Honeycomb, assume it's a phone */
	    	} else {
	    		setMobileUserAgent();
	    	}
	    
	    /** Force the desktop version to load */
    	} else if (SITE_MODE.equals(Constants.PREFS_SITE_MODE_DESKTOP)) {
    		setDesktopUserAgent();
    		
    	/** Otherwise force the mobile version to load */
    	} else {
    		setMobileUserAgent();
    	}
    	
    }
    
    /**
     * Load the notification page view via the menu button
     */
    private void loadNotificationsView() {
    	
    	if (!desktopView)
    		fbWrapper.loadUrl(Constants.URL_MOBILE_NOTIFICATIONS);
    	else
    		fbWrapper.loadUrl(Constants.URL_DESKTOP_NOTIFICATIONS);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
    	/** Check if the key event was the BACK key and if there's history */
        if ((keyCode == KeyEvent.KEYCODE_BACK) && fbWrapper.canGoBack()) {
        	fbWrapper.goBack();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
    		case R.id.menu_refresh:
    			fbWrapper.reload();
    			return true;
    		case R.id.menu_notifications:
    			loadNotificationsView();
    			return true;
    		case R.id.menu_preferences:
    			startActivity(new Intent(this, Preferences.class));
    			return true;
    		case R.id.menu_exit:
    			finish();
    			return true;
    	}
    
    	return false;
    }
    
}