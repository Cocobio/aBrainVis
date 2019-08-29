package com.example.ifiber;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ifiber.VisualizationObjects.MRI;
import com.example.ifiber.VisualizationObjects.Mesh;

import java.util.List;
import java.util.Vector;

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

        Button SelectAll = (Button) view.findViewById(R.id.delete_button);
            SelectAll.setOnClickListener(new View.OnClickListener() {
               @Override
                public void onClick(View v) {
                    for(int i=DisplayFiles.length-1; i>=0 ; i--){
                        Log.e("FLF", "DisplayFiles["+i+"]: "+DisplayFiles[i]);
                        if (DisplayFiles[i]){
                            String fileName = files.get(i);
                            Log.e("FLF", "fileName: "+fileName);
                            if (fileName.endsWith(".nii")|| fileName.endsWith(".nii.gz")){
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
            Button UnselectAll = (Button) view.findViewById(R.id.settings_button);
            UnselectAll.setOnClickListener(new View.OnClickListener() {
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
                                String extension = fileName.substring(fileName.lastIndexOf('.')+1);

                                if (MRI.validFileExtensions.contains(extension))
                                    ((MainActivity) getActivity()).startMRI_settings(fileName);
                                else if (com.example.ifiber.VisualizationObjects.Bundle.validFileExtensions.contains(extension))
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