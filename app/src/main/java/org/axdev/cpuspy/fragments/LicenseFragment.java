//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.TypefaceSpan;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LicenseFragment extends Fragment {

    @InjectView(R.id.supportlib) TextView mSupportLib;
    @InjectView(R.id.materialdialog) TextView mMaterialDialog;
    @InjectView(R.id.snackbar) TextView mSnackbar;
    @InjectView(R.id.switchprefcompat) TextView mSwitchPrefCompat;
    @InjectView(R.id.materialripple) TextView mMaterialRipple;
    @InjectView(R.id.butterknife) TextView mButterKnife;

    /** Inflate the license layout */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.license_layout, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Use custom Typeface for action bar title on KitKat devices
        if (Build.VERSION.SDK_INT == 19) {
            SpannableString s = new SpannableString(getResources().getString(R.string.pref_title_license));
            s.setSpan(new TypefaceSpan(getActivity(), "Roboto-Medium.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(s);
        } else {
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_title_license);
        }

        ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Medium.ttf");

        // Allow strings to use HTML and hyperlinks
        mSupportLib.setTypeface(tf);
        mSupportLib.setMovementMethod(LinkMovementMethod.getInstance());
        mSupportLib.setText(Html.fromHtml(getResources().getString(R.string.pref_license_supportlib)));

        mMaterialDialog.setTypeface(tf);
        mMaterialDialog.setMovementMethod(LinkMovementMethod.getInstance());
        mMaterialDialog.setText(Html.fromHtml(getResources().getString(R.string.pref_license_materialdialog)));

        mSnackbar.setTypeface(tf);
        mSnackbar.setMovementMethod(LinkMovementMethod.getInstance());
        mSnackbar.setText(Html.fromHtml(getResources().getString(R.string.pref_license_snackbar)));

        mSwitchPrefCompat.setTypeface(tf);
        mSwitchPrefCompat.setMovementMethod(LinkMovementMethod.getInstance());
        mSwitchPrefCompat.setText(Html.fromHtml(getResources().getString(R.string.pref_license_switchprefcompat)));

        mMaterialRipple.setTypeface(tf);
        mMaterialRipple.setMovementMethod(LinkMovementMethod.getInstance());
        mMaterialRipple.setText(Html.fromHtml(getResources().getString(R.string.pref_license_materialripple)));

        mButterKnife.setTypeface(tf);
        mButterKnife.setMovementMethod(LinkMovementMethod.getInstance());
        mButterKnife.setText(Html.fromHtml(getResources().getString(R.string.pref_license_butterknife)));
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}