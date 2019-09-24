package com.example.ifiber.VisualizationObjects;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.example.ifiber.Tools.Shader;
import com.example.ifiber.Tools.SortingAlgorithms;
import com.example.ifiber.Tools.ThirdParty.Gifti.GIFTI;
import com.example.ifiber.Tools.ThirdParty.Gifti.GiftiFormatException;
import com.example.ifiber.Tools.ThirdParty.Gifti.GiftiReader;
import com.example.ifiber.Tools.VisualizationType;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Mesh extends BaseVisualization implements CameraBasedVisualizations{
    public static ArrayList<String> validFileExtensions =  new ArrayList<>(Arrays.asList("mesh", "gii"));
    public static VisualizationType identifier = VisualizationType.MESH;

    private String filePath;
    private String fileName;

    private float[] vertex;
    private float[] normals;
    private int[] faces;
    private float[] triangleCentroid;
    private float[] eyeToTriangleCentroid;
    private int[] sortedTriangleIndexes;
    private boolean updateGL = false;

    private float[] pColor = {0, 1, 0};
    private float[] lColor = {0, 0, 0};
    private float[] tColor = {0.7f, 0.7f, 0.7f};
    private float alpha = 0.5f;

    private final static float[] materialValues = {1f, 0.8f, 0.7f, 5f};

    private boolean drawTriangles = true;
    private boolean drawPoints = false;
    private boolean drawLines = false;


    protected BoundingBox boundingbox;

    public Mesh(Map<VisualizationType, Shader[]> shaderChain, String file) {
        super();

        TAG = "MESH";

        shader = shaderChain.get(identifier);

        filePath = file;

        String[] tmp = file.split("[/]");
        fileName = tmp[tmp.length-1];

        readData();

        openGLLoaded = false;
        draw = true;
        drawBB = true;

        float[] dim = new float[3];
        float[] center = new float[3];
        calculateBoundingBoxDimCenter(dim,0, center,0);

        boundingbox = new BoundingBox(shaderChain, dim, 0, center, 0, model);

        calculateTriangleCentroid();

        Log.d(TAG, "Loading ready "+filePath+" "+faces.length/4+" triangles and "+vertex.length/3+" vertex.");
    }


    public void loadOpenGLVariables(){
        if (openGLLoaded)
            return;
        loadGLBuffers();
        boundingbox.loadOpenGLVariables();
        openGLLoaded = true;
    }

    private void readData() {
        String[] tokens = filePath.split("[.]");
        String extension = tokens[tokens.length-1];

        if (extension.equals("mesh"))
            readMesh();
        else if (extension.equals("gii"))
            readGIFTI();
        else
            Log.e(TAG, "Unsupported mesh file: "+extension);
    }


    private void readMesh() {
        try {
            RandomAccessFile rFile = new RandomAccessFile(filePath,"r");
            long fileSize = rFile.length();
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buf_in = ByteBuffer.allocate((int)fileSize);

            buf_in.order(ByteOrder.LITTLE_ENDIAN);
            buf_in.clear();
            inChannel.read(buf_in);
            buf_in.rewind();

            int nVertexVector = buf_in.getInt(29);
            int nNormalsVector = buf_in.getInt(29+4+nVertexVector*3*4);
            int nFacesVector = buf_in.getInt(29+4+nVertexVector*3*4+4+nNormalsVector*3*4+4);

            vertex = new float[nVertexVector*3];
            normals = new float[nNormalsVector*3];
            faces = new int[nFacesVector*4];

            buf_in.position(29 + 4);
            buf_in.asFloatBuffer().get(vertex, 0, nVertexVector*3);

            buf_in.position(29 + 4 + nVertexVector*3*4 + 4);
            buf_in.asFloatBuffer().get(normals, 0, nNormalsVector*3);

            buf_in.position(29 + 4 + nVertexVector*3*4 + 4 +nNormalsVector*3*4+4+4);

            IntBuffer tmpBuffer = buf_in.asIntBuffer();
            for(int i=0; i<nFacesVector; i++) {
                tmpBuffer.get(faces, i*4, 3);
                faces[i*4+3] = -1;
            }

        }catch (IOException ex) {
            Log.e(TAG, "Error at reading file "+fileName+" : " + ex.toString());
            System.err.println(ex.getMessage());
        }
    }


    private void readGIFTI() {
        GiftiReader reader = new GiftiReader(new File(this.filePath));
        GIFTI gifti = null;

        try {
            gifti = reader.parseGiftiXML();

            FloatBuffer points = gifti.getPoints();
            IntBuffer indices = gifti.getIndices();
            FloatBuffer normals = gifti.getNormals();

            // init arrays
            vertex = new float[points.capacity()];
            faces = new int[indices.capacity()/3*4];

            // populate arrays
            points.get(vertex);
            for(int i=0; i<indices.capacity()/3; i++) {
                indices.get(faces, i*4, 3);
                faces[i*4+3] = -1;
            }

            if (normals == null) calculateNormals();
            else this.normals = normals.array();


            // do something with data...
        } catch (GiftiFormatException ex) {
            // do something with error...
        }
    }


    private void calculateNormals() {
        normals = new float[vertex.length];
        Arrays.fill(normals, 0);

        float[] n = new float[3];
        float norm;
        float[] segment0 = new float[3], segment1 = new float[3];

        for(int i=0; i<faces.length; i+=4) {
            segment1[0] = vertex[ 3*faces[i+1] ] - vertex[ 3*faces[i] ];
            segment1[1] = vertex[3*faces[i+1]+1] - vertex[3*faces[i]+1];
            segment1[2] = vertex[3*faces[i+1]+2] - vertex[3*faces[i]+2];

            segment0[0] = vertex[ 3*faces[i+2] ] - vertex[ 3*faces[i] ];
            segment0[1] = vertex[3*faces[i+2]+1] - vertex[3*faces[i]+1];
            segment0[2] = vertex[3*faces[i+2]+2] - vertex[3*faces[i]+2];

            // cross product
            n[0] = segment1[1]*segment0[2] - segment1[2]*segment0[1];
            n[1] = segment1[2]*segment0[0] - segment1[0]*segment0[2];
            n[2] = segment1[0]*segment0[1] - segment1[1]*segment0[0];

            // normalize
            norm = (float)Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2]);
            n[0] /= norm;
            n[1] /= norm;
            n[2] /= norm;

            normals[ 3*faces[i] ] = n[0];
            normals[3*faces[i]+1] = n[1];
            normals[3*faces[i]+2] = n[2];
        }

        for(int i=0; i<faces.length; i+=4) {
            // normalize
            norm = (float)Math.sqrt(
                    normals[3*faces[i]]  *normals[3*faces[i]] +
                            normals[3*faces[i]+1]*normals[3*faces[i]+1] +
                            normals[3*faces[i]+2]*normals[3*faces[i]+2]);

            normals[ 3*faces[i] ] /= norm;
            normals[3*faces[i]+1] /= norm;
            normals[3*faces[i]+2] /= norm;
        }
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

        // Enable attributes
        int positionAttribute =	shader[0].glGetAttribLocation("vertexPos");
        int normalAttribute =	shader[0].glGetAttribLocation("vertexNor");

        // VBO
        FloatBuffer floatBuffer;
        IntBuffer intBuffer;

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 4*vertex.length+4*normals.length, null, GLES32.GL_STATIC_DRAW);

        // Vertex
        floatBuffer = FloatBuffer.wrap(vertex);
        floatBuffer.rewind();

        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, 0, 4*vertex.length, floatBuffer);
        GLES32.glEnableVertexAttribArray(positionAttribute);
        GLES32.glVertexAttribPointer(positionAttribute, 3, GLES32.GL_FLOAT, false, 0, 0);

        // Normal
        floatBuffer = FloatBuffer.wrap(normals);
        floatBuffer.rewind();

        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER,4*vertex.length,4*normals.length, floatBuffer);
        GLES32.glEnableVertexAttribArray(normalAttribute);
        GLES32.glVertexAttribPointer(normalAttribute, 3, GLES32.GL_FLOAT, false, 0, 4*vertex.length);

        // EBO
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        intBuffer = IntBuffer.wrap(faces);
        intBuffer.rewind();

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, 4*faces.length, intBuffer, GLES32.GL_STATIC_DRAW);
    }


    @Override
    protected void loadUniform() {
        GLES32.glUniformMatrix4fv(shader[selectedShader].glGetUniformLocation("M"), 1, false, model, 0);
    }


    @Override
    public void drawSolid() {
        if(!draw) return;
        configGL();

        if(updateGL) {
            loadSortedEBO();
            updateGL = false;
        }

        // Draw points
        if (drawPoints) {
            GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("opacity"), 1.0f);
            GLES32.glUniform3fv(shader[selectedShader].glGetUniformLocation("meshColor"), 1, pColor, 0);
//            GLES32.glLineWidth(0.01f);
            GLES32.glDrawElements(GLES32.GL_POINTS, faces.length, GLES32.GL_UNSIGNED_INT, 0);
//            GLES32.glLineWidth(1f);
        }

        // Draw lines
        if (drawLines) {
            GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("opacity"), 1.0f);
            GLES32.glUniform3fv(shader[selectedShader].glGetUniformLocation("meshColor"), 1, lColor, 0);
            GLES32.glLineWidth(0.01f);
            GLES32.glDrawElements(GLES32.GL_LINE_LOOP, faces.length, GLES32.GL_UNSIGNED_INT, 0);
            GLES32.glLineWidth(1f);
        }

        // Draw solid triangles
        if (alpha == 1f && drawTriangles) {
            GLES32.glEnable(GLES32.GL_POLYGON_OFFSET_FILL);
            GLES32.glEnable(GLES32.GL_CULL_FACE);

            GLES32.glPolygonOffset(1f,1f);
            GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("opacity"), alpha);
            GLES32.glUniform3fv(shader[selectedShader].glGetUniformLocation("meshColor"), 1, tColor,0);

            GLES32.glDrawElements(GLES32.GL_TRIANGLES, faces.length, GLES32.GL_UNSIGNED_INT, 0);

            GLES32.glDisable(GLES32.GL_POLYGON_OFFSET_FILL);
            GLES32.glDisable(GLES32.GL_CULL_FACE);
        }

        boundingbox.drawSolid();
    }


    @Override
    public void drawTransparent() {
        if (!draw || alpha==1f) return;
        configGL();

        if(updateGL) {
            loadSortedEBO();
            updateGL = false;
        }

        if (!drawTriangles) return;

        // Draw triangles
        GLES32.glEnable(GLES32.GL_POLYGON_OFFSET_FILL);
        GLES32.glEnable(GLES32.GL_BLEND);
        GLES32.glEnable(GLES32.GL_CULL_FACE);

        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);
        GLES32.glPolygonOffset(1f,1f);
        GLES32.glUniform1f(shader[selectedShader].glGetUniformLocation("opacity"), alpha);
        GLES32.glUniform3fv(shader[selectedShader].glGetUniformLocation("meshColor"), 1, tColor,0);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, faces.length, GLES32.GL_UNSIGNED_INT, 0);

        GLES32.glDisable(GLES32.GL_POLYGON_OFFSET_FILL);
        GLES32.glDisable(GLES32.GL_BLEND);
        GLES32.glDisable(GLES32.GL_CULL_FACE);
    }


    private void calculateTriangleCentroid() {
        triangleCentroid = new float[(faces.length/4)*3];
        eyeToTriangleCentroid = new float[faces.length/4];

        // This part did not work as intended. Made the mesh till and lose some triangles when drawing (not being drawn)
//        sortedTriangleIndexes = new int[faces.length/3];
//        for (int i=0; i<sortedTriangleIndexes.length; i++)
//            sortedTriangleIndexes[i] = i;

        for (int i=0; i<faces.length/4; i++) {
            triangleCentroid[ i*3 ] = (vertex[ 3*faces[i*4] ] + vertex[ 3*faces[i*4+1] ] + vertex[3*faces[i*4+2]  ])/3;
            triangleCentroid[i*3+1] = (vertex[3*faces[i*4]+1] + vertex[3*faces[i*4+1]+1] + vertex[3*faces[i*4+2]+1])/3;
            triangleCentroid[i*3+2] = (vertex[3*faces[i*4]+2] + vertex[3*faces[i*4+1]+2] + vertex[3*faces[i*4+2]+2])/3;
        }
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


    private void calculateBoundingBoxDimCenter(float[] dim, int dOffset, float[] center, int cOffset) {
        float[] max = new float[3];
        float[] min = new float[3];

        max[0] = vertex[0];
        max[1] = vertex[1];
        max[2] = vertex[2];

        min[0] = vertex[0];
        min[1] = vertex[1];
        min[2] = vertex[2];

        for (int i=3; i<vertex.length; i+=3) {
            if (vertex[i]>max[0]) max[0] = vertex[i];
            else if (vertex[i]<min[0]) min[0] = vertex[i];

            if (vertex[i+1]>max[1]) max[1] = vertex[i+1];
            else if (vertex[i+1]<min[1]) min[1] = vertex[i+1];

            if (vertex[i+2]>max[2]) max[2] = vertex[i+2];
            else if (vertex[i+2]<min[2]) min[2] = vertex[i+2];
        }

        dim[ dOffset ] = max[0]-min[0];
        dim[dOffset+1] = max[1]-min[1];
        dim[dOffset+2] = max[2]-min[2];

        center[ cOffset ] = min[0]+dim[0]/2;
        center[cOffset+1] = min[1]+dim[1]/2;
        center[cOffset+2] = min[2]+dim[2]/2;
    }


    public static Shader[] shaderPrograms(Context c) {
        Shader[] shaderReturn = new Shader[shaderN];

        String[] vs = {"mesh.vs"};
        String[] fs = {"standardFragmentShader.fs"};
        String[] gs = {""};
        shaderReturn[0] = new Shader(vs, fs, gs, c);

        return shaderReturn;
    }


    public void updateCameraEye(float[] eye, int offset) {
        float[] relativeEye = new float[4];
        Matrix.multiplyMV(relativeEye,0, inverseModel,0, eye, offset);

        float[] tmp = new float[3];
        for (int i=0; i<eyeToTriangleCentroid.length; i++) {
            tmp[0] = triangleCentroid[ i*3 ] - relativeEye[0];
            tmp[1] = triangleCentroid[i*3+1] - relativeEye[1];
            tmp[2] = triangleCentroid[i*3+2] - relativeEye[2];

            eyeToTriangleCentroid[i] = (float)Math.sqrt(tmp[0]*tmp[0] + tmp[1]*tmp[1] + tmp[2]*tmp[2]);
        }

//        SortingAlgorithms.ArgQuickSort(eyeToTriangleCentroid,0, eyeToTriangleCentroid.length, sortedTriangleIndexes);
        sortedTriangleIndexes = SortingAlgorithms.ArgQuickSort(eyeToTriangleCentroid,0, eyeToTriangleCentroid.length);
        updateGL = true;
    }


    private void loadSortedEBO() {
        IntBuffer intBuffer = IntBuffer.allocate(faces.length);
        for (int i=sortedTriangleIndexes.length-1; i>=0; i--)
            intBuffer.put(faces, sortedTriangleIndexes[i]*4,4);

        intBuffer.rewind();

        GLES32.glBufferSubData(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0, 4*faces.length, intBuffer);
    }


    public void setAlpha(float a) {
        alpha = a;
    }


    public float getAlpha() { return alpha; }


    public void setTriangleColor(float r, float g, float b) {
        tColor[0] = r;
        tColor[1] = g;
        tColor[2] = b;
    }


    public boolean getDrawTriangles() { return drawTriangles; }


    public void setDrawTriangles(boolean newDrawTriangles) { drawTriangles = newDrawTriangles; }


    public void getTriangleColor(float[] container, int offset) { for (int i=0; i<3; i++) container[offset+i] = tColor[i]; }


    public void setDrawLines(boolean newDrawLines) { drawLines = newDrawLines; }


    public boolean getDrawLines() { return drawLines; }


    public void setLinesColor(float r, float g, float b) {
        lColor[0] = r;
        lColor[1] = g;
        lColor[2] = b;
    }


    public void getLinesColor(float[] container, int offset) { for (int i=0; i<3; i++) container[offset+i] = lColor[i]; }


    public void setDrawPoints(boolean newDrawPoints) { drawPoints = newDrawPoints; }


    public boolean getDrawPoints() { return drawPoints; }


    public void setPointsColor(float r, float g, float b) {
        pColor[0] = r;
        pColor[1] = g;
        pColor[2] = b;
    }


    public void getPointsColor(float[] container, int offset) { for (int i=0; i<3; i++) container[offset+i] = pColor[i]; }


    public void setDrawBB(boolean newDrawBB) {
        drawBB = newDrawBB;
        boundingbox.setDraw(drawBB);
    }


    public String getFileName() { return fileName; }


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
        updateGL = true;
        boundingbox.onPause();
    }


    @Override
    public void updateReferenceToShader(Map<VisualizationType, Shader[]> shaderChain) {
        shader = shaderChain.get(identifier);
        boundingbox.updateReferenceToShader(shaderChain);
    }
}
