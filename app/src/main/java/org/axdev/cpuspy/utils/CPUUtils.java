//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CPUUtils {

    public static final String CPU0 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String CPU1 = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq";
    public static final String CPU2 = "/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq";
    public static final String CPU3 = "/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq";
    public static final String CPU4 = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_cur_freq";
    public static final String CPU5 = "/sys/devices/system/cpu/cpu5/cpufreq/scaling_cur_freq";
    public static final String CPU6 = "/sys/devices/system/cpu/cpu6/cpufreq/scaling_cur_freq";
    public static final String CPU7 = "/sys/devices/system/cpu/cpu7/cpufreq/scaling_cur_freq";

    private static final String TAG_INFO = "CpuSpyInfo";
    private static String mArch;
    private static String mFeatures;
    private static String mFreq;
    private static String mString;
    private static String mTemp;
    private static String mTempFile;

    private static String readFile(String PATH, String LOG_MSG) {
        try {
            final File file = new File(PATH);
            if (file.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null) {
                    mString = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, LOG_MSG);
        }

        return mString;
    }

    /** Set the current min/max CPU frequency */
    private static String setFreq(String PATH, String LOG_MSG) {
        try {
            final File file = new File(PATH);
            if (file.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null) {
                    mFreq = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, LOG_MSG);
        }

        final int i = Integer.parseInt(mFreq) / 1000;
        mFreq = Integer.toString(i) + "MHz";

        return mFreq;
    }

    /** Retrieves information for ARM CPUs. */
    private static String setFeatures() {
        try {
            final File file = new File("/proc/cpuinfo");
            if (file.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Features\t:")) {
                        mFeatures = parseLine(line);
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, "Unable to read cpu features");
        }

        return mFeatures;
    }

    private static String setArch() {
        try {
            File file = new File("/proc/cpuinfo");
            if (file.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Processor\t:")) {
                        mArch = parseLine(line);
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, "Unable to read cpu architecture");
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

    /** Retrieves the current CPU temperature */
    private static String setTemp() {
        try {
            final File file = new File(mTempFile);
            if (file.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null) {
                    mTemp = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, "Unable to read cpu temperature");
        }

        long temp = Long.parseLong(mTemp);
        if (temp > 1000) temp /= 1000;
        else if (temp > 200) temp /= 10;
        return ((double) temp) + "°C";
    }

    private static String[] tempFiles = {
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/kernel/debug/tegra_thermal/temp_tj",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone1/temp",
            "/sys/devices/system/cpu/cpufreq/cput_attributes/cur_temp",
            "/sys/devices/platform/s5p-tmu/curr_temp",
            "/sys/devices/platform/s5p-tmu/temperature",
    };

    public static boolean hasTemp() {
        for (String s : tempFiles) {
            final File file = new File(s);
            if (file.canRead()) mTempFile = s;
        }

        return mTempFile != null;
    }

    /**
     * Returns a SystemProperty
     *
     * @param propName The Property to retrieve
     * @return The Property, or NULL if not found
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = br.readLine();
            br.close();
        }
        catch (IOException ex) {
            Log.e(TAG_INFO, "Unable to read sysprop " + propName, ex);
            return null;
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                    Log.e(TAG_INFO, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    /** @return the CPU0 string */
    public static String getCpu0() {
        return setFreq(CPU0, "Unable to read CPU0 frequency");
    }

    /** @return the CPU1 string */
    public static String getCpu1() {
        return setFreq(CPU1, "Unable to read CPU1 frequency");
    }

    /** @return the CPU2 string */
    public static String getCpu2() {
        return setFreq(CPU2, "Unable to read CPU2 frequency");
    }

    /** @return the CPU3 string */
    public static String getCpu3() {
        return setFreq(CPU3, "Unable to read CPU3 frequency");
    }

    /** @return the CPU4 string */
    public static String getCpu4() {
        return setFreq(CPU4, "Unable to read CPU4 frequency");
    }

    /** @return the CPU5 string */
    public static String getCpu5() {
        return setFreq(CPU5, "Unable to read CPU5 frequency");
    }

    /** @return the CPU6 string */
    public static String getCpu6() {
        return setFreq(CPU6, "Unable to read CPU6 frequency");
    }

    /** @return the CPU7 string */
    public static String getCpu7() {
        return setFreq(CPU7, "Unable to read CPU7 frequency");
    }

    /** @return CPU governor string */
    public static String getGovernor() {
        final String governor = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
        return readFile(governor, "Unable to read CPU governor");
    }

    /** @return CPU min/max frequency string */
    public static String getMinMax() {
        final String minFreq = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
        final String maxFreq = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
        return setFreq(minFreq, "Unable to read min frequency")
                + " - " + setFreq(maxFreq, "Unable to read max frequency");
    }

    /** @return CPU features string */
    public static String getFeatures() {
        return setFeatures();
    }

    /** @return the kernel version string */
    public static String getKernelVersion() {
        final String kernelVersion = "/proc/version";
        return readFile(kernelVersion, "Problem reading kernel version file");
    }

    /** @return CPU architecture string */
    public static String getArch() {
        return setArch();
    }

    /** @return CPU temperature string */
    public static String getTemp() {
        return setTemp();
    }

    /** @return Number of CPU cores */
    public static int getCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }
}