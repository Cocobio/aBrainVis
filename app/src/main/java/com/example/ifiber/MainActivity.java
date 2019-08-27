package com.example.ifiber;

import android.opengl.GLSurfaceView;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;

import com.example.ifiber.Controllers.Bundle_settings;
import com.example.ifiber.Controllers.MRI_settings;
import com.example.ifiber.Controllers.Mesh_settings;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public GLSurfaceView mGLView;
    public MyGLRenderer myRenderer;
    public FrameLayout FragmentContainer, GLcontainer;
    public IlluminationFragment IlumFragment;
    public FileBrowserFragment FBFragment;
    public BundleListFragment BLFragment;
    public FileListFragment FLFragment;

    public MRI_settings MRI_settingsFragment;
    public Bundle_settings BUNDLE_settingsFragment;
    public Mesh_settings MESH_settingsFragment;

    public String MRI_fileSettings;
    public String BUNDLE_fileSettings;
    public String MESH_fileSettings;

    FragmentManager fragmentManager = getSupportFragmentManager();

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        IlumFragment=new IlluminationFragment();
        FBFragment=new FileBrowserFragment();
        BLFragment=new BundleListFragment();
        FLFragment = new FileListFragment();

        // Setting fragments
        MRI_settingsFragment = new MRI_settings();
        BUNDLE_settingsFragment = new Bundle_settings();
        MESH_settingsFragment = new Mesh_settings();

        FragmentContainer=(FrameLayout)findViewById(R.id.c2);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));

        myRenderer = new MyGLRenderer();
        mGLView = new MyGLSurfaceView(this,myRenderer);
        GLcontainer=(FrameLayout)findViewById(R.id.c1);
        GLcontainer.addView(mGLView);
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
                    if(MRI_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(MRI_settingsFragment).commit();
                    if(BUNDLE_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(BUNDLE_settingsFragment).commit();
                    if(MESH_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(MESH_settingsFragment).commit();

                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, FBFragment).commit();
                    }
                break;
            case 2:
                    if (IlumFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom).remove(IlumFragment).commit();
                    if (FBFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FBFragment).commit();
                    if(BLFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(BLFragment).commit();
                    if(FLFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FLFragment).commit();
                    if(MRI_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(MRI_settingsFragment).commit();
                    if(BUNDLE_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(BUNDLE_settingsFragment).commit();
                    if(MESH_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(MESH_settingsFragment).commit();
                break;
            case 3:
                myRenderer.ResetCamera();
                mGLView.requestRender();
                break;
            case 4:
                if(!FLFragment.isAdded()){
                    if(FBFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FBFragment).commit();
                    if(BLFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(BLFragment).commit();
                    if(MRI_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(MRI_settingsFragment).commit();
                    if(BUNDLE_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(BUNDLE_settingsFragment).commit();
                    if(MESH_settingsFragment.isAdded())
                        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(MESH_settingsFragment).commit();

                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, FLFragment).commit();
                }
                break;
            case 5:
                myRenderer.toggleBoundingBoxes();
                mGLView.requestRender();
                break;
        }
    }

    void startMRI_settings(String fileName) {
        MRI_fileSettings = fileName;
        if(FLFragment.isAdded())
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FLFragment).commit();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, MRI_settingsFragment).commit();
    }


    void startMESH_settings(String fileName) {
        MESH_fileSettings = fileName;
        if(FLFragment.isAdded())
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FLFragment).commit();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, MESH_settingsFragment).commit();
    }


    void startBUNDLE_settings(String fileName) {
        BUNDLE_fileSettings = fileName;
        if(FLFragment.isAdded())
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).remove(FLFragment).commit();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right).add(R.id.c3, BUNDLE_settingsFragment).commit();
    }

    @Override
    protected void onPause() {
        Log.e("MainActivity", "onPause");
        super.onPause();
        mGLView.onPause();
        myRenderer.onPause();
    }

    @Override
    protected void onResume() {
        Log.e("MainActivity", "onResume");
        super.onResume();
        mGLView.onResume();
        myRenderer.onResume();
    }
}
