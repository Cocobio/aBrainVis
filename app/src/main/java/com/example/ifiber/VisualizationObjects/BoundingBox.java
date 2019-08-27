package com.example.ifiber.VisualizationObjects;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.Matrix;

import com.example.ifiber.Tools.Shader;
import com.example.ifiber.Tools.VisualizationType;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public class BoundingBox extends BaseVisualization {
    protected float[] bbModel = new float[16];
    protected float[] dim = new float[3];
    protected float[] center = new float[3];

    public static VisualizationType identifier = VisualizationType.BOUNDING_BOX;


    public BoundingBox(Map<VisualizationType, Shader[]> shaderChain, float dimX, float dimY, float dimZ, float centerX, float centerY, float centerZ, float[] model) {
        super();

        TAG = "BB";

        shader = shaderChain.get(identifier);

        Matrix.setIdentityM(bbModel,0);
        Matrix.translateM(bbModel,0, centerX, centerY, centerZ);
        Matrix.scaleM(bbModel,0, dimX, dimY, dimZ);

        this.model = model;

        dim[0] = dimX;
        dim[1] = dimY;
        dim[2] = dimZ;

        center[0] = centerX;
        center[1] = centerY;
        center[2] = centerZ;

        openGLLoaded = false;
        draw = true;
    }


    public BoundingBox(Map<VisualizationType, Shader[]> shaderChain, float[] dim, int dOffset, float[] center, int cOffset, float[] model) {
        super();

        TAG = "BB";

        shader = shaderChain.get(identifier);

        Matrix.setIdentityM(bbModel,0);
        Matrix.translateM(bbModel,0, center[cOffset], center[cOffset+1], center[cOffset+2]);
        Matrix.scaleM(bbModel,0, dim[dOffset], dim[dOffset+1], dim[dOffset+2]);

        this.model = model;

        for (int i=0; i<3; i++){
            this.dim[i] = dim[i+dOffset];
            this.center[i] = center[i+cOffset];
        }

        openGLLoaded = false;
        draw = true;
    }


    public BoundingBox(Map<VisualizationType, Shader[]> shaderChain, float[] dim, int dOffset, float[] center, int cOffset, float[] model, float[] otherModel) {
        super();

        TAG = "BB";

        shader = shaderChain.get(identifier);

        float[] tmp = new float[16];
        Matrix.setIdentityM(tmp,0);
        Matrix.translateM(tmp,0, center[cOffset], center[cOffset+1], center[cOffset+2]);
        Matrix.scaleM(tmp,0, dim[dOffset], dim[dOffset+1], dim[dOffset+2]);

        Matrix.multiplyMM(bbModel,0, otherModel,0, tmp, 0);

        this.model = model;

        for (int i=0; i<3; i++){
            this.dim[i] = dim[i+dOffset];
            this.center[i] = center[i+cOffset];
        }

        openGLLoaded = false;
        draw = true;
    }


    public void loadOpenGLVariables(){
        if (openGLLoaded)
            return;
        loadGLBuffers();
        openGLLoaded = true;
    }


    private void loadGLBuffers() {
        if (vao == null) {
            vao = new int[1];
            GLES32.glGenVertexArrays(1, vao, 0);
        }
        GLES32.glBindVertexArray(vao[0]);

        if (vbo == null) {
            vbo = new int[1];
            GLES32.glGenBuffers(1, vbo, 0);
        }

        if (ebo == null) {
            ebo = new int[1];
            GLES32.glGenBuffers(1, ebo, 0);
        }


        float[] vertex = {  0.5f,	0.5f,	-0.5f,
                            0.5f,	0.5f,	0.5f,
                            -0.5f,	0.5f,	0.5f,
                            -0.5f,	0.5f,	-0.5f,
                            0.5f,	-0.5f,	-0.5f,
                            0.5f,	-0.5f,	0.5f,
                            -0.5f,	-0.5f,	-0.5f,
                            -0.5f,	-0.5f,	0.5f};

        int[] element = {   0, 1, 1, 2, 2, 3, 3, 6, 0, 3, 0, 4,
                            1, 5, 2, 7, 4, 6, 4, 5, 5, 7, 6, 7};

        // VBO
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer floatBuffer = FloatBuffer.wrap(vertex);
        floatBuffer.rewind();
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, vertex.length*4, floatBuffer, GLES32.GL_STATIC_DRAW);

		// EBO
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        IntBuffer intBuffer = IntBuffer.wrap(element);
        intBuffer.rewind();
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, element.length*4, intBuffer, GLES32.GL_STATIC_DRAW);

		// Enable attributes
        int positionAttribute =	shader[0].glGetAttribLocation("vertexPos");

        // Connect attributes
        GLES32.glEnableVertexAttribArray(positionAttribute);
        GLES32.glVertexAttribPointer(positionAttribute, 3, GLES32.GL_FLOAT, false, 0, 0);
    }


    public void updateBBModel(float dimX, float dimY, float dimZ, float centerX, float centerY, float centerZ) {
        Matrix.setIdentityM(bbModel,0);
        Matrix.scaleM(bbModel,0, dimX, dimY, dimZ);
        Matrix.translateM(bbModel,0, centerX, centerY, centerZ);

        dim[0] = dimX;
        dim[1] = dimY;
        dim[2] = dimZ;

        center[0] = centerX;
        center[1] = centerY;
        center[2] = centerZ;
    }


    @Override
    protected void loadUniform() {
        GLES32.glUniformMatrix4fv(shader[0].glGetUniformLocation("bbM"), 1, false, bbModel, 0);
        GLES32.glUniformMatrix4fv(shader[0].glGetUniformLocation("M"), 1, false, model, 0);
    }


    @Override
    public void drawSolid() {
        if (!draw) return;
        configGL();

        GLES32.glDrawElements(GLES32.GL_LINES, 24, GLES32.GL_UNSIGNED_INT, 0);
    }


    @Override
    public void cleanOpenGL() {
        if (vbo != null) {
            GLES32.glDeleteBuffers(1, vbo, 0);
            vbo = null;
        }

        if (ebo != null) {
            GLES32.glDeleteBuffers(1, ebo, 0);
            ebo = null;
        }

        if (vao != null) {
            GLES32.glDeleteVertexArrays(1, vao, 0);
            vao = null;
        }
    }


    public static Shader[] shaderPrograms(Context c) {
        Shader[] shaderReturn = new Shader[1];

        String[] vs = {"boundingbox.vs"};
        String[] fs = {"standardFragmentShader.fs"};
        String[] gs = {""};
        shaderReturn[0] = new Shader(vs, fs, gs, c);

        return shaderReturn;
    }


    public void setDraw(boolean newDraw) { draw = newDraw; }


    @Override
    public void onPause() {
        cleanOpenGL();

        openGLLoaded = false;
    }
}
