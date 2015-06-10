//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.PrefsActivity;
import org.axdev.cpuspy.utils.CPUUtils;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.ThemeUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InfoFragment extends Fragment implements OnClickListener {

    @InjectView(R.id.btn_kernel_more) ImageButton mKernelMoreButton;
    @InjectView(R.id.kernel_header) TextView mKernelHeader;
    @InjectView(R.id.kernel_governor_header) TextView mKernelGovernorHeader;
    @InjectView(R.id.kernel_governor) TextView mKernelGovernor;
    @InjectView(R.id.kernel_version_header) TextView mKernelVersionHeader;
    @InjectView(R.id.kernel_version) TextView mKernelVersion;
    @InjectView(R.id.cpu_header) TextView mCpuHeader;
    @InjectView(R.id.cpu_abi_header) TextView mCpuAbiHeader;
    @InjectView(R.id.cpu_abi) TextView mCpuAbi;
    @InjectView(R.id.cpu_arch_header) TextView mCpuArchHeader;
    @InjectView(R.id.cpu_arch) TextView mCpuArch;
    @InjectView(R.id.cpu_core_header) TextView mCpuCoreHeader;
    @InjectView(R.id.cpu_core) TextView mCpuCore;
    @InjectView(R.id.cpu_freq_header) TextView mCpuFreqHeader;
    @InjectView(R.id.cpu_freq) TextView mCpuFreq;
    @InjectView(R.id.cpu_temp_header) TextView mCpuTempHeader;
    @InjectView(R.id.cpu_temp) TextView mCpuTemp;
    @InjectView(R.id.cpu_features_header) TextView mCpuFeaturesHeader;
    @InjectView(R.id.cpu_features) TextView mCpuFeatures;
    @InjectView(R.id.device_header) TextView mDeviceInfo;
    @InjectView(R.id.device_build_header) TextView mDeviceBuildHeader;
    @InjectView(R.id.device_build) TextView mDeviceBuild;
    @InjectView(R.id.device_api_header) TextView mDeviceApiHeader;
    @InjectView(R.id.device_api) TextView mDeviceApi;
    @InjectView(R.id.device_manuf_header) TextView mDeviceManufHeader;
    @InjectView(R.id.device_manuf) TextView mDeviceManuf;
    @InjectView(R.id.device_model_header) TextView mDeviceModelHeader;
    @InjectView(R.id.device_model) TextView mDeviceModel;
    @InjectView(R.id.device_board_header) TextView mDeviceBoardHeader;
    @InjectView(R.id.device_board) TextView mDeviceBoard;
    @InjectView(R.id.device_platform_header) TextView mDevicePlatformHeader;
    @InjectView(R.id.device_platform) TextView mDevicePlatform;
    @InjectView(R.id.cpu_usage_header) TextView mCoreHeader;
    @InjectView(R.id.cpu0_header) TextView mCpu0Header;
    @InjectView(R.id.cpu1_header) TextView mCpu1Header;
    @InjectView(R.id.cpu2_header) TextView mCpu2Header;
    @InjectView(R.id.cpu3_header) TextView mCpu3Header;

    @InjectView(R.id.cpu_freq0) TextView mCore0;
    @InjectView(R.id.cpu_freq1) TextView mCore1;
    @InjectView(R.id.cpu_freq2) TextView mCore2;
    @InjectView(R.id.cpu_freq3) TextView mCore3;

    private boolean mMonitorCpu0;
    private boolean mMonitorCpu1;
    private boolean mMonitorCpu2;
    private boolean mMonitorCpu3;
    private Typeface mediumFont;

    private final Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.info_layout, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTypeface();

        /** Set color for drawables based on selected theme */
        final ColorStateList dark = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_dark));
        final ColorStateList light = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_light));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mKernelMoreButton.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
        } else {
            final Drawable kernelMoreButton = DrawableCompat.wrap(mKernelMoreButton.getDrawable());
            mKernelMoreButton.setImageDrawable(kernelMoreButton);
            DrawableCompat.setTintList(kernelMoreButton, (ThemeUtils.DARKTHEME ? dark : light));
        }

        /** Set onClickListener for kernel info button */
        mKernelMoreButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        checkTempMonitor();
        checkCoreMonitor();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void setTypeface() {
        /** Set text and typeface for TextViews */
        final String api = CPUUtils.getSystemProperty("ro.build.version.sdk");
        final String platform = CPUUtils.getSystemProperty("ro.board.platform");

        mKernelVersion.setText(System.getProperty("os.version"));
        mKernelGovernor.setText(CPUUtils.getGovernor());
        mCpuAbi.setText(Build.CPU_ABI);
        mCpuArch.setText(CPUUtils.getArch());
        mCpuCore.setText(Integer.toString(CPUUtils.getCoreCount()));
        mCpuFreq.setText(CPUUtils.getMinMax());
        mCpuFeatures.setText(CPUUtils.getFeatures());
        mDeviceBuild.setText(Build.ID);
        mDeviceApi.setText(api);
        mDeviceManuf.setText(Build.MANUFACTURER);
        mDeviceModel.setText(Build.MODEL);
        mDeviceBoard.setText(Build.BOARD);
        mDevicePlatform.setText(platform);

        // Applying Roboto-Medium font
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mediumFont = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        } else {
            this.mediumFont = TypefaceHelper.get(getActivity(), TypefaceHelper.MEDIUM_FONT);
        }

        mKernelHeader.setTypeface(mediumFont);
        mKernelGovernorHeader.setTypeface(mediumFont);
        mKernelVersionHeader.setTypeface(mediumFont);
        mCpuHeader.setTypeface(mediumFont);
        mCpuAbiHeader.setTypeface(mediumFont);
        mCpuArchHeader.setTypeface(mediumFont);
        mCpuCoreHeader.setTypeface(mediumFont);
        mCpuFreqHeader.setTypeface(mediumFont);
        mCpuFeaturesHeader.setTypeface(mediumFont);
        mDeviceInfo.setTypeface(mediumFont);
        mDeviceBuildHeader.setTypeface(mediumFont);
        mDeviceApiHeader.setTypeface(mediumFont);
        mDeviceManufHeader.setTypeface(mediumFont);
        mDeviceModelHeader.setTypeface(mediumFont);
        mDeviceBoardHeader.setTypeface(mediumFont);
        mDevicePlatformHeader.setTypeface(mediumFont);
        mCoreHeader.setTypeface(mediumFont);
        mCpu0Header.setTypeface(mediumFont);
        mCpu1Header.setTypeface(mediumFont);
        mCpu2Header.setTypeface(mediumFont);
        mCpu3Header.setTypeface(mediumFont);
    }

    /** Check if we should monitor cpu temp */
    private void checkTempMonitor() {
        if (CPUUtils.hasTemp()) {
            mHandler.post(monitorTemp);
            mCpuTempHeader.setTypeface(mediumFont);
        } else {
            mCpuTempHeader.setVisibility(View.GONE);
            mCpuTemp.setVisibility(View.GONE);
        }
    }

    /** Monitor CPU temperature */
    private final Runnable monitorTemp = new Runnable() {
        public void run() {
            try {
                if (CPUUtils.getTemp() != null) {
                    mCpuTemp.setText(CPUUtils.getTemp());
                } else {
                    mCpuTemp.setText(R.string.temp_unavailable);
                    mCpuTemp.setTypeface(null, Typeface.ITALIC);
                }
            } catch (NumberFormatException ignored) {}
            mHandler.postDelayed(monitorTemp, 3000);
        }
    };

    private final Runnable monitorCpu = new Runnable() {
        final File cpu0 = new File(CPUUtils.CPU0);
        final File cpu1 = new File(CPUUtils.CPU1);
        final File cpu2 = new File(CPUUtils.CPU2);
        final File cpu3 = new File(CPUUtils.CPU3);

        public void run() {
            /** Set the frequency for CPU0 */
            if(mMonitorCpu0) {
                try {
                    if (cpu0.length() == 0) {
                        // CPU0 should never be empty
                        mCore0.setText(null);
                        Log.e("CpuSpyInfo", "Problem getting CPU cores");
                        return;
                    } else {
                        mCore0.setText(CPUUtils.getCpu0() + "MHz");
                    }
                } catch (NumberFormatException ignored) {
                    //DO SOMETHING
                }
            }

            /** Set the frequency for CPU1 */
            if(mMonitorCpu1) {
                try {
                    if (cpu1.length() == 0) {
                        mCore1.setText(R.string.core_offline);
                    } else {
                        mCore1.setText(CPUUtils.getCpu1() + "MHz");
                    }
                } catch (NumberFormatException ignored) {
                    // DO SOMETHING
                }
            }

            /** Set the frequency for CPU2 */
            if(mMonitorCpu2) {
                try {
                    if (cpu2.length() == 0) {
                        mCore2.setText(R.string.core_offline);
                    } else {
                        mCore2.setText(CPUUtils.getCpu2() + "MHz");
                    }
                } catch (NumberFormatException ignored) {
                    // DO SOMETHING
                }
            }

            /** Set the frequency for CPU3 */
            if(mMonitorCpu3) {
                try {
                    if (cpu3.length() == 0) {
                        mCore3.setText(R.string.core_offline);
                    } else {
                        mCore3.setText(CPUUtils.getCpu3() + "MHz");
                    }
                } catch (NumberFormatException ignored) {
                    //DO SOMETHING
                }
            }

            mHandler.postDelayed(monitorCpu, 1000); // 1 second
        }
    };

    /** Check which CPU cores to start monitoring */
    private void checkCoreMonitor() {
        switch (CPUUtils.getCoreCount()) {
            case 1:
                mMonitorCpu0 = true;
                mMonitorCpu1 = false;
                mMonitorCpu2 = false;
                mMonitorCpu3 = false;

                mCore0.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.GONE);
                mCore2.setVisibility(View.GONE);
                mCore3.setVisibility(View.GONE);
                break;
            case 2:
                mMonitorCpu0 = true;
                mMonitorCpu1 = true;
                mMonitorCpu2 = false;
                mMonitorCpu3 = false;

                mCore0.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.GONE);
                mCore3.setVisibility(View.GONE);
                break;
            case 3:
                mMonitorCpu0 = true;
                mMonitorCpu1 = true;
                mMonitorCpu2 = true;
                mMonitorCpu3 = false;

                mCore0.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.VISIBLE);
                mCore3.setVisibility(View.GONE);
                break;
            case 4:
                mMonitorCpu0 = true;
                mMonitorCpu1 = true;
                mMonitorCpu2 = true;
                mMonitorCpu3 = true;

                mCore0.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.VISIBLE);
                mCore3.setVisibility(View.VISIBLE);
                break;
        }
        mHandler.post(monitorCpu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_kernel_more:
                final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .content(CPUUtils.getKernelVersion())
                        .build();

                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogInfoAnimation;
                dialog.show();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
            case R.id.menu_settings:
                this.startActivity(new Intent(getActivity(), PrefsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        ButterKnife.reset(this);
    }
}
