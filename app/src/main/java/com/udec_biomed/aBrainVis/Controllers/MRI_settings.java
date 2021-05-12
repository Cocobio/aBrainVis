package com.udec_biomed.aBrainVis.Controllers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;


import com.udec_biomed.aBrainVis.MainActivity;
import com.udec_biomed.aBrainVis.R;
import com.udec_biomed.aBrainVis.VisualizationObjects.MRI;
import com.udec_biomed.aBrainVis.VisualizationObjects.MRISlice;
import com.udec_biomed.aBrainVis.VisualizationObjects.MRIVolume;


public class MRI_settings extends Fragment {

    public SeekBar sliceX, sliceY, sliceZ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_mri_settings, container,false);

        String mriFile = ((MainActivity) getActivity()).MRI_fileSettings;
        sliceX= view.findViewById(R.id.seekBar_x);
        sliceY= view.findViewById(R.id.seekBar_y);
        sliceZ=view.findViewById(R.id.seekBar_z);
        sliceX.setPadding(0,0,0,0);
        sliceY.setPadding(0,0,0,0);
        sliceZ.setPadding(0,0,0,0);

        final int[] dim = new int[4];
        MRI mri = (MRI)((MainActivity) getActivity()).myRenderer.getListDisplayedObjects().get(mriFile+"MRI");

        mri.getMRIDimension(dim,0);
        final MRISlice x = (MRISlice)((MainActivity) getActivity()).myRenderer.getListDisplayedObjects().get(mriFile+"X");
        final MRISlice y = (MRISlice)((MainActivity) getActivity()).myRenderer.getListDisplayedObjects().get(mriFile+"Y");
        final MRISlice z = (MRISlice)((MainActivity) getActivity()).myRenderer.getListDisplayedObjects().get(mriFile+"Z");
        final MRIVolume vol = (MRIVolume)((MainActivity) getActivity()).myRenderer.getListDisplayedObjects().get(mriFile+"vol");

        int sX = (int)(x.getSlicePosition()*dim[0]);
        sliceX.setMax(dim[0]);
        sliceX.setProgress(sX);

        int sY = (int)(y.getSlicePosition()*dim[1]);
        sliceY.setMax(dim[1]);
        sliceY.setProgress(sY);

        int sZ = (int)(z.getSlicePosition()*dim[2]);
        sliceZ.setMax(dim[2]);
        sliceZ.setProgress(sZ);

        SeekBar[] SB_arr = {sliceX, sliceY, sliceZ};
        final MRISlice[] MRI_arr = {x,y,z};
        for(int i=0;i<3;i++){
            final int finalI = i;
            SB_arr[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        seekBar.setProgress((int)(MRI_arr[finalI].getSlicePosition()*dim[finalI]));
                        return;
                    }
                    MRI_arr[finalI].setSlicePosition(((float)progress)/dim[finalI]);
                    ((MainActivity) getActivity()).mGLView.requestRender();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

        }

        CheckBox CBx= view.findViewById(R.id.checkBox_x);
        CheckBox CBy= view.findViewById(R.id.checkBox_y);
        CheckBox CBz= view.findViewById(R.id.checkBox_z);

        CBx.setChecked(x.getDraw());
        CBy.setChecked(y.getDraw());
        CBz.setChecked(z.getDraw());
        CheckBox[] CB_arr = {CBx, CBy, CBz};

        for(int i=0;i<3;i++) {
            final int finalI = i;
            CB_arr[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    buttonView.setChecked(MRI_arr[finalI].getDraw());
                }

            });

            CB_arr[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MRI_arr[finalI].setDraw(!((CheckBox) v).isChecked());
                    ((CheckBox) v).setChecked(!((CheckBox) v).isChecked());

                    ((MainActivity) getActivity()).mGLView.requestRender();
                }
            });
        }

        final CheckBox CBvol= view.findViewById(R.id.checkBox_volume);
        CBvol.setChecked(vol.getDraw());

        CBvol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(vol.getDraw());
            }

        });

        CBvol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vol.setDraw(!((CheckBox) v).isChecked());
                ((CheckBox) v).setChecked(!((CheckBox) v).isChecked());

                ((MainActivity) getActivity()).mGLView.requestRender();
            }
        });


        SeekBar VolAlpha = view.findViewById(R.id.seekBar_alpha);
        VolAlpha.setProgress((int)(vol.getAlpha()*100));
        VolAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    seekBar.setProgress((int)(vol.getAlpha()*100));
                    return;
                }
                vol.setAlpha((float) (progress/100.));
                ((MainActivity) getActivity()).mGLView.requestRender();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

}