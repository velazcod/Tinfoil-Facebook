package com.danvelazco.fbwrapper;

import android.app.Activity;
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
	
	private WebView mFBWrapper;
	
	private boolean mDesktopView = false;
	private String USERAGENT_ANDROID_DEFAULT;
	
	private ProgressBar mProgressBar;
	
	private boolean V = false;
	private boolean mAllowCheckins = false;
	private boolean mOpenLinksInside = false;
	private String mSiteMode;
	
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
        mSiteMode = mSharedPrefs.getString(Constants.PREFS_SITE_MODE, Constants.PREFS_SITE_MODE_AUTO);
        
        /** Creates new CookieSyncManager instance that will manage cookies */
        CookieSyncManager.createInstance(this);
        
        /** Declare ProgressBar in order for it to be used later */
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        
        /** Configure WebView */
        mFBWrapper = (WebView) findViewById(R.id.webview);
        mFBWrapper.setWebViewClient(new FbWebViewClient());
        mFBWrapper.setWebChromeClient(new FbWebChromeClient());
        mFBWrapper.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        
        /** Apply settings for WebView */
        WebSettings webSettings = mFBWrapper.getSettings(); 
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        
        /** Load default User Agent */
        USERAGENT_ANDROID_DEFAULT = webSettings.getUserAgentString();
        
        /** Allow us to load a custom URL */
        Intent intent = getIntent();
        Uri urlToLoad = null;
		
        /** Check to see if we receive Intent Data */
        if (intent.getData() != null) {
        	urlToLoad = intent.getData();
        }        
        
        if (savedInstanceState != null) {
        	/** Restore the state of the WebView using the saved instance state */
        	mFBWrapper.restoreState(savedInstanceState);
        } else {
        	/** Loads proper URL depending on device type */
        	initSession(urlToLoad);
        }
        
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	/** Save the state of the WebView as a Bundle to the Instance State */
    	mFBWrapper.saveState(outState);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){
    	/** Handle orientation configuration changes */
    	super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (V) Log.w(Constants.TAG, "Reset activity destroyer timeout");
    	mDestroyHandy.removeMessages(Constants.REQUEST_WEB_VIEW_CLEANUP);
    	
    	/** Start synchronizing the CookieSyncManager */
    	CookieSyncManager.getInstance().startSync();
    	
    	/** Re-load these preferences in case some of them were changed */
    	V = mSharedPrefs.getBoolean(Constants.PREFS_LOGCAT_ENABLED, false);
    	mAllowCheckins = mSharedPrefs.getBoolean(Constants.PREFS_ALLOW_CHECKINS, false);
    	
    	mOpenLinksInside = mSharedPrefs.getBoolean(Constants.PREFS_OPEN_LINKS_INSIDE, false);
    	
    	/** Check to see if the Site mode preference was just changed */
    	if (!mSiteMode.equals(mSharedPrefs.getString(Constants.PREFS_SITE_MODE, Constants.PREFS_SITE_MODE_AUTO))) {
    	
    		/** Store the new changes on the global field */
    		mSiteMode = mSharedPrefs.getString(Constants.PREFS_SITE_MODE, Constants.PREFS_SITE_MODE_AUTO);
    		
    		/** Loads proper URL depending on device type */
        	initSession(null);
    	}
    	
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	/** Stop synchronizing the CookieSyncManager */
    	CookieSyncManager.getInstance().stopSync();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    	if (V) Log.w(Constants.TAG, "Schedule activity cleanup in " + Constants.REQUEST_WEB_VIEW_CLEANUP_TIMEOUT + " millis");
    	
    	mDestroyHandy.sendMessageDelayed(Message.obtain(mDestroyHandy, 
    			Constants.REQUEST_WEB_VIEW_CLEANUP), Constants.REQUEST_WEB_VIEW_CLEANUP_TIMEOUT);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (V) Log.w(Constants.TAG, "Cleaning up and destroying activity");
    	
    	mDestroyHandy.removeMessages(Constants.REQUEST_WEB_VIEW_CLEANUP);
    	
    	/** Avoid an NPE */
    	if (mFBWrapper != null) {
    		
    		/** Free memory and destroy WebView*/
    		mFBWrapper.freeMemory();
    		mFBWrapper.destroy();
    		mFBWrapper = null;
    	}
    	
    	/** Avoid an NPE */
    	if (mSharedPrefs != null) {
    		
    		/** Clean up shared preferences */
    		mSharedPrefs = null;
    	}
    	
    	/** Force Garbage Collector */
    	System.gc();
    	
    }
    
    private final Handler mDestroyHandy = new Handler() {
    	@Override
    	public void handleMessage(Message m) 
    	{
    		if (m.what == Constants.REQUEST_WEB_VIEW_CLEANUP) {
    			new Thread() {
	    			public void run() 
					{
	    				if (V) Log.w(Constants.TAG, "Request the activity to be cleaned up and destroyed");
	    				finish();
		    			return;
					} 
				}.start();
    		}
    	}
    };
 
    private class FbWebChromeClient extends WebChromeClient {
    	@Override
    	public void onProgressChanged(WebView view, int progress) {
    		
    		/** Posts current progress to the ProgressBar */
    		mProgressBar.setProgress(progress);
    	}
    	
    	@Override
    	public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
    		super.onGeolocationPermissionsShowPrompt(origin, callback);
    		
    		callback.invoke(origin, mAllowCheckins, false);
    	}
    }
    
    private class FbWebViewClient extends WebViewClient {
        
    	@Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	
    		/** Avoid NPEs when clicking on some weird links on facebook.com */
    		if (url.equals("about:blank")) return false;
    		
    		if (!mOpenLinksInside) {
	    		if (url.startsWith("https://m.facebook.com/l.php") ||  
	    				url.startsWith("http://m.facebook.com/l.php") ||
	    				url.startsWith("https://www.facebook.com/l.php") ||
	    				url.startsWith("http://www.facebook.com/l.php") ||
	    				url.startsWith("https://facebook.com/l.php") ||
	    				url.startsWith("http://facebook.com/l.php")) {
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
    private void setDefaultUserAgent(Uri urlToLoad) {
    	
    	if (V) Log.w(Constants.TAG, "Initialize default user-agent");
    	
    	mDesktopView = false;
    	mFBWrapper.getSettings().setUserAgentString(USERAGENT_ANDROID_DEFAULT);
    	
    	if (urlToLoad != null)
    		mFBWrapper.loadUrl(urlToLoad.toString());
    	else
    		mFBWrapper.loadUrl(Constants.URL_MOBILE_SITE);
    }
    
    /**
     * Sets the user agent to the default user agent
     * and load the mobile site.
     */
    private void setMobileUserAgent(Uri urlToLoad) {
    	
    	if (V) Log.w(Constants.TAG, "Initialize for mobile");
    	
    	mDesktopView = false;
    	mFBWrapper.getSettings().setUserAgentString(Constants.USER_AGENT_MOBILE);
    	
    	if (urlToLoad != null)
    		mFBWrapper.loadUrl(urlToLoad.toString());
    	else
    		mFBWrapper.loadUrl(Constants.URL_MOBILE_SITE);
    }
    
    /**
     * Sets the user agent to the desktop one
     * and load the desktop site
     */
    private void setDesktopUserAgent(Uri urlToLoad) {
    	
    	if (V) Log.w(Constants.TAG, "Initialize for desktop");
    	
    	mDesktopView = true;
    	mFBWrapper.getSettings().setUserAgentString(Constants.USER_AGENT_DESKTOP);
    	
    	if (urlToLoad != null)
    		mFBWrapper.loadUrl(urlToLoad.toString());
    	else
    		mFBWrapper.loadUrl(Constants.URL_DESKTOP_SITE);
    }
    
    /**
     * Determines whether to load the mobile or desktop version
     * depending on screen configuration. 
     */
    private void initSession(Uri urlToLoad) {
    	
    	/** Automatically check the proper site to load depending on screen size */
    	if (mSiteMode.equals(Constants.PREFS_SITE_MODE_AUTO)) {
    	
	    	/** ICS allows phones AND tablets */
	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	        	
		    	Configuration config = getResources().getConfiguration();
		    	if (config.smallestScreenWidthDp >= 600) {
		    		/** For tablets */
		    		setDesktopUserAgent(urlToLoad);
		    	} else {
		    		/** For phones */
		    		setDefaultUserAgent(urlToLoad);
		    	}
		   
		    /** Honeycomb only allowed tablets, always assume it's a tablet */
	    	} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
	    			&& Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
	    		setDesktopUserAgent(urlToLoad);
	    	
	    	/** There were no tablets before Honeycomb, assume it's a phone */
	    	} else {
	    		setDefaultUserAgent(urlToLoad);
	    	}
	    
	    /** Force the desktop version to load */
    	} else if (mSiteMode.equals(Constants.PREFS_SITE_MODE_DESKTOP)) {
    		setDesktopUserAgent(urlToLoad);
    	
    	/** Force the mobile version to load */
    	} else if (mSiteMode.equals(Constants.PREFS_SITE_MODE_MOBILE)) {
    		setMobileUserAgent(urlToLoad);
    		
    	/** Otherwise force the mobile version to load */
    	} else {
    		setDefaultUserAgent(urlToLoad);
    	}
    	
    }
    
    /**
     * Load the notification page view via the menu button
     */
    private void loadNotificationsView() {
    	
    	if (!mDesktopView)
    		mFBWrapper.loadUrl(Constants.URL_MOBILE_NOTIFICATIONS);
    	else
    		mFBWrapper.loadUrl(Constants.URL_DESKTOP_NOTIFICATIONS);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
    	/** Check if the key event was the BACK key and if there's history */
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mFBWrapper.canGoBack()) {
        	mFBWrapper.goBack();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH)
        {
        	openSearchBox();
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    /*
     * Use some hacky js to open the search box in the webview
     */
    private void openSearchBox() {
    	mFBWrapper.loadUrl(getResources().getString(R.string.search_js));
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
	    	case R.id.menu_news_feed:
				initSession(null);
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
    			finish();
    			return true;
    	}
    
    	return false;
    }
    
}