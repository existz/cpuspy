package org.axdev.cpuspy.ui;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.axdev.cpuspy.CpuSpyApp;
import org.axdev.cpuspy.R;

public class InfoActivity extends ActionBarActivity {

    private String mFreqString;

    private final Handler mHandler = new Handler();

    private boolean mMonitorCpuFreq;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);
        CpuSpyApp _app = (CpuSpyApp) getApplicationContext();

        // set Toolbar as ActionBar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Override entering Activity animation
        overridePendingTransition(R.anim.slide_on_start_enter, R.anim.slide_on_start_exit);

        // Start monitoring CPU frequency
        mMonitorCpuFreq = true;
        mHandler.post(monitorCpuFreq);

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Medium.ttf");

        TextView mKernelHeader = (TextView) findViewById(R.id.ui_kernel_header);
        mKernelHeader.setTypeface(tf);

        TextView mKernelString = (TextView) findViewById(R.id.ui_kernel_string);
        mKernelString.setText(_app.getKernelVersion());

        TextView mCpuHeader = (TextView) findViewById(R.id.ui_cpu_header);
        mCpuHeader.setTypeface(tf);

        TextView mCpuInfo = (TextView) findViewById(R.id.ui_cpu_info);
        mCpuInfo.setText(getCpuInfo());
    }

    private String getCpuFrequency() {
        try {
            InputStream is = new FileInputStream("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mFreqString = line;
            }

            is.close();
        } catch (IOException ignored) {}

        // made it
        return mFreqString;
    }

    private final Runnable monitorCpuFreq = new Runnable() {
        public void run() {
            if(mMonitorCpuFreq) {
                TextView mFreqText = (TextView) findViewById(R.id.ui_cpu_cur_freq);

                int i = Integer.parseInt(getCpuFrequency()) / 1000;
                String s = String.valueOf(i) + "MHz";
                mFreqText.setText(s);

                mHandler.postDelayed(monitorCpuFreq, 1000); // 1 second
            }
        }
    };

    @SuppressWarnings("deprecation")
    private String getCpuInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("ABI: ").append(Build.CPU_ABI).append("\n");
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(
                        new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    sb.append(aLine).append("\n");
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        mMonitorCpuFreq = false;
        // Override exiting Activity animation
        overridePendingTransition(R.anim.slide_on_stop_enter, R.anim.slide_on_stop_exit);
    }
}
