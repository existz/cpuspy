//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

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

import com.afollestad.materialdialogs.internal.ThemeSingleton;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.ThemedActivity;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class LicenseFragment extends Fragment implements AdapterView.OnItemClickListener {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.license_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Resources res = getResources();
        final TextView mLicenseHeader = ButterKnife.findById(getActivity(), R.id.license_header);
        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(getActivity());
        final int color = ThemeSingleton.get().widgetColor;
        final int accentColor = color == 0 ? ContextCompat.getColor(getActivity(), R.color.primary) : color;
        mLicenseHeader.setTypeface(robotoMedium);
        mLicenseHeader.setTextColor(accentColor);

        final ListView mListView1 = ButterKnife.findById(getActivity(), R.id.license_list);
        final List<String[]> licenseList = new ArrayList<>();
        licenseList.add(new String[]{"Android Support Library", "Android Open Source Project"});
        licenseList.add(new String[]{"Butter Knife", "Jake Wharton"});
        licenseList.add(new String[]{"Discrete Seekbar", "AnderWeb"});
        licenseList.add(new String[]{"Material Dialogs", "Aidan Follestad"});
        licenseList.add(new String[]{"Snackbar", "William Mora"});
        mListView1.setAdapter(new ArrayAdapter<String[]>(
                getActivity(),
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
                mText2.setTextColor(ContextCompat.getColor(getActivity(), ThemedActivity.mIsDarkTheme ?
                        R.color.secondary_text_color_dark : R.color.secondary_text_color_light));
                return view;
            }
        });

        Utils.setDynamicHeight(mListView1);
        mListView1.setDivider(null);
        mListView1.setDividerHeight(0);
        mListView1.setOnItemClickListener(this);

        final ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);

        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(res.getString(R.string.pref_title_license));
        } else {
            final SpannableString s = new SpannableString(res.getString(R.string.pref_title_license));
            s.setSpan(new TypefaceSpan(getActivity(), TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Context context = getActivity();
        switch (position) {
            case 0:
                Utils.openURL(context, "http://developer.android.com/tools/support-library/index.html");
                break;
            case 1:
                Utils.openURL(context, "https://github.com/JakeWharton/butterknife");
                break;
            case 2:
                Utils.openURL(context, "https://github.com/AnderWeb/discreteSeekBar");
                break;
            case 3:
                Utils.openURL(context, "https://github.com/afollestad/material-dialogs");
                break;
            case 4:
                Utils.openURL(context, "https://github.com/nispok/snackbar");
                break;
        }
    }
}