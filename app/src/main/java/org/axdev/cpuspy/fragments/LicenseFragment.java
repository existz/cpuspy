//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.ThemedActivity;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class LicenseFragment extends Fragment implements AdapterView.OnItemClickListener {

    private Context mContext;
    private int primaryColor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.license_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mContext = this.getActivity();
        final Resources res = getResources();
        final TextView mLicenseHeader = ButterKnife.findById(getActivity(), R.id.license_header);
        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(mContext);
        final ThemedActivity act = (ThemedActivity) mContext;
        primaryColor = act.primaryColor();
        final int color = act.accentColor();
        final int accentColor = color == 0 ? ContextCompat.getColor(mContext, R.color.accent) : color;
        mLicenseHeader.setTypeface(robotoMedium);
        mLicenseHeader.setTextColor(accentColor);

        final ListView mListView1 = ButterKnife.findById(getActivity(), R.id.license_list);
        final List<String[]> licenseList = new ArrayList<>(8);
        licenseList.add(new String[]{"Android Support Library", "Android Open Source Project"});
        licenseList.add(new String[]{"Android Processes", "Jared Rummler"});
        licenseList.add(new String[]{"Butter Knife", "Jake Wharton"});
        licenseList.add(new String[]{"CircleImageView", "Henning Dodenhof"});
        licenseList.add(new String[]{"Discrete Seekbar", "AnderWeb"});
        licenseList.add(new String[]{"Material Dialogs", "Aidan Follestad"});
        licenseList.add(new String[]{"Picasso", "Square"});
        licenseList.add(new String[]{"Snackbar", "William Mora"});
        mListView1.setAdapter(new ArrayAdapter<String[]>(
                mContext,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                licenseList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // Must always return just a View.
                final View view = super.getView(position, convertView, parent);

                // If you look at the android.R.layout.simple_list_item_2 source, you'll see
                // it's a TwoLineListItem with 2 TextViews - mText1 and mText2.
                //TwoLineListItem listItem = (TwoLineListItem) view;
                final String[] entry = licenseList.get(position);
                final TextView mText1 = ButterKnife.findById(view, android.R.id.text1);
                final TextView mText2 = ButterKnife.findById(view, android.R.id.text2);
                mText1.setText(entry[0]);
                mText2.setText(entry[1]);
                mText2.setTextColor(ContextCompat.getColor(mContext, ThemedActivity.mIsDarkTheme ?
                        R.color.secondary_text_color_dark : R.color.secondary_text_color_light));
                return view;
            }
        });

        Utils.setDynamicHeight(mListView1);
        mListView1.setDivider(null);
        mListView1.setDividerHeight(0);
        mListView1.setOnItemClickListener(this);

        final ActionBar mActionBar = ((AppCompatActivity) mContext).getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);

        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(res.getString(R.string.pref_title_license));
        } else {
            final SpannableString s = new SpannableString(res.getString(R.string.pref_title_license));
            s.setSpan(new TypefaceSpan(mContext, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Activity activity = getActivity();
        switch (position) {
            case 0:
                Utils.openChromeTab(activity, "http://developer.android.com/tools/support-library/index.html", primaryColor);
                break;
            case 1:
                Utils.openChromeTab(activity, "https://github.com/jaredrummler/AndroidProcesses", primaryColor);
                break;
            case 2:
                Utils.openChromeTab(activity, "https://github.com/JakeWharton/butterknife", primaryColor);
                break;
            case 3:
                Utils.openChromeTab(activity, "https://github.com/hdodenhof/CircleImageView", primaryColor);
                break;
            case 4:
                Utils.openChromeTab(activity, "https://github.com/AnderWeb/discreteSeekBar", primaryColor);
                break;
            case 5:
                Utils.openChromeTab(activity, "https://github.com/afollestad/material-dialogs", primaryColor);
                break;
            case 6:
                Utils.openChromeTab(activity, "https://github.com/square/picasso", primaryColor);
                break;
            case 7:
                Utils.openChromeTab(activity, "https://github.com/nispok/snackbar", primaryColor);
                break;
        }
    }
}