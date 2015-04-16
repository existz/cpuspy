//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.fragments.AboutFragment;
import org.axdev.cpuspy.fragments.LicenseFragment;
import org.axdev.cpuspy.fragments.WhatsNewDialog;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.ThemeUtils;

public class PrefsActivity extends ActionBarActivity {

    /** Whether or not the theme has changed */
    public static boolean mThemeChanged = false;

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Create new fragment and transaction
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_wrapper, new AboutFragment())
                            .addToBackStack(null)
                            .commit();
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

            if (Build.VERSION.SDK_INT >= 21) {
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
        public void onResume() {
            super.onResume();
            // Use custom Typeface for action bar title on KitKat devices
            if (Build.VERSION.SDK_INT == 19) {
                final SpannableString s = new SpannableString(getResources().getString(R.string.settings));
                s.setSpan(new TypefaceSpan(getActivity(), "Roboto-Medium.ttf"), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Update the action bar title with the TypefaceSpan instance
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(s);
            } else {
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);

                int mToolbarElevation = (int) getResources().getDimension(R.dimen.toolbar_elevation);
                ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(mToolbarElevation);
            }
        }

    } /** End PrefsFragment **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            ThemeUtils.onActivityCreateSetNavBar(this);
        }
        ThemeUtils.onActivityCreateSetTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        checkBackStack();
    }

    /** called when we want to infalte the menu */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // request inflater from activity and inflate into its menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

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
                final String Urldonate="http://goo.gl/X2sA4D";
                final Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urldonate));
                startActivity(i);
                break;
            case android.R.id.home:
                checkBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

} /** End PrefsActivity **/