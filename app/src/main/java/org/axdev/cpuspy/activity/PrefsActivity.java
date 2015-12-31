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
import org.axdev.cpuspy.fragments.DeveloperFragment;
import org.axdev.cpuspy.fragments.CreditsFragment;
import org.axdev.cpuspy.fragments.LicenseFragment;
import org.axdev.cpuspy.fragments.WhatsNewDialog;
import org.axdev.cpuspy.services.SleepService;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;
import org.axdev.cpuspy.views.CpuSpyPreference;

public class PrefsActivity extends ThemedActivity implements ColorChooserDialog.ColorCallback {

    public static int primaryColor;

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
                findPreference("developer").setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_developer, null));
                findPreference("version").setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_version, null));
                findPreference("credit").setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_credits, null));
                findPreference("license").setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_opensource, null));

                // Tint icons depending on selected theme
                final int color = Utils.resolveColor(mContext, R.attr.colorDrawableTint);
                findPreference("developer").getIcon().setTint(color);
                findPreference("version").getIcon().setTint(color);
                findPreference("credit").getIcon().setTint(color);
                findPreference("license").getIcon().setTint(color);
            }

            /** Get versionName and set as summary */
            findPreference("version").setSummary(BuildConfig.VERSION_NAME);

            findPreference("developer").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Create new fragment and transaction
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_wrapper, new DeveloperFragment())
                            .addToBackStack(null)
                            .commit();
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
                case 0: // Light theme
                    findPreference("themes").setSummary(s[0]);
                    break;
                case 1: // Dark theme
                    findPreference("themes").setSummary(s[1]);
                    break;
                case 2: // Auto theme
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
                                    if (mContext != null && selected != position)
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
            primaryColor.setColor(((ThemedActivity) mContext).primaryColor());
            primaryColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    PrefsActivity act = (PrefsActivity) mContext;
                    if (act == null) return false;
                    new ColorChooserDialog.Builder(act, preference.getTitleRes())
                            .preselect(act.primaryColor())
                            .doneButton(R.string.md_done_label)
                            .cancelButton(R.string.md_cancel_label)
                            .backButton(R.string.md_back_label)
                            .show();
                    return true;
                }
            });

            CpuSpyPreference accentColor = (CpuSpyPreference) findPreference("accent_color");
            accentColor.setColor(((ThemedActivity) mContext).accentColor());
            accentColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    PrefsActivity act = (PrefsActivity) mContext;
                    if (act == null) return false;
                    new ColorChooserDialog.Builder(act, preference.getTitleRes())
                            .preselect(act.accentColor())
                            .accentMode(true)
                            .doneButton(R.string.md_done_label)
                            .cancelButton(R.string.md_cancel_label)
                            .backButton(R.string.md_back_label)
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
                mActionBar.setElevation(mContext.getResources().getDimension(R.dimen.ab_elevation));
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
        primaryColor = primaryColor();

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