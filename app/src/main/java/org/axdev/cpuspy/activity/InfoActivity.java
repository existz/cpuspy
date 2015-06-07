//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.activity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.CPUUtils;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.ThemeUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InfoActivity extends AppCompatActivity implements OnClickListener {

    @InjectView(R.id.card_view_kernel) CardView mKernelCardView;
    @InjectView(R.id.card_view_device) CardView mDeviceCardView;
    @InjectView(R.id.card_view_cpu) CardView mCpuCardView;
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

    private Typeface mediumFont;

    private final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ThemeUtils.onActivityCreateSetNavBar(this);
        }
        ThemeUtils.onActivityCreateSetTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);
        ButterKnife.inject(this);

        /** Only animate cardviews on Lollipop and above */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Animation slideInTop = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
            final Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
            final Animation slideInLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);

            mKernelCardView.setAnimation(slideInTop);
            mDeviceCardView.setAnimation(slideInLeft);
            mCpuCardView.setAnimation(slideInBottom);
        }

        /** Set UI elements for dark and light themes */
        mKernelCardView.setCardBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.card_dark_background : R.color.card_light_background));
        mDeviceCardView.setCardBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.card_dark_background : R.color.card_light_background));
        mCpuCardView.setCardBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.card_dark_background : R.color.card_light_background));

        // Set color for drawables based on selected theme
        final ColorStateList dark = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_dark));
        final ColorStateList light = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_light));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mKernelMoreButton.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
        } else {
            final Drawable kernelMoreButton = DrawableCompat.wrap(mKernelMoreButton.getDrawable());
            mKernelMoreButton.setImageDrawable(kernelMoreButton);
            DrawableCompat.setTintList(kernelMoreButton, (ThemeUtils.DARKTHEME ? dark : light));
        }

        /** Use custom Typeface for action bar title on KitKat devices */
        if (getSupportActionBar() != null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.information);
            }
        } else {
            final SpannableString s = new SpannableString(getResources().getString(R.string.information));
            s.setSpan(new TypefaceSpan(this, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(s);
            }
        }

        /** Set text and fontface for TextViews */
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
            this.mediumFont = TypefaceHelper.get(this, TypefaceHelper.MEDIUM_FONT);
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

        /** Set onClickListener for kernel info button */
        mKernelMoreButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkTempMonitor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
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
            mHandler.postDelayed(monitorTemp, 1000);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_kernel_more:
                final MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .content(CPUUtils.getKernelVersion())
                        .build();

                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogInfoAnimation;
                dialog.show();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
