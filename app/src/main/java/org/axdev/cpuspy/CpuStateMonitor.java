//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy;

// imports
import android.util.Log;
import android.util.SparseArray;

import org.axdev.cpuspy.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    private final List<CpuState> _states = new ArrayList<>();
    private SparseArray<Long> _offsets = new SparseArray<>();

    private int MAX_TRIES = 10;

    /** exception class */
    public class CpuStateMonitorException extends Exception {

        private static final long serialVersionUID = 1L;

        public CpuStateMonitorException(String s) {
            super(s);
        }
    }

    /** @return List of CpuState with the offsets applied */
    public List<CpuState> getStates() {
        final List<CpuState> states = new ArrayList<>(_states.size());

        /* check for an existing offset, and if it's not too big, subtract it
         * from the duration, otherwise just add it to the return List */
        boolean success = false;
        int count = 0;
        while (!success && count++ < MAX_TRIES) {
            try {
                for (final CpuState state : _states) {
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
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (!success) {
            Log.e("CpuSpy", "Unable to get cpu states: retry limit reached");
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
        int count = 0;

        while (!success && count++ < MAX_TRIES) {
            try {
                for (final CpuState state : _states) {
                    sum += state.duration;
                }
                for (int i = 0; i < _offsets.size(); i++) {
                    offset += _offsets.valueAt(i);
                }
                success = true;
            } catch (ConcurrentModificationException e) {
                Log.e("CpuSpy", "Getting total state time is busy, retrying..");
                return getTotalStateTime();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        if (!success) {
            Log.e("CpuSpy", "Unable to get total state time: retry limit reached");
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
        int count = 0;
        while (!success && count++ < MAX_TRIES) {
            try {
                _offsets.clear();
                updateStates();

                for (final CpuState state : _states) {
                    _offsets.put(state.freq, state.duration);
                }
                success = true;
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }
        }
        if (!success) {
            throw new CpuStateMonitorException("Problem resetting timers: retry limit reached");
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
    public void updateStates() throws CpuStateMonitorException {
        /* attempt to create a buffered reader to the time in state
         * file and read in the states to the class */
        boolean success = false;
        int count = 0;
        while (!success && count++ < MAX_TRIES) {
            try {
                final String TIME_IN_STATE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
                final File file = new File(TIME_IN_STATE_PATH);
                if (file.canRead()) {
                    final BufferedReader br = new BufferedReader(new FileReader(file));
                    _states.clear();
                    readInStates(br);
                    br.close();
                } else {
                    Log.e("CpuSpy", "Unable to read file: " + TIME_IN_STATE_PATH);
                }


                /* deep sleep time determined by difference between elapsed
                 * (total) boot time and the system uptime (awake)
                 */
                final long sleepTime = Utils.getDeepSleep();
                _states.add(new CpuState(0, sleepTime));

                Collections.sort(_states, Collections.reverseOrder());
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new CpuStateMonitorException("Problem opening time-in-states file");
            }
        }
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
                final String[] nums = line.split(" ");
                if (nums.length > 0) {
                    _states.add(new CpuState(Integer.parseInt(nums[0]), Long.parseLong(nums[1])));
                }
            }
        } catch (Exception e) {
            throw new CpuStateMonitorException(
                    "Problem processing time-in-states file");
        }
    }
}
