package com.danvelazco.fbwrapper.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.danvelazco.fbwrapper.R;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final int[] pics;

    public MySimpleArrayAdapter(Context context, String[] values, int[] pics) {
        super(context, R.layout.drawer_list_item, values);
        this.context = context;
        this.values = values;
        this.pics = pics;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.drawer_item_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.drawer_item_icon);
        textView.setText(values[position]);
        imageView.setImageResource(pics[position]);

        return rowView;
    }
}