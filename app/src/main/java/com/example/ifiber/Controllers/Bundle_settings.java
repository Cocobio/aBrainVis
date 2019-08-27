package com.example.ifiber.Controllers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ifiber.MainActivity;
import com.example.ifiber.MyBundleListArrayAdapter;
import com.example.ifiber.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Bundle_settings extends ListFragment {

    private List<String> item = null;
    private TextView FileName;
    public boolean DisplayBundles[];
    MyBundleListArrayAdapter fileList;
    int percentage;
    public SeekBar percent;
    private com.example.ifiber.VisualizationObjects.Bundle bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_bundle_settings, container,false);

        String bundleFile = ((MainActivity) getActivity()).BUNDLE_fileSettings;
        FileName = (TextView)view.findViewById(R.id.FileText);
        item = new ArrayList<String>();

        bundle = (com.example.ifiber.VisualizationObjects.Bundle) ((MainActivity) getActivity()).myRenderer.getListDisplayedObjects().get(bundleFile);
        FileName.setText(bundle.getFileName());

        final Vector<String> bundlesNames = bundle.getBundlesNames();
        DisplayBundles = new boolean[bundlesNames.size()];
        bundle.getSelectedBundles(DisplayBundles, 0);

        for (int i=0; i<bundlesNames.size(); i++) item.add(bundlesNames.get(i));

        Button SelectAll = (Button) view.findViewById(R.id.selectall_button);
        SelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeState(v, bundlesNames, true);
            }
        });

        Button UnselectAll = (Button) view.findViewById(R.id.unselect_button);
        UnselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeState(v, bundlesNames, false);
            }
        });

        percent = view.findViewById(R.id.PercentageseekBar);
        percent.refreshDrawableState();
        percent.setMax(100);
        percent.setProgress(bundle.getPercentage());
        percent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    percent.setProgress(bundle.getPercentage());
                    return;
                }
                    percentage = progress;
                    bundle.setPercentage(percentage);
                    ((MainActivity) getActivity()).mGLView.requestRender();
//                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        fileList = new MyBundleListArrayAdapter(getActivity(), item, DisplayBundles);
        setListAdapter(fileList);
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((CheckBox)v.findViewById(R.id.bundlecheckbox)).toggle();
        DisplayBundles[position] = !DisplayBundles[position];

        bundle.setSelectedBundles(DisplayBundles, 0);
        ((MainActivity) getActivity()).mGLView.requestRender();
    }

    private void ChangeState(View v,Vector<String> bundles, boolean isChecked){
        for (int i = 0; i < bundles.size(); i++) {
            DisplayBundles[i] = isChecked;
        }
        fileList.notifyDataSetChanged();

        bundle.setSelectedBundles(DisplayBundles, 0);
        ((MainActivity) getActivity()).mGLView.requestRender();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}