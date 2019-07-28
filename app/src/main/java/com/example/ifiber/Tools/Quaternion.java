package com.example.ifiber.Tools;

import android.util.Log;

public class Quaternion {
    static public void setIdentityQ(float[] quaternion, int offset) {
        quaternion[ offset ] = 1f;
        quaternion[offset+1] = 0f;
        quaternion[offset+2] = 0f;
        quaternion[offset+3] = 0f;
    }


    static public void fromAngleAndAxis(float[] quaternion, int offset, float angleDegree, float x, float y, float z) {
        double module = Math.sqrt(x*x + y*y + z*z);
        double s = Math.sin(Math.toRadians(angleDegree/2)) / module;

        quaternion[ offset ] = (float)Math.cos(Math.toRadians(angleDegree/2));
        quaternion[offset+1] = (float)(x*s);
        quaternion[offset+2] = (float)(y*s);
        quaternion[offset+3] = (float)(z*s);
    }


    static public void fromElements(float[] quaternion, int offset, float w, float x, float y, float z) {
        quaternion[ offset ] = w;
        quaternion[offset+1] = x;
        quaternion[offset+2] = y;
        quaternion[offset+3] = z;
    }


    static public void fromQuaternion(float[] toQuaternion, int offset, float[] fromQuaternion, int offsetFromQ) {
        toQuaternion[ offset ] = fromQuaternion[ offsetFromQ ];
        toQuaternion[offset+1] = fromQuaternion[offsetFromQ+1];
        toQuaternion[offset+2] = fromQuaternion[offsetFromQ+2];
        toQuaternion[offset+3] = fromQuaternion[offsetFromQ+3];
    }


    static public void normalize(float[] quaternion, int offset) {
        float w = quaternion[offset], x = quaternion[offset+1], y = quaternion[offset+2], z = quaternion[offset+3];
        float module = (float)Math.sqrt(w*w + x*x + y*y + z*z);

        for (int i=0; i<4; i++) quaternion[offset+i] /= module;
    }


    static public void invert(float[] toQuaternion, int offset, float[] fromQuaternion, int offsetFromQ) {
        toQuaternion[ offset ] =  fromQuaternion[ offsetFromQ ];
        toQuaternion[offset+1] = -fromQuaternion[offsetFromQ+1];
        toQuaternion[offset+2] = -fromQuaternion[offsetFromQ+2];
        toQuaternion[offset+3] = -fromQuaternion[offsetFromQ+3];
    }


    static public void rotate3V(float[] vector, int vOffset, float[] quaternion, int qOffset) {
        float[] p = new float[4];
        float[] invertedQuaternion = new float[4];
        float[] res = new float[4];
        invert(invertedQuaternion,0, quaternion, qOffset);

        fromElements(p,0,0f, vector[vOffset], vector[vOffset+1], vector[vOffset+2]);
        multiplyQQ(res,0, p,0, invertedQuaternion,0);
        multiplyQQ(res,0, quaternion,0, res,0);

        for (int i=0; i<3; i++) vector[vOffset+i] = res[i+1];
    }


    static public void rotateV(float[] vector, int vOffset, float[] quaternion, int qOffset) {
        float[] p = new float[4];
        float[] invertedQuaternion = new float[4];
        float[] res = new float[4];
        invert(invertedQuaternion,0, quaternion, qOffset);

        fromElements(p,0,0f, vector[vOffset], vector[vOffset+1], vector[vOffset+2]);
        multiplyQQ(res,0, p,0, invertedQuaternion,0);
        multiplyQQ(res,0, quaternion, qOffset, res,0);

        for (int i=0; i<3; i++) vector[vOffset+i] = res[i+1];
        vector[vOffset+3] = 1f;
    }


