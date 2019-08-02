/*
https://github.com/cabeen/niftijio information about the niftivolume class
 */

package com.example.ifiber.VisualizationObjects;

import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.example.ifiber.Shader;
import com.example.ifiber.Tools.ThirdParty.NiftiVolume;
import com.example.ifiber.Tools.VisualizationType;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MRI extends BaseVisualization {
    public static ArrayList<String> validFileExtensions =  new ArrayList<>(Arrays.asList("nii", "gz"));
    public static VisualizationType identifier = VisualizationType.MRI;

    private String filePath;
    private String fileName;

    private int[] MRIDimension = new int[4];

    protected float[] activeTransform = new float[16];
    protected float[] MRIVolume;

    protected int[] hMRITexture;

    protected BoundingBox boundingbox;



    public MRI(Map<VisualizationType, Shader[]> shaderChain, String file) {
        super();

        TAG = "MRI";

        shader = null;

        filePath = file;

        Matrix.setIdentityM(activeTransform,0);

        String[] tmp = file.split("[/]");
        fileName = tmp[tmp.length-1];

        readData();

        openGLLoaded = false;
        draw = true;
        drawBB = true;

        float[] dim = {MRIDimension[0], MRIDimension[1], MRIDimension[2]};
        float[] center = {MRIDimension[0]/2, MRIDimension[1]/2, MRIDimension[2]/2};

        boundingbox = new BoundingBox(shaderChain, dim, 0, center, 0, model);

        Log.d(TAG, "Loading ready "+filePath+" Dimensions of MRI: ["+MRIDimension[0]+", "+MRIDimension[1]+", "+MRIDimension[2]+" ]");
    }


    public void loadOpenGLVariables(){
        if (openGLLoaded)
            return;
        loadMRITexture();
        loadGLBuffers();
        boundingbox.loadOpenGLVariables();
        openGLLoaded = true;
    }


    private void readData() {
        String[] tokens = filePath.split("[.]");
        String extension = tokens[tokens.length - 1];

        if (extension.equals("nii") || extension.equals("gz"))
            readNifti();
//        else if (extension.equals("dicom"))
//            readDicom();
        else
            Log.e(TAG, "Unsupported mri file: " + extension);
    }


    private void readNifti() {
        try {
            NiftiVolume volume = NiftiVolume.read(filePath);

            MRIDimension[0] = volume.header.dim[1];
            MRIDimension[1] = volume.header.dim[2];
            MRIDimension[2] = volume.header.dim[3];
            MRIDimension[3] = volume.header.dim[4];

            // Not implemented for multiples volumes in a single MRI
//            if (MRIDimension[3] == 0)
            MRIDimension[3] = 1;

            MRIVolume = new float[MRIDimension[0]*MRIDimension[1]*MRIDimension[2]*MRIDimension[3]];

            int index = 0;

            for (int d=0; d<MRIDimension[3]; d++)
                for (int k = 0; k < MRIDimension[2]; k++)
                    for (int j = 0; j < MRIDimension[1]; j++)
                        for (int i = 0; i < MRIDimension[0]; i++)
                            MRIVolume[index++] = (float)volume.data.get(i,j,k,d);

            // We set the affine transform as the active one
            for (int i=0; i<4; i++) {
                activeTransform[ i*4 ] = volume.header.srow_x[i];
                activeTransform[i*4+1] = volume.header.srow_y[i];
                activeTransform[i*4+2] = volume.header.srow_z[i];
                activeTransform[i*4+3] = 0;
            }
            activeTransform[15] = 1f;

            calculateModel();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadMRITexture() {
        if (hMRITexture == null) {
            hMRITexture = new int[1];
            GLES32.glGenTextures(1, hMRITexture,0);
        }

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_3D, hMRITexture[0]);

        GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_BORDER);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_BORDER);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_WRAP_R, GLES32.GL_CLAMP_TO_BORDER);

        float[] bgColor = {1.0f, 1.0f, 1.0f, 1.0f};
        GLES32.glTexParameterfv(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_BORDER_COLOR, bgColor,0);

        GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_3D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);

//		# Not filtered
//		# glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
//		# glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

//		# Must swap axes 0 and 2, cuz data is not alined
        FloatBuffer volumeBuffer = FloatBuffer.wrap(MRIVolume);
        volumeBuffer.rewind();
        GLES32.glTexImage3D(GLES32.GL_TEXTURE_3D, 0, GLES32.GL_R32F, MRIDimension[0], MRIDimension[1], MRIDimension[2], 0, GLES32.GL_RED, GLES32.GL_FLOAT, volumeBuffer);
    }



    private void loadGLBuffers() {

        if (vbo == null) {
            vbo = new int[1];
            GLES32.glGenBuffers(1, vbo, 0);
        }

        int[] vertex = {0,1,2,3,4,5};
        IntBuffer intBuffer = IntBuffer.wrap(vertex);
        intBuffer.rewind();

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, vertex.length*4, intBuffer, GLES32.GL_STATIC_DRAW);
    }


    @Override
    public void drawSolid() {
        if (!draw) return;
        boundingbox.drawSolid();
    }

    @Override
    public void drawTransparent() { }


    @Override
    public void cleanOpenGL() {
        GLES32.glDeleteBuffers(1, vbo, 0);
    }


    @Override
    protected void calculateModel() {
        float[] tmpM = new float[16], tmpM2 = new float[16];

        Matrix.multiplyMM(tmpM,0, scaleMat,0, activeTransform, 0);
        Matrix.multiplyMM(tmpM2,0, rotationMat,0, tmpM,0);
        Matrix.multiplyMM(model, 0, translateMat, 0, tmpM2, 0);

        Matrix.invertM(inverseModel, 0, model, 0);
    }


    @Override
    public void resetModel() {
        float[] tmp = new float[16];
        if (activeTransform == null) {
            activeTransform = new float[16];
            Matrix.setIdentityM(activeTransform, 0);
        }

        Matrix.setIdentityM(tmp, 0);
        Matrix.multiplyMM(model,0, tmp,0, activeTransform,0);
        Matrix.invertM(inverseModel, 0, model, 0);

        Matrix.setIdentityM(rotationMat, 0);
        Matrix.setIdentityM(translateMat, 0);
        Matrix.setIdentityM(scaleMat, 0);
    }


    public String getFilePath() {
        return filePath;
    }


    public String getFileName() {
        return fileName;
    }


    public void getMRIDimension(float[] placeHolder, int offset) {
        for (int i=0; i<MRIDimension.length; i++)
            placeHolder[offset+i] = MRIDimension[i];
    }


    public void setDrawBB(boolean newDrawBB) {
        drawBB = newDrawBB;
        boundingbox.setDraw(drawBB);
    }
}
