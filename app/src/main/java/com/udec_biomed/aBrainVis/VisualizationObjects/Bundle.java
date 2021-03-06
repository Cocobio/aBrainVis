package com.udec_biomed.aBrainVis.VisualizationObjects;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.udec_biomed.aBrainVis.Tools.Quaternion;
import com.udec_biomed.aBrainVis.Tools.Shader;
import com.udec_biomed.aBrainVis.Tools.VisualizationType;

import java.io.BufferedReader;
import java.io.FileReader;
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
import java.util.Vector;

public class Bundle extends BaseVisualization {
    static final private int cylinderNFaces = 7;
    static final private float cylinderRadius = 0.3f;
    public static ArrayList<String> validFileExtensions =  new ArrayList<>(Arrays.asList("bundles", "tck", "trk"));
    public static VisualizationType identifier = VisualizationType.BUNDLE;
    
    private String filePath;
    private String fileName;

    private int curvesCount;
    private String[] bundlesName;
    private int[] bundlesStart;

    private float[] vertex;
    private float[] normals;
    private int[] color;
    private int[] element;
    private int[] fiberSizes;

    private float[] colorTable;

    protected int[] hColorTableTexture = null;

    protected BoundingBox boundingbox;

    static protected int shaderN = 2;

    // In place segmentation bundle implementation
    private boolean[] selectedBundles;
    private int percentage = 100;
    private boolean updateEBOFlag = false;
    private int elementLength;
    /////////////////////////////////////////////

    private final static float[] materialValues = {1f, 0.8f, 0.7f, 5f};


    public Bundle(Map<VisualizationType, Shader[]> shaderChain, String file) {
        super();

        TAG = "BUN";

        shader = shaderChain.get(identifier);

        filePath = file;

        String[] tmp = file.split("[/]");
        fileName = tmp[tmp.length-1];

        readData();
        createColorTable();

        openGLLoaded = false;
		draw = true;
        drawBB = true;

        float[] dim = new float[3];
        float[] center = new float[3];
        calculateBoundingBoxDimCenter(dim,0, center,0);

        boundingbox = new BoundingBox(shaderChain, dim, 0, center, 0, model);

        Log.d(TAG, "Loading ready "+filePath+" "+curvesCount+" fibers and "+bundlesName.length+" bundles.");
    }


    public void loadOpenGLVariables(){
        if (updateEBOFlag) {
            updateEBO();
            updateEBOFlag = false;
        }
        if (openGLLoaded)
            return;
        loadColorTexture();
        loadGLBuffers();
        vertexAttribPointer();
        boundingbox.loadOpenGLVariables();
        openGLLoaded = true;
    }


    private void readData() {
        String[] tokens = filePath.split("[.]");
        String extension = tokens[tokens.length-1];

        if (extension.equals("bundles"))
            readBundle();
        else if (extension.equals("trk"))
            readTrk();
        else if (extension.equals("tck"))
            readTck();
        else
            Log.e(TAG, "Unsupported track file: "+extension);
    }


    private void readBundle() {
        readBundleHeader();
        readBundleBody();
    }