    static public void rotation3M(float[] matrix, int mOffset, float[] quaternion, int qOffset){
        float xx = quaternion[qOffset+1]*quaternion[qOffset+1];
        float yy = quaternion[qOffset+2]*quaternion[qOffset+2];
        float zz = quaternion[qOffset+3]*quaternion[qOffset+3];
        float xy = quaternion[qOffset+1]*quaternion[qOffset+2];
        float wz = quaternion[ qOffset ]*quaternion[qOffset+3];
        float xz = quaternion[qOffset+1]*quaternion[qOffset+3];
        float wy = quaternion[ qOffset ]*quaternion[qOffset+2];
        float yz = quaternion[qOffset+2]*quaternion[qOffset+3];
        float wx = quaternion[ qOffset ]*quaternion[qOffset+1];

        // Column 0
        matrix[ mOffset ] = 1-2*(yy+zz);
        matrix[mOffset+1] = 2*(xy+wz);
        matrix[mOffset+2] = 2*(xz-wy);

        // Column 1
        matrix[mOffset+3] = 2*(xy-wz);
        matrix[mOffset+4] = 1-2*(xx+zz);
        matrix[mOffset+5] = 2*(yz+wx);

        // Column 2
        matrix[mOffset+6] = 2*(xz+wy);
        matrix[mOffset+7] = 2*(yz-wx);
        matrix[mOffset+8] = 1-2*(xx+yy);
    }


    static public void rotationM(float[] matrix, int mOffset, float[] quaternion, int qOffset){
        float xx = quaternion[qOffset+1]*quaternion[qOffset+1];
        float yy = quaternion[qOffset+2]*quaternion[qOffset+2];
        float zz = quaternion[qOffset+3]*quaternion[qOffset+3];
        float xy = quaternion[qOffset+1]*quaternion[qOffset+2];
        float wz = quaternion[ qOffset ]*quaternion[qOffset+3];
        float xz = quaternion[qOffset+1]*quaternion[qOffset+3];
        float wy = quaternion[ qOffset ]*quaternion[qOffset+2];
        float yz = quaternion[qOffset+2]*quaternion[qOffset+3];
        float wx = quaternion[ qOffset ]*quaternion[qOffset+1];

        // Column 0
        matrix[ mOffset ] = 1-2*(yy+zz);
        matrix[mOffset+1] = 2*(xy+wz);
        matrix[mOffset+2] = 2*(xz-wy);
        matrix[mOffset+3] = 0f;

        // Column 1
        matrix[mOffset+4] = 2*(xy-wz);
        matrix[mOffset+5] = 1-2*(xx+zz);
        matrix[mOffset+6] = 2*(yz+wx);
        matrix[mOffset+7] = 0f;

        // Column 2
        matrix[mOffset+8] = 2*(xz+wy);
        matrix[mOffset+9] = 2*(yz-wx);
        matrix[mOffset+10]= 1-2*(xx+yy);
        matrix[mOffset+11]= 0f;

        // Column 3
        matrix[mOffset+12]= 0f;
        matrix[mOffset+13]= 0f;
        matrix[mOffset+14]= 0f;
        matrix[mOffset+15]= 1f;
    }


    static public float toAngleAxis(float[] axis, int aOffset, float[] quaternion, int qOffset){
        float angle = (float) Math.toDegrees(Math.acos(quaternion[qOffset])*2);
        float sqrt = (float) Math.sqrt(1-quaternion[qOffset]*quaternion[qOffset]);

        axis[ aOffset ] = quaternion[qOffset+1]/sqrt;
        axis[ aOffset ] = quaternion[qOffset+2]/sqrt;
        axis[ aOffset ] = quaternion[qOffset+3]/sqrt;

        return angle;
    }


    static public void slerp(float[] resQ, int rqOffset, float[] originQ, int oqOffset, float[] destinationQ, int dqOffset, float step){
        float[] tmpQ = new float[4];
        float[] invertedOriginQ = new float[4];
        invert(invertedOriginQ, 0, originQ, oqOffset);

        multiplyQQ(tmpQ,0, destinationQ, dqOffset, invertedOriginQ,0);
        scaleQuaternionIn(tmpQ,0, tmpQ,0, step);

        multiplyQQ(resQ, rqOffset, tmpQ,0, originQ, oqOffset);
    }


