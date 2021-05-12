package com.udec_biomed.aBrainVis.Tools;

//import java.nio.Buffer;

import java.nio.FloatBuffer;

public class Histogram {
    static public int[] HistogramFromFloatBuffer(FloatBuffer data, int bins) {
        int[] resultHistogram = new int[bins];

        for (int i=0; i<resultHistogram.length; i++) resultHistogram[i] = 0;

        float[] array = data.array();
        double l = array[0], u = array[0];

        for (float e : array) {
            if (e<l) l = e;
            else if(e>u) u = e;
        }

        float n = (float)(bins*(1-0.0000000001));

        for (float x : array) resultHistogram[(int)(n*(x-l)/(u-l))] += 1;

        return resultHistogram;
    }


    static public int[] HistogramFromFloatArray(float[] data, int bins) {
        int[] tmpHistogram = new int[bins+1];
        int[] resultHistogram = new int[bins];

        for (int i=0; i<tmpHistogram.length; i++) tmpHistogram[i] = 0;

        float l = data[0], u = data[0];

        for (float e : data) {
            if (e<l) l = e;
            else if(e>u) u = e;
        }

        for (float x : data) tmpHistogram[(int)(bins*(x-l)/(u-l))] += 1;
        for (int i=0; i<bins; i++) resultHistogram[i] = tmpHistogram[i];
        resultHistogram[bins-1] += tmpHistogram[bins];

        return resultHistogram;
    }
}
