//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import org.axdev.cpuspy.BuildConfig;
import org.axdev.cpuspy.R;
import org.axdev.cpuspy.fragments.CreditsFragment;
import org.axdev.cpuspy.fragments.LicenseFragment;
import org.axdev.cpuspy.fragments.WhatsNewDialog;
import org.axdev.cpuspy.services.SleepService;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;
import org.axdev.cpuspy.views.CpuSpyPreference;

public class PrefsActivity extends ThemedActivity implements ColorChooserDialog.ColorCallback {

    public static class PrefsFragment extends PreferenceFragment {

        private final String googleURL = "https://plus.google.com/+RobBeane";

        private Context mContext;
        private Resources res;
        private SharedPreferences sp;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            this.mContext = this.getActivity();
            res = getResources();
            sp = PreferenceManager.getDefaultSharedPreferences(mContext);

            /** Apply preference icons for Lollipop and above */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                findPreference("developer").setIcon(ResourcesCompat.getDrawable(res, ThemedActivity.mIsDarkTheme ?
                        R.drawable.ic_developer_dark : R.drawable.ic_developer, null));
                findPreference("version").setIcon(ResourcesCompat.getDrawable(res, ThemedActivity.mIsDarkTheme ?
                        R.drawable.ic_version_dark : R.drawable.ic_version, null));
                findPreference("credit").setIcon(ResourcesCompat.getDrawable(res, ThemedActivity.mIsDarkTheme ?
                        R.drawable.ic_credits_dark : R.drawable.ic_credits, null));
                findPreference("license").setIcon(ResourcesCompat.getDrawable(res, ThemedActivity.mIsDarkTheme ?
                        R.drawable.ic_opensource_dark : R.drawable.ic_opensource, null));
            }

            /** Get versionName and set as summary */
            findPreference("version").setSummary(BuildConfig.VERSION_NAME);

            findPreference("developer").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utils.openURL(mContext, googleURL);
                    return true;
                }
            });

            findPreference("credit").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Create new fragment and transaction
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_wrapper, new CreditsFragment())
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

            /** Set current theme as summary */
            final int selected = sp.getInt("theme", 0);
            final String[] s = res.getStringArray(R.array.themes);
            switch (selected) {
                case 0:
                    findPreference("themes").setSummary(s[0]);
                    break;
                case 1:
                    findPreference("themes").setSummary(s[1]);
                    break;
                case 2:
                    findPreference("themes").setSummary(s[2]);
                    break;
            }

            findPreference("themes").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new MaterialDialog.Builder(mContext)
                            .title(res.getString(R.string.pref_title_themes))
                            .items(res.getStringArray(R.array.themes))
                            .itemsCallbackSingleChoice(selected, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View view, int position, CharSequence text) {
                                    sp.edit().putInt("theme", position).apply();
                                    if (mContext != null)
                                        getActivity().recreate();
                                    return true;
                                }
                            })
                            .positiveText(res.getString(android.R.string.ok))
                            .show();
                    return true;
                }
            });

            CpuSpyPreference primaryColor = (CpuSpyPreference) findPreference("primary_color");
            primaryColor.setColor(((ThemedActivity) mContext).primaryColor(), Utils.resolveColor(mContext, R.attr.colorAccent));
            primaryColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    PrefsActivity act = (PrefsActivity) mContext;
                    if (act == null) return false;
                    new ColorChooserDialog.Builder(act, preference.getTitleRes())
                            .preselect(act.primaryColor())
                            .backButton(R.string.action_back)
                            .doneButton(R.string.action_done)
                            .cancelButton(android.R.string.cancel)
                            .show();
                    return true;
                }
            });

            CpuSpyPreference accentColor = (CpuSpyPreference) findPreference("accent_color");
            accentColor.setColor(((ThemedActivity) mContext).accentColor(), Utils.resolveColor(mContext, R.attr.colorAccent));
            accentColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    PrefsActivity act = (PrefsActivity) mContext;
                    if (act == null) return false;
                    new ColorChooserDialog.Builder(act, preference.getTitleRes())
                            .preselect(act.accentColor())
                            .accentMode(true)
                            .backButton(R.string.action_back)
                            .doneButton(R.string.action_done)
                            .cancelButton(android.R.string.cancel)
                            .show();
                    return true;
                }
            });

            findPreference("sleepDetection").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals("true")) {
                        mContext.startService(new Intent(mContext, SleepService.class));
                    } else {
                        mContext.stopService(new Intent(mContext, SleepService.class));
                    }
                    return true;
                }
            });

            findPreference("crashReport").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals("false")) {
                        SnackbarManager.show(Snackbar.with(mContext)
                                .type(SnackbarType.MULTI_LINE)
                                .text(res.getString(R.string.snackbar_text_crashreport)));
                    }
                    return true;
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                findPreference("coloredNavBar").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (mContext != null)
                            getActivity().recreate();
                        return true;
                    }
                });
            }
        }

        @Override
        public void onStart() {
            super.onStart();

            final ActionBar mActionBar = ((AppCompatActivity) mContext).getSupportActionBar();
            assert mActionBar != null;
            mActionBar.setDisplayHomeAsUpEnabled(true);

            /** Use custom Typeface for action bar title on KitKat devices */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mActionBar.setTitle(res.getString(R.string.settings));
            } else {
                final SpannableString s = new SpannableString(res.getString(R.string.settings));
                s.setSpan(new TypefaceSpan(mContext, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Update the action bar title with the TypefaceSpan instance
                mActionBar.setTitle(s);
            }
        }

    } /** End PrefsFragment **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.content_wrapper, new PrefsFragment()).commit();
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
                final String donateURL = "http://goo.gl/X2sA4D";
                Utils.openURL(this, donateURL);
                break;
            case android.R.id.home:
                this.checkBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int color) {
        if (colorChooserDialog.isAccentMode()) {
            accentColor(color);
        } else {
            primaryColor(color);
        }
        recreate();
    }

} /** End PrefsActivity **/