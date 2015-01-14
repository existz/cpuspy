package org.axdev.cpuspy.ui;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.fragments.AboutFragment;
import org.axdev.cpuspy.fragments.LicenseFragment;

public class PrefsActivity extends ActionBarActivity {

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
        }

        @Override
        public void onResume() {
            super.onResume();
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                int elev = (int) getResources().getDimension(R.dimen.ab_elevation);
                ((ActionBarActivity)getActivity()).getSupportActionBar().setElevation(elev);
            }
        }

    } /** End PrefsFragment **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().add(R.id.content_wrapper, new PrefsFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getFragmentManager().getBackStackEntryCount() == 0) {
                this.finish();
            } else {
                getFragmentManager().popBackStack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

} /** End PrefsActivity **/