    static public void multiplyQQ(float[] resQuaternion, int resOffset, float[] leftQuaternion, int leftOffset, float[] rightQuaternion, int rightOffset) {
        float lQw = leftQuaternion[leftOffset], lQx = leftQuaternion[leftOffset+1], lQy = leftQuaternion[leftOffset+2], lQz = leftQuaternion[leftOffset+3];
        float rQw = rightQuaternion[rightOffset], rQx = rightQuaternion[rightOffset+1], rQy = rightQuaternion[rightOffset+2], rQz = rightQuaternion[rightOffset+3];

        resQuaternion[ resOffset ] = lQw*rQw - lQx*rQx - lQy*rQy - lQz*rQz;
        resQuaternion[resOffset+1] = lQx*rQw + lQw*rQx - lQz*rQy + lQy*rQz;
        resQuaternion[resOffset+2] = lQy*rQw + lQz*rQx + lQw*rQy - lQx*rQz;
        resQuaternion[resOffset+3] = lQz*rQw - lQy*rQx + lQx*rQy + lQw*rQz;
    }


    static public void scaleQuaternionIn(float[] resQ, int rqOffset, float[] quaternion, int qOffset, float step) {
        float[] axis = new float[3];
        float angle = toAngleAxis(axis,0, quaternion, qOffset);

        fromAngleAndAxis(resQ, rqOffset, angle*step, axis[0], axis[1], axis[2]);
    }


    static public void test() {
        float[] q1 = new float[4];
        float[] q2 = new float[4];
        float[] q3 = new float[4];
        float[] identity = new float[4];

        Quaternion.fromAngleAndAxis(q1,0,90,0,1,0);
        Quaternion.fromAngleAndAxis(q2,0,45,1,0,0);
        Quaternion.multiplyQQ(q3,0, q2,0, q1,0);

        String output = "";

        float[] axis = new float[3];
        float angle = Quaternion.toAngleAxis(axis,0, q1,0);
        output += "q1: [ "+angle+", "+axis[0]+", "+axis[1]+", "+axis[2]+" ]\n";

        angle = Quaternion.toAngleAxis(axis,0, q2,0);
        output += "q2: [ "+angle+", "+axis[0]+", "+axis[1]+", "+axis[2]+" ]\n";

        output += "Quaternion 1: [ "+q1[0]+", "+q1[1]+", "+q1[2]+", "+q1[3]+" ]\n";
        output += "Quaternion 2: [ "+q2[0]+", "+q2[1]+", "+q2[2]+", "+q2[3]+" ]\n";
        output += "Quaternion 3: [ "+q3[0]+", "+q3[1]+", "+q3[2]+", "+q3[3]+" ]\n";

        float[] v0 = {1, 0, 0};
        Quaternion.rotate3V(v0,0, q3,0);
        output += "Rotation [1,0,0] with q3: [ "+v0[0]+", "+v0[1]+", "+v0[2]+" ]\n";

        float[] v1 = {1, 0, 0};
        Quaternion.rotate3V(v1,0, q1,0);
        output += "Rotation [1,0,0] with q1: [ "+v1[0]+", "+v1[1]+", "+v1[2]+" ]\n";

        float[] v2 = {0, 0, -1};
        Quaternion.rotate3V(v2,0, q2,0);
        output += "Rotation [0,0,-1] with q2: [ "+v2[0]+", "+v2[1]+", "+v2[2]+" ]\n";

        Quaternion.setIdentityQ(identity,0);
        output += "No rotation Quaternion: [ "+identity[0]+", "+identity[1]+", "+identity[2]+", "+identity[3]+" ]";

        Log.e("QUATERNION_TEST", output);
    }
}
