package com.udec_biomed.aBrainVis;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.udec_biomed.aBrainVis.VisualizationObjects.MRI;
import com.udec_biomed.aBrainVis.VisualizationObjects.Mesh;

import java.util.List;

public class FileListFragment extends ListFragment {
    public boolean DisplayFiles[];
    MyFilesListArrayAdapter fileList;
    List<String> files;


    void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    void detach(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_files_list, container,false);


        files = ((MainActivity)getActivity()).myRenderer.DisplayedFiles;
        DisplayFiles =  new boolean[files.size()];
        fileList = new MyFilesListArrayAdapter(getActivity(),files, DisplayFiles);

        Button deleteButton = (Button) view.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
               @Override
                public void onClick(View v) {
                    for(int i=DisplayFiles.length-1; i>=0 ; i--){
                        if (DisplayFiles[i]){
                            String fileName = files.get(i);

                            if (fileName.contains(".nii") || fileName.contains(".nii.gz")){
                                String axis[] = { "X","Y","Z","MRI","vol"};
                                for (String s : axis){
                                    ((MainActivity) getActivity()).myRenderer.sceneTree.remove(((MainActivity) getActivity()).myRenderer.listDisplayedObjects.get(fileName+s));
                                    if(( (MainActivity) getActivity()).myRenderer.cameraBasedObjects.contains(((MainActivity) getActivity()).myRenderer.listDisplayedObjects.get(fileName+s))){
                                        ((MainActivity) getActivity()).myRenderer.cameraBasedObjects.remove(((MainActivity) getActivity()).myRenderer.listDisplayedObjects.get(fileName+s));
                                    }
                                    ((MainActivity) getActivity()).myRenderer.listDisplayedObjects.remove(fileName+s);

                                }
                            }
                            else{
                                ((MainActivity) getActivity()).myRenderer.sceneTree.remove(((MainActivity) getActivity()).myRenderer.listDisplayedObjects.get(fileName));
                                if(( (MainActivity) getActivity()).myRenderer.cameraBasedObjects.contains(((MainActivity) getActivity()).myRenderer.listDisplayedObjects.get(fileName))){
                                    ((MainActivity) getActivity()).myRenderer.cameraBasedObjects.remove(((MainActivity) getActivity()).myRenderer.listDisplayedObjects.get(fileName));
                                }
                                ((MainActivity) getActivity()).myRenderer.listDisplayedObjects.remove(fileName);
                            }

                            ((MainActivity) getActivity()).myRenderer.DisplayedFiles.remove(fileName);
                            refresh();
                            ((MainActivity) getActivity()).mGLView.requestRender();
                        }

                    }
                }
            });

            Button settingsButton = (Button) view.findViewById(R.id.settings_button);
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int counter = 0;
                    for(int i=0;i< DisplayFiles.length;i++ ){
                        if (DisplayFiles[i]){
                            counter++;
                        }
                    }
                    if(counter>1)
                        Toast.makeText(getActivity(),"Only one element must be selected",Toast.LENGTH_LONG).show();
                    else{
                        for(int i=0;i< DisplayFiles.length;i++ ){
                            if (DisplayFiles[i]){
                                String fileName = files.get(i);
                                int dotIndex = fileName.lastIndexOf('.')+1;
                                int endExtensionIndex = fileName.lastIndexOf(" ");
                                String extension;

                                if (endExtensionIndex>dotIndex) extension = fileName.substring(dotIndex, endExtensionIndex);
                                else extension = fileName.substring(dotIndex);

                                if (MRI.validFileExtensions.contains(extension))
                                    ((MainActivity) getActivity()).startMRI_settings(fileName);
                                else if (com.udec_biomed.aBrainVis.VisualizationObjects.Bundle.validFileExtensions.contains(extension))
                                    ((MainActivity) getActivity()).startBUNDLE_settings(fileName);
                                else if (Mesh.validFileExtensions.contains(extension))
                                    ((MainActivity) getActivity()).startMESH_settings(fileName);
                            }
                        }
                    }
                }
            });

        setListAdapter(fileList);
        return view;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((CheckBox)v.findViewById(R.id.bundlecheckbox)).toggle();
        DisplayFiles[position] = !DisplayFiles[position];

        ((MainActivity) getActivity()).mGLView.requestRender();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}