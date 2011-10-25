package com.danvelazco.fbwrapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
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
	
	private boolean V = Constants.OUTPUT_LOGS;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.webview);
        
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
        
        /** Loads proper URL depending on device type */
        initSession();
        
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	/** Start synchronizing the CookieSyncManager */
    	CookieSyncManager.getInstance().startSync();
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
    }
    
    private class FbWebViewClient extends WebViewClient {
        
    	@Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	
    		/** Avoid NPEs when clicking on some weird links on facebook.com */
    		if (url.equals("about:blank")) return false;
    		
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
    
    private void showAboutAlert() {
    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.menu_about));
        alertDialog.setMessage(getString(R.string.txt_about));
        alertDialog.setIcon(R.drawable.ic_launcher); 
        alertDialog.setButton(getString(R.string.lbl_dialog_close), new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		return;
        	} 
        });
        alertDialog.show();
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
    		case R.id.menu_notifications:
    			loadNotificationsView();
    			return true;
    		//case R.id.menu_preferences:
    			//return true;
    		case R.id.menu_about:
    			showAboutAlert();
    			return true;
    		case R.id.menu_exit:
    			finish();
    			return true;
    	}
    
    	return false;
    }
    
}