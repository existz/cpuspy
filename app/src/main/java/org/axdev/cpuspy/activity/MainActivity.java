//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.axdev.cpuspy.BuildConfig;
import org.axdev.cpuspy.R;
import org.axdev.cpuspy.fragments.InfoFragment;
import org.axdev.cpuspy.fragments.TimerFragment;
import org.axdev.cpuspy.services.SleepService;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends ThemedActivity {

    private Resources res;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        setupTabs();

        res = getResources();
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        final ActionBar mActionBar = getSupportActionBar();
        assert mActionBar != null;
        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(res.getString(R.string.app_name_long));
            mActionBar.setElevation(0);
        } else {
            final SpannableString s = new SpannableString(res.getString(R.string.app_name_long));
            s.setSpan(new TypefaceSpan(this, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Show warning dialog if Xposed is installed
        if (Utils.isXposedInstalled(this)) {
            final boolean showXposedWarning = sp.getBoolean("showXposedWarning", true);

            if (showXposedWarning) {
                final MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(res.getString(R.string.xposed_warning_title))
                        .content(res.getString(R.string.xposed_warning_content))
                        .positiveText(res.getString(R.string.action_dismiss))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                sp.edit().putBoolean("crashReport", false).apply();
                                sp.edit().putBoolean("showXposedWarning", false).apply();
                                recreate();
                            }
                        })
                        .build();

                // Override dialog enter/exit animation
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.show();
            }
        }

        // Add +1 to current build version and check if that URL exists
        final String currentVersion = String.valueOf(BuildConfig.VERSION_NAME).replace(".", "");
        final int upcomingVersion = Integer.parseInt(currentVersion) + 1;
        final String upcomingVersionURL = "https://app.box.com/cpuspy-v" + upcomingVersion;
        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(MainActivity.this);

        // Check if an update is available
        final Thread t = new Thread(new Runnable() {
            public void run() {
                if (Utils.urlExists(upcomingVersionURL)) {
                    SnackbarManager.show(
                            Snackbar.with(MainActivity.this)
                                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                    .text(res.getString(R.string.snackbar_text_update))
                                    .actionLabel(res.getString(R.string.action_view))
                                    .actionLabelTypeface(robotoMedium)
                                    .actionColor(accentColor() == 0 ? ContextCompat.getColor(MainActivity.this, R.color.primary) : accentColor())
                                    .actionListener(new ActionClickListener() {
                                        @Override
                                        public void onActionClicked(Snackbar snackbar) {
                                            final String xdaURL = "http://goo.gl/AusQy8";
                                            Utils.openURL(MainActivity.this, xdaURL);
                                        }
                                    })
                    );
                }
            }
        });
        t.start();

        // Start service if its not automatically started on boot
        if (sp.getBoolean("sleepDetection", true)) {
            if (!Utils.isServiceRunning(this, SleepService.class)) {
                startService(new Intent(this, SleepService.class));
            }
        }
    }

    private void setupTabs() {
        // Assigning ViewPager View and setting the adapter
        final ViewPager viewPager = ButterKnife.findById(this, R.id.pager);
        setupViewPager(viewPager);

        // Assigning the Sliding Tab Layout View
        final TabLayout tabs = ButterKnife.findById(this, R.id.tabLayout);
        if (ThemedActivity.isLightAB(this)) {
            final ColorStateList sl = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tabsTextColor_lightAB));
            tabs.setTabTextColors(sl);
            tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.tabsScrollColor_lightAB));
        }
        tabs.setBackgroundColor(primaryColor());
        tabs.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        final Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new TimerFragment(), getResources().getString(R.string.tab_title_timers));
        adapter.addFragment(new InfoFragment(), getResources().getString(R.string.tab_title_info));
        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        private Adapter(FragmentManager fm) {
            super(fm);
        }

        private void addFragment(Fragment fragment, String title) {
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
    }

    /** called when we want to inflate the menu */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // request inflater from activity and inflate into its menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // tint settings icon based on theme
        if (ThemedActivity.isLightAB(this)) {
            final MenuItem settings = menu.findItem(R.id.menu_settings);
            Utils.colorMenuItem(settings, ContextCompat.getColor(this, R.color.drawable_color_lightAB), 0);
        }

        // made it
        return true;
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
            case R.id.menu_settings:
                this.startActivity(new Intent(this, PrefsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}