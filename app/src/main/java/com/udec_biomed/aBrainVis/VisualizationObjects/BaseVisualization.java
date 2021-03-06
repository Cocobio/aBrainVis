package com.udec_biomed.aBrainVis.VisualizationObjects;

import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.udec_biomed.aBrainVis.Tools.Shader;
import com.udec_biomed.aBrainVis.Tools.VisualizationType;

import java.util.Map;


public class BaseVisualization{
    protected VisualizationType identifier = null;
    protected String TAG = "NOT_ASSIGNED";
    protected String id;

    protected int[] vbo;
    protected int[] ebo;
    protected int[] vao;

    protected Shader[] shader;
    protected int selectedShader = 0;
    static public int shaderN = 1;

    // Matrixs
    protected float[] model = new float[16];
    protected float[] inverseModel = new float[16];

    protected float[] rotationMat = new float[16];
    protected float[] translateMat = new float[16];
    protected float[] scaleMat = new float[16];

    // Flags
    protected boolean openGLLoaded;
    protected boolean draw = false;
    protected boolean drawBB = false;


    public void loadOpenGLVariables(){
        Log.e(TAG, "loadOpenGLVariables not defined");
    }


    public BaseVisualization() {
        shader = null;

        resetModel();
    }


    protected void finalize () throws Throwable {
        super.finalize();
        Log.e(TAG, "FINALIZE NOT IMPLEMENTED IN "+TAG);
    }

    public void cleanOpenGL() {
        Log.e(TAG, "Must clean OpenGL buffers before destroying.");
    }


    public void drawSolid() { Log.e(TAG, "Method drawSolid() not implemented."); }


    public void drawTransparent() { Log.e(TAG, "Method drawTransparent() not implemented."); }


    protected void configGL() {
        shader[selectedShader].glUseProgram();
        loadUniform();
        GLES32.glBindVertexArray(vao[selectedShader]);
    }


    protected void loadUniform() { Log.e(TAG, "Method loadUniforms() not implemented."); }


    public void rotate(float centerX, float centerY, float centerZ, float angle, float axisX, float axisY, float axisZ){
        float[] newRotate = new float[16];
        float[] translateCenter = new float[16];
        float[] translateInverseCenter = new float[16];
        float[] tmp = new float[16];

        Matrix.rotateM(newRotate, 0, angle, axisX, axisY, axisZ);
        Matrix.translateM(translateCenter, 0, centerX, centerY, centerZ);
        Matrix.translateM(translateInverseCenter, 0, -centerX, -centerY, -centerZ);

        Matrix.multiplyMM(tmp,0, newRotate, 0, translateInverseCenter,0);
        Matrix.multiplyMM(newRotate, 0, translateCenter, 0, tmp, 0);

        Matrix.multiplyMM(tmp, 0, newRotate, 0, rotationMat, 0);
        for(int i=0; i<16; i++) rotationMat[i] = tmp[i];

        calculateModel();
    }


    public void translate(float x, float y, float z){
        Matrix.translateM(translateMat,0, x, y, z);

        calculateModel();
    }


    public void stackTranslate(float vecX, float vecY, float vecZ){
        float[] newTranslate = new float[16];
        float[] tmp = new float[16];

        Matrix.translateM(newTranslate, 0, vecX, vecY, vecZ);

        Matrix.multiplyMM(tmp, 0, newTranslate, 0, translateMat, 0);
        for(int i=0; i<16; i++) translateMat[i] = tmp[i];

        calculateModel();
    }


    public void scale(float vecX, float vecY, float vecZ, float centerX, float centerY, float centerZ) {
        float[] newScaleMat = new float[16];
        float[] translateCenter = new float[16];
        float[] translateInverseCenter = new float[16];
        float[] tmp = new float[16];

        Matrix.scaleM(newScaleMat, 0, vecX, vecY, vecZ);
        Matrix.translateM(translateCenter, 0, centerX, centerY, centerZ);
        Matrix.translateM(translateInverseCenter, 0, -centerX, -centerY, -centerZ);

        Matrix.multiplyMM(tmp,0, newScaleMat,0, translateInverseCenter, 0);
        Matrix.multiplyMM(scaleMat,0, translateCenter, 0, tmp, 0);

        calculateModel();
    }


    public void stackScale(float vecX, float vecY, float vecZ, float centerX, float centerY, float centerZ) {
        float[] newScaleMat = new float[16];
        float[] translateCenter = new float[16];
        float[] translateInverseCenter = new float[16];
        float[] tmp = new float[16];

        Matrix.scaleM(newScaleMat, 0, vecX, vecY, vecZ);
        Matrix.translateM(translateCenter, 0, centerX, centerY, centerZ);
        Matrix.translateM(translateInverseCenter, 0, -centerX, -centerY, -centerZ);

        Matrix.multiplyMM(tmp,0, newScaleMat,0, translateInverseCenter, 0);
        Matrix.multiplyMM(newScaleMat,0, translateCenter, 0, tmp, 0);

        Matrix.multiplyMM(tmp,0, newScaleMat,0, scaleMat, 0);
        for(int i=0; i<16; i++) scaleMat[i] = tmp[i];

        calculateModel();
    }


    protected void calculateModel() {
        float[] tmp = new float[16];

        Matrix.multiplyMM(tmp,0, rotationMat,0, scaleMat,0);
        Matrix.multiplyMM(model, 0, translateMat, 0, tmp, 0);

        Matrix.invertM(inverseModel, 0, model, 0);
    }


    public void resetModel() {
        Matrix.setIdentityM(model, 0);
        Matrix.setIdentityM(inverseModel, 0);

        Matrix.setIdentityM(rotationMat, 0);
        Matrix.setIdentityM(translateMat, 0);
        Matrix.setIdentityM(scaleMat, 0);
    }


    public void setId(String newId, int n) {
        if (n == 0)
            id = newId;
        else
            id = newId + " ("+n+")";
    }


    public String getId() { return id; }


    public void setDraw(boolean newDraw) { draw = newDraw; }


    public void setDrawBB(boolean newDrawBB) {
        Log.e(TAG, "setDrawBB not implemented in: "+TAG);
    }


    public int getSelectedShader() { return selectedShader; }


    public void setSelectedShader(int newSelectedShader) {
        if (newSelectedShader < shaderN) selectedShader = newSelectedShader;
        else Log.e(TAG, "Selected shader out of bound ("+newSelectedShader+"), maximum is "+shaderN+".");
    }


    public void onPause() { Log.e(TAG, "onPause method is not implemented for: "+TAG); }


    public void updateReferenceToShader(Map<VisualizationType, Shader[]> shaderChain) { Log.e(TAG, "updateReferenceToShader method is not implemented for: "+TAG); }
}
