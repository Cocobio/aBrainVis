package com.example.ifiber;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
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

public class BundleListFragment extends ListFragment {

    private List<String> item = null;
    private TextView FileName;
    public boolean DisplayBundles[];
    MyBundleListArrayAdapter fileList;
    int percentage;
    public SeekBar percent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_bundle_list, container,false);

        FileName = (TextView)view.findViewById(R.id.FileText);
        item = new ArrayList<String>();

//        if(((MainActivity) getActivity()).myRenderer.currentObject!=null) {
//            if(((MainActivity) getActivity()).myRenderer.getCurrentObjectFileName()!=null){
//                String name=((MainActivity) getActivity()).myRenderer.getBundleFileName();
//                FileName.setText(name);
//            }
//            final Vector<String> bundles = ((MainActivity) getActivity()).myRenderer.bun.bundleNames;
//            DisplayBundles=((MainActivity) getActivity()).myRenderer.selectedBundles;
//            for (int i = 0; i < bundles.size(); i++) {
//                item.add(bundles.get(i));
//            }
//            Button SelectAll = (Button) view.findViewById(R.id.selectall_button);
//            SelectAll.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ChangeState(v, bundles, true);
//                }
//            });
//            Button UnselectAll = (Button) view.findViewById(R.id.unselect_button);
//            UnselectAll.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ChangeState(v, bundles, false);
//                }
//            });
//            percent=(SeekBar) view.findViewById(R.id.PercentageseekBar);
//            percent.refreshDrawableState();
//            percent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                @Override
//                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    if(((MainActivity) getActivity()).myRenderer.flagResetSeekBar){
//                        percent.setProgress(((MainActivity) getActivity()).myRenderer.percentage);
//                        ((MainActivity) getActivity()).myRenderer.flagResetSeekBar=false;
//                    }else{
//                        percentage = progress;
//                        ((MainActivity) getActivity()).myRenderer.percentage=percentage;
//                        ((MainActivity) getActivity()).myRenderer.flagPartialData=true;
//                        ((MainActivity) getActivity()).mGLView.requestRender();
//                    }
//                }
//                @Override
//                public void onStartTrackingTouch(SeekBar seekBar) {
//                }
//                @Override
//                public void onStopTrackingTouch(SeekBar seekBar) {
//                }
//            });
//        }
//        else if (((MainActivity) getActivity()).myRenderer.MeshPath!=null){
//            if(((MainActivity) getActivity()).myRenderer.getMeshFileName()!=null){
//                String name=((MainActivity) getActivity()).myRenderer.getMeshFileName();
//                FileName.setText(name);
//
//            }
//
//        }

        fileList = new MyBundleListArrayAdapter(getActivity(),item, DisplayBundles);
        setListAdapter(fileList);
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((CheckBox)v.findViewById(R.id.bundlecheckbox)).toggle();
        DisplayBundles[position] = !DisplayBundles[position];

//        ((MainActivity) getActivity()).myRenderer.flagPartialData=true;
        ((MainActivity) getActivity()).mGLView.requestRender();
    }

    private void ChangeState(View v,Vector<String> bundles, boolean isChecked){
        for (int i = 0; i < bundles.size(); i++) {
            DisplayBundles[i] = isChecked;
        }
        fileList.notifyDataSetChanged();
//        ((MainActivity) getActivity()).myRenderer.flagPartialData=true;
        ((MainActivity) getActivity()).mGLView.requestRender();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}