package com.example.ifiber;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.ifiber.Tools.Shader;
import com.example.ifiber.Tools.VisualizationType;
import com.example.ifiber.Tools.Camera;
import com.example.ifiber.VisualizationObjects.BaseVisualization;
import com.example.ifiber.VisualizationObjects.Bundle;
import com.example.ifiber.VisualizationObjects.CameraBasedVisualizations;
import com.example.ifiber.VisualizationObjects.MRI;
import com.example.ifiber.VisualizationObjects.MRISlice;
import com.example.ifiber.VisualizationObjects.MRIVolume;
import com.example.ifiber.VisualizationObjects.Mesh;
import com.example.ifiber.VisualizationObjects.CoordinateSystem;
import com.example.ifiber.VisualizationObjects.BoundingBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private static final String[] visualizationBaseClasses = {"Bundle", "Mesh", "MRI", "MRISlice", "MRIVolume"};
    private static final String TAG = "MyGLRenderer";

    private float[] systemScale = new float[16];
    private float scaleFactor = 0.02f;

    final private static float[] defaultBackgroundColor = {0.2f, 0.2f, 0.2f, 1.0f};
    final private static float[] defaultLightPosition = {0f, 100f, 0f};
    final private float defaultLightLa = 0.5f;
    final private float defaultLightLd = 0.6f;
    final private float defaultLightLs = 1f;
    final private float defaultMaterialKa = 1f;
    final private float defaultMaterialKd = 0.8f;
    final private float defaultMaterialKs = 0.7f;
    final private float defaultShininess = 5f;

    private Map<VisualizationType, Shader[]> shaderChain = new HashMap<>();
    private Shader[] coordinateSystemShader;
    private Camera camera = new Camera(450.0f, scaleFactor);
    static final private float FOV_DEFAULT_VALUE = 35.0f;
    static final private float FOV_FLOOR_LIMITER = 1.0f;
    static final private float FOV_CEIL_LIMITER = 150.f;
    private float fov = FOV_DEFAULT_VALUE;
    private Vector<Shader> lightShader = new Vector<>();

    private float[] clearColor = {defaultBackgroundColor[0], defaultBackgroundColor[1], defaultBackgroundColor[2], defaultBackgroundColor[3]};

    private float[] lightPosition = {defaultLightPosition[0], defaultLightPosition[1], defaultLightPosition[2]};
    private float lightLa = defaultLightLa, lightLd = defaultLightLd, lightLs = defaultLightLs;

    private float materialKa = defaultMaterialKa, materialKd = defaultMaterialKd, materialKs = defaultMaterialKs;
    private float shininess = defaultShininess;

    private int surfaceWidth = 0, surfaceHeight = 0, coordinateWidth = 500, coordinateHeight = 500;

    private CoordinateSystem coordinateSystem;

    private Context context;

    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] coordinateViewMatrix = new float[16];
    private final float[] coordinateProjectionMatrix = new float[16];

    private float csRadius = (float)(1/Math.sin(Math.toRadians(22.5)));
    private boolean bbDefaultState = true;

    public final ArrayList<BaseVisualization> sceneTree = new ArrayList<>();
    public final ArrayList<CameraBasedVisualizations> cameraBasedObjects = new ArrayList<>();
    protected final List<String> DisplayedFiles = new ArrayList<>();
    HashMap<String, Object> listDisplayedObjects = new HashMap<String,  Object>();

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        int[] maxTextureSize = new int[1], max3DTextureSize = new int[1];
        GLES32.glGetIntegerv(GLES32.GL_MAX_TEXTURE_SIZE, maxTextureSize,0);
        GLES32.glGetIntegerv(GLES32.GL_MAX_3D_TEXTURE_SIZE, max3DTextureSize, 0);

        Log.d(TAG, "OpenGL version: "+GLES32.glGetString(GLES32.GL_VERSION));
        Log.d(TAG, "Max texture size: "+maxTextureSize[0]);
        Log.d(TAG, "Max 3D texture size: "+max3DTextureSize[0]);

        // Shader collection
        shaderChain.put(Bundle.identifier, Bundle.shaderPrograms(context));
        shaderChain.put(Mesh.identifier, Mesh.shaderPrograms(context));
        shaderChain.put(BoundingBox.identifier, BoundingBox.shaderPrograms(context));
        shaderChain.put(MRIVolume.identifier, MRIVolume.shaderPrograms(context));
        shaderChain.put(MRISlice.identifier, MRISlice.shaderPrograms(context));

        coordinateSystemShader = CoordinateSystem.shaderPrograms(context);

        // Scale so the depth buffer works properly
        Matrix.setIdentityM(systemScale,0);
        Matrix.scaleM(systemScale,0, scaleFactor, scaleFactor, scaleFactor);
        configSystemScale();

        camera.getView(viewMatrix);
        camera.getViewOfOrientationWithRadius(coordinateViewMatrix, csRadius);

        populateLightShader();

        // Config light parameters and view
        configLight();
        configView();

        // Initialize some variables
        coordinateSystem = new CoordinateSystem(coordinateSystemShader);

        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glEnable(GLES32.GL_PRIMITIVE_RESTART_FIXED_INDEX);
    }


    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;

        coordinateHeight = height/5;
        coordinateWidth = coordinateHeight;

        Matrix.perspectiveM(projectionMatrix, 0, fov, (float)(width)/(float)(height), 0.01f, 10000f);
        Matrix.perspectiveM(coordinateProjectionMatrix,0,45f, (float)(coordinateWidth)/(float)(coordinateHeight), 0.01f, 10f);
        configPerspective();
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        // Shouldn't be here, but cant be called out of the gl thread
        configPerspective();
        configView();
        configCoordinateView();

        GLES32.glViewport(0, 0, surfaceWidth, surfaceHeight);

        GLES32.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        for (BaseVisualization obj : sceneTree) {
            obj.loadOpenGLVariables();
            obj.drawSolid();
        }

        for (BaseVisualization obj : sceneTree) {
            obj.drawTransparent();
        }

        GLES32.glClear(GLES32.GL_DEPTH_BUFFER_BIT);
        GLES32.glViewport(0,0, coordinateWidth, coordinateHeight);
        coordinateSystem.drawSolid();
    }


    private void setDefaultValues() {
        setDefaultBackgroundColor();
        setDefaultLight();
        setDefaultMaterial();
    }


    private void setDefaultBackgroundColor() {
        for (int i=0; i<4; i++) clearColor [i] = defaultBackgroundColor[i];
    }


    private void setDefaultLight() {
        for(int i=0; i<3; i++) lightPosition[i] = defaultLightPosition[i];
        lightLa = defaultLightLa;
        lightLd = defaultLightLd;
        lightLs = defaultLightLs;
    }


    private void setDefaultMaterial() {
        materialKa = defaultMaterialKa;
        materialKd = defaultMaterialKd;
        materialKs = defaultMaterialKs;
        shininess = defaultShininess;
    }


    private void populateLightShader() {
        for (Shader[] ss : shaderChain.values())
            for (Shader s : ss) {
                boolean validator = true;
                validator &= s.glGetUniformLocation("Light.pos") != -1;
                validator &= s.glGetUniformLocation("Light.La") != -1;
                validator &= s.glGetUniformLocation("Light.Ld") != -1;
                validator &= s.glGetUniformLocation("Light.Ls") != -1;

                validator &= s.glGetUniformLocation("Material.Ka") != -1;
                validator &= s.glGetUniformLocation("Material.Kd") != -1;
                validator &= s.glGetUniformLocation("Material.Ks") != -1;
                validator &= s.glGetUniformLocation("Material.shininess") != -1;

                if (validator)
                    lightShader.add(s);
            }
    }


    private void configLight() {
        for (Shader s : lightShader) {
            s.glUseProgram();
            GLES32.glUniform3f(s.glGetUniformLocation("Light.pos"), lightPosition[0], lightPosition[1], lightPosition[2]);
            GLES32.glUniform3f(s.glGetUniformLocation("Light.La"), lightLa, lightLa, lightLa);
            GLES32.glUniform3f(s.glGetUniformLocation("Light.Ld"), lightLd, lightLd, lightLd);
            GLES32.glUniform3f(s.glGetUniformLocation("Light.Ls"), lightLs, lightLs, lightLs);

            GLES32.glUniform3f(s.glGetUniformLocation("Material.Ka"), materialKa, materialKa, materialKa);
            GLES32.glUniform3f(s.glGetUniformLocation("Material.Kd"), materialKd, materialKd, materialKd);
            GLES32.glUniform3f(s.glGetUniformLocation("Material.Ks"), materialKs, materialKs, materialKs);
            GLES32.glUniform1f(s.glGetUniformLocation("Material.shininess"), shininess);
        }
    }


    private void configView() {
        for (Shader[] ss : shaderChain.values())
            for (Shader s : ss) {
                s.glUseProgram();
                GLES32.glUniformMatrix4fv(s.glGetUniformLocation("V"),1,false, viewMatrix, 0);
            }
    }


    private void configCoordinateView() {
        coordinateSystemShader[0].glUseProgram();
        GLES32.glUniformMatrix4fv(coordinateSystemShader[0].glGetUniformLocation("V"), 1, false, coordinateViewMatrix, 0);
    }


    private void configPerspective() {
        for (Shader[] ss : shaderChain.values())
            for (Shader s : ss) {
                s.glUseProgram();
                GLES32.glUniformMatrix4fv(s.glGetUniformLocation("P"),1,false, projectionMatrix,0);
            }

        coordinateSystemShader[0].glUseProgram();
        GLES32.glUniformMatrix4fv(coordinateSystemShader[0].glGetUniformLocation("P"),1,false, coordinateProjectionMatrix,0);
    }


    private void configSystemScale() {
        for (Shader[] ss : shaderChain.values()) {
            for (Shader s: ss) {
                s.glUseProgram();
                GLES32.glUniformMatrix4fv(s.glGetUniformLocation("systemScaleM"),1,false, systemScale,0);
            }
        }
    }


    public void onChangeBackground(float value, int color){
        switch (color){
            case 0: clearColor[0] = value;
                break;
            case 1: clearColor[1] = value;
                break;
            case 2: clearColor[2] = value;
                break;
            case 3: lightPosition[0] = value;
                break;
            case 4: lightPosition[1] = value;
                break;
            case 5: lightPosition[2] = value;
                break;
        }
    }


    public void readFile(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1);

        if (Bundle.validFileExtensions.contains(extension)){
            Bundle bundle  = new Bundle(shaderChain, filePath);
            bundle.setDrawBB(bbDefaultState);
            sceneTree.add(bundle);
            DisplayedFiles.add(filePath);
            listDisplayedObjects.put(filePath,bundle);
        }
        if (Mesh.validFileExtensions.contains(extension)) {
            Mesh mesh = new Mesh(shaderChain, filePath);
            mesh.setDrawBB(bbDefaultState);
            sceneTree.add(mesh);
            DisplayedFiles.add(filePath);
            cameraBasedObjects.add(mesh);
            listDisplayedObjects.put(filePath,mesh);
            mesh.updateCameraEye(camera.getEye(), 0);
        }

        if (MRI.validFileExtensions.contains(extension)) {
            MRI mri = new MRI(shaderChain, filePath);
            mri.setDrawBB(bbDefaultState);
            sceneTree.add(mri);
            DisplayedFiles.add(filePath);
            listDisplayedObjects.put(filePath+"MRI",mri);

            // Testing MRIVolume
            MRIVolume mriVol = new MRIVolume(shaderChain, mri);
            mriVol.setDraw(false);
            mriVol.setDrawBB(bbDefaultState);
            sceneTree.add(mriVol);
            cameraBasedObjects.add(mriVol);
            listDisplayedObjects.put(filePath+"vol",mriVol);
            mriVol.updateCameraEye(camera.getEye(), 0);

            // Testing MRISlice
            MRISlice mriSliceX = new MRISlice(shaderChain, mri);
            mriSliceX.setAxis(1,0,0);
            MRISlice mriSliceY = new MRISlice(shaderChain, mri);
            mriSliceY.setAxis(0,1,0);
            MRISlice mriSliceZ = new MRISlice(shaderChain, mri);
            mriSliceX.setAxis(0,0,1);

            mriSliceX.setDrawBB(bbDefaultState);
            mriSliceY.setDrawBB(bbDefaultState);
            mriSliceZ.setDrawBB(bbDefaultState);

            sceneTree.add(mriSliceX);
            sceneTree.add(mriSliceY);
            sceneTree.add(mriSliceZ);
            listDisplayedObjects.put(filePath+"X",mriSliceX);
            listDisplayedObjects.put(filePath+"Y",mriSliceY);
            listDisplayedObjects.put(filePath+"Z",mriSliceZ);
        }
    }


    public void orbitCamera(float dx, float dy) {
        camera.orbit(dx/surfaceWidth*520, dy/surfaceHeight*520);
        camera.getView(viewMatrix);
        camera.getViewOfOrientationWithRadius(coordinateViewMatrix, csRadius);
        notifyObjectsBasedInCameraUpdate();
    }


    public void panCamera(float dx, float dy) {
        float r = camera.getRadius();
        float panningX = (float)(dx/surfaceHeight*2*r*Math.tan(Math.toRadians(fov/2)));
        float panningY = (float)(dy/surfaceHeight*2*r*Math.tan(Math.toRadians(fov/2)));

        camera.panning(panningX, panningY);
        camera.getView(viewMatrix);

        notifyObjectsBasedInCameraUpdate();
    }


