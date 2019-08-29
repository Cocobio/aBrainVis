package com.example.ifiber.Tools;

import android.opengl.Matrix;

public class Camera {
    private float radius;
    private float[] center = new float[3];
    private float[] rotation = new float[4];
    private float[] view = new float[16];

    private float[] eye = new float[3];
    private float[] up = new float[3];
    private float scaleFactor = 1f;


    public Camera(float r, float sf) {
        radius = r;
        scaleFactor = sf;

        defaultValues();
    }

    public Camera(float r) {
        radius = r;

        defaultValues();
    }

    public void defaultValues() {
        Quaternion.setIdentityQ(rotation, 0);

        eye[0] = 0f;
        eye[1] = 0f;
        eye[2] = 1f;

        center[0] = 0f;
        center[1] = 0f;
        center[2] = 0f;

        up[0] = 0f;
        up[1] = 1f;
        up[2] = 0f;

        calculateView();
    }


    public void orbit(float dx, float dy) {
        float angleMagnitude = (float) Math.sqrt(dx * dx + dy * dy);
        if (angleMagnitude < 0.001) return;

        float x = -dy / angleMagnitude;
        float y = -dx / angleMagnitude;
        float z = 0f;

        float[] newRotationQ = new float[4];
        Quaternion.fromAngleAndAxis(newRotationQ,0, angleMagnitude, x, y, z);
        Quaternion.multiplyQQ(rotation,0, rotation,0, newRotationQ,0);

        Quaternion.normalize(rotation,0);

        calculateView();
    }


    public void transverseRotation(float angle) {
        if (angle == 0.0) return;

        float[] newRotationQ = new float[4];
        Quaternion.fromAngleAndAxis(newRotationQ,0, angle, 0, 0, 1);
        Quaternion.multiplyQQ(rotation,0, rotation,0, newRotationQ,0);

        Quaternion.normalize(rotation,0);

        calculateView();
    }


    public void panning(float dx, float dy) {
        float[] vector = {-dx * scaleFactor, dy * scaleFactor, 0f, 1f};
        Quaternion.rotate3V(vector,0, rotation,0);
        for (int i = 0; i < 3; i++) center[i] += vector[i];

        calculateView();
    }


    public float axisAndAngleFromScreen(float[] axis, int aOffset, float dx, float dy) {
        float angleMagnitude = (float) Math.sqrt(dx*dx + dy*dy);
        float[] q = new float[4];
        Quaternion.fromAngleAndAxis(q,0,90,0,0,1);

        axis[ aOffset ] = dx;
        axis[aOffset+1] = -dy;
        axis[aOffset+2] = 0;

        Quaternion.rotate3V(axis,0, q,0);

        Quaternion.rotate3V(axis, aOffset, rotation,0);

        return angleMagnitude;
    }


    public void vectorFromScreen(float[] vector, int offset, float dx, float dy) {
        vector[ offset ] = dx;
        vector[offset+1] = -dy;
        vector[offset+2] = 0;

        Quaternion.rotate3V(vector, offset, rotation,0);
    }


    public void frontView() {
        Quaternion.setIdentityQ(rotation,0);
        calculateView();
    }


    public void backView() {
        Quaternion.fromAngleAndAxis(rotation,0, 180,0,1,0);
        calculateView();
    }


    public void leftView() {
        Quaternion.fromAngleAndAxis(rotation,0,90,0,1,0);
        calculateView();
    }

    public void rightView() {
        Quaternion.fromAngleAndAxis(rotation,0,90,0,-1,0);
        calculateView();
    }


    public void topView() {
        Quaternion.fromAngleAndAxis(rotation,0,270,1,0,0);
        calculateView();
    }


    public void bottomView() {
        Quaternion.fromAngleAndAxis(rotation,0,90,1,0,0);
        calculateView();
    }


    private void calculateView() {
        float[] currentEye = {eye[0]*radius*scaleFactor, eye[1]*radius*scaleFactor, eye[2]*radius*scaleFactor};
        Quaternion.rotate3V(currentEye,0, rotation,0);
        for (int i=0; i<3; i++) currentEye[i] += center[i];

        float[] currentUp = {up[0], up[1], up[2]};
        Quaternion.rotate3V(currentUp,0, rotation,0);


        Matrix.setLookAtM(view, 0,
                currentEye[0], currentEye[1], currentEye[2],
                center[0], center[1], center[2],
                currentUp[0], currentUp[1], currentUp[2]);
    }


    public void getView(float[] view_copy) {
        if (view_copy.length < 16) return;
        for (int i=0; i<16; i++) view_copy[i] = view[i];
    }


    public float[] getView() {
        float[] view_copy = new float[16];
        for (int i = 0; i < 16; i++) view_copy[i] = view[i];

        return view_copy;
    }


    public void getViewOfOrientationWithRadius(float[] view_copy, float r){
        float[] currentEye = {eye[0]*r, eye[1]*r, eye[2]*r};
        Quaternion.rotate3V(currentEye,0, rotation,0);

        float[] currentUp = {up[0], up[1], up[2]};
        Quaternion.rotate3V(currentUp,0, rotation,0);


        Matrix.setLookAtM(view_copy, 0,
                currentEye[0], currentEye[1], currentEye[2],
                0, 0, 0,
                currentUp[0], currentUp[1], currentUp[2]);
    }


    public float[] getEye() {
        float[] currentEye = {eye[0]*radius, eye[1]*radius, eye[2]*radius, 1};
        Quaternion.rotate3V(currentEye,0, rotation,0);
        for (int i=0; i<3; i++) currentEye[i] += center[i];

        return currentEye;
    }


    public float getRadius() {return radius;}
}
