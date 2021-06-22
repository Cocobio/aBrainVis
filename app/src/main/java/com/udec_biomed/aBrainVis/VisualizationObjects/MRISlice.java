/*
Latest devices cant render the texture if set to linear interpolation.
It is working only with nearest.
    - linearInterpolation = false

 */

package com.udec_biomed.aBrainVis.VisualizationObjects;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.udec_biomed.aBrainVis.Tools.Shader;
import com.udec_biomed.aBrainVis.Tools.VisualizationType;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public class MRISlice extends BaseVisualization {
    public static VisualizationType identifier = VisualizationType.MRI_SLICE;

    private String filePath;
    private String fileName;

    private float[] scaleModel = new float[16];
    private int[] MRIDimension = new int[4];
    private int[] hMRITexture;

    private float[] normalPlane = new float[3];
    private float[] axis = new float[3];
    private float slicePosition;           // this value goes from 0-1
    private float dPlaneEnd, dPlaneBegin, dPlane;
    private int frontIdx;

    private float MRIVolumeMax;
    private float slope, bright, contrast;
    private boolean discardValues = false;
    private boolean linearInterpolation = false;

    private float threshold;

    private BoundingBox boundingbox;

    protected MRI reference;

    static private final float[] vertexPoints = {   1,1,0,  1,1,1,	0,1,0,	0,1,1,
                                                    1,0,0,	1,0,1,	0,0,0,	0,0,1};

    public MRISlice(Map<VisualizationType, Shader[]> shaderChain, MRI mri) {
        super();

        TAG = "MRI_SLICE";

        shader = shaderChain.get(identifier);

        filePath = mri.getFilePath();
        fileName = mri.getFileName()+" - Slice render";

        reference = mri;

        float[] tmpM = new float[16];
        mri.getMRIDimension(MRIDimension,0);

        Matrix.setIdentityM(tmpM,0);
        Matrix.scaleM(tmpM,0, MRIDimension[0], MRIDimension[1], MRIDimension[2]);
        Matrix.multiplyMM(scaleModel,0, mri.activeTransform,0, tmpM,0);

        // calculate the maximum value in the volume
        MRIVolumeMax = mri.MRIVolume[0];
        for (float vox : mri.MRIVolume) if (vox>MRIVolumeMax) MRIVolumeMax = vox;

        // default values
        slope = 1.0f/MRIVolumeMax;
        bright = 0;
        contrast = 1;

        axis[0] = 1f;
        axis[1] = 0f;
        axis[2] = 0f;

        slicePosition = 0.5f;

        threshold = MRIVolumeMax*0.1f;

        // Matrixs
        for (int i=0; i<16; i++) {
            scaleMat[i] = mri.scaleMat[i];
            translateMat[i] = mri.translateMat[i];
            rotationMat[i] = mri.rotationMat[i];
        }
        calculateModel();

        openGLLoaded = false;
        draw = true;
        drawBB = true;

        boundingbox = new BoundingBox(shaderChain, mri.boundingbox.dim, 0, mri.boundingbox.center, 0, model, mri.activeTransform);

        Log.d(TAG, "MRI Volume visualization ready: "+filePath);
    }

    public void setDraw(boolean D){
        draw = D;
    }

    public boolean getDraw(){
        return draw;
    }

    public void loadOpenGLVariables(){
        if (openGLLoaded)
            return;

        // MRI data already loaded in the gpu
        hMRITexture = reference.hMRITexture;
        vbo = reference.vbo;

        loadGLBuffers();
        boundingbox.loadOpenGLVariables();
        openGLLoaded = true;
    }


    private void loadGLBuffers() {
        if (vao == null) {
            vao = new int[1];
            GLES32.glGenVertexArrays(1, vao,0);
        }
        GLES32.glBindVertexArray(vao[0]);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);

        // Enable attributes
        int vertexIdxAttribute = shader[0].glGetAttribLocation("vertexIdx");

        // Connect attributes
        GLES32.glEnableVertexAttribArray(vertexIdxAttribute);
        GLES32.glVertexAttribIPointer(vertexIdxAttribute, 1, GLES32.GL_INT, 0, 0);
    }


    @Override
    protected void loadUniform() {
        // Vertex shader
        GLES32.glUniformMatrix4fv(shader[selectedShader].glGetUniformLocation("M"), 1, false, model,0);
        GLES32.glUniformMatrix4fv(shader[selectedShader].glGetUniformLocation("S"), 1, false, scaleModel,0);
        GLES32.glUniform1i(shader[selectedShader].glGetUniformLocation("frontIdx"), frontIdx);
        GLES32.glUniform4f(shader[selectedShader].glGetUniformLocation("np"), normalPlane[0], normalPlane[1], normalPlane[2], 0f);
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("dPlane"), dPlane);
//        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("dPlaneStep"), 0f);     Not needed because its multiply by gl instance id, which is 0

        // Fragment shader
        GLES32.glUniform1i(shader[selectedShader].glGetUniformLocation("mriTexture"), 0);
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("slope"), slope);
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("bright"), bright);
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("contrast"), contrast);
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("threshold"), discardValues ? threshold : 0.0f);
    }


    public void calculatePlaneVariables() {
        float[] end = {axis[0], axis[1], axis[2], 1f};
        float[] begin = {0, 0, 0, 1};
        float[] tmp = new float[4];

        Matrix.multiplyMV(tmp,0, scaleModel,0, begin,0);
        Matrix.multiplyMV(begin,0, model,0, tmp,0);

        Matrix.multiplyMV(tmp,0, scaleModel,0, end,0);
        Matrix.multiplyMV(end,0, model, 0, tmp,0);

        for (int i=0; i<4; i++) tmp[i] = end[i] - begin[i];

        float norm = (float)Math.sqrt(tmp[0]*tmp[0] + tmp[1]*tmp[1] + tmp[2]*tmp[2]);
        for (int i=0; i<3; i++) normalPlane[i] = tmp[i]/norm;

        float[] tmpV = new float[4];
        for (int i=0; i<3; i++) tmpV[i] = vertexPoints[i];
        tmpV[3] = 1f;

        Matrix.multiplyMV(tmp,0, scaleModel,0, tmpV,0);
        Matrix.multiplyMV(tmpV,0, model,0, tmp,0);

        dPlaneBegin = tmpV[0]*normalPlane[0] + tmpV[1]*normalPlane[1] + tmpV[2]*normalPlane[2];
        dPlaneEnd = dPlaneBegin;
        frontIdx = 0;

        float d;
        for (int i=1; i<8; i++) {
            for (int j=0; j<3; j++) tmpV[j] = vertexPoints[j+i*3];
            tmpV[3] = 1f;
            Matrix.multiplyMV(tmp,0, scaleModel,0, tmpV,0);
            Matrix.multiplyMV(tmpV,0, model,0, tmp,0);

            d = tmpV[0]*normalPlane[0] + tmpV[1]*normalPlane[1] + tmpV[2]*normalPlane[2];

            if (d<dPlaneBegin) {
                dPlaneBegin = d;
                frontIdx = i;
            }
            else if (d>dPlaneEnd) dPlaneEnd = d;
        }

        dPlane = (dPlaneEnd-dPlaneBegin)*slicePosition + dPlaneBegin;
    }
    public float getSlicePosition(){
        return slicePosition;
    }

    public void setSlicePosition(float newPosition) {
        if (newPosition>1f-0.0001f) newPosition = 1f-0.0001f;
        else if (newPosition<0.0001f) newPosition = 0.0001f;

        slicePosition = newPosition;

        dPlane = (dPlaneEnd - dPlaneBegin)*slicePosition + dPlaneBegin;
    }


    @Override
    public void drawSolid() {
        if (!draw) return;
        configGL();

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_3D, hMRITexture[0]);

        if (linearInterpolation) {
            // Filtered, otherwise binary textures don't have a good visual representation
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        }
        else {
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);
        }
        GLES32.glDrawArraysInstanced(GLES32.GL_TRIANGLE_FAN, 0, 6, 1);

        boundingbox.drawSolid();
    }


    @Override
    public void drawTransparent() {}


    @Override
    public void cleanOpenGL() {
        if (vao != null) {
            GLES32.glDeleteVertexArrays(1, vao, 0);
            vao = null;
        }
    }


    private static void loadStaticUniforms(Shader shader) {
        shader.glUseProgram();

        int[] v1 = {0,1,3,-1, 1,0,1,3, 0,4,5,-1, 4,0,4,5, 0,2,6,-1, 2,0,2,6};
        int[] v2 = {1,3,7,-1, 5,1,3,7, 4,5,7,-1, 6,4,5,7, 2,6,7,-1, 3,2,6,7};

        int[] nSequence = { 0,1,2,3,4,5,6,7,    1,3,0,2,5,7,4,6,    2,0,3,1,6,4,7,5,    3,2,1,0,7,6,5,4,
                4,0,6,2,5,1,7,3,    5,1,4,0,7,3,6,2,    6,2,7,3,4,0,5,1,    7,6,3,2,5,4,1,0};

        IntBuffer intBuffer = IntBuffer.wrap(v1);
        intBuffer.rewind();
        GLES32.glUniform1iv(shader.glGetUniformLocation("v1"), v1.length, intBuffer);

        intBuffer = intBuffer.wrap(v2);
        intBuffer.rewind();
        GLES32.glUniform1iv(shader.glGetUniformLocation("v2"), v2.length, intBuffer);

        intBuffer = intBuffer.wrap(nSequence);
        intBuffer.rewind();
        GLES32.glUniform1iv(shader.glGetUniformLocation("nSequence"), nSequence.length, intBuffer);

        FloatBuffer floatBuffer = FloatBuffer.wrap(vertexPoints);
        floatBuffer.rewind();
        GLES32.glUniform3fv(shader.glGetUniformLocation("vertexPoints"), vertexPoints.length, floatBuffer);
    }


    public void setThreshold(float newThreshold){
        threshold = newThreshold;
    }


    @Override
    public void rotate(float centerX, float centerY, float centerZ, float angle, float axisX, float axisY, float axisZ){
        super.rotate(centerX, centerY, centerZ, angle, axisX, axisY, axisZ);
        calculatePlaneVariables();
    }

    @Override
    public void translate(float x, float y, float z){
        super.translate(x, y, z);
        calculatePlaneVariables();
    }


    @Override
    public void stackTranslate(float vecX, float vecY, float vecZ){
        super.stackTranslate(vecX, vecY, vecZ);
        calculatePlaneVariables();
    }


    @Override
    public void scale(float vecX, float vecY, float vecZ, float centerX, float centerY, float centerZ) {
        super.scale(vecX, vecY, vecZ, centerX, centerY, centerZ);
        calculatePlaneVariables();
    }


    @Override
    public void stackScale(float vecX, float vecY, float vecZ, float centerX, float centerY, float centerZ) {
        super.stackScale(vecX, vecY, vecZ, centerX, centerY, centerZ);
        calculatePlaneVariables();
    }


    @Override
    protected void calculateModel() {
        super.calculateModel();
        calculatePlaneVariables();
    }


    @Override
    public void resetModel() {
        super.resetModel();
        if (!openGLLoaded) return;
        calculatePlaneVariables();
    }


    public void setAxis(float x, float y, float z) {
        float norm = (float)Math.sqrt(x*x + y*y + z*z);
        if (norm == 0f) return;

        axis[0] = x/norm;
        axis[1] = y/norm;
        axis[2] = z/norm;

        calculatePlaneVariables();
    }


    public void setAxis(float[] newAxis, int offset) {
        float norm = (float)Math.sqrt(newAxis[offset]*newAxis[offset] + newAxis[offset+1]*newAxis[offset+1] + newAxis[offset+2]*newAxis[offset+2]);
        if (norm == 0f) return;

        for (int i=0; i<3; i++) axis[i] = newAxis[offset+i]/norm;

        calculatePlaneVariables();
    }


    public void setLinearInterpolation(boolean activateLinearInter) {
        linearInterpolation = activateLinearInter;
    }

    public boolean getLinearInterpolation() {
        return linearInterpolation;
    }


    public void setDiscardValues(boolean activateDiscardValues) {
        discardValues = activateDiscardValues;
    }


    public void setThresholdForDiscardValues(float newThreshold) {
        threshold = newThreshold;
    }


    public void setBright(float newBright) {
        bright = newBright;
    }


    public void setContrast(float newContrast) {
        contrast = newContrast;
    }


    public void setDrawBB(boolean newDrawBB) {
        drawBB = newDrawBB;
        boundingbox.setDraw(drawBB);
    }


    public static Shader[] shaderPrograms(Context c) {
        Shader[] shaderReturn = new Shader[shaderN];

        String[] vs = {"volume-slice.vs"};
        String[] fs = {"slice.fs"};
        String[] gs = {};
        shaderReturn[0] = new Shader(vs, fs, gs, c);

        loadStaticUniforms(shaderReturn[0]);

        return shaderReturn;
    }


    public void onPause() {
        cleanOpenGL();

        openGLLoaded = false;
        boundingbox.onPause();
    }


    @Override
    public void updateReferenceToShader(Map<VisualizationType, Shader[]> shaderChain) {
        shader = shaderChain.get(identifier);
        boundingbox.updateReferenceToShader(shaderChain);
    }
}
