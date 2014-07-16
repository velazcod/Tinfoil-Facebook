package com.danvelazco.fbwrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.danvelazco.fbwrapper.activity.BaseFacebookWebViewActivity;
import com.danvelazco.fbwrapper.preferences.FacebookPreferences;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class AccessTokenDialogFragment extends DialogFragment {
	
	private WebView mWebView;
	
	public static AccessTokenDialogFragment newInstance() {
		AccessTokenDialogFragment f = new AccessTokenDialogFragment();
        f.setArguments(new Bundle());
        return f;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.access_token_fragment_dialog, container, false);
        
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        v.findViewById(R.id.access_layout).setMinimumHeight((int) (height*0.7));
        v.findViewById(R.id.access_layout).setMinimumWidth((int) (width*0.9));
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        mWebView = (WebView)v.findViewById(R.id.access_webview);
        mWebView.setWebViewClient(new WebViewClient() {
        	
        	@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		if (url.startsWith("https://alexburka.com/tinfoil")) {
        			((FacebookPreferences)getActivity()).received_access_token(url.split("=")[1].split("&")[0]);
        			dismiss();
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        		Toast.makeText(getActivity(), "Internet error: " + description, Toast.LENGTH_SHORT).show();
        		dismiss();
        	}
        });
        mWebView.loadUrl("https://graph.facebook.com/oauth/authorize?type=user_agent&client_id=266257470187937&redirect_uri=https%3A%2F%2Falexburka.com%2Ftinfoil&scope=user_friends&offline_access=true");

        return v;
    }

}
