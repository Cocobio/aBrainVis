package com.udec_biomed.aBrainVis.Controllers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.udec_biomed.aBrainVis.MainActivity;
import com.udec_biomed.aBrainVis.MyBundleListArrayAdapter;
import com.udec_biomed.aBrainVis.R;

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
    private com.udec_biomed.aBrainVis.VisualizationObjects.Bundle bundle;
    private Spinner cylinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_bundle_settings, container,false);

        String bundleFile = ((MainActivity) getActivity()).BUNDLE_fileSettings;
        FileName = (TextView)view.findViewById(R.id.FileText);
        cylinder = view.findViewById(R.id.bundle_rendering_spinner);
        item = new ArrayList<String>();

        bundle = (com.udec_biomed.aBrainVis.VisualizationObjects.Bundle) ((MainActivity) getActivity()).myRenderer.getListDisplayedObjects().get(bundleFile);
        FileName.setText(bundle.getFileName());

        final Vector<String> bundlesNames = bundle.getBundlesNames();
        DisplayBundles = new boolean[bundlesNames.size()];
        bundle.getSelectedBundles(DisplayBundles, 0);

        cylinder.setSelection(bundle.getSelectedShader());

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

        // Spinner select listener
        cylinder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                bundle.setSelectedShader(position);
                ((MainActivity) getActivity()).mGLView.requestRender();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
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