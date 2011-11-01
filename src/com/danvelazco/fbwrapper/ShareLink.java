package com.danvelazco.fbwrapper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ShareLink extends Activity {

	private WebView fbWrapper;
	
	private ProgressBar mProgressBar;
	
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
		
		Intent intent = getIntent();
		
		String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		String url = intent.getStringExtra(Intent.EXTRA_TEXT);
		
		String shareLink = String.format(Constants.URL_SHARE_LINKS, url, subject);
		
		Log.d(Constants.TAG, "Share - Go to: " + shareLink);
		
		fbWrapper.loadUrl(shareLink);
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
    	public void onCloseWindow(WebView window) {
			window.destroy();
			finish();
		}
    	
    }
	
	private class FbWebViewClient extends WebViewClient {
        
    	@Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
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
	
}