//    public void zoomCam(float delta) {
//        fov = (float) Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(fov/2)) * (delta / surfaceHeight + 1)));
//
//        if (fov < FOV_FLOOR_LIMITER) fov = FOV_FLOOR_LIMITER;
//        else if (fov > FOV_CEIL_LIMITER) fov = FOV_CEIL_LIMITER;
//        Matrix.perspectiveM(projectionMatrix, 0, fov, (float)(surfaceWidth)/(float)(surfaceHeight), 0.01f, 10000f);
//
////        notifyObjectsBasedInCameraUpdate();
//    }


    public void modifyCamera(float x_1_prev, float y_1_prev, float x_2_prev, float y_2_prev, float x_1_next, float y_1_next, float x_2_next, float y_2_next) {
        float initTanX = (x_2_prev-x_1_prev), initTanY = (y_2_prev-y_1_prev);
        float endTanX = (x_2_next-x_1_next), endTanY = (y_2_next-y_1_next);

        float initAngle = (float) Math.toDegrees(Math.atan(initTanY/initTanX));
        float endAngle =  (float) Math.toDegrees(Math.atan(endTanY/endTanX));

        // Homogenize quadrant
        if (initTanX<0) initAngle += 180;
        if (endTanX<0) endAngle += 180;

        float deltaAngle = endAngle - initAngle;

        float r = camera.getRadius();

        float avrXPrev = (x_2_prev+x_1_prev)/2, avrXNext = (x_2_next+x_1_next)/2;
        float avrYPrev = (y_2_prev+y_1_prev)/2, avrYNext = (y_2_next+y_1_next)/2;

        float deltaPanningX_toCenter = (float)((surfaceWidth - 2*avrXPrev-avrXNext+avrXPrev)/surfaceHeight*r*Math.tan(Math.toRadians(fov/2)));
        float deltaPanningY_toCenter = (float)((surfaceHeight- 2*avrYPrev-avrYNext+avrYPrev)/surfaceHeight*r*Math.tan(Math.toRadians(fov/2)));

        float deltaPanningX_toEnd = (float)((2*avrXNext-surfaceWidth+avrXNext-avrXPrev)/surfaceHeight*r*Math.tan(Math.toRadians(fov/2)));
        float deltaPanningY_toEnd = (float)((2*avrYNext-surfaceHeight+avrYNext-avrYPrev)/surfaceHeight*r*Math.tan(Math.toRadians(fov/2)));

        float deltaFov = (float)((Math.sqrt(initTanX*initTanX + initTanY*initTanY)) -
                                 (Math.sqrt(endTanX*endTanX   + endTanY*endTanY)));

        camera.panning(deltaPanningX_toCenter, deltaPanningY_toCenter);
        camera.transverseRotation(deltaAngle);

        fov = (float) Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(fov/2)) * (deltaFov / surfaceHeight + 1)));

        if (fov < FOV_FLOOR_LIMITER) fov = FOV_FLOOR_LIMITER;
        else if (fov > FOV_CEIL_LIMITER) fov = FOV_CEIL_LIMITER;
        Matrix.perspectiveM(projectionMatrix, 0, fov, (float)(surfaceWidth)/(float)(surfaceHeight), 0.01f, 10000f);
        camera.panning(deltaPanningX_toEnd, deltaPanningY_toEnd);


        camera.getView(viewMatrix);
        camera.getViewOfOrientationWithRadius(coordinateViewMatrix, csRadius);

        notifyObjectsBasedInCameraUpdate();
    }


    public void ResetCamera() {
        camera.defaultValues();
        fov = FOV_DEFAULT_VALUE;
        Matrix.perspectiveM(projectionMatrix, 0, fov, (float)(surfaceWidth)/(float)(surfaceHeight), 0.01f, 10000f);
//        configPerspective();

        camera.getView(viewMatrix);
        camera.getViewOfOrientationWithRadius(coordinateViewMatrix, csRadius);
        notifyObjectsBasedInCameraUpdate();
    }


    public void toggleBoundingBoxes() {
        bbDefaultState = !bbDefaultState;

        for (BaseVisualization obj : sceneTree)
            obj.setDrawBB(bbDefaultState);
    }


    public void notifyObjectsBasedInCameraUpdate() {
        for (CameraBasedVisualizations obj : cameraBasedObjects) {
            obj.updateCameraEye(camera.getEye(),0);
        }
    }


    public HashMap getListDisplayedObjects() {
        return listDisplayedObjects;
    }


    public void setContext(Context c) {context = c;}

    public ArrayList<String> getValidFileExtensions() {
        ArrayList<String> validExtensions = new ArrayList();

        for (String s : Bundle.validFileExtensions)
            validExtensions.add(s);

        for (String s : Mesh.validFileExtensions)
            validExtensions.add(s);

        for (String s : MRI.validFileExtensions)
            validExtensions.add(s);

        return validExtensions;
//        try {
//            ArrayList<Class> visualizationClassesArray = new ArrayList();
//            for (String className : visualizationBaseClasses) {
//                Method methGetValidExt;
//                methGetValidExt = Class.forName(className).getMethod("getValidFileExtension");
//                BaseVisualization a = Class.forName(className);
//                for (Object s : methGetValidExt.)
//            }
//            for (Class c : visualizationClassesArray)
//                if (c.get)
//                for (String s : c.)
//        }
//        catch (Throwable e) {
//            System.err.println(e);
//        }
    }


    public void onPause() {
        for (BaseVisualization i : sceneTree)
            i.onPause();
    }


    public void onResume() {
//        Actualizar los shaders
    }
}