package com.udec_biomed.aBrainVis.Tools;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.opengl.GLES32;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Shader {
    private final int program;
    private final String TAG = "Shader";
    private List<Integer> shaders = new ArrayList<>();
    private AssetManager resources;

    public Shader(String[] vShader, String[] fShader, String[] gShader, Context context) {
        resources = context.getAssets();

        program = GLES32.glCreateProgram();

        addShader(vShader, fShader, gShader);

        GLES32.glLinkProgram(program);

        checkLinkedStatus();

        deleteShader();
    }


    private void addShader(String[] vShader, String[] fShader, String[] gShader) {
        if (vShader.length != 0) shaders.add(addShader(vShader, GLES32.GL_VERTEX_SHADER));
        if (fShader.length != 0) shaders.add(addShader(fShader, GLES32.GL_FRAGMENT_SHADER));
        if (gShader.length != 0) shaders.add(addShader(gShader, GLES32.GL_GEOMETRY_SHADER));
    }


    private void checkLinkedStatus(){
        int[] linkStatus = {GLES32.GL_FALSE};
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, linkStatus, 0);

        if(linkStatus[0] != GLES32.GL_TRUE) {
            String log = GLES32.glGetProgramInfoLog(program);
            GLES32.glDeleteProgram(program);
            deleteShader();
            Log.e(TAG, "Error linking program: " + log);
        }
    }


    private void deleteShader() {
        for(int shader : shaders)
            GLES32.glDeleteShader(shader);
    }


    private int addShader(String shaderFile, int shaderType) {
        int shader = GLES32.glCreateShader(shaderType);
        String shaderSource = readFileAsString(shaderFile);
        if (shaderSource==null) {
            Log.e(TAG, "Error at reading: "+shaderFile);
        }
        GLES32.glShaderSource(shader, shaderSource);

        GLES32.glCompileShader(shader);

        int shaderCompileStatus[] = {GLES32.GL_FALSE};
        GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, shaderCompileStatus, 0);

        if (shaderCompileStatus[0] != GLES32.GL_TRUE) {
            String info = GLES32.glGetShaderInfoLog(shader);
            Log.e(TAG, "File: "+shaderFile+" Shadercompilation failed: "+info);
        }
        GLES32.glAttachShader(program, shader);
        return shader;
    }


    private int addShader(String[] shaderFiles, int shaderType) {
        int shader = GLES32.glCreateShader(shaderType);

        String shaderSource = "";
        for (String s : shaderFiles) {
            shaderSource += readFileAsString(s);
            if (shaderSource == null) Log.e(TAG, "Error at reading shader: "+s);
        }
        GLES32.glShaderSource(shader, shaderSource);

        GLES32.glCompileShader(shader);

        int shaderCompileStatus[] = {GLES32.GL_FALSE};
        GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, shaderCompileStatus, 0);

        if (shaderCompileStatus[0] != GLES32.GL_TRUE) {
            String info = GLES32.glGetShaderInfoLog(shader);
            String shaderFileString = shaderFiles[0];
            for (int i=1; i<shaderFiles.length; i++) shaderFileString += ", " + shaderFiles[i];

            Log.e(TAG, "Files: "+shaderFileString+" Shadercompilation failed: "+info);
        }
        GLES32.glAttachShader(program, shader);
        return shader;
    }


    public String readFileAsString(String assetFile) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = resources.open("sc/"+assetFile);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }

            return buf.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error opening asset " + assetFile);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing asset " + assetFile);
                }
            }
        }

        return null;
    }


    public int glGetUniformLocation(String name){
        int uniform = GLES32.glGetUniformLocation(program, name);

        if (uniform == -1)
            Log.e(TAG, "Uniform not found: "+name);
        return uniform;
    }


    public int glGetAttribLocation(String name) {
        int attribute = GLES32.glGetAttribLocation(program, name);

        if (attribute == -1)
            Log.e(TAG, "Attribute not found: "+name);
        return attribute;
    }


    public void glUseProgram() {
        GLES32.glUseProgram(program);
    }
}
