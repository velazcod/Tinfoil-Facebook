package com.danvelazco.fbwrapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

/**
 * TODO:
 * - Init url
 * - Attempt to use check-ins toggle
 * - Attempt to use Proxy
 * - Open external links inside wrapper
 * - Block images
 *
 */
public class XWalkFbWrapper extends Activity {

    // Members
    private XWalkView mXWalkWebView;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xwalk);

        mXWalkWebView =(XWalkView)findViewById(R.id.xwalkWebView);
        mXWalkWebView.load("https://touch.facebook.com", null);

        if (BuildConfig.DEBUG) {
            // turn on debugging
            XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mXWalkWebView != null) {
            mXWalkWebView.pauseTimers();
            mXWalkWebView.onHide();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mXWalkWebView != null) {
            mXWalkWebView.resumeTimers();
            mXWalkWebView.onShow();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mXWalkWebView != null) {
            mXWalkWebView.onDestroy();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mXWalkWebView != null) {
            mXWalkWebView.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
