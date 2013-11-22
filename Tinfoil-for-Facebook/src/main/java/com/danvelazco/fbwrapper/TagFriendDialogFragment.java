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

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class TagFriendDialogFragment extends DialogFragment {
	
	private class GraphSearchTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			mID = ID_LOADING;
			tag_list.setAdapter( new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_black_list_item, new String[]{"..."}));
		}
		
		@Override
		protected String doInBackground(String... names) {
			String name = names[0];
			
			HttpClient client = new DefaultHttpClient();
            String json = "";
            try {
                String line = "";
                Log.d("TFD", BaseFacebookWebViewActivity.URL_GRAPH_SEARCH(getActivity(), name));
                HttpGet request = new HttpGet(BaseFacebookWebViewActivity.URL_GRAPH_SEARCH(getActivity(), name));
                HttpResponse response = client.execute(request);
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                while ((line = rd.readLine()) != null) {
                    json += line + System.getProperty("line.separator");
                }
            } catch (IllegalArgumentException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return json;
		}
		
		@Override
		protected void onPostExecute(String json) {
			Log.d("TFD", json);
			
			mIDs.clear();
			mFulls.clear();
			mFirsts.clear();
			mLasts.clear();
			try {
				JSONArray peeps = new JSONObject(json).getJSONArray("data");
				for (int i = 0; i < peeps.length(); ++i) {
					mIDs.add(peeps.getJSONObject(i).getString("uid"));
					mFulls.add(peeps.getJSONObject(i).getString("name"));
					mFirsts.add(peeps.getJSONObject(i).getString("first_name"));
					mLasts.add(peeps.getJSONObject(i).getString("last_name"));
				}
				
		        tag_list.setAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_black_list_item, mFulls));
		        mID = ID_CHOOSEFRIEND;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    }
	
	private View.OnClickListener mClickListener = null;
    private TextWatcher mTextListener = null;
    private GraphSearchTask mTask = null;
    private static final int ID_LOADING = -1;
    private static final int ID_CHOOSEFRIEND = -2;
    private int mID = ID_LOADING;
	private ArrayList<String> mIDs, mFulls, mFirsts, mLasts;
	private EditText tag_in;
	private ListView tag_list;
	
	static TagFriendDialogFragment newInstance() {
		TagFriendDialogFragment f = new TagFriendDialogFragment();
        f.setArguments(new Bundle());
        return f;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.tag_friend_fragment_dialog, container, false);
        tag_in = (EditText)v.findViewById(R.id.tag_in);
        tag_list = (ListView)v.findViewById(R.id.tag_list);
        
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        
        // List view adapter
        ((ListView)v.findViewById(R.id.tag_list)).setAdapter( new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_black_list_item, new String[]{"enter name above"}));
        
        mIDs = new ArrayList<String>();
        mFulls = new ArrayList<String>();
        mFirsts = new ArrayList<String>();
        mLasts = new ArrayList<String>();

        // listeners
        mTextListener = new TextWatcher() {

    		@Override
    		public void afterTextChanged(Editable s) {
    			// don't care
    			
    		}

    		@Override
    		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    			// don't care
    			
    		}

    		@Override
    		public void onTextChanged(CharSequence s, int start, int before, int count) {   
    			
    			if (s.length() > 0) {
    				if (mTask != null) mTask.cancel(true);
    				mTask = new GraphSearchTask();
    				mTask.execute(s.toString());
    			}
    		}
        };

        tag_in.addTextChangedListener(mTextListener);
        tag_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mID == ID_LOADING) {
					return;
				}
				if (mID != ID_CHOOSEFRIEND) {
					String name;
					switch (position) {
					case 0:
						name = "0";
						break;
					case 1:
						name = mFirsts.get(mID);
						break;
					case 2:
						name = mLasts.get(mID);
						break;
					default:
						return;
					}
					String str = "@[" + mIDs.get(mID) + ":" + name + "]";
	            	
	                ((FbWrapper)getActivity()).friendTagged(str);
	                dismiss();
				} else {
					mID = position;
				
					tag_list.setAdapter( new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_black_list_item, new String[]{"tag as: " + mFulls.get(position), "tag as: " + mFirsts.get(position), "tag as: " + mLasts.get(position)}));
				}
			}
        	
        });

        return v;
    }

}
