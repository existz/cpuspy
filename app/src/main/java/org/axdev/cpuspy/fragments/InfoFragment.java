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

import butterknife.Bind;
import butterknife.ButterKnife;

public class InfoFragment extends Fragment implements OnClickListener {

    @Bind(R.id.btn_kernel_more) ImageButton mKernelMoreButton;
    @Bind(R.id.kernel_header) TextView mKernelHeader;
    @Bind(R.id.kernel_governor_header) TextView mKernelGovernorHeader;
    @Bind(R.id.kernel_governor) TextView mKernelGovernor;
    @Bind(R.id.kernel_version_header) TextView mKernelVersionHeader;
    @Bind(R.id.kernel_version) TextView mKernelVersion;
    @Bind(R.id.cpu_header) TextView mCpuHeader;
    @Bind(R.id.cpu_abi_header) TextView mCpuAbiHeader;
    @Bind(R.id.cpu_abi) TextView mCpuAbi;
    @Bind(R.id.cpu_arch_header) TextView mCpuArchHeader;
    @Bind(R.id.cpu_arch) TextView mCpuArch;
    @Bind(R.id.cpu_core_header) TextView mCpuCoreHeader;
    @Bind(R.id.cpu_core) TextView mCpuCore;
    @Bind(R.id.cpu_freq_header) TextView mCpuFreqHeader;
    @Bind(R.id.cpu_freq) TextView mCpuFreq;
    @Bind(R.id.cpu_temp_header) TextView mCpuTempHeader;
    @Bind(R.id.cpu_temp) TextView mCpuTemp;
    @Bind(R.id.cpu_features_header) TextView mCpuFeaturesHeader;
    @Bind(R.id.cpu_features) TextView mCpuFeatures;
    @Bind(R.id.device_header) TextView mDeviceInfo;
    @Bind(R.id.device_build_header) TextView mDeviceBuildHeader;
    @Bind(R.id.device_build) TextView mDeviceBuild;
    @Bind(R.id.device_api_header) TextView mDeviceApiHeader;
    @Bind(R.id.device_api) TextView mDeviceApi;
    @Bind(R.id.device_manuf_header) TextView mDeviceManufHeader;
    @Bind(R.id.device_manuf) TextView mDeviceManuf;
    @Bind(R.id.device_model_header) TextView mDeviceModelHeader;
    @Bind(R.id.device_model) TextView mDeviceModel;
    @Bind(R.id.device_board_header) TextView mDeviceBoardHeader;
    @Bind(R.id.device_board) TextView mDeviceBoard;
    @Bind(R.id.device_platform_header) TextView mDevicePlatformHeader;
    @Bind(R.id.device_platform) TextView mDevicePlatform;
    @Bind(R.id.device_runtime_header) TextView mDeviceRuntimeHeader;
    @Bind(R.id.device_runtime) TextView mDeviceRuntime;

    @Bind(R.id.cpu0_header) TextView mCore0Header;
    @Bind(R.id.cpu1_header) TextView mCore1Header;
    @Bind(R.id.cpu2_header) TextView mCore2Header;
    @Bind(R.id.cpu3_header) TextView mCore3Header;
    @Bind(R.id.cpu4_header) TextView mCore4Header;
    @Bind(R.id.cpu5_header) TextView mCore5Header;
    @Bind(R.id.cpu6_header) TextView mCore6Header;
    @Bind(R.id.cpu7_header) TextView mCore7Header;
    @Bind(R.id.cpu_freq0) TextView mCore0;
    @Bind(R.id.cpu_freq1) TextView mCore1;
    @Bind(R.id.cpu_freq2) TextView mCore2;
    @Bind(R.id.cpu_freq3) TextView mCore3;
    @Bind(R.id.cpu_freq4) TextView mCore4;
    @Bind(R.id.cpu_freq5) TextView mCore5;
    @Bind(R.id.cpu_freq6) TextView mCore6;
    @Bind(R.id.cpu_freq7) TextView mCore7;

    private boolean mIsVisible;
    private boolean mIsMonitoringTemp;
    private boolean mIsMonitoringCpu;
    private boolean mHasCpu0;
    private boolean mHasCpu1;
    private boolean mHasCpu2;
    private boolean mHasCpu3;
    private boolean mHasCpu4;
    private boolean mHasCpu5;
    private boolean mHasCpu6;
    private boolean mHasCpu7;

    private final Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.info_layout, container, false);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTextViews();

        /** Set color for drawables based on selected theme */
        final ColorStateList dark = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_dark));
        final ColorStateList light = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_light));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mKernelMoreButton.setImageTintList(ThemeUtils.darkTheme ? dark : light);
        } else {
            final Drawable kernelMoreButton = DrawableCompat.wrap(mKernelMoreButton.getDrawable());
            mKernelMoreButton.setImageDrawable(kernelMoreButton);
            DrawableCompat.setTintList(kernelMoreButton, (ThemeUtils.darkTheme ? dark : light));
        }

        /** Set onClickListener for kernel info button */
        mKernelMoreButton.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mIsVisible) setMonitoring(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.mIsVisible) setMonitoring(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                mIsVisible = false;
                setMonitoring(false);
            }

            if (isVisibleToUser) {
                mIsVisible = true;
                setMonitoring(true);
            }
        }
    }

    private void setMonitoring(boolean enabled) {
        if (enabled) {
            checkTempMonitor();
            checkCoreMonitor();
        } else {
            mIsMonitoringCpu = false;
            mIsMonitoringTemp = false;
        }
    }

    private void setMediumTypeface(TextView tv) {
        // Applying Roboto-Medium font
        Typeface mediumFont;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediumFont = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        } else {
            mediumFont = TypefaceHelper.get(getActivity(), TypefaceHelper.MEDIUM_FONT);
        }

        tv.setTypeface(mediumFont);
    }

    private void setTextViews() {
        /** Set text and typeface for TextViews */
        final String api = CPUUtils.getSystemProperty("ro.build.version.sdk");
        final String platform = CPUUtils.getSystemProperty("ro.board.platform");

        /** @return the current number of CPU cores */
        final int coreCount = CPUUtils.getCoreCount();
        if (coreCount != 0) {
            setMediumTypeface(mCpuCoreHeader);
            mCpuCore.setText(Integer.toString(CPUUtils.getCoreCount()));
        } else {
            mCpuCoreHeader.setVisibility(View.GONE);
            mCpuCore.setVisibility(View.GONE);
        }

        mKernelVersion.setText(System.getProperty("os.version"));
        mKernelGovernor.setText(CPUUtils.getGovernor());
        mCpuAbi.setText(Build.CPU_ABI);
        mCpuArch.setText(CPUUtils.getArch());
        mCpuFreq.setText(CPUUtils.getMinMax());
        mCpuFeatures.setText(CPUUtils.getFeatures());
        mDeviceBuild.setText(Build.ID);
        mDeviceApi.setText(api);
        mDeviceManuf.setText(Build.MANUFACTURER);
        mDeviceModel.setText(Build.MODEL);
        mDeviceBoard.setText(Build.BOARD);
        mDevicePlatform.setText(platform);
        mDeviceRuntime.setText(getRuntime());

        setMediumTypeface(mKernelHeader);
        setMediumTypeface(mKernelGovernorHeader);
        setMediumTypeface(mKernelVersionHeader);
        setMediumTypeface(mCpuHeader);
        setMediumTypeface(mCpuAbiHeader);
        setMediumTypeface(mCpuArchHeader);
        setMediumTypeface(mCpuFreqHeader);
        setMediumTypeface(mCpuFeaturesHeader);
        setMediumTypeface(mDeviceInfo);
        setMediumTypeface(mDeviceBuildHeader);
        setMediumTypeface(mDeviceApiHeader);
        setMediumTypeface(mDeviceManufHeader);
        setMediumTypeface(mDeviceModelHeader);
        setMediumTypeface(mDeviceBoardHeader);
        setMediumTypeface(mDevicePlatformHeader);
        setMediumTypeface(mDeviceRuntimeHeader);
    }

    /** @return the current runtime: ART or Dalvik */
    private String getRuntime() {
        String runtime;
        final String vmVersion = System.getProperty("java.vm.version");

        if (vmVersion != null) {
            if (vmVersion.startsWith("2")) {
                runtime = getResources().getString(R.string.information_device_runtime_art)
                        + " v" + vmVersion.substring(0, 5);
            } else {
                runtime = getResources().getString(R.string.information_device_runtime_dalvik)
                        + " v" + vmVersion.substring(0, 5);
            }
        } else {
            runtime = null;
            mDeviceRuntimeHeader.setVisibility(View.GONE);
            mDeviceRuntime.setVisibility(View.GONE);
        }
        return runtime;
    }

    /** Check if we should monitor cpu temp */
    private void checkTempMonitor() {
        if (CPUUtils.hasTemp()) {
            mIsMonitoringTemp = true;
            mHandler.post(monitorTemp);
            setMediumTypeface(mCpuTempHeader);
        } else {
            mIsMonitoringTemp = false;
            mCpuTempHeader.setVisibility(View.GONE);
            mCpuTemp.setVisibility(View.GONE);
        }
    }

    /** Monitor CPU temperature */
    private final Runnable monitorTemp = new Runnable() {
        public void run() {
            if (mIsMonitoringTemp) {
                try {
                    if (CPUUtils.getTemp() != null) {
                        mCpuTemp.setText(CPUUtils.getTemp());
                    } else {
                        mIsMonitoringTemp = false;
                        mCpuTemp.setText(R.string.error);
                        mCpuTemp.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                        Log.e("CpuSpyInfo", "Error reading cpu temp: null");
                    }
                } catch (NumberFormatException e) {
                    mCpuTemp = null;
                }
                mHandler.postDelayed(monitorTemp, 3000);
            }
        }
    };

    private final Runnable monitorCpu = new Runnable() {
        public void run() {
            if (mIsMonitoringCpu) {
                /** Set the frequency for CPU0 */
                if (mHasCpu0) {
                    try {
                        // CPU0 should never be null
                        if (CPUUtils.getCpu0() == null) {
                            mIsMonitoringCpu = false;
                            mCore0.setText(R.string.error);
                            mCore0.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                            Log.e("CpuSpyInfo", "Error reading cpu0: null");
                            return;
                        } else {
                            mCore0.setText(CPUUtils.getCpu0());
                        }
                    } catch (NumberFormatException e) {
                        mCore0 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu0 = false;
                        mCore0.setText(R.string.error);
                        mCore0.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                    }
                }
                /** Set the frequency for CPU1 */
                if (mHasCpu1) {
                    try {
                        final File cpu1 = new File(CPUUtils.CPU1);
                        if (cpu1.length() == 0) {
                            mCore1.setText(R.string.core_offline);
                        } else {
                            mCore1.setText(CPUUtils.getCpu1());
                        }
                    } catch (NumberFormatException e) {
                        mCore1 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu1 = false;
                        mCore1.setText(R.string.error);
                        mCore1.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                    }
                }
                /** Set the frequency for CPU2 */
                if (mHasCpu2) {
                    try {
                        final File cpu2 = new File(CPUUtils.CPU2);
                        if (cpu2.length() == 0) {
                            mCore2.setText(R.string.core_offline);
                        } else {
                            mCore2.setText(CPUUtils.getCpu2());
                        }
                    } catch (NumberFormatException e) {
                        mCore2 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu2 = false;
                        mCore2.setText(R.string.error);
                        mCore2.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                    }
                }
                /** Set the frequency for CPU3 */
                if (mHasCpu3) {
                    try {
                        final File cpu3 = new File(CPUUtils.CPU3);
                        if (cpu3.length() == 0) {
                            mCore3.setText(R.string.core_offline);
                        } else {
                            mCore3.setText(CPUUtils.getCpu3());
                        }
                    } catch (NumberFormatException e) {
                        mCore3 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu3 = false;
                        mCore3.setText(R.string.error);
                        mCore3.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                    }
                }
                /** Set the frequency for CPU4 */
                if (mHasCpu4) {
                    try {
                        final File cpu4 = new File(CPUUtils.CPU4);
                        if (cpu4.length() == 0) {
                            mCore4.setText(R.string.core_offline);
                        } else {
                            mCore4.setText(CPUUtils.getCpu4());
                        }
                    } catch (NumberFormatException e) {
                        mCore4 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu4 = false;
                        mCore4.setText(R.string.error);
                        mCore4.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                    }
                }
                /** Set the frequency for CPU5 */
                if (mHasCpu5) {
                    try {
                        final File cpu5 = new File(CPUUtils.CPU5);
                        if (cpu5.length() == 0) {
                            mCore5.setText(R.string.core_offline);
                        } else {
                            mCore5.setText(CPUUtils.getCpu5());
                        }
                    } catch (NumberFormatException e) {
                        mCore5 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu5 = false;
                        mCore5.setText(R.string.error);
                        mCore5.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                    }
                }
                /** Set the frequency for CPU6 */
                if (mHasCpu6) {
                    try {
                        final File cpu6 = new File(CPUUtils.CPU6);
                        if (cpu6.length() == 0) {
                            mCore6.setText(R.string.core_offline);
                        } else {
                            mCore6.setText(CPUUtils.getCpu6());
                        }
                    } catch (NumberFormatException e) {
                        mCore6 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu6 = false;
                        mCore6.setText(R.string.error);
                        mCore6.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                    }
                }
                /** Set the frequency for CPU7 */
                if (mHasCpu7) {
                    try {
                        final File cpu7 = new File(CPUUtils.CPU7);
                        if (cpu7.length() == 0) {
                            mCore7.setText(R.string.core_offline);
                        } else {
                            mCore7.setText(CPUUtils.getCpu7());
                        }
                    } catch (NumberFormatException e) {
                        mCore7 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu7 = false;
                        mCore7.setText(R.string.error);
                        mCore7.setTextColor(getResources().getColor(R.color.primary_text_color_error));
                    }
                }

                mHandler.postDelayed(monitorCpu, 1000); // 1 second
            }
        }
    };

    /** Check which CPU cores to start monitoring */
    private void checkCoreMonitor() {
        switch (CPUUtils.getCoreCount()) {
            default:
                return;
            case 1:
                mCore0Header.setVisibility(View.VISIBLE);
                mCore0.setVisibility(View.VISIBLE);
                mHasCpu0 = true;
                break;
            case 2:
                mCore0Header.setVisibility(View.VISIBLE);
                mCore0.setVisibility(View.VISIBLE);
                mHasCpu0 = true;

                mCore1Header.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mHasCpu1 = true;
                break;
            case 4:
                mCore0Header.setVisibility(View.VISIBLE);
                mCore0.setVisibility(View.VISIBLE);
                mHasCpu0 = true;

                mCore1Header.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mHasCpu1 = true;

                mCore2Header.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.VISIBLE);
                mHasCpu2 = true;

                mCore3Header.setVisibility(View.VISIBLE);
                mCore3.setVisibility(View.VISIBLE);
                mHasCpu3 = true;
                break;
            case 6:
                mCore0Header.setVisibility(View.VISIBLE);
                mCore0.setVisibility(View.VISIBLE);
                mHasCpu0 = true;

                mCore1Header.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mHasCpu1 = true;

                mCore2Header.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.VISIBLE);
                mHasCpu2 = true;

                mCore3Header.setVisibility(View.VISIBLE);
                mCore3.setVisibility(View.VISIBLE);
                mHasCpu3 = true;

                mCore4Header.setVisibility(View.VISIBLE);
                mCore4.setVisibility(View.VISIBLE);
                mHasCpu4 = true;

                mCore5Header.setVisibility(View.VISIBLE);
                mCore5.setVisibility(View.VISIBLE);
                mHasCpu5 = true;
                break;
            case 8:
                mCore0Header.setVisibility(View.VISIBLE);
                mCore0.setVisibility(View.VISIBLE);
                mHasCpu0 = true;

                mCore1Header.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mHasCpu1 = true;

                mCore2Header.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.VISIBLE);
                mHasCpu2 = true;

                mCore3Header.setVisibility(View.VISIBLE);
                mCore3.setVisibility(View.VISIBLE);
                mHasCpu3 = true;

                mCore4Header.setVisibility(View.VISIBLE);
                mCore4.setVisibility(View.VISIBLE);
                mHasCpu4 = true;

                mCore5Header.setVisibility(View.VISIBLE);
                mCore5.setVisibility(View.VISIBLE);
                mHasCpu5 = true;

                mCore6Header.setVisibility(View.VISIBLE);
                mCore6.setVisibility(View.VISIBLE);
                mHasCpu6 = true;

                mCore7Header.setVisibility(View.VISIBLE);
                mCore7.setVisibility(View.VISIBLE);
                mHasCpu7 = true;
                break;
        }
        mIsMonitoringCpu = true;
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
        setMonitoring(false);
        ButterKnife.unbind(this);
    }
}
