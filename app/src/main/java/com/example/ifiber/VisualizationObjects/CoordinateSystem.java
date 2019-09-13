package com.example.ifiber.VisualizationObjects;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.example.ifiber.Tools.Shader;
import com.example.ifiber.Tools.VisualizationType;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class CoordinateSystem extends BaseVisualization {
    final public static VisualizationType identifier = VisualizationType.COORDINATE_SYSTEM;

    float[] vertex;
    int[] elements;
    float[] color = {   1.0f, 0.0f, 0.0f, 1.0f, // Red
                        0.0f, 1.0f, 0.0f, 1.0f, // Blue
                        0.0f, 0.0f, 1.0f, 1.0f};// Green

    float radiusCylinder;
    float lengthCylinder;
    float radiusCone;
    float lengthCone;

    public CoordinateSystem(Shader[] coordinateShader) {
        super();

        shader = coordinateShader;

        TAG = "COORDINATESYSTEM";

        radiusCylinder = 0.015f;
        lengthCylinder = 0.8f;

        radiusCone = 0.05f;
        lengthCone = 0.2f;

        createArrow(10);

        float[] model0 = new float[16];
        float[] model1 = new float[16];
        float[] model2 = new float[16];
        Matrix.setIdentityM(model0, 0);

        Matrix.setIdentityM(model1, 0);
        Matrix.rotateM(model1,0, 90f, 0, 0, 1);

        Matrix.setIdentityM(model2, 0);
        Matrix.rotateM(model2,0, -90, 0, 1, 0);


        model = new float[16*3];
        for (int i=0; i<16; i++) {
            model[i] = model0[i];
            model[i+16] = model1[i];
            model[i+32] = model2[i];
        }

        loadGLBuffers();
    }

    private void createArrow(int detail) {
        if (detail<3) {
            Log.e(TAG, "In createArrow the detail lvl must be at least 3.");
            return;
        }

        createArrowBody(detail);
        createArrowFaces(detail);
    }

    private void createArrowBody(int detail) {
        int vertexDim = 4;

        int n = (detail+1)*3 *vertexDim;
        vertex = new float[n];
        for (int i=0; i<n; i++) vertex[i] = 0;

        vertex[vertexDim-1] = 1f;

        vertex[vertexDim] = lengthCylinder;
        vertex[vertexDim*2-1] = 1f;

        vertex[vertexDim*2] = lengthCylinder + lengthCone;
        vertex[vertexDim*3-1] = 1f;

        float angle = 360f/detail;
        float[] body = new float[vertexDim*2];
        for (int i=0; i<vertexDim*2; i++) body[i] = 0;
        body[1] = radiusCylinder;
        body[vertexDim-1] = 1f;

        body[vertexDim] = lengthCylinder;
        body[vertexDim+1] = radiusCylinder;
        body[vertexDim*2-1] = 1f;

        float[] head =  {lengthCylinder, radiusCone, 0, 1};
        float[] tmpRotationMat = new float[16];

        int k = vertexDim*3 + detail*vertexDim*2;
        for (int i=0; i<detail; i++) {
            // Arrow body
            Matrix.setIdentityM(tmpRotationMat,0);
            Matrix.rotateM(tmpRotationMat,0,angle*i,1,0,0);
            Matrix.multiplyMV(vertex, vertexDim*3+i*vertexDim*2, tmpRotationMat, 0, body, 0);
            Matrix.multiplyMV(vertex, vertexDim*4+i*vertexDim*2, tmpRotationMat, 0, body, vertexDim);

            // Arrow head
            Matrix.setIdentityM(tmpRotationMat,0);
            Matrix.rotateM(tmpRotationMat, 0, angle*i, 1,0,0);
            Matrix.multiplyMV(vertex, k+i*vertexDim, tmpRotationMat,0, head,0);
        }
    }


    private void createArrowFaces(int detail) {
        elements = new int[15*detail];
        int k = 0;

        for (int i=0; i<detail; i++) {
            elements[k++] = 0;
            elements[k++] = i*2+3;
            elements[k++] = i*2+2+3;
        }
        elements[k-1] = 3;

        for (int i=0; i<detail; i++) {
            elements[k++] = 3+i*2;
            elements[k++] = 3+i*2+1;
            elements[k++] = 3+i*2+2;

            elements[k++] = 3+i*2+1;
            elements[k++] = 3+i*2+2;
            elements[k++] = 3+i*2+3;
        }

        elements[k-1] = 4;
        elements[k-2] = 3;
        elements[k-4] = 3;

        for (int i=0; i<detail; i++) {
            elements[k++] = 1;
            elements[k++] = i+3+2*detail;
            elements[k++] = i+1+3+2*detail;
        }
        elements[k-1] = 3+2*detail;

        for (int i=0; i<detail; i++) {
            elements[k++] = 2;
            elements[k++] = i+3+2*detail;
            elements[k++] = i+1+3+2*detail;
        }
        elements[k-1] = 3+2*detail;
    }


    private void loadGLBuffers() {
        vao = new int[1];
        GLES32.glGenVertexArrays(1, vao,0);
        GLES32.glBindVertexArray(vao[0]);

        vbo = new int[1];
        ebo = new int[1];

        GLES32.glGenBuffers(1, vbo, 0);
        GLES32.glGenBuffers(1, ebo, 0);

		// VBO
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer floatBuffer;
        floatBuffer = FloatBuffer.wrap(vertex);
        floatBuffer.rewind();
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, vertex.length*4, floatBuffer, GLES32.GL_STATIC_DRAW);

		// EBO
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        IntBuffer intBuffer;
        intBuffer = IntBuffer.wrap(elements);
        intBuffer.rewind();
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, elements.length*4, intBuffer, GLES32.GL_STATIC_DRAW);

		// Enable attributes
        int positionAttribute =	shader[0].glGetAttribLocation("vertexPos");

		// Connect attributes
        GLES32.glEnableVertexAttribArray(positionAttribute);
        GLES32.glVertexAttribPointer(positionAttribute, 4, GLES32.GL_FLOAT, false, 0, 0);
    }

    @Override
    protected void loadUniform(){
        GLES32.glUniform4fv(shader[selectedShader].glGetUniformLocation("colorArray"), 3, color, 0);
        GLES32.glUniformMatrix4fv(shader[selectedShader].glGetUniformLocation("M"), 3, false, model, 0);
    }

    @Override
    public void drawSolid() {
        configGL();

        GLES32.glDrawElementsInstanced(GLES32.GL_TRIANGLES, elements.length, GLES32.GL_UNSIGNED_INT, 0, 3);
    }

    @Override
    public void cleanOpenGL() {
        GLES32.glDeleteVertexArrays(1, vao, 0);

        GLES32.glDeleteBuffers(1, vbo, 0);
        GLES32.glDeleteBuffers(1, ebo, 0);
    }


    public static Shader[] shaderPrograms(Context c) {
        Shader[] shaderReturn = new Shader[shaderN];

        String[] vs = {"coordinateSystem.vs"};
        String[] fs = {"standardFragmentShader.fs"};
        String[] gs = {""};
        shaderReturn[0] = new Shader(vs, fs, gs, c);

        return shaderReturn;
    }
}
