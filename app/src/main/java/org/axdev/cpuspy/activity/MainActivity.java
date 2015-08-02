//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.fragments.InfoFragment;
import org.axdev.cpuspy.fragments.TimerFragment;
import org.axdev.cpuspy.services.CheckUpdateService;
import org.axdev.cpuspy.services.SleepService;
import org.axdev.cpuspy.utils.ThemeUtils;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private boolean mLastTheme;
    private boolean mLastNavBar;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ThemeUtils.onActivityCreateSetNavBar(this);
        }
        ThemeUtils.onActivityCreateSetTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        setupTabs();

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        final ActionBar mActionBar = getSupportActionBar();
        assert mActionBar != null;
        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(R.string.app_name_long);
            mActionBar.setElevation(0);
        } else {
            final SpannableString s = new SpannableString(getResources().getString(R.string.app_name_long));
            s.setSpan(new TypefaceSpan(this, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mLastTheme = ThemeUtils.darkTheme;
        mLastNavBar = ThemeUtils.coloredNavBar;

        // Show warning dialog if Xposed is installed
        if (Utils.isXposedInstalled(this)) {
            final boolean showXposedWarning = sp.getBoolean("showXposedWarning", true);

            if (showXposedWarning) {
                final MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.xposed_warning_title)
                        .content(R.string.xposed_warning_content)
                        .positiveText(R.string.action_dismiss)
                        .btnSelector(R.drawable.btn_selector_custom, DialogAction.POSITIVE)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                sp.edit().putBoolean("crashReport", false).apply();
                                sp.edit().putBoolean("showXposedWarning", false).apply();
                            }
                        })
                        .build();

                // Override dialog enter/exit animation
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.show();
            }
        }

        // Start service if its not automatically started on boot
        if (sp.getBoolean("sleepDetection", true)) {
            if (!Utils.isServiceRunning(this, SleepService.class)) {
                startService(new Intent(this, SleepService.class));
            }
        }

        if (sp.getBoolean("checkUpdates", true)) {
            if (!Utils.isServiceRunning(this, CheckUpdateService.class)) {
                startService(new Intent(this, CheckUpdateService.class));
            }
        }
    }

    private void setupTabs() {
        // Assigning ViewPager View and setting the adapter
        final ViewPager viewPager = ButterKnife.findById(this, R.id.pager);
        setupViewPager(viewPager);

        // Assigning the Sliding Tab Layout View
        final TabLayout tabs = ButterKnife.findById(this, R.id.tabLayout);
        tabs.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        final Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new TimerFragment(), getResources().getString(R.string.tab_title_timers));
        adapter.addFragment(new InfoFragment(), getResources().getString(R.string.tab_title_info));
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize and start automatic crash reporting
        if (sp.getBoolean("crashReport", true) && !Utils.isXposedInstalled(this)) {
            Fabric.with(this, new Crashlytics());
        } else {
            sp.edit().putBoolean("crashReport", false).apply();
        }

        // Restart activity if theme or navbar changed
        if (mLastTheme != ThemeUtils.darkTheme
                || mLastNavBar != ThemeUtils.coloredNavBar) {
            this.recreate();
        }
    }
}