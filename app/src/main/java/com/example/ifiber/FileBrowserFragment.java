package com.example.ifiber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowserFragment extends ListFragment {

    private ArrayList<String> validExtensions;

    private List<String> item = null;
    private List<String> path = null;
    private String root;
    private TextView myPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_file_browser, container,false);
        validExtensions = ((MainActivity) getActivity()).myRenderer.getValidFileExtensions();

        myPath = (TextView)view.findViewById(R.id.path);
        root = Environment.getExternalStorageDirectory().getPath();
        getDir(root);

        return view;
    }

    private void getDir(String dirPath) {
        myPath.setText("Location: " + dirPath);
        item = new ArrayList<String>();
        path = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();

        if(!dirPath.equals(root))
        {
            item.add(root+".root");
            path.add(root);
            item.add("back");
            path.add(f.getParent());
        }

        for (File file : files) {
            String filename = file.getName();
            String extension = filename.substring(filename.lastIndexOf(".") + 1);

            if(!file.isHidden() && file.canRead()){

                if(file.isDirectory()){
                    item.add(file.getName() + "/");
                    path.add(file.getPath());
                }else if(validExtensions.contains(extension)){
                    item.add(file.getName());
                    path.add(file.getPath());
                }
            }
        }

        MyArrayAdapter fileList = new MyArrayAdapter(getActivity(),item);
        setListAdapter(fileList);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        final File file = new File(path.get(position));

        if (file.isDirectory())
        {
            if(file.canRead()){
                getDir(path.get(position));
            }else{
                new AlertDialog.Builder(getActivity())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("[" + file.getName() + "] folder can't be read!")
                        .setPositiveButton("OK", null).show();
            }
        }else {
//            if(file.toString().endsWith(".bundles")){
            AlertDialog dialog =new AlertDialog.Builder(getActivity())
                    .setMessage("Display " + file.getName() + "?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((MainActivity) getActivity()).myRenderer.readFile(file.toString());
                            ((MainActivity) getActivity()).mGLView.requestRender();
                            ((MainActivity) getActivity()).fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(((MainActivity) getActivity()).FBFragment).commit();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    }).create();
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                dialog.show();
//            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        }
}