    private void readBundleHeader() {
        // Read bundle file
        BufferedReader br = null;
        String bundles = new String();

        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(filePath));
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains("\'bundles\'")){
                    bundles=sCurrentLine;
                }
                else if(sCurrentLine.contains("\'curves_count\'")){
                    curvesCount = Integer.valueOf(sCurrentLine.split(" ")[6].replace(",",""));
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error at reading file "+fileName+" : " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                Log.e(TAG, "Error at closing file "+fileName+" : " + ex.toString());
                ex.printStackTrace();
            }
        }

        String[] tmp=bundles.substring(bundles.indexOf('[')+1,bundles.lastIndexOf(']')).replace(" ", "").split(",");

        bundlesName = new String[(tmp.length)/2];
        bundlesStart = new int[(tmp.length)/2+1];

        // In place segmentation bundle implementation
        selectedBundles = new boolean[(tmp.length)/2];
        for (int i=0; i<selectedBundles.length; i++) selectedBundles[i] = true;
        //////////////////////////////////////////////

        for(int i=0;i<tmp.length-1;i+=2) {
            bundlesName[i/2] = tmp[i];
            bundlesStart[i/2] = Integer.parseInt(tmp[i+1]);
        }

        bundlesStart[bundlesStart.length-1] = curvesCount;
    }


    private void readBundleBody() {
        // READ BUNDLES DATA
        try {
            fiberSizes = new int[curvesCount];
            RandomAccessFile rFile = new RandomAccessFile(filePath + "data", "r");
            long fileSize = rFile.length();
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buf_in = ByteBuffer.allocate((int)fileSize);

            int vertexSize = (int)(fileSize/4-curvesCount);

            vertex = new float[vertexSize];
            normals = new float[vertexSize];
            color = new int[vertexSize/3];

            int elementLength = vertexSize/3 + curvesCount;

            element = new int[elementLength];
            this.elementLength = element.length;

            buf_in.order(ByteOrder.LITTLE_ENDIAN);
            buf_in.clear();
            inChannel.read(buf_in);
            buf_in.rewind();

            int offset = 0, index = 0;
            float[] normal = new float[3];
            float norma;

            for (int i=0, j, k; i<bundlesName.length; i++) {
                for (j=bundlesStart[i]; j<bundlesStart[i+1]; j++) {
                    fiberSizes[j] = buf_in.getInt();
                    buf_in.asFloatBuffer().get(vertex, offset, fiberSizes[j]*3);
                    buf_in.position(buf_in.position()+fiberSizes[j]*12);

                    for (k=0; k<fiberSizes[j]-1; k++) {
                        element[index] = (index++)-j;

                        normal[0] = vertex[offset+k*3+3] - vertex[ offset+k*3 ];
                        normal[1] = vertex[offset+k*3+4] - vertex[offset+k*3+1];
                        normal[2] = vertex[offset+k*3+5] - vertex[offset+k*3+2];
                        norma = (float) Math.sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]);

                        normals[ offset+k*3 ] = normal[0]/norma;
                        normals[offset+k*3+1] = normal[1]/norma;
                        normals[offset+k*3+2] = normal[2]/norma;

                        color[offset/3+k] = i;
                    }

                    element[index] = (index++)-j;
                    element[index++] = -1;

                    normals[ offset+k*3 ] = normals[offset+k*3-3];
                    normals[offset+k*3+1] = normals[offset+k*3-2];
                    normals[offset+k*3+2] = normals[offset+k*3-1];

                    color[offset/3] = i;
                    offset += fiberSizes[j]*3;
                }
            }

            inChannel.close();
        } catch (IOException ex) {
            Log.e(TAG, "Error at reading file "+fileName+" : " + ex.toString());
            System.err.println(ex.getMessage());
        }
    }


    private void readTrk() {
        short[] trkNScalars = new short[1], trkNProperties = new short[1];
        boolean[] trkLittleEndian = new boolean[1];

        float[] voxelSize = new float[3];
        float[] vox2RasMat = new float[16];

        readTrkHeader(trkLittleEndian, trkNScalars, trkNProperties, voxelSize, vox2RasMat);
        readTrkBody(trkLittleEndian[0], trkNScalars[0], trkNProperties[0]);

        // Apply vox2RasMat, and half voxelSize correction
        float[] inverseVoxelSize = new float[16], tmp = new float[16], trkMat = new float[16];
        Matrix.setIdentityM(inverseVoxelSize, 0);
        Matrix.scaleM(inverseVoxelSize,0,1/voxelSize[0],1/voxelSize[1],1/voxelSize[2]);
        Matrix.transposeM(tmp,0, vox2RasMat,0);

        Matrix.multiplyMM(trkMat,0, tmp,0, inverseVoxelSize,0);
        float[] halfVoxelSize = new float[16];
        Matrix.setIdentityM(halfVoxelSize,0);
        Matrix.translateM(halfVoxelSize,0,-voxelSize[0]/2,-voxelSize[1]/2,-voxelSize[2]/2);

        Matrix.multiplyMM(tmp,0, trkMat,0, halfVoxelSize,0);

        applyMatrix(tmp, 0);

        // Calculate normals
        calculateNormals();
    }


    private void readTrkHeader(boolean[] trkLittleEndian, short[] trkNScalars, short[] trkNProperties, float[] voxelSize, float[] vox2RasMat) {
        // Read trk header (1000 bytes)
        try {
            RandomAccessFile rFile = new RandomAccessFile(filePath, "r");
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buf_in = ByteBuffer.allocate(1000);

            trkLittleEndian[0] = true;
            buf_in.order(ByteOrder.LITTLE_ENDIAN);
            buf_in.clear();
            inChannel.read(buf_in);

            int headerSize = buf_in.getInt(996);
            if (headerSize != 1000) {
                buf_in.order(ByteOrder.BIG_ENDIAN);
                trkLittleEndian[0] = false;
            }

            buf_in.position(12);
            buf_in.asFloatBuffer().get(voxelSize, 0, 3);

            trkNScalars[0] = buf_in.getShort(36);
            trkNProperties[0] = buf_in.getShort(238);

            buf_in.position(440);
            buf_in.asFloatBuffer().get(vox2RasMat,0,16);

            curvesCount = buf_in.getInt(988);

            bundlesName = new String[1];
            bundlesStart = new int[2];

            bundlesName[0] = fileName;

            bundlesStart[0] = 0;
            bundlesStart[1] = curvesCount;

            // In place segmentation bundle implementation
            selectedBundles = new boolean[1];
            selectedBundles[0] = true;
            //////////////////////////////////////////////

            inChannel.close();
        } catch (IOException ex) {
            Log.e(TAG, "Error at reading file "+fileName+" : " + ex.toString());
            System.err.println(ex.getMessage());
        }
    }


    private void readTrkBody(boolean trkLittleEndian, short trkNScalars, short trkNProperties) {
        // Read fiber data
        try {
            fiberSizes = new int[curvesCount];
            RandomAccessFile rFile = new RandomAccessFile(filePath, "r");
            long fileSize = rFile.length()-1000;
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buf_in = ByteBuffer.allocate((int)fileSize);

            int vertexSize = (int)(fileSize/4-curvesCount*(trkNProperties+1))*3/(3+trkNScalars);
            vertex = new float[vertexSize];
            normals = new float[vertexSize];
            color = new int[vertexSize/3];

            for (int i=0; i<color.length; i++) color[i] = 0;

            int elementLength = vertexSize/3 + curvesCount;

            element = new int[elementLength];
            this.elementLength = element.length;

            if (trkLittleEndian) buf_in.order(ByteOrder.LITTLE_ENDIAN);
            else buf_in.order(ByteOrder.BIG_ENDIAN);

            buf_in.clear();
            inChannel.read(buf_in,1000);
            buf_in.rewind();

            // Reading trk with only points
            if (trkNScalars==0 && trkNProperties==0) {
                int offset = 0, index = 0;

                for (int i=0; i<curvesCount; i++) {
                    fiberSizes[i] = buf_in.getInt();
                    buf_in.asFloatBuffer().get(vertex, offset, fiberSizes[i]*3);
                    buf_in.position(buf_in.position()+fiberSizes[i]*12);

                    for (int k=0; k<fiberSizes[i]-1; k++)
                        element[index] = (index++)-i;

                    element[index] = (index++)-i;
                    element[index++] = -1;

                    offset += fiberSizes[i]*3;
                }
            }

            // Reading points in a trk with points and scalars
            else if (trkNScalars!=0 && trkNProperties==0) {
                int offset = 0, index = 0;

                for (int i=0; i<curvesCount; i++) {
                    fiberSizes[i] = buf_in.getInt();
                    for(int j=0; j<fiberSizes[i]; j++) {
                        buf_in.asFloatBuffer().get(vertex, offset+j*3, 3);
                        buf_in.position(buf_in.position()+(3+trkNScalars)*4);
                    }

                    for (int k=0; k<fiberSizes[i]-1; k++)
                        element[index] = (index++)-i;

                    element[index] = (index++)-i;
                    element[index++] = -1;

                    offset += fiberSizes[i]*3;
                }
            }

            // Reading only points in a trk with nproperties
            else if (trkNScalars==0 && trkNProperties!=0){
                int offset = 0, index = 0;

                for (int i=0; i<curvesCount; i++) {
                    fiberSizes[i] = buf_in.getInt();
                    buf_in.asFloatBuffer().get(vertex, offset, fiberSizes[i]*3);
                    buf_in.position(buf_in.position()+fiberSizes[i]*12+trkNProperties*4);

                    for (int k=0; k<fiberSizes[i]-1; k++)
                        element[index] = (index++)-i;

                    element[index] = (index++)-i;
                    element[index++] = -1;

                    offset += fiberSizes[i]*3;
                }
            }

            // Reading only points in a trk with properties and nscalars
            else {
                int offset = 0, index = 0;

                for (int i=0; i<curvesCount; i++) {
                    fiberSizes[i] = buf_in.getInt();
                    fiberSizes[i] = buf_in.getInt();
                    for(int j=0; j<fiberSizes[i]; j++) {
                        buf_in.asFloatBuffer().get(vertex, offset+j*3, 3);
                        buf_in.position(buf_in.position()+(3+trkNScalars)*4);
                    }
                    buf_in.position(buf_in.position()+trkNProperties*4);

                    for (int k=0; k<fiberSizes[i]-1; k++)
                        element[index] = (index++)-i;

                    element[index] = (index++)-i;
                    element[index++] = -1;

                    offset += fiberSizes[i]*3;
                }
            }

            inChannel.close();
        } catch (IOException ex) {
            Log.e(TAG, "Error at reading file "+fileName+" : " + ex.toString());
            System.err.println(ex.getMessage());
        }
    }


    private void readTck() {
        Log.e(TAG, "Tck extension not implemented.");
    }


    private void createColorTable() {
        colorTable = new float[bundlesName.length*4];

        for (int i=0; i<bundlesName.length; i++) {
            colorTable[i * 4] = (float) Math.random() * 0.7f + 0.3f;
            colorTable[i*4+1] = (float) Math.random() * 0.7f + 0.3f;
            colorTable[i*4+2] = (float) Math.random() * 0.7f + 0.3f;
            colorTable[i*4+3] = 1.0f;
        }
    }


    private void loadColorTexture() {
        if (hColorTableTexture == null) {
            hColorTableTexture = new int[1];
            GLES32.glGenTextures(1, hColorTableTexture, 0);
        }

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, hColorTableTexture[0]);


        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_BORDER);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_BORDER);

        float[] bgColor = {1.0f, 1.0f, 1.0f, 1.0f};
        GLES32.glTexParameterfv(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_BORDER_COLOR, bgColor, 0);

        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);

        FloatBuffer colorTableBuffer = FloatBuffer.wrap(colorTable);
        colorTableBuffer.rewind();
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA32F, bundlesName.length, 1, 0, GLES32.GL_RGBA, GLES32.GL_FLOAT, colorTableBuffer);
    }


    private void loadGLBuffers() {
        if (vao == null) {
            vao = new int[shaderN];
            GLES32.glGenVertexArrays(shaderN, vao, 0);
        }
        GLES32.glBindVertexArray(0);

        if (vbo == null) {
            vbo = new int[3];
            GLES32.glGenBuffers(3, vbo, 0);
        }

        if (ebo == null) {
            ebo = new int[1];
            GLES32.glGenBuffers(1, ebo, 0);
        }

		// VBO
        FloatBuffer floatBuffer;
        IntBuffer intBuffer;

        // Vertex
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        floatBuffer = FloatBuffer.wrap(vertex);
        floatBuffer.rewind();

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 4*vertex.length, floatBuffer, GLES32.GL_STATIC_DRAW);

        // Normal
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[1]);
        floatBuffer = FloatBuffer.wrap(normals);
        floatBuffer.rewind();

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 4*normals.length, floatBuffer, GLES32.GL_STATIC_DRAW);

        // Color
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[2]);
        intBuffer = IntBuffer.wrap(color);
        intBuffer.rewind();

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 4*color.length, intBuffer, GLES32.GL_STATIC_DRAW);

		// EBO
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        intBuffer = IntBuffer.wrap(element);
        intBuffer.rewind();

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, 4*element.length, intBuffer, GLES32.GL_STATIC_DRAW);
    }


    private void vertexAttribPointer() {
        // Data for shader[0]
        GLES32.glBindVertexArray(vao[0]);

        // Enable attributes
        int positionAttribute =	shader[0].glGetAttribLocation("vertexPos");
        int normalAttribute =	shader[0].glGetAttribLocation("vertexNor");
        int colorAttribute =	shader[0].glGetAttribLocation("vertexCol");

        // vertex spatial data
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glEnableVertexAttribArray(positionAttribute);
        GLES32.glVertexAttribPointer(positionAttribute, 3, GLES32.GL_FLOAT, false, 0, 0);

        // vertex normal data
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[1]);
        GLES32.glEnableVertexAttribArray(normalAttribute);
        GLES32.glVertexAttribPointer(normalAttribute, 3, GLES32.GL_FLOAT, false, 0, 0);

        // vertex color id data
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[2]);
        GLES32.glEnableVertexAttribArray(colorAttribute);
        GLES32.glVertexAttribPointer(colorAttribute, 1, GLES32.GL_INT, false, 0, 0);

        // EBO
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);

        // Data for shader[1]
        GLES32.glBindVertexArray(vao[1]);

        // Enable attributes
        positionAttribute =	shader[1].glGetAttribLocation("vertexPos");
        colorAttribute =	shader[1].glGetAttribLocation("vertexCol");

        // vertex spatial data
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glEnableVertexAttribArray(positionAttribute);
        GLES32.glVertexAttribPointer(positionAttribute, 3, GLES32.GL_FLOAT, false, 0, 0);

        // vertex color id data
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[2]);
        GLES32.glEnableVertexAttribArray(colorAttribute);
        GLES32.glVertexAttribPointer(colorAttribute, 1, GLES32.GL_INT, false, 0, 0);

        // EBO
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);

        // Dereference the vao
        GLES32.glBindVertexArray(0);
    }


    @Override
    protected void loadUniform() {
        GLES32.glUniformMatrix4fv(shader[selectedShader].glGetUniformLocation("M"), 1, false, model, 0);
        GLES32.glUniform1i(shader[selectedShader].glGetUniformLocation("colorTable"), 0);
    }


    @Override
    public void drawSolid() {
        if (!draw) return;
        configGL();
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, hColorTableTexture[0]);

