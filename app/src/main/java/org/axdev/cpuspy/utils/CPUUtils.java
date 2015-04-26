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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CPUUtils {

    public static final String CPU0 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String CPU1 = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq";
    public static final String CPU2 = "/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq";
    public static final String CPU3 = "/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq";

    private static String TEMP_FILE;
    private static final String CPU_TEMP_ZONE0 = "/sys/class/thermal/thermal_zone0/temp";
    private static final String CPU_TEMP_ZONE1 = "/sys/class/thermal/thermal_zone1/temp";

    private static final String TAG = "CPUSpy";
    private static final String TAG_INFO = "CPUSpy";

    private static String mArch;
    private static String mFeatures;
    private static String mGovernor;
    private static String mMinFreq;
    private static String mMaxFreq;
    private static String mFreq0;
    private static String mFreq1;
    private static String mFreq2;
    private static String mFreq3;
    private static String mTemp;

    /** Get the current frequency for CPU0 */
    private static String setCpu0() {
        try {
            final File cpu0 = new File(CPU0);
            if (cpu0.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(cpu0));

                String line;
                while ((line = br.readLine()) != null) {
                    mFreq0 = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to read CPU0");
        }

        final int i = Integer.parseInt(mFreq0) / 1000;
        mFreq0 = String.valueOf(i);

        return mFreq0;
    }

    /** Get the current frequency for CPU1 */
    private static String setCpu1() {
        try {
            final File cpu1 = new File(CPU1);
            if (cpu1.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(cpu1));

                String line;
                while ((line = br.readLine()) != null) {
                    mFreq1 = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to read CPU1");
        }

        final int i = Integer.parseInt(mFreq1) / 1000;
        mFreq1 = String.valueOf(i);

        return mFreq1;
    }

    /** Get the current frequency for CPU2 */
    private static String setCpu2() {
        try {
            final File cpu2 = new File(CPU2);
            if (cpu2.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(cpu2));

                String line;
                while ((line = br.readLine()) != null) {
                    mFreq2 = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to read CPU2");
        }

        // made it
        final int i = Integer.parseInt(mFreq2) / 1000;
        mFreq2 = String.valueOf(i);

        return mFreq2;
    }

    /** Get the current frequency for CPU3 */
    private static String setCpu3() {
        try {
            final File cpu3 = new File(CPU3);
            if (cpu3.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(cpu3));

                String line;
                while ((line = br.readLine()) != null) {
                    mFreq3 = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to read CPU3");
        }

        final int i = Integer.parseInt(mFreq3) / 1000;
        mFreq3 = String.valueOf(i);

        return mFreq3;
    }

    /** Get the current cpu governor */
    private static String setGovernor() {
        final String GOVERNOR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
        try {
            final File governor = new File(GOVERNOR);
            if (governor.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(governor));

                String line;
                while ((line = br.readLine()) != null) {
                    mGovernor = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, "Unable to read cpu governor");
        }

        return mGovernor;
    }

    /** Get the current minimum frequency */
    private static String setMinFreq() {
        final String MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
        try {
            final File min_freq = new File(MIN_FREQ);
            if (min_freq.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(min_freq));

                String line;
                while ((line = br.readLine()) != null) {
                    mMinFreq = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, "Unable to read min cpu frequency");
        }

        final int i = Integer.parseInt(mMinFreq) / 1000;
        mMinFreq = Integer.toString(i) + "MHz";

        return mMinFreq;
    }

    /** Get the current maximum frequency */
    private static String setMaxFreq() {
        final String MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
        try {
            final File max_freq = new File(MAX_FREQ);
            if (max_freq.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(max_freq));

                String line;
                while ((line = br.readLine()) != null) {
                    mMaxFreq = line;
                }

                br.close();
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, "Unable to read max cpu frequency");
        }

        final int i = Integer.parseInt(mMaxFreq) / 1000;
        mMaxFreq = Integer.toString(i) + "MHz";

        return mMaxFreq;
    }

    /** Retrieves information for ARM CPUs. */
    private static String setFeatures() {
        try {
            final File info = new File("/proc/cpuinfo");
            if (info.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(info));

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
            File info = new File("/proc/cpuinfo");
            if (info.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(info));

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
            final File temp = new File(TEMP_FILE);
            if (temp.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(temp));

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

    public static boolean hasTemp() {
        final File temp0 = new File(CPU_TEMP_ZONE0);
        final File temp1 = new File(CPU_TEMP_ZONE1);

        if (temp0.exists()) {
            TEMP_FILE = CPU_TEMP_ZONE1;
        } else if (temp1.exists()) {
            TEMP_FILE = CPU_TEMP_ZONE0;
        }
        return TEMP_FILE != null;
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

    /** @return CPU temperature string */
    public static String getTemp() {
        return setTemp();
    }

    /** @return Number of CPU cores */
    public static int getCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }
}