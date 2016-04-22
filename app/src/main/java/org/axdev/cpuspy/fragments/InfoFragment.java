//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.ThemedActivity;
import org.axdev.cpuspy.utils.CPUUtils;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.chainfire.libsuperuser.Shell;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class InfoFragment extends Fragment {

    @Bind(R.id.kernel_menu) CardView mKernelMenu;
    @Bind(R.id.device_menu) CardView mDeviceMenu;
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
    @Bind(R.id.cpu_usage_header) TextView mCpuUsageHeader;
    @Bind(R.id.cpu_usage) TextView mCpuUsage;
    @Bind(R.id.cpu_features_header) TextView mCpuFeaturesHeader;
    @Bind(R.id.cpu_features) TextView mCpuFeatures;
    @Bind(R.id.device_header) TextView mDeviceHeader;
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
    @Bind(R.id.device_bootloader_header) TextView mDeviceBootloaderHeader;
    @Bind(R.id.device_bootloader) TextView mDeviceBootloader;
    @Bind(R.id.container) View mContainer;

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

    @BindColor(R.color.material_blue_500) int mMaterialBlue500;
    @BindColor(R.color.material_grey_200) int mMaterialGrey200;
    @BindColor(R.color.material_grey_700) int mMaterialGrey700;
    @BindColor(R.color.primary_text_color_error) int errorTextColor;
    @BindDrawable(R.drawable.text_select_handle_left_mtrl_alpha) Drawable mTextSelectHandleLeft;
    @BindDrawable(R.drawable.text_select_handle_right_mtrl_alpha) Drawable mTextSelectHandleRight;
    @BindString(R.string.error) String errorText;
    @BindString(R.string.core_offline) String coreOfflineText;
    @BindString(R.string.information_device_runtime_art) String artRuntimeText;
    @BindString(R.string.information_device_runtime_dalvik) String dalvikRuntimeText;
    @BindString(R.string.information_kernel_version_unavailable) String versionUnavailableText;
    @BindString(R.string.logcat_file_saved) String logcatFileSaved;
    @BindString(R.string.logcat_error_saving) String logcatErrorSaving;
    @BindString(R.string.snackbar_text_delete) String snackBarDelete;

    private boolean mIsVisible;
    private boolean mIsMonitoringTemp;
    private boolean mIsMonitoringCpu;
    private boolean mIsMonitoringUsage;
    private boolean mHasCpu0;
    private boolean mHasCpu1;
    private boolean mHasCpu2;
    private boolean mHasCpu3;
    private boolean mHasCpu4;
    private boolean mHasCpu5;
    private boolean mHasCpu6;
    private boolean mHasCpu7;

    private Animation popupEnterMtrl;
    private Context mContext;
    private Handler mHandler;
    private Typeface robotoMedium;

    private File mLogcatFile;
    private MaterialProgressBar mProgressBarLogcat;
    private NestedScrollView mLogcatScrollView;
    private RelativeLayout mLogcatTitleBar;
    private TextView mLogcatSummary;

    private int accentColor;
    private int mNumCores;
    private int mTextHighlightColor;

    private final int REQUEST_WRITE_STORAGE = 112;

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
        /** Set text and typeface for TextViews */
        this.mContext = this.getActivity();
        mHandler = new Handler();
        robotoMedium = TypefaceHelper.mediumTypeface(mContext);
        popupEnterMtrl = AnimationUtils.loadAnimation(mContext, R.anim.popup_enter_mtrl);

        final String api = CPUUtils.getSystemProperty("ro.build.version.sdk");
        final String platform = CPUUtils.getSystemProperty("ro.board.platform");
        final String kernelVersion = System.getProperty("os.version");

        /** @return the current number of CPU cores */
        mNumCores = CPUUtils.getCoreCount();
        if (mNumCores != 0) {
            mCpuCoreHeader.setTypeface(robotoMedium);
            mCpuCore.setText(String.format(Locale.US, "%d", mNumCores));
        } else {
            mCpuCoreHeader.setVisibility(View.GONE);
            mCpuCore.setVisibility(View.GONE);
        }

        if (kernelVersion != null) mKernelVersion.setText(kernelVersion);
        if (CPUUtils.getGovernor() != null) mKernelGovernor.setText(CPUUtils.getGovernor());
        if (Build.CPU_ABI != null) mCpuAbi.setText(Build.CPU_ABI);
        if (CPUUtils.getArch() != null) mCpuArch.setText(CPUUtils.getArch());
        if (CPUUtils.getMinMax() != null) mCpuFreq.setText(CPUUtils.getMinMax());
        if (CPUUtils.getFeatures() != null) mCpuFeatures.setText(CPUUtils.getFeatures());
        if (Build.ID != null) mDeviceBuild.setText(Build.ID);
        if (api != null) mDeviceApi.setText(api);
        if (Build.MANUFACTURER != null) mDeviceManuf.setText(Build.MANUFACTURER);
        if (Build.MODEL != null) mDeviceModel.setText(Build.MODEL);
        if (Build.BOARD != null) mDeviceBoard.setText(Build.BOARD);
        if (Build.BOOTLOADER != null) mDeviceBootloader.setText(Build.BOOTLOADER);
        if (platform != null) mDevicePlatform.setText(platform);
        if (getRuntime() != null) mDeviceRuntime.setText(getRuntime());

        mKernelHeader.setTypeface(robotoMedium);
        mKernelGovernorHeader.setTypeface(robotoMedium);
        mKernelVersionHeader.setTypeface(robotoMedium);
        mCpuHeader.setTypeface(robotoMedium);
        mCpuAbiHeader.setTypeface(robotoMedium);
        mCpuArchHeader.setTypeface(robotoMedium);
        mCpuFreqHeader.setTypeface(robotoMedium);
        mCpuFeaturesHeader.setTypeface(robotoMedium);
        mCpuUsageHeader.setTypeface(robotoMedium);
        mDeviceHeader.setTypeface(robotoMedium);
        mDeviceBuildHeader.setTypeface(robotoMedium);
        mDeviceApiHeader.setTypeface(robotoMedium);
        mDeviceManufHeader.setTypeface(robotoMedium);
        mDeviceModelHeader.setTypeface(robotoMedium);
        mDeviceBoardHeader.setTypeface(robotoMedium);
        mDevicePlatformHeader.setTypeface(robotoMedium);
        mDeviceRuntimeHeader.setTypeface(robotoMedium);
        mDeviceBootloaderHeader.setTypeface(robotoMedium);

        accentColor = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("accent_color", mMaterialBlue500);

        mTextHighlightColor = ColorUtils.setAlphaComponent(accentColor, 128);
        mKernelHeader.setTextColor(accentColor);
        mCpuHeader.setTextColor(accentColor);
        mDeviceHeader.setTextColor(accentColor);

        /** Use accent color for text selector handles */
        if (mTextSelectHandleLeft != null && mTextSelectHandleRight != null) {
            DrawableCompat.wrap(mTextSelectHandleLeft);
            DrawableCompat.wrap(mTextSelectHandleRight);
            DrawableCompat.setTint(mTextSelectHandleLeft, accentColor);
            DrawableCompat.setTint(mTextSelectHandleRight, accentColor);
        }

        mContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                if (mDeviceMenu.isShown()
                        && Utils.isOutOfBounds(mDeviceMenu, ev)) {
                    mDeviceMenu.setVisibility(View.GONE);
                    return true;
                } else if (mKernelMenu.isShown()
                        && Utils.isOutOfBounds(mKernelMenu, ev)) {
                    mKernelMenu.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });

        // Allow closing card/menus with back button
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();

            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (mDeviceMenu.isShown()) {
                                mDeviceMenu.setVisibility(View.GONE);
                                return true;
                            } else if (mKernelMenu.isShown()) {
                                mKernelMenu.setVisibility(View.GONE);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mIsVisible) setMonitoring(false);
        if (mDeviceMenu.isShown()) mDeviceMenu.setVisibility(View.GONE);
        if (mKernelMenu.isShown()) mKernelMenu.setVisibility(View.GONE);
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

            mIsMonitoringUsage = true;
            mHandler.post(monitorCpuUsage);
        } else {
            mIsMonitoringCpu = false;
            mIsMonitoringTemp = false;
            mIsMonitoringUsage = false;
        }
    }

    /** @return the current runtime: ART or Dalvik */
    private String getRuntime() {
        String runtime;
        final String vmVersion = System.getProperty("java.vm.version");

        if (vmVersion != null) {
            if (vmVersion.startsWith("2")) {
                runtime = artRuntimeText + " v" + vmVersion.substring(0, 5);
            } else {
                runtime = dalvikRuntimeText + " v" + vmVersion.substring(0, 5);
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
            mCpuTempHeader.setTypeface(robotoMedium);
            mCpuTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(mContext)
                            .content(CPUUtils.getTempFile())
                            .show();
                }
            });
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
                        mCpuTemp.setText(errorText);
                        mCpuTemp.setTextColor(errorTextColor);
                        Log.e("CpuSpyInfo", "Error reading cpu temp: null");
                    }
                } catch (NumberFormatException e) {
                    mCpuTemp = null;
                } catch (Exception e) {
                    mIsMonitoringTemp = false;
                    mCpuTemp.setText(errorText);
                    mCpuTemp.setTextColor(errorTextColor);
                    e.printStackTrace();
                }
                mHandler.postDelayed(this, 3000);
            }
        }
    };

    /** Monitor CPU usage */
    private final Runnable monitorCpuUsage = new Runnable() {
        @Override
        public void run() {
            if (mIsMonitoringUsage) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final float usage = CPUUtils.getCpuUsage();
                        if (usage != 0) {
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCpuUsage.setText(String.format(Locale.US, "%.01f%%", usage));
                                    }
                                });
                            } catch (NumberFormatException e) {
                                mCpuUsage = null;
                            } catch (Exception e) {
                                mIsMonitoringUsage = false;
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                mHandler.postDelayed(this, 3000);
            }
        }
    };

    private final Runnable monitorCpu = new Runnable() {
        public void run() {
            if (mIsMonitoringCpu) {
                /** Set the frequency for CPU0 */
                if (mHasCpu0) {
                    try {
                        mCore0Header.setVisibility(View.VISIBLE);
                        mCore0.setVisibility(View.VISIBLE);
                        // CPU0 should never be null
                        if (CPUUtils.getCpu0() != null) {
                            mCore0.setText(CPUUtils.getCpu0());
                        } else {
                            mIsMonitoringCpu = false;
                            mCore0.setText(errorText);
                            mCore0.setTextColor(errorTextColor);
                            Log.e("CpuSpyInfo", "Error reading cpu0: null");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        mCore0 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu0 = false;
                        mCore0.setText(errorText);
                        mCore0.setTextColor(errorTextColor);
                    }
                }
                /** Set the frequency for CPU1 */
                if (mHasCpu1) {
                    try {
                        mCore1Header.setVisibility(View.VISIBLE);
                        mCore1.setVisibility(View.VISIBLE);
                        final File cpu1 = new File(CPUUtils.CPU1);
                        if (cpu1.length() == 0) {
                            mCore1.setText(coreOfflineText);
                        } else {
                            mCore1.setText(CPUUtils.getCpu1());
                        }
                    } catch (NumberFormatException e) {
                        mCore1 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu1 = false;
                        mCore1.setText(errorText);
                        mCore1.setTextColor(errorTextColor);
                    }
                }
                /** Set the frequency for CPU2 */
                if (mHasCpu2) {
                    try {
                        mCore2Header.setVisibility(View.VISIBLE);
                        mCore2.setVisibility(View.VISIBLE);
                        final File cpu2 = new File(CPUUtils.CPU2);
                        if (cpu2.length() == 0) {
                            mCore2.setText(coreOfflineText);
                        } else {
                            mCore2.setText(CPUUtils.getCpu2());
                        }
                    } catch (NumberFormatException e) {
                        mCore2 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu2 = false;
                        mCore2.setText(errorText);
                        mCore2.setTextColor(errorTextColor);
                    }
                }
                /** Set the frequency for CPU3 */
                if (mHasCpu3) {
                    try {
                        mCore3Header.setVisibility(View.VISIBLE);
                        mCore3.setVisibility(View.VISIBLE);
                        final File cpu3 = new File(CPUUtils.CPU3);
                        if (cpu3.length() == 0) {
                            mCore3.setText(coreOfflineText);
                        } else {
                            mCore3.setText(CPUUtils.getCpu3());
                        }
                    } catch (NumberFormatException e) {
                        mCore3 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu3 = false;
                        mCore3.setText(errorText);
                        mCore3.setTextColor(errorTextColor);
                    }
                }
                /** Set the frequency for CPU4 */
                if (mHasCpu4) {
                    try {
                        mCore4Header.setVisibility(View.VISIBLE);
                        mCore4.setVisibility(View.VISIBLE);
                        final File cpu4 = new File(CPUUtils.CPU4);
                        if (cpu4.length() == 0) {
                            mCore4.setText(coreOfflineText);
                        } else {
                            mCore4.setText(CPUUtils.getCpu4());
                        }
                    } catch (NumberFormatException e) {
                        mCore4 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu4 = false;
                        mCore4.setText(errorText);
                        mCore4.setTextColor(errorTextColor);
                    }
                }
                /** Set the frequency for CPU5 */
                if (mHasCpu5) {
                    try {
                        mCore5Header.setVisibility(View.VISIBLE);
                        mCore5.setVisibility(View.VISIBLE);
                        final File cpu5 = new File(CPUUtils.CPU5);
                        if (cpu5.length() == 0) {
                            mCore5.setText(coreOfflineText);
                        } else {
                            mCore5.setText(CPUUtils.getCpu5());
                        }
                    } catch (NumberFormatException e) {
                        mCore5 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu5 = false;
                        mCore5.setText(errorText);
                        mCore5.setTextColor(errorTextColor);
                    }
                }
                /** Set the frequency for CPU6 */
                if (mHasCpu6) {
                    try {
                        mCore6Header.setVisibility(View.VISIBLE);
                        mCore6.setVisibility(View.VISIBLE);
                        final File cpu6 = new File(CPUUtils.CPU6);
                        if (cpu6.length() == 0) {
                            mCore6.setText(coreOfflineText);
                        } else {
                            mCore6.setText(CPUUtils.getCpu6());
                        }
                    } catch (NumberFormatException e) {
                        mCore6 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu6 = false;
                        mCore6.setText(errorText);
                        mCore6.setTextColor(errorTextColor);
                    }
                }
                /** Set the frequency for CPU7 */
                if (mHasCpu7) {
                    try {
                        mCore7Header.setVisibility(View.VISIBLE);
                        mCore7.setVisibility(View.VISIBLE);
                        final File cpu7 = new File(CPUUtils.CPU7);
                        if (cpu7.length() == 0) {
                            mCore7.setText(coreOfflineText);
                        } else {
                            mCore7.setText(CPUUtils.getCpu7());
                        }
                    } catch (NumberFormatException e) {
                        mCore7 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHasCpu7 = false;
                        mCore7.setText(errorText);
                        mCore7.setTextColor(errorTextColor);
                    }
                }

                mHandler.postDelayed(this, 1000); // 1 second
            }
        }
    };

    /** Check which CPU cores to start monitoring */
    private void checkCoreMonitor() {
        switch (mNumCores) {
            default:
                return;
            case 1:
                mHasCpu0 = true;
                break;
            case 2:
                mHasCpu0 = true;
                mHasCpu1 = true;
                break;
            case 4:
                mHasCpu0 = true;
                mHasCpu1 = true;
                mHasCpu2 = true;
                mHasCpu3 = true;
                break;
            case 6:
                mHasCpu0 = true;
                mHasCpu1 = true;
                mHasCpu2 = true;
                mHasCpu3 = true;
                mHasCpu4 = true;
                mHasCpu5 = true;
                break;
            case 8:
                mHasCpu0 = true;
                mHasCpu1 = true;
                mHasCpu2 = true;
                mHasCpu3 = true;
                mHasCpu4 = true;
                mHasCpu5 = true;
                mHasCpu6 = true;
                mHasCpu7 = true;
                break;
        }
        mIsMonitoringCpu = true;
        mHandler.post(monitorCpu);
    }

    /** Bind button listeners */
    @OnClick(R.id.full_kernel_version)
    protected void fullKernelVersion() {
        mKernelMenu.setVisibility(View.GONE);
        showKernelBottomSheet();
    }

    @OnClick(R.id.btn_kernel_more)
    protected void kernelMoreButton() {
        mKernelMenu.startAnimation(popupEnterMtrl);
        mKernelMenu.setVisibility(View.VISIBLE);
        Utils.setElevation(mKernelMenu, getResources().getDimension(R.dimen.floating_toolbar_elevation));
    }

    @OnClick(R.id.btn_device_more)
    protected void deviceMoreButton() {
        mDeviceMenu.startAnimation(popupEnterMtrl);
        mDeviceMenu.setVisibility(View.VISIBLE);
        Utils.setElevation(mDeviceMenu, getResources().getDimension(R.dimen.floating_toolbar_elevation));
    }

    @OnClick(R.id.running_processes)
    protected void runningProcessesButton() {
        mDeviceMenu.setVisibility(View.GONE);
        showProcessBottomSheet();
    }

    @OnClick(R.id.logcat)
    protected void logcatButton() {
        mDeviceMenu.setVisibility(View.GONE);
        showLogcatBottomSheet();
    }

    @SuppressWarnings("unused")
    private void writeLogcatToFile() {
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .title(R.string.logcat_input_title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(1, 32, accentColor)
                .positiveText(R.string.action_done)
                .input(R.string.logcat_input_hint, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        mLogcatFile = new File(Environment.getExternalStorageDirectory(), input.toString());

                        try {
                            if (mLogcatFile.exists()) { boolean delete = mLogcatFile.delete(); }
                            final FileWriter writer = new FileWriter(mLogcatFile);
                            writer.write(mLogcatSummary.getText().toString());
                            writer.flush();
                            writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            SnackbarManager.show(Snackbar.with(mContext)
                                    .text(logcatErrorSaving)
                                    .actionLabelTypeface(robotoMedium)
                                    .actionLabel(errorText)
                                    .actionColor(errorTextColor));
                        } finally {
                            SnackbarManager.show(Snackbar.with(mContext)
                                    .text(logcatFileSaved + input.toString())
                                    .actionLabelTypeface(robotoMedium)
                                    .actionLabel(snackBarDelete) // action button label
                                    .actionColor(accentColor)
                                    .actionListener(new ActionClickListener() {
                                        @Override
                                        public void onActionClicked(Snackbar snackbar) {
                                            if (mLogcatFile.exists()) { boolean delete = mLogcatFile.delete(); }
                                        }
                                    }));
                        }
                    }
                }).build();

        final EditText editText = dialog.getInputEditText();
        if (editText != null)
            editText.setHighlightColor(mTextHighlightColor);

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeLogcatToFile();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showKernelBottomSheet() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_kernel, null);
        final TextView mKernelVersionFullHeader = ButterKnife.findById(view, R.id.kernel_version_full_header);
        mKernelVersionFullHeader.setTypeface(robotoMedium);

        final TextView mKernelVersionFull = ButterKnife.findById(view, R.id.kernel_version_full);
        if (CPUUtils.getKernelVersion() != null) {
            mKernelVersionFull.setText(CPUUtils.getKernelVersion());
            mKernelVersionFull.setHighlightColor(mTextHighlightColor);
        } else {
            mKernelVersionFull.setText(versionUnavailableText);
        }

        final BottomSheetDialog mKernelBottomSheetDialog = new BottomSheetDialog(mContext);
        mKernelBottomSheetDialog.setContentView(view);
        mKernelBottomSheetDialog.show();

        final BottomSheetBehavior behavior = BottomSheetBehavior.from(((View) view.getParent()));
        behavior.setPeekHeight((int)getResources().getDimension(R.dimen.bottom_sheet_min_height));
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch(newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        mKernelBottomSheetDialog.dismiss();
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        final ImageButton mKernelCloseButton = ButterKnife.findById(view, R.id.btn_kernel_close);
        mKernelCloseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mKernelBottomSheetDialog.dismiss();
            }
        });
    }

    private void showProcessBottomSheet() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_processes, null);
        final TextView mProcessHeader = ButterKnife.findById(view, R.id.process_header);
        mProcessHeader.setTypeface(robotoMedium);

        final BottomSheetDialog mProcessBottomSheetDialog = new BottomSheetDialog(mContext);
        mProcessBottomSheetDialog.setContentView(view);
        mProcessBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                removeFragment(R.id.processListFragment);
            }
        });
        mProcessBottomSheetDialog.show();

        final BottomSheetBehavior behavior = BottomSheetBehavior.from(((View) view.getParent()));
        behavior.setPeekHeight((int)getResources().getDimension(R.dimen.bottom_sheet_min_height));
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch(newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        mProcessBottomSheetDialog.dismiss();
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        final ImageButton mProcessCloseButton = ButterKnife.findById(view, R.id.btn_process_close);
        mProcessCloseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mProcessBottomSheetDialog.dismiss();
            }
        });
    }

    private void showLogcatBottomSheet() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_logcat, null);
        mProgressBarLogcat = ButterKnife.findById(view, R.id.logcat_progressbar);
        mLogcatSummary = ButterKnife.findById(view, R.id.logcat_output);
        mLogcatTitleBar = ButterKnife.findById(view, R.id.logcat_title_bar);
        mLogcatScrollView = ButterKnife.findById(view, R.id.logcat_scrollview);
        final TextView mLogcatHeader = ButterKnife.findById(view, R.id.logcat_header);
        mLogcatHeader.setTypeface(robotoMedium);

        final BottomSheetDialog mLogcatBottomSheetDialog = new BottomSheetDialog(mContext);
        mLogcatBottomSheetDialog.setContentView(view);
        mLogcatBottomSheetDialog.show();

        final View mLogcatBottomSheet = view.findViewById(R.id.logcat_sheet_contents);
        final BottomSheetBehavior mLogcatBottomSheetBehavior = BottomSheetBehavior.from(((View) view.getParent()));
        mLogcatBottomSheetBehavior.setPeekHeight((int)getResources().getDimension(R.dimen.bottom_sheet_min_height));
        mLogcatBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch(newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        //mLogcatSummary.setText(null);
                        mLogcatBottomSheetDialog.dismiss();
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mLogcatScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                            @Override
                            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                if (mLogcatBottomSheet.getMeasuredHeight() <= scrollY + v.getHeight()) {
                                    Utils.setElevation(mLogcatTitleBar, getResources().getDimension(R.dimen.ab_elevation));
                                } else {
                                    Utils.setElevation(mLogcatTitleBar, 0);
                                }
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        final ImageButton mLogcatCloseButton = ButterKnife.findById(view, R.id.btn_logcat_close);
        mLogcatCloseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mLogcatBottomSheetDialog.dismiss();
            }
        });

        final ImageButton mLogcatSaveButton = ButterKnife.findById(view, R.id.btn_logcat_save);
        mLogcatSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE);
                } else {
                    writeLogcatToFile();
                }
            }
        });

        int progressBarColor = ThemedActivity.mIsDarkTheme ? mMaterialGrey700 : mMaterialGrey200;
        MDTintHelper.setTint(mProgressBarLogcat, progressBarColor);
        mProgressBarLogcat.setVisibility(View.VISIBLE);

        Tasks.executeInBackground(getActivity(), new BackgroundWork<String>() {
            @Override
            public String doInBackground() throws Exception {
                boolean suAvailable = Shell.SU.available();
                final String logcatCommand = "logcat -d -v brief -t 500";
                if (suAvailable) {
                    TimeUnit.MILLISECONDS.sleep(500);
                    return Shell.SU.run(logcatCommand).toString();
                } else {
                    final Process process = Runtime.getRuntime().exec(logcatCommand);
                    final BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    final StringBuilder log = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        log.append(line);
                    }
                    return log.toString();
                }
            }
        }, new Completion<String>() {
            @Override
            public void onSuccess(Context context, String result) {
                if (mLogcatSummary != null) {
                    mLogcatSummary.setText(result);
                    mLogcatSummary.setHighlightColor(mTextHighlightColor);
                }
                if (mProgressBarLogcat.getVisibility() == View.VISIBLE) {
                    mProgressBarLogcat.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Context context, Exception e) {
                if (mProgressBarLogcat.getVisibility() == View.VISIBLE) {
                    mProgressBarLogcat.setVisibility(View.GONE);
                }
                if (mLogcatSummary != null) {
                    mLogcatSummary.setText(errorText);
                    mLogcatSummary.setTextColor(errorTextColor);
                }
                e.printStackTrace();
            }
        });
    }

    private void removeFragment(int fragment) {
        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragmentManager.findFragmentById(fragment)).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setMonitoring(false);
        ButterKnife.unbind(this);
    }
}
