/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.axdev.cpuspy.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.Spanned;
import android.text.format.Formatter;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;
import com.jaredrummler.android.processes.models.Statm;
import com.jaredrummler.android.processes.models.Status;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.HtmlBuilder;
import org.axdev.cpuspy.utils.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProcessInfoDialog extends DialogFragment {

    private Context mContext;
    private static final String TAG = "ProcessInfoDialog";

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        AndroidAppProcess process = getArguments().getParcelable("process");
        return new MaterialDialog.Builder(mContext)
                .title(Utils.getPackageName(mContext, process))
                .content(getProcessInfo(process))
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private Spanned getProcessInfo(AndroidAppProcess process) {
        final HtmlBuilder html = new HtmlBuilder();
        final Resources res = getResources();

        html.p().strong(res.getString(R.string.process_name_header) + " ").append(process.name).close();
        html.p().strong(res.getString(R.string.process_policy_header) + " ").append(process.foreground ? "fg" : "bg").close();
        html.p().strong(res.getString(R.string.process_pid_header) + " ").append(process.pid).close();

        try {
            Status status = process.status();
            html.p().strong(res.getString(R.string.process_uid_header) + " ").append(status.getUid()).append('/').append(status.getGid()).close();
        } catch (IOException e) {
            Log.d(TAG, String.format("Error reading /proc/%d/status.", process.pid));
        }

        try {
            Stat stat = process.stat();
            html.p().strong(res.getString(R.string.process_ppid_header) + " ").append(stat.ppid()).close();
            long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            long startTime = bootTime + (10 * stat.starttime());
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a", Locale.getDefault());
            html.p().strong(res.getString(R.string.process_started_header) + " ").append(sdf.format(startTime)).close();
            html.p().strong(res.getString(R.string.process_cputime_header) + " ").append((stat.stime() + stat.utime()) / 100).close();
            html.p().strong(res.getString(R.string.process_nice_header) + " ").append(stat.nice()).close();
            int rtPriority = stat.rt_priority();
            if (rtPriority == 0) {
                html.p().strong(res.getString(R.string.process_scheduling_header) + " ").append("non-real-time").close();
            } else if (rtPriority >= 1 && rtPriority <= 99) {
                html.p().strong(res.getString(R.string.process_scheduling_header) + " ").append("real-time").close();
            }
            long userModeTicks = stat.utime();
            long kernelModeTicks = stat.stime();
            long percentOfTimeUserMode;
            long percentOfTimeKernelMode;
            if ((kernelModeTicks + userModeTicks) > 0) {
                percentOfTimeUserMode = (userModeTicks * 100) / (userModeTicks + kernelModeTicks);
                percentOfTimeKernelMode = (kernelModeTicks * 100) / (userModeTicks + kernelModeTicks);
                html.p().strong(res.getString(R.string.process_usermode_header) + " ").append(percentOfTimeUserMode + "%").close();
                html.p().strong(res.getString(R.string.process_kernelmode_header) + " ").append(percentOfTimeKernelMode + "%").close();
            }
        } catch (IOException e) {
            Log.d(TAG, String.format("Error reading /proc/%d/stat.", process.pid));
        }

        try {
            Statm statm = process.statm();
            html.p().strong(res.getString(R.string.process_size_header) + " ").append(Formatter.formatFileSize(mContext, statm.getSize())).close();
            html.p().strong(res.getString(R.string.process_rss_header) + " ").append(Formatter.formatFileSize(mContext, statm.getResidentSetSize())).close();
        } catch (IOException e) {
            Log.d(TAG, String.format("Error reading /proc/%d/statm.", process.pid));
        }

        return html.toSpan();
    }
}