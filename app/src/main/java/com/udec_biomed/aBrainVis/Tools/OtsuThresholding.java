package com.udec_biomed.aBrainVis.Tools;

import java.nio.FloatBuffer;

public class OtsuThresholding {
    static public float OtsuThreholdWithFloatBuffer(FloatBuffer data, int bins) {
        // based in https://en.wikipedia.org/wiki/Otsu%27s_method
        int[] histogram = Histogram.HistogramFromFloatBuffer(data, bins);
        float[] array = data.array();

        float dataMin = array[0], dataMax = array[0];

        for (float e : array) {
            if (e<dataMin) dataMin = e;
            else if (e>dataMax) dataMax = e;
        }

        int wB = 0;
        int wF = 0;
        int wT = array.length;

        long sumB = 0;
        long sumT = 0;
        for (int i=0; i<histogram.length; i++) sumT += i*histogram[i];

        double mB, mF;

        double maxVar = 0f, var;
        int thr = 0;

        for (int i=0; i<bins; i++) {
            wF = wT-wB;
            if (wB>0 && wF>0) {
                mB = sumB/(double)wB;
                mF = (sumT-sumB)/(double)wF;

                var = (double)wB * wF * (mB-mF)*(mB-mF);

                if (var>maxVar) {
                    maxVar = var;
                    thr = i;
                }
            }

            wB += histogram[i];
            sumB += i*histogram[i];
        }

        float threshold = (thr-0.5f)/bins * (dataMax-dataMin) + dataMin;
        return threshold;
    }


    static public float OtsuThreholdWithFloatArray(float[] data, int bins) {
        // based in https://en.wikipedia.org/wiki/Otsu%27s_method
        int[] histogram = Histogram.HistogramFromFloatArray(data, bins);

        float dataMin = data[0], dataMax = data[0];

        for (float e : data) {
            if (e<dataMin) dataMin = e;
            else if (e>dataMax) dataMax = e;
        }

        int wB = 0;
        int wF = 0;
        int wT = data.length;

        long sumB = 0;
        long sumT = 0;
        for (int i=0; i<histogram.length; i++) sumT += i*histogram[i];

        double mB, mF;

        double maxVar = 0f, var;
        int thr = 0;

        for (int i=0; i<bins; i++) {
            wF = wT-wB;
            if (wB>0 && wF>0) {
                mB = sumB/(double)wB;
                mF = (sumT-sumB)/(double)wF;

                var = (double)wB * wF * (mB-mF)*(mB-mF);

                if (var>maxVar) {
                    maxVar = var;
                    thr = i;
                }
            }

            wB += histogram[i];
            sumB += i*histogram[i];
        }

        float threshold = (thr-0.5f)/bins * (dataMax-dataMin) + dataMin;
        return threshold;
    }
}
