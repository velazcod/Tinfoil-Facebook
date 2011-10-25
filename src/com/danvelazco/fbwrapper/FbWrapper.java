package com.danvelazco.fbwrapper;

import android.app.Activity;
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
        
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        
        CookieSyncManager.createInstance(this);
        
        fbWrapper = (WebView) findViewById(R.id.webview);
        fbWrapper.setWebViewClient(new FbWebViewClient());
        fbWrapper.setWebChromeClient(new FbWebChromeClient());
        fbWrapper.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        
        WebSettings webSettings = fbWrapper.getSettings(); 
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(true);
        webSettings.setSupportZoom(true);
        USERAGENT_ANDROID_DEFAULT = webSettings.getUserAgentString();
        
        initSession();
        
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	CookieSyncManager.getInstance().startSync();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	CookieSyncManager.getInstance().stopSync();
    }
 
    private class FbWebChromeClient extends WebChromeClient {
    	@Override
    	public void onProgressChanged(WebView view, int progress) {
    		mProgressBar.setProgress(progress);
    	}
    }
    
    private class FbWebViewClient extends WebViewClient {
        
    	@Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	
    		if (url.equals("about:blank")) return false;
    		
        	String domain = Uri.parse(url).getHost();
        	
        	if (domain != null) {
        	
	        	//Output URL
	        	if (V) Log.d(Constants.TAG, "URL: " + url);
	        	
	        	// Let this WebView load the page. Do not override it.
	            if (domain.equals("m.facebook.com")) {
	                return false;
	            } else if (domain.equals("facebook.com")) {
	            	return false;
	            } else if (domain.equals("www.facebook.com")) {
	            	return false;
	            }
	            
        	}
            
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
        
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
    		super.onPageStarted(view, url, favicon);
    		
    		mProgressBar.setVisibility(View.VISIBLE);
    	}
    	
    	@Override
    	public void onPageFinished (WebView view, String url) {
    		super.onPageFinished(view, url);
    		
    		mProgressBar.setVisibility(View.INVISIBLE);
    		
    		CookieSyncManager.getInstance().sync();
    	}
    	
    }
    
    private void setMobileUserAgent() {
    	desktopView = false;
    	fbWrapper.getSettings().setUserAgentString(USERAGENT_ANDROID_DEFAULT);
    	fbWrapper.loadUrl("https://m.facebook.com");
    }
    
    private void setDesktopUserAgent() {
    	desktopView = true;
    	fbWrapper.getSettings().setUserAgentString(Constants.USER_AGENT_DESKTOP);
    	fbWrapper.loadUrl("https://www.facebook.com");
    }
    
    private void initSession() {
    	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        	
	    	Configuration config = getResources().getConfiguration();
	    	if (config.smallestScreenWidthDp >= 600) {
	    		//Tablet
	    		setDesktopUserAgent();
	    	} else {
	    		//Phone
	    		setMobileUserAgent();
	    	}
	   
    	} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
    			&& Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
    		
    		//Tablet
    		setDesktopUserAgent();
    		
    	} else {
    		//Phone
    		setMobileUserAgent();
    	}
    	
    }
    
    private void loadNotificationsView() {
    	
    	if (desktopView)
    		fbWrapper.loadUrl("https://www.facebook.com/notifications.php");
    	else
    		fbWrapper.loadUrl("https://m.facebook.com/notifications.php");
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
    	// Check if the key event was the BACK key and if there's history
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
    		case R.id.menu_notifications:
    			loadNotificationsView();
    			return true;
    		//case R.id.menu_preferences:
    			//return true;
    		case R.id.menu_about:
    			return true;
    		case R.id.menu_exit:
    			finish();
    			return true;
    	}
    
    	return false;
    }
    
}