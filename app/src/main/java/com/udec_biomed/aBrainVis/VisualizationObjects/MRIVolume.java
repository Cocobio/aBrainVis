package com.udec_biomed.aBrainVis.VisualizationObjects;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.udec_biomed.aBrainVis.Tools.Shader;
import com.udec_biomed.aBrainVis.Tools.OtsuThresholding;
import com.udec_biomed.aBrainVis.Tools.VisualizationType;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public class MRIVolume extends BaseVisualization implements CameraBasedVisualizations{
    public static VisualizationType identifier = VisualizationType.MRI_VOLUME;

    private String filePath;
    private String fileName;

    private float[] scaleModel = new float[16];
    private int[] MRIDimension = new int[4];
    private int[] hMRITexture;

    private float[] eye = new float[3];
    private float[] normalPlane = new float[3];
    private float[] axis = new float[9];
    private float dPlaneEnd, dPlaneBegin, dPlane, dPlaneStep;
    private int frontIdx;
    private int sliceFr;

    private float MRIVolumeMax;
    private float slope;

    private boolean drawInside = false;
    private float subSamplingFactor = 0.2f;
    private float threshold;
    private float alpha = 0.5f;

    private BoundingBox boundingbox;

    protected MRI reference;

    static private final float[] vertexPoints = {   1,1,0,  1,1,1,	0,1,0,	0,1,1,
                                                    1,0,0,	1,0,1,	0,0,0,	0,0,1};

    private final static float[] materialValues = {0.1f, 0.6f, 0.2f, 100f};

    public MRIVolume(Map<VisualizationType, Shader[]> shaderChain, MRI mri) {
        super();

        TAG = "MRI_VOLUME";

        shader = shaderChain.get(identifier);

        filePath = mri.getFilePath();
        fileName = mri.getFileName()+" - Volume render";

        reference = mri;

        float[] tmpM = new float[16];
        mri.getMRIDimension(MRIDimension,0);

        Matrix.setIdentityM(tmpM,0);
        Matrix.scaleM(tmpM,0, MRIDimension[0], MRIDimension[1], MRIDimension[2]);
        Matrix.multiplyMM(scaleModel,0, mri.activeTransform,0, tmpM,0);

		// We compute Otsu threshold for volume render and slope for grey color
        threshold = OtsuThresholding.OtsuThreholdWithFloatArray(mri.MRIVolume, 256);
        MRIVolumeMax = mri.MRIVolume[0];
        for (float vox : mri.MRIVolume) if (vox>MRIVolumeMax) MRIVolumeMax = vox;

        slope = 1.0f/MRIVolumeMax;

        // Matrixs
        for (int i=0; i<16; i++) {
            scaleMat[i] = mri.scaleMat[i];
            translateMat[i] = mri.translateMat[i];
            rotationMat[i] = mri.rotationMat[i];
        }
        calculateModel();

        calculateAxisMat3();

        // Sampling frequency
        sliceFr = (int)(Math.sqrt(MRIDimension[0]*MRIDimension[0] + MRIDimension[1]*MRIDimension[1] + MRIDimension[2]*MRIDimension[2]));

        openGLLoaded = false;
        draw = true;
        drawBB = true;

        boundingbox = new BoundingBox(shaderChain, mri.boundingbox.dim, 0, mri.boundingbox.center, 0, model, mri.activeTransform);

        Log.d(TAG, "MRI Volume visualization ready: "+filePath+" with threshold: "+ threshold);
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
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("dPlaneStep"), dPlaneStep);

		// Fragment shader
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("slope"), slope);
        GLES32.glUniform1i(shader[selectedShader].glGetUniformLocation("mriTexture"), 0);
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("threshold"), threshold);
        GLES32.glUniform3f(shader[selectedShader].glGetUniformLocation("eye"), eye[0], eye[1], eye[2]);
        GLES32.glUniformMatrix3fv(shader[selectedShader].glGetUniformLocation("axis"), 1, false, axis,0);
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("alpha"), alpha);
    }


    public void updateCameraEye(float[] newEye, int offset) {
        for (int i=0; i<3; i++) eye[i] = newEye[i+offset];

        calculateDPlaneVariables();
    }


    public void calculateDPlaneVariables() {
        float[] volumeCenter = new float[4];
        float[] tmp = new float[4], tmpV = {0.5f, 0.5f, 0.5f, 1f};
        Matrix.multiplyMV(tmp,0, scaleModel,0, tmpV,0);
        Matrix.multiplyMV(volumeCenter,0, model, 0, tmp,0);

        for (int i=0; i<3; i++) normalPlane[i] = eye[i]-volumeCenter[i];
        float norm = (float)Math.sqrt(normalPlane[0]*normalPlane[0] + normalPlane[1]*normalPlane[1] + normalPlane[2]*normalPlane[2]);
        for (int i=0; i<3; i++) normalPlane[i] /= norm;

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

            if (d>dPlaneBegin) {
                dPlaneBegin = d;
                frontIdx = i;
            }
            else if (d<dPlaneEnd) dPlaneEnd = d;
        }

        setDPlaneAndStep();
    }

    private void configTexture() {
        // Filtered, otherwise binary textures don't have a good visual representation
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
    }


    private void calculateAxisMat3() {
        float[] x = {1, 0, 0, 1};
        float[] y = {0, 1, 0, 1};
        float[] z = {0, 0, 1, 1};
        float[] begin = {0, 0, 0, 1};

        float[] tmp = new float[4];

        Matrix.multiplyMV(tmp,0, scaleModel,0, begin,0);
        Matrix.multiplyMV(begin,0, model,0, tmp,0);

        Matrix.multiplyMV(tmp,0, scaleModel,0, x,0);
        Matrix.multiplyMV(x,0, model,0, tmp,0);

        Matrix.multiplyMV(tmp,0, scaleModel,0, y,0);
        Matrix.multiplyMV(y,0, model,0, tmp,0);

        Matrix.multiplyMV(tmp,0, scaleModel,0, z,0);
        Matrix.multiplyMV(z,0, model,0, tmp,0);

        for (int i=0; i<3; i++) {
            x[i] -= begin[i];
            y[i] -= begin[i];
            z[i] -= begin[i];
        }

        float xNorm = (float)Math.sqrt(x[0]*x[0] + x[1]*x[1] + x[2]*x[2]);
        float yNorm = (float)Math.sqrt(y[0]*y[0] + y[1]*y[1] + y[2]*y[2]);
        float zNorm = (float)Math.sqrt(z[0]*z[0] + z[1]*z[1] + z[2]*z[2]);

        for (int i=0; i<3; i++) {
            axis[ i ] =  x[i]/xNorm;
            axis[3+i] = -y[i]/yNorm;
            axis[6+i] =  z[i]/zNorm;
        }
    }


    @Override
    public void drawSolid() {
        if (!draw || alpha!=1f) return;
        configGL();
        configTexture();

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_3D, hMRITexture[0]);

        GLES32.glDrawArraysInstanced(GLES32.GL_TRIANGLE_FAN, 0, 6, (int)(subSamplingFactor*sliceFr));

        boundingbox.drawSolid();
    }


    @Override
    public void drawTransparent() {
        if (!draw || alpha==1f) return;
        configGL();
        configTexture();

        GLES32.glEnable(GLES32.GL_BLEND);
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_3D, hMRITexture[0]);

        GLES32.glDrawArraysInstanced(GLES32.GL_TRIANGLE_FAN, 0, 6, (int)(subSamplingFactor*sliceFr));

        GLES32.glDisable(GLES32.GL_BLEND);
    }


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


    @Override
    public void rotate(float centerX, float centerY, float centerZ, float angle, float axisX, float axisY, float axisZ){
        super.rotate(centerX, centerY, centerZ, angle, axisX, axisY, axisZ);
        calculateDPlaneVariables();
        calculateAxisMat3();
    }

    @Override
    public void translate(float x, float y, float z){
        super.translate(x, y, z);
        calculateDPlaneVariables();
        calculateAxisMat3();
    }


    @Override
    public void stackTranslate(float vecX, float vecY, float vecZ){
        super.stackTranslate(vecX, vecY, vecZ);
        calculateDPlaneVariables();
        calculateAxisMat3();
    }


    @Override
    public void scale(float vecX, float vecY, float vecZ, float centerX, float centerY, float centerZ) {
        super.scale(vecX, vecY, vecZ, centerX, centerY, centerZ);
        calculateDPlaneVariables();
        calculateAxisMat3();
    }


    @Override
    public void stackScale(float vecX, float vecY, float vecZ, float centerX, float centerY, float centerZ) {
        super.stackScale(vecX, vecY, vecZ, centerX, centerY, centerZ);
        calculateDPlaneVariables();
        calculateAxisMat3();
    }


    @Override
    protected void calculateModel() {
        super.calculateModel();
        calculateDPlaneVariables();
        calculateAxisMat3();
    }


    @Override
    public void resetModel() {
        super.resetModel();
        if (!openGLLoaded) return;
        calculateDPlaneVariables();
        calculateAxisMat3();
    }


    private void setDPlaneAndStep() {
        if (alpha == 1 || !drawInside) {
            dPlane = dPlaneBegin;
            dPlaneStep = (dPlaneEnd - dPlaneBegin)/(subSamplingFactor*sliceFr);
        }
        else {
            dPlane = dPlaneEnd;
            dPlaneStep = (dPlaneBegin - dPlaneEnd)/(subSamplingFactor*sliceFr);
        }
    }


    public void setSubSamplingFactor(float fr) {
        if (fr <= 0) return;

        subSamplingFactor = fr;
        Log.e(TAG,"subsampling: "+fr);
        setDPlaneAndStep();
    }


    public void setDrawInside(boolean activeDrawInside) {
        drawInside = activeDrawInside;

        setDPlaneAndStep();
    }


    public void setAlpha(float newAlpha) {
        alpha = newAlpha;
        setDPlaneAndStep();
    }

    public float getAlpha(){
        return alpha;
    }

    public void setThreshold(float newThreshold){
        threshold = newThreshold;
    }


    public void setDrawBB(boolean newDrawBB) {
        drawBB = newDrawBB;
        boundingbox.setDraw(drawBB);
    }


    public static Shader[] shaderPrograms(Context c) {
        Shader[] shaderReturn = new Shader[shaderN];

        String[] vs = {"volume-slice.vs"};
        String[] fs = {"volume.fs"};
        String[] gs = {};
        shaderReturn[0] = new Shader(vs, fs, gs, c);

        loadStaticUniforms(shaderReturn[0]);

        return shaderReturn;
    }


    public static void updateMaterialValues(Map<VisualizationType, Shader[]> shaderChain) {
        Shader[] ss = shaderChain.get(identifier);

        for (Shader s : ss) {
            s.glUseProgram();
            GLES32.glUniform1f(s.glGetUniformLocation("Material.Ka"), materialValues[0]);
            GLES32.glUniform1f(s.glGetUniformLocation("Material.Kd"), materialValues[1]);
            GLES32.glUniform1f(s.glGetUniformLocation("Material.Ks"), materialValues[2]);
            GLES32.glUniform1f(s.glGetUniformLocation("Material.shininess"), materialValues[3]);
        }
    }


    public static void updateMaterialValues(Map<VisualizationType, Shader[]> shaderChain, float newKa, float newKd, float newKs, float newShininess) {
        Shader[] ss = shaderChain.get(identifier);

        materialValues[0] = newKa;
        materialValues[1] = newKd;
        materialValues[2] = newKs;
        materialValues[3] = newShininess;

        for (Shader s : ss) {
            s.glUseProgram();
            GLES32.glUniform1f(s.glGetUniformLocation("Material.Ka"), materialValues[0]);
            GLES32.glUniform1f(s.glGetUniformLocation("Material.Kd"), materialValues[1]);
            GLES32.glUniform1f(s.glGetUniformLocation("Material.Ks"), materialValues[2]);
            GLES32.glUniform1f(s.glGetUniformLocation("Material.shininess"), materialValues[3]);
        }
    }


    public static void getMaterialValues(float[] container, int offset) { for (int i=0; i<4; i++) container[offset+i] = materialValues[i]; }


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