//        if (selectedShader == 0)
            GLES32.glDrawElements(GLES32.GL_LINE_STRIP, elementLength, GLES32.GL_UNSIGNED_INT, 0);
//        else {
//            GLES32.glEnable(GLES32.GL_CULL_FACE);
//            GLES32.glCullFace(GLES32.GL_FRONT);
//            GLES32.glFrontFace(GLES32.GL_CCW);
//            GLES32.glDrawElements(GLES32.GL_LINE_STRIP, elementLength, GLES32.GL_UNSIGNED_INT, 0);
//            GLES32.glDisable(GLES32.GL_CULL_FACE);
//        }

        boundingbox.drawSolid();
    }

    @Override
    public void drawTransparent() { }


    @Override
    public void cleanOpenGL() {
        if (vbo != null) {
            GLES32.glDeleteBuffers(3, vbo, 0);
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

        GLES32.glDeleteTextures(1, hColorTableTexture, 0);
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


    private void applyMatrix(float[] mat, int offset) {
            float[] tmp = new float[3];

            for (int i=0; i<vertex.length; i+= 3)
                vec3MultiplyBy4x4Matrix(vertex, i, mat, offset, tmp, 0);
    }


    private void vec3MultiplyBy4x4Matrix(float[] v, int vOffset, float[] m, int mOffset, float[] tmpPlaceHolder, int phOffset) {
        tmpPlaceHolder[ phOffset ] = v[vOffset]*m[ mOffset ] + v[vOffset+1]*m[mOffset+4] + v[vOffset+2]*m[mOffset+8]  + m[mOffset+12];
        tmpPlaceHolder[phOffset+1] = v[vOffset]*m[mOffset+1] + v[vOffset+1]*m[mOffset+5] + v[vOffset+2]*m[mOffset+9]  + m[mOffset+13];
        tmpPlaceHolder[phOffset+2] = v[vOffset]*m[mOffset+2] + v[vOffset+1]*m[mOffset+6] + v[vOffset+2]*m[mOffset+10] + m[mOffset+14];

        v[ vOffset ] = tmpPlaceHolder[ phOffset ];
        v[vOffset+1] = tmpPlaceHolder[phOffset+1];
        v[vOffset+2] = tmpPlaceHolder[phOffset+2];
    }


    private void calculateNormals() {
        if (normals == null) normals = new float[vertex.length];

        float[] normal = new float[3];
        float norma = 1f;

        int offset = 0;

        for (int i=0; i<curvesCount; i++) {
            for (int j=0; j<fiberSizes[i]-1; j++) {
                normal[0] = vertex[offset+j*3+3] - vertex[offset+ j*3 ];
                normal[1] = vertex[offset+j*3+4] - vertex[offset+j*3+1];
                normal[2] = vertex[offset+j*3+5] - vertex[offset+j*3+2];
                norma = (float)Math.sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]);

			normals[offset+ j*3 ] = normal[0]/norma;
			normals[offset+j*3+1] = normal[1]/norma;
			normals[offset+j*3+2] = normal[2]/norma;
            }

		normals[ offset+fiberSizes[i] ] = normal[0]/norma;
		normals[offset+fiberSizes[i]+1] = normal[1]/norma;
		normals[offset+fiberSizes[i]+2] = normal[2]/norma;

		offset += fiberSizes[i]*3;
        }
    }


    static private void createCylinder(float[] cylinderData, int offset, int cylinderNFaces, float cylinderRadius) {
        float[] rotationX = new float[4];

        float[] cylinderOffset = {  0, cylinderRadius, 0};
        Quaternion.fromAngleAndAxis(rotationX, 0, 360f/cylinderNFaces, 1, 0, 0);

        // Rotate vertex
        for (int i=0; i<cylinderData.length; i+=2) {
            Quaternion.rotate3V(cylinderOffset,0, rotationX,0);
            cylinderData[ i+offset ] = cylinderOffset[1];
            cylinderData[i+1+offset] = cylinderOffset[2];
        }
    }


    public void setDrawBB(boolean newDrawBB) {
        drawBB = newDrawBB;
        boundingbox.setDraw(drawBB);
    }


    public String getFileName() { return fileName; }


    public Vector<String> getBundlesNames() { return new Vector<String>(Arrays.asList(bundlesName)); }


    // In place segmentation bundle implementation
    private void updateEBO() {
        // VAO
        GLES32.glBindVertexArray(vao[0]);

        IntBuffer intBuffer = IntBuffer.wrap(element,0,elementLength);
        intBuffer.rewind();

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, 4*elementLength, intBuffer, GLES32.GL_STATIC_DRAW);
    }


    private void createNewEBO() {
        long start = System.currentTimeMillis();
        int j, index=0, fw_iterator=0, bw_iterator=element.length;
        float step=100.0f/percentage;

        for (int i=0; i<selectedBundles.length; i++) {
            if (selectedBundles[i]) {
                for (j = 0; step *j < bundlesStart[i + 1] - bundlesStart[i]; j++) {
                    for (int k = 0; k < fiberSizes[bundlesStart[i] + ((int) (step * j))]; k++)
                        element[fw_iterator++] = index++;
                    element[fw_iterator++] = -1;

                    for (int k = ((int) (step * j)) + bundlesStart[i] + 1; k < Math.min(((int)(step * (j + 1))) + bundlesStart[i], bundlesStart[i + 1]); k++)
                        index += fiberSizes[k];
                }
            }
            else {
                for (int k=bundlesStart[i]; k<bundlesStart[i+1]; k++) index += fiberSizes[k];
            }
        }

        elementLength = fw_iterator;
        updateEBOFlag = true;

        long time = System.currentTimeMillis()-start;
        Log.e("TIMER", "NewEBO creation: "+time+" [ms]");
    }


    public void getSelectedBundles(boolean[] container, int offset) {
        for (int i=0; i<selectedBundles.length; i++)
            container[offset+i] = selectedBundles[i];
    }


    public void setSelectedBundles(boolean[] container, int offset) {
        for (int i=0; i<selectedBundles.length; i++)
            selectedBundles[i] = container[offset+i];
        createNewEBO();
    }


    public int getPercentage() { return percentage; }


    public void setPercentage(int newPercentage) {
        percentage = newPercentage;
        createNewEBO();
    }
    //////////////////////////////////////////////


    static private void loadStaticUniformData(Shader[] shader) {
        // create static data
        float[] cylinderData = new float[(cylinderNFaces+1)*2];
        createCylinder(cylinderData,0, cylinderNFaces, cylinderRadius);

        // load data
        shader[1].glUseProgram();
        FloatBuffer floatBuffer = FloatBuffer.wrap(cylinderData);
        floatBuffer.rewind();
        GLES32.glUniform2fv(shader[1].glGetUniformLocation("cylinderVertex"), cylinderData.length, floatBuffer);
    }


    public static Shader[] shaderPrograms(Context c) {
        Shader[] shaderReturn = new Shader[shaderN];

        String[] vs_0 = {"bundle.vs"};
        String[] fs = {"standardFragmentShader.fs"};
        String[] gs_0 = {};
        shaderReturn[0] = new Shader(vs_0, fs, gs_0, c);

        String[] vs_1 = {"cylinder.vs"};
        String[] gs_1 = {"cylinder.gs", "quaternion.sf"};
        shaderReturn[1] = new Shader(vs_1, fs, gs_1, c);

        loadStaticUniformData(shaderReturn);
        return shaderReturn;
    }


    public void setSelectedShader(int newSelectedShader) {
        if (newSelectedShader < shaderN) selectedShader = newSelectedShader;
        else Log.e(TAG, "Selected shader out of bound ("+newSelectedShader+"), maximum is "+shaderN+".");
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


    @Override
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
