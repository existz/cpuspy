//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy;

// imports

import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * CpuStateMonitor is a class responsible for querying the system and getting
 * the time-in-state information, as well as allowing the user to set/reset
 * offsets to "restart" the state timers
 */
public class CpuStateMonitor {

    private static final String TIME_IN_STATE_PATH =
            "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";

    private final List<CpuState>      _states = new ArrayList<>();
    private SparseArray<Long>  _offsets = new SparseArray<>();

    /** exception class */
    public class CpuStateMonitorException extends Exception {
        public CpuStateMonitorException(String s) {
            super(s);
        }
    }

    /** @return List of CpuState with the offsets applied */
    public List<CpuState> getStates() {
        List<CpuState> states = new ArrayList<>();

        /* check for an existing offset, and if it's not too big, subtract it
         * from the duration, otherwise just add it to the return List */
        boolean success = false;
        int count = 0, MAX_TRIES = 10;
        while (!success && count++ < MAX_TRIES) {
            try {
                for (CpuState state : _states) {
                    long duration = state.duration;
                    final Long value = _offsets.get(state.freq);
                    if (value != null) {
                        long offset = value;
                        if (offset <= duration) {
                            duration -= offset;
                        } else {
                    /* offset > duration implies our offsets are now invalid,
                     * so clear and recall this function */
                            _offsets.clear();
                            return getStates();
                        }
                    }
                    states.add(new CpuState(state.freq, duration));
                }
                success = true;
            } catch (ConcurrentModificationException e) {
                Log.e("CpuSpy", "Getting cpu states is busy, retrying..");
                return getStates();
            }
        }
        if (!success) {
            Log.e("CpuSpy", "Unable to get cpu states");
        }

        return states;
    }

    /**
     * @return Sum of all state durations including deep sleep, accounting
     * for offsets
     */
    public long getTotalStateTime() {
        long sum = 0;
        long offset = 0;
        boolean success = false;
        int count = 0, MAX_TRIES = 10;

        while (!success && count++ < MAX_TRIES) {
            try {
                for (CpuState state : _states) {
                    sum += state.duration;
                }
                for(int i = 0; i < _offsets.size(); i++) {
                    offset += _offsets.valueAt(i);
                }
                success = true;
            } catch (ConcurrentModificationException e) {
                Log.e("CpuSpy", "Getting total state time is busy, retrying..");
                return getTotalStateTime();
            }
        }
        if (!success) {
            Log.e("CpuSpy", "Unable to get total state time");
        }

        return sum - offset;
    }

    /**
     * @return Map of freq->duration of all the offsets
     */
    public SparseArray<Long> getOffsets() {
        return _offsets;
    }

    /** Sets the offset map (freq->duration offset) */
    public void setOffsets(SparseArray<Long> offsets) {
        _offsets = offsets;
    }

    /**
     * Updates the current time in states and then sets the offset map to the
     * current duration, effectively "zeroing out" the timers
     */
    public void setOffsets() throws CpuStateMonitorException {
        boolean success = false;
        int count = 0, MAX_TRIES = 10;
        while (!success && count++ < MAX_TRIES) {
            try {
                _offsets.clear();
                updateStates();

                for (CpuState state : _states) {
                    _offsets.put(state.freq, state.duration);
                }
                success = true;
            } catch (ConcurrentModificationException ignored) {}
        }
        if (!success) {
            throw new CpuStateMonitorException("Problem resetting timers");
        }
    }

    /** removes state offsets */
    public void removeOffsets() {
        _offsets.clear();
    }

    /**
     * @return a list of all the CPU frequency states, which contains
     * both a frequency and a duration (time spent in that state
     */
    public List<CpuState> updateStates()
            throws CpuStateMonitorException {
        /* attempt to create a buffered reader to the time in state
         * file and read in the states to the class */
        try {
            final File file = new File(TIME_IN_STATE_PATH);
            if (file.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(file));
                _states.clear();
                readInStates(br);
                br.close();
            }
        } catch (IOException e) {
            throw new CpuStateMonitorException(
                    "Problem opening time-in-states file");
        }

        /* deep sleep time determined by difference between elapsed
         * (total) boot time and the system uptime (awake) */
        long sleepTime = (SystemClock.elapsedRealtime()
                - SystemClock.uptimeMillis()) / 10;
        _states.add(new CpuState(0, sleepTime));

        Collections.sort(_states, Collections.reverseOrder());

        return _states;
    }

    /** read from a provided BufferedReader the state lines into the
     * States member field
     */
    private void readInStates(BufferedReader br)
            throws CpuStateMonitorException {
        try {
            String line;
            while ((line = br.readLine()) != null) {
                // split open line and convert to Integers
                String[] nums = line.split(" ");
                _states.add(new CpuState(
                        Integer.parseInt(nums[0]),
                        Long.parseLong(nums[1])));
            }
        } catch (IOException e) {
            throw new CpuStateMonitorException(
                    "Problem processing time-in-states file");
        }
    }
}
