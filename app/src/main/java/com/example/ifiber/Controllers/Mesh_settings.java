package com.example.ifiber.Controllers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ifiber.MainActivity;
import com.example.ifiber.R;

public class Mesh_settings extends Fragment {
    private TextView FileName;

    private com.example.ifiber.VisualizationObjects.Mesh mesh;
    public SeekBar red, green, blue, alpha;
    public Spinner colorFrom;
    public Switch triangles, wireframe, vertex;
    private int colorSelected;
    private float[] color = new float[3];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_mesh_settings, container,false);

        String meshFile = ((MainActivity) getActivity()).MESH_fileSettings;
        FileName = view.findViewById(R.id.FileText);

        red = view.findViewById(R.id.mesh_red_seekBar);
        green = view.findViewById(R.id.mesh_green_seekBar);
        blue = view.findViewById(R.id.mesh_blue_seekBar);
        alpha = view.findViewById(R.id.mesh_alpha_seekBar);

        colorFrom = view.findViewById(R.id.color_from_spinner);
        colorSelected = colorFrom.getSelectedItemPosition();

        triangles = view.findViewById(R.id.mesh_triangle);
        wireframe = view.findViewById(R.id.mesh_wireframe);
        vertex = view.findViewById(R.id.mesh_vertex);

        mesh = (com.example.ifiber.VisualizationObjects.Mesh) ((MainActivity) getActivity()).myRenderer.getListDisplayedObjects().get(meshFile);
        FileName.setText(mesh.getFileName());

        // Initial values
        updateColorArray();
        red.setProgress((int)(color[0]*255));
        green.setProgress((int)(color[1]*255));
        blue.setProgress((int)(color[2]*255));

        alpha.setProgress((int)(mesh.getAlpha()*100));

        triangles.setChecked(mesh.getDrawTriangles());
        wireframe.setChecked(mesh.getDrawLines());
        vertex.setChecked(mesh.getDrawPoints());

        // Spinner select listener
        colorFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != colorSelected) {
                    colorSelected = position;
                    updateColorArray();

                    red.setProgress((int)(color[0]*255));
                    green.setProgress((int)(color[1]*255));
                    blue.setProgress((int)(color[2]*255));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        // colors seek bars
        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    updateColorArray();
                    seekBar.setProgress((int)(color[0]*255));
                    return;
                }
                color[0] = progress/255f;
                setColorArrayToMesh();
                ((MainActivity) getActivity()).mGLView.requestRender();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    updateColorArray();
                    seekBar.setProgress((int)(color[1]*255));
                    return;
                }
                color[1] = progress/255f;
                setColorArrayToMesh();
                ((MainActivity) getActivity()).mGLView.requestRender();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    updateColorArray();
                    seekBar.setProgress((int)(color[2]*255));
                    return;
                }
                color[2] = progress/255f;
                setColorArrayToMesh();
                ((MainActivity) getActivity()).mGLView.requestRender();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // alpha seek bar
        alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    seekBar.setProgress((int)(mesh.getAlpha()*100));
                    return;
                }
                mesh.setAlpha((float) (progress/100.));
                ((MainActivity) getActivity()).mGLView.requestRender();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Switches for the triangles, wireframe and vertex visualization flag update
        triangles.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(mesh.getDrawTriangles());
            }

        });

        triangles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mesh.setDrawTriangles(!((Switch) v).isChecked());
                ((Switch) v).setChecked(!((Switch) v).isChecked());

                ((MainActivity) getActivity()).mGLView.requestRender();
            }
        });

        wireframe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(mesh.getDrawLines());
            }

        });

        wireframe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mesh.setDrawLines(!((Switch) v).isChecked());
                ((Switch) v).setChecked(!((Switch) v).isChecked());

                ((MainActivity) getActivity()).mGLView.requestRender();
            }
        });

        vertex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(mesh.getDrawPoints());
            }

        });

        vertex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mesh.setDrawPoints(!((Switch) v).isChecked());
                ((Switch) v).setChecked(!((Switch) v).isChecked());

                ((MainActivity) getActivity()).mGLView.requestRender();
            }
        });

        return view;
    }


    private void updateColorArray() {
        switch (colorSelected) {
            case 0:
                mesh.getTriangleColor(color, 0);
                break;
            case 1:
                mesh.getLinesColor(color, 0);
                break;
            case 2:
                mesh.getPointsColor(color, 0);
                break;
            default:
                Log.e("MESH_SETTINGS", "Unexpected item selected in 'color from spinner'");
                break;
        }
    }


    private void setColorArrayToMesh() {
        switch (colorSelected) {
            case 0:
                mesh.setTriangleColor(color[0], color[1], color[2]);
                break;
            case 1:
                mesh.setLinesColor(color[0], color[1], color[2]);
                break;
            case 2:
                mesh.setPointsColor(color[0], color[1], color[2]);
                break;
            default:
                Log.e("MESH_SETTINGS", "Unexpected item selected in 'color from spinner'");
                break;
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}