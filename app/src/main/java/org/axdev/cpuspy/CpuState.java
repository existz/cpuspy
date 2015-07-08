package org.axdev.cpuspy;

import android.support.annotation.NonNull;

/**
 * simple struct for states/time
 */
public class CpuState implements Comparable<CpuState> {
    /** init with freq and duration */
    public CpuState(int a, long b) { freq = a; duration = b; }
    public int freq = 0;
    public long duration = 0;
    /** for sorting, compare the freqs */
    public int compareTo(@NonNull CpuState state) {
        Integer a = freq;
        Integer b = state.freq;
        return a.compareTo(b);
    }
}