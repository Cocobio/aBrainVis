package com.example.ifiber;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class MyFilesListArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;
    public CheckBox cb;
    public boolean DisplayBundles[];

    public MyFilesListArrayAdapter(Context context, List<String> values, boolean[] displayBundle) {
        super(context, R.layout.bundle_list_row, values);
        this.context = context;
        this.values = (ArrayList<String>) values;
        DisplayBundles = displayBundle;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bundle_list_row, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.bundlerowtext);
        cb=(CheckBox) rowView.findViewById(R.id.bundlecheckbox);
        cb.setChecked(DisplayBundles[position]);

        String s = values.get(position);
        String[] fileName = s.split("/");
        textView.setText(fileName[fileName.length-1]);
        return rowView;


    }
}

