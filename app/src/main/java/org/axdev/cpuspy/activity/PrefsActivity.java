//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.fragments.LicenseFragment;
import org.axdev.cpuspy.fragments.WhatsNewDialog;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.ThemeUtils;

public class PrefsActivity extends AppCompatActivity {

    /** Whether or not the theme has changed */
    public static boolean mThemeChanged = false;

    public static class PrefsFragment extends PreferenceFragment {

        private final String googleURL = "https://plus.google.com/+RobBeane";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            findPreference("developer").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(googleURL));
                    startActivity(i);
                    return true;
                }
            });

            findPreference("license").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Create new fragment and transaction
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_wrapper, new LicenseFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
            });

            final CheckBoxPreference crashReport = (CheckBoxPreference) getPreferenceManager().findPreference("crashReport");

            crashReport.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals("false")) {
                        SnackbarManager.show(Snackbar.with(getActivity())
                                .type(SnackbarType.MULTI_LINE)
                                .text(R.string.snackbar_text_crashreport));
                    }
                    return true;
                }
            });

            final CheckBoxPreference darkTheme = (CheckBoxPreference) getPreferenceManager().findPreference("darkTheme");

            darkTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals("true")) {
                        ThemeUtils.changeToTheme(getActivity(), ThemeUtils.DARK);
                    } else {
                        ThemeUtils.changeToTheme(getActivity(), ThemeUtils.LIGHT);
                    }
                    mThemeChanged = true;
                    return true;
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final CheckBoxPreference coloredNavBar = (CheckBoxPreference) getPreferenceManager().findPreference("coloredNavBar");

                coloredNavBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue.toString().equals("true")) {
                            ThemeUtils.changeNavBar(getActivity(), ThemeUtils.NAVBAR_COLORED);
                        } else {
                            ThemeUtils.changeNavBar(getActivity(), ThemeUtils.NAVBAR_DEFAULT);
                        }
                        mThemeChanged = true;
                        return true;
                    }
                });
            }
        }

        @Override
        public void onStart() {
            super.onStart();

            // Use custom Typeface for action bar title on KitKat devices
            final ActionBar supportActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (supportActionBar != null) {
                    supportActionBar.setTitle(R.string.settings);
                    supportActionBar.setElevation(getResources().getDimension(R.dimen.ab_elevation));
                }
            } else {
                final SpannableString s = new SpannableString(getResources().getString(R.string.settings));
                s.setSpan(new TypefaceSpan(getActivity(), TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Update the action bar title with the TypefaceSpan instance
                if (supportActionBar != null) {
                    supportActionBar.setTitle(s);
                }
            }
            if (supportActionBar != null) { supportActionBar.setDisplayHomeAsUpEnabled(true); }
        }

    } /** End PrefsFragment **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ThemeUtils.onActivityCreateSetNavBar(this);
        }
        ThemeUtils.onActivityCreateSetTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        getFragmentManager().beginTransaction().add(R.id.content_wrapper, new PrefsFragment()).commit();
    }

    private void checkBackStack() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onBackPressed() {
        this.checkBackStack();
    }

    /** called when we want to inflate the menu */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // request inflater from activity and inflate into its menu
        getMenuInflater().inflate(R.menu.settings_menu, menu);

        // made it
        return true;
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
            case R.id.menu_changelog:
                final WhatsNewDialog newFragment = new WhatsNewDialog();
                newFragment.show(getFragmentManager(), "whatsnew");
                break;
            case R.id.menu_donate:
                final Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://goo.gl/X2sA4D"));
                startActivity(i);
                break;
            case android.R.id.home:
                this.checkBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

} /** End PrefsActivity **/