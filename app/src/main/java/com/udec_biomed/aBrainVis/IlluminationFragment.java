package com.udec_biomed.aBrainVis;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.udec_biomed.aBrainVis.VisualizationObjects.MRIVolume;
import com.udec_biomed.aBrainVis.VisualizationObjects.Mesh;

public class IlluminationFragment extends Fragment {
    private static final int [] seekarID={R.id.bg_red_seekbar,R.id.bg_green_seekbar,R.id.bg_blue_seekbar,R.id.light_ambient_seekbar,R.id.light_diffuse_seekbar,R.id.light_specular_seekbar,R.id.material_ambient_seekbar,R.id.material_diffuse_seekbar,R.id.material_specular_seekbar,R.id.material_shininess_seekbar};
    public static SeekBar[] Colors = new SeekBar[10];
    private float divider[]={255f,255f,255f,100f,100f,100f,100f,100f,100f,20f};
    private SeekBar bg_red, bg_green, bg_blue;
    private SeekBar light_ambient, light_diffuse, light_specular;
    private SeekBar materialKa, materialKd, materialKs, materialSh;
    private Spinner materialID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_illum, container,false);

        // Background color
        bg_red = view.findViewById(R.id.bg_red_seekbar);
        bg_green = view.findViewById(R.id.bg_green_seekbar);
        bg_blue = view.findViewById(R.id.bg_blue_seekbar);
        SeekBar[] iter = {bg_red, bg_green, bg_blue};

        for (SeekBar seekBar : iter) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ((MainActivity) getActivity()).myRenderer.changeBackground(bg_red.getProgress()/255f, bg_green.getProgress()/255f, bg_blue.getProgress()/255f);
                    ((MainActivity) getActivity()).mGLView.requestRender();
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        // Light parameters
        light_ambient = view.findViewById(R.id.light_ambient_seekbar);
        light_diffuse = view.findViewById(R.id.light_diffuse_seekbar);
        light_specular = view.findViewById(R.id.light_specular_seekbar);
        iter[0] = light_ambient;
        iter[1] = light_diffuse;
        iter[2] = light_specular;

        for (SeekBar seekBar : iter) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ((MainActivity) getActivity()).myRenderer.changeLight(light_ambient.getProgress()/100f, light_diffuse.getProgress()/100f, light_specular.getProgress()/100f);
                    ((MainActivity) getActivity()).mGLView.requestRender();
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        // Materials parameters
        materialKa = view.findViewById(R.id.material_ambient_seekbar);
        materialKd = view.findViewById(R.id.material_diffuse_seekbar);
        materialKs = view.findViewById(R.id.material_specular_seekbar);
        materialSh = view.findViewById(R.id.material_shininess_seekbar);
        materialID = view.findViewById(R.id.materialSpinner);

        SeekBar[] iter2 = {materialKa, materialKd, materialKs, materialSh};


        for (SeekBar seekBar : iter2) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        float[] materialValues = new float[4];
                        switch (materialID.getSelectedItemPosition()) {
                            case 0:
                                com.udec_biomed.aBrainVis.VisualizationObjects.Bundle.getMaterialValues(materialValues, 0);
                                break;
                            case 1:
                                Mesh.getMaterialValues(materialValues, 0);
                                break;
                            case 2:
                                MRIVolume.getMaterialValues(materialValues, 0);
                                break;
                        }
                        if (seekBar == materialKa) materialKa.setProgress((int)(materialValues[0]*100f));
                        if (seekBar == materialKd) materialKd.setProgress((int)(materialValues[1]*100f));
                        if (seekBar == materialKs) materialKs.setProgress((int)(materialValues[2]*100f));
                        if (seekBar == materialSh) materialSh.setProgress((int)(materialValues[3]*100f));
                        return;
                    }
                    ((MainActivity) getActivity()).myRenderer.changeMaterial(materialID.getSelectedItemPosition(), materialKa.getProgress()/100f, materialKd.getProgress()/100f, materialKs.getProgress()/100f, materialSh.getProgress()/100f);
                    ((MainActivity) getActivity()).mGLView.requestRender();
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        // Spinner select listener
        materialID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                float[] materialValues = new float[4];
                switch (position) {
                    case 0:
                        com.udec_biomed.aBrainVis.VisualizationObjects.Bundle.getMaterialValues(materialValues, 0);
                        break;
                    case 1:
                        Mesh.getMaterialValues(materialValues, 0);
                        break;
                    case 2:
                        MRIVolume.getMaterialValues(materialValues, 0);
                        break;
                }
                materialKa.setProgress((int)(materialValues[0]*100f));
                materialKd.setProgress((int)(materialValues[1]*100f));
                materialKs.setProgress((int)(materialValues[2]*100f));
                materialSh.setProgress((int)(materialValues[3]*100f));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }
}