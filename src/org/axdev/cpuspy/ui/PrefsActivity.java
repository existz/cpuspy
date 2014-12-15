package org.axdev.cpuspy.ui;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.fragments.*;

public class PrefsActivity extends PreferenceActivity {

    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        toolbar.setTitle(getTitle());
        toolbar.setNavigationIcon(R.drawable.ic_ab_back_mtrl_am_alpha);

        Preference aboutPref = (Preference) findPreference("about");
        aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
             public boolean onPreferenceClick(Preference preference) {
                 AboutDialog newFragment = new AboutDialog();
                 newFragment.show(getFragmentManager(), "ABOUT");
                 return true;
             }
        });
        Preference libraryPref = (Preference) findPreference("library");
        libraryPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
             public boolean onPreferenceClick(Preference preference) {
                     LibraryDialog newFragment = new LibraryDialog();
                     newFragment.show(getFragmentManager(), "LIBRARY");
                 return true;
             }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.settings_layout, new LinearLayout(this), false);

        toolbar = (Toolbar) contentView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.content_wrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

        getWindow().setContentView(contentView);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }
}
