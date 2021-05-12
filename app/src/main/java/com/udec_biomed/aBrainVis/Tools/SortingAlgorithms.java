package com.udec_biomed.aBrainVis.Tools;

public class SortingAlgorithms {
    public static int QuickSortPartition(float[] a, int begin, int end) {
        int pivot = begin-1;
        float threshold = a[end-1], swapTmp;

        for (int i=begin; i<end-1; i++)
            if (a[i] <= threshold) {
                pivot++;
                swapTmp = a[pivot];
                a[pivot] = a[i];
                a[i] = swapTmp;
            }

        swapTmp = a[pivot+1];
        a[pivot+1] = threshold;
        a[end-1] = swapTmp;

        return pivot+1;
    }


    public static void QuickSort(float[] a, int begin, int end) {
        if (begin<end) {
            int pivot = QuickSortPartition(a, begin, end);

            QuickSort(a, begin, pivot);
            QuickSort(a, pivot+1, end);
        }
    }


    public static int QuickSortPartitionIndexes(float[] a, int begin, int end, int[] indexes) {
        int pivot = begin-1;
        float threshold = a[indexes[end-1]];
        int swapTmp;

        for (int i=begin; i<end-1; i++)
            if (a[indexes[i]] <= threshold) {
                pivot++;
                swapTmp = indexes[pivot];
                indexes[pivot] = indexes[i];
                indexes[i] = swapTmp;
            }

        swapTmp = indexes[pivot+1];
        indexes[pivot+1] = indexes[end-1];
        indexes[end-1] = swapTmp;

        return pivot+1;
    }


    public static void QuickSortIndexes(float[] a, int begin, int end, int[] indexes) {
        if (begin<end) {
            int pivot = QuickSortPartitionIndexes(a, begin, end, indexes);

            QuickSortIndexes(a, begin, pivot, indexes);
            QuickSortIndexes(a,pivot+1, end, indexes);
        }
    }


    public static int[] ArgQuickSort(float[] a, int begin, int end) {
        int[] argSorted = new int[end-begin];
        for (int i=begin; i<end; i++)
            argSorted[i-begin] = i+begin;

        QuickSortIndexes(a, begin, end, argSorted);
        return argSorted;
    }



    public static void ArgQuickSort(float[] a, int begin, int end, int[] argToBeSorted) {
        for (int i=begin; i<end; i++)
            argToBeSorted[i-begin] = i+begin;

        QuickSortIndexes(a, begin, end, argToBeSorted);
    }
}
