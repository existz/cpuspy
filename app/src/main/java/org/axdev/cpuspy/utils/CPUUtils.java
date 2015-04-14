//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.utils;

import android.util.Log;

import org.axdev.cpuspy.ui.HomeActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CPUUtils {

    public static final String CPU0 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String CPU1 = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq";
    public static final String CPU2 = "/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq";
    public static final String CPU3 = "/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq";

    private static final String MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
    private static final String MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
    private static final String GOVERNOR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";

    private static final String TAG = "CPUSpyInfo";

    private static String mArch;
    private static String mFeatures;
    private static String mGovernor;
    private static String mMinFreq;
    private static String mMaxFreq;
    private static String mFreq0;
    private static String mFreq1;
    private static String mFreq2;
    private static String mFreq3;

    /** Get the current frequency for CPU0 */
    private static String setCpu0() {
        try {
            InputStream is = new FileInputStream(CPU0);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mFreq0 = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e(HomeActivity.TAG, "Unable to read CPU0");
        }

        int i = Integer.parseInt(mFreq0) / 1000;
        mFreq0 = String.valueOf(i);

        return mFreq0;
    }

    /** Get the current frequency for CPU1 */
    private static String setCpu1() {
        try {
            InputStream is = new FileInputStream(CPU1);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mFreq1 = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e(HomeActivity.TAG, "Unable to read CPU1");
        }

        int i = Integer.parseInt(mFreq1) / 1000;
        mFreq1 = String.valueOf(i);

        return mFreq1;
    }

    /** Get the current frequency for CPU2 */
    private static String setCpu2() {
        try {
            InputStream is = new FileInputStream(CPU2);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mFreq2 = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e(HomeActivity.TAG, "Unable to read CPU2");
        }

        // made it
        int i = Integer.parseInt(mFreq2) / 1000;
        mFreq2 = String.valueOf(i);

        return mFreq2;
    }

    /** Get the current frequency for CPU3 */
    private static String setCpu3() {
        try {
            InputStream is = new FileInputStream(CPU3);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mFreq3 = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e(HomeActivity.TAG, "Unable to read CPU3");
        }

        int i = Integer.parseInt(mFreq3) / 1000;
        mFreq3 = String.valueOf(i);

        return mFreq3;
    }

    /** Get the current cpu governor */
    private static String setGovernor() {
        try {
            InputStream is = new FileInputStream(GOVERNOR);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mGovernor = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Unable to read cpu governor");
        }

        return mGovernor;
    }

    /** Get the current minimum frequency */
    private static String setMinFreq() {
        try {
            InputStream is = new FileInputStream(MIN_FREQ);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mMinFreq = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Unable to read min cpu frequency");
        }

        int i = Integer.parseInt(mMinFreq) / 1000;
        mMinFreq = Integer.toString(i) + "MHz";

        return mMinFreq;
    }

    /** Get the current maximum frequency */
    private static String setMaxFreq() {
        try {
            InputStream is = new FileInputStream(MAX_FREQ);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mMaxFreq = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Unable to read max cpu frequency");
        }

        int i = Integer.parseInt(mMaxFreq) / 1000;
        mMaxFreq = Integer.toString(i) + "MHz";

        return mMaxFreq;
    }

    /** Retrieves information for ARM CPUs. */
    private static String setFeatures() {
        try {
            File info = new File("/proc/cpuinfo");
            if (info.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(info));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Features\t:")) {
                        mFeatures = parseLine(line);
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to read cpu features");
        }

        return mFeatures;
    }

    private static String setArch() {
        try {
            File info = new File("/proc/cpuinfo");
            if (info.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(info));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Processor\t:")) {
                        mArch = parseLine(line);
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to read cpu architecture");
        }

        return mArch;
    }

    // Basic function for parsing cpuinfo format strings.
    // cpuinfo format strings consist of [label:info] parts.
    // We only want to retrieve the info portion so we split
    // them using ':' as a delimeter.
    private static String parseLine(String line)  {
        String[] temp = line.split(":");
        if (temp.length != 2)
            return "N/A";

        return temp[1].trim();
    }

    /**
     * Returns a SystemProperty
     *
     * @param propName The Property to retrieve
     * @return The Property, or NULL if not found
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop " + propName, ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    /** @return the CPU0 string */
    public static String getCpu0() {
        return setCpu0();
    }

    /** @return the CPU1 string */
    public static String getCpu1() {
        return setCpu1();
    }

    /** @return the CPU2 string */
    public static String getCpu2() {
        return setCpu2();
    }

    /** @return the CPU3 string */
    public static String getCpu3() {
        return setCpu3();
    }

    /** @return CPU governor string */
    public static String getGovernor() {
        return setGovernor();
    }

    /** @return CPU min/max frequency string */
    public static String getMinMax() {
        return setMinFreq() + " - " + setMaxFreq();
    }

    /** @return CPU features string */
    public static String getFeatures() {
        return setFeatures();
    }

    /** @return CPU architecture string */
    public static String getArch() {
        return setArch();
    }
}