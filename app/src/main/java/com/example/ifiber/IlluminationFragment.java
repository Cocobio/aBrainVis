package com.example.ifiber;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class IlluminationFragment extends Fragment {
    private static final int [] seekarID={R.id.red,R.id.green,R.id.blue,R.id.ilum1,R.id.ilum2,R.id.ilum3,R.id.ilum4,R.id.ilum5,R.id.ilum6,R.id.ilum7,R.id.red_mesh,R.id.green_mesh,R.id.blue_mesh,R.id.alpha};
    public static SeekBar[] Colors = new SeekBar[14];
    private float divider[]={255f,255f,255f,100f,100f,100f,100f,100f,100f,20f,255f,255f,255f,100f};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_illum, container,false);

        for(int i =0;i<Colors.length;i++) {
            final int finalI = i;
            Colors[i]=(SeekBar) view.findViewById(seekarID[i]);
            Colors[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progressChanged = 0;
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressChanged = progress;
                    ((MainActivity) getActivity()).myRenderer.onChangeBackground(progressChanged / divider[finalI], finalI);
                    ((MainActivity) getActivity()).mGLView.requestRender();
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }
}