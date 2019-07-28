package com.example.ifiber;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;

    public MyArrayAdapter(Context context, List<String> values) {
        super(context, R.layout.row, values);
        this.context = context;
        this.values = (ArrayList<String>) values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.rowtext);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.IconView);
        String s = values.get(position);
        if (s.endsWith("/")) {
            imageView.setImageResource(R.drawable.directory_icon);
            textView.setText(values.get(position).replace("/", ""));
        }
        else if (s.endsWith(".bundles")){
            imageView.setImageResource(R.drawable.file_icon);
            textView.setText(values.get(position).replace(".bundles", ""));
        }
        else if (s.endsWith(".trk")){
            imageView.setImageResource(R.drawable.file_icon_trk);
            textView.setText(values.get(position).replace(".trk", ""));
        }
        else if (s.endsWith(".nii")){
            imageView.setImageResource(R.drawable.file_icon_mri);
            textView.setText(values.get(position).replace(".nii", ""));
        }
        else if (s.endsWith(".nii.gz")){
            imageView.setImageResource(R.drawable.file_icon_mri);
            textView.setText(values.get(position).replace(".nii.gz", ""));
        }
        else if (s.endsWith(".mesh")){
            imageView.setImageResource(R.drawable.file_icon_mesh);
            textView.setText(values.get(position).replace(".mesh", ""));
        }
        else if (s.equals("back")){
            imageView.setImageResource(R.drawable.directory_up);
            textView.setText(values.get(position).replace("back", ""));
        }
        else if (s.endsWith(".root")){
            imageView.setImageResource(R.drawable.home_icon);
            textView.setText(values.get(position).replace(s, ""));
        }
        return rowView;
    }
}