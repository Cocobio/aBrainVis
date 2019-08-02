package com.example.ifiber;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public GLSurfaceView mGLView;
    public MyGLRenderer myRenderer;
    public FrameLayout FragmentContainer, GLcontainer;
    public IlluminationFragment IlumFragment;
    public FileBrowserFragment FBFragment;
    public BundleListFragment BLFragment;
    public FileListFragment FLFragment;
    public MRI_settings MRI_settginsFragment;
    public String MRI_fileSettings;
    FragmentManager fragmentManager = getSupportFragmentManager();

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        IlumFragment=new IlluminationFragment();
        FBFragment=new FileBrowserFragment();
        BLFragment=new BundleListFragment();
        FLFragment = new FileListFragment();
        MRI_settginsFragment = new MRI_settings();

        FragmentContainer=(FrameLayout)findViewById(R.id.c2);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));

        myRenderer = new MyGLRenderer();
        mGLView = new MyGLSurfaceView(this,myRenderer);
        GLcontainer=(FrameLayout)findViewById(R.id.c1);
        GLcontainer.addView(mGLView);
        Log.d("MainActivity", "----------------------------------------------------------------------------------------------------------------------------------------------------");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.d("MainActivity", "onNavigationDrawerItemSelected");
        switch (position) {
            case 0:
                if(!IlumFragment.isAdded())
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom).add(R.id.c2, IlumFragment).commit();
                break;
            case 1:
                if(!FBFragment.isAdded()){
                    if(BLFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(BLFragment).commit();
                    if(FLFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FLFragment).commit();

                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, FBFragment).commit();
                    }
                break;
            case 2:
                if(!BLFragment.isAdded()){
                    if(FBFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FBFragment).commit();
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, BLFragment).commit();
                    }
                break;
            case 3:
                    if (IlumFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom).remove(IlumFragment).commit();
                    if (FBFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FBFragment).commit();
                    if(BLFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(BLFragment).commit();
                    if(FLFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FLFragment).commit();
                    if(MRI_settginsFragment.isAdded())
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(MRI_settginsFragment).commit();
                break;
            case 4:
                myRenderer.ResetCamera();
                mGLView.requestRender();
                break;
            case 5:
                if(!FLFragment.isAdded()){
                    if(FBFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FBFragment).commit();
                    if(BLFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(BLFragment).commit();

                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, FLFragment).commit();
                }
                break;
        }
    }

    void startMRI_settings(String fileName){
        MRI_fileSettings = fileName;
        if(FLFragment.isAdded())
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FLFragment).commit();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, MRI_settginsFragment).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
}