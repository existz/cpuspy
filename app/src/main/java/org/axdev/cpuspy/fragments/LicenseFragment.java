//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LicenseFragment extends Fragment {

    @InjectView(R.id.card_view_supportlib) CardView mCardViewSupport;
    @InjectView(R.id.card_view_butterknife) CardView mCardViewButter;
    @InjectView(R.id.card_view_materialdialog) CardView mCardViewDialog;
    @InjectView(R.id.card_view_materialripple) CardView mCardViewRipple;
    @InjectView(R.id.card_view_snackbar) CardView mCardViewSnackbar;
    @InjectView(R.id.supportlib) TextView mSupportLib;
    @InjectView(R.id.supportlib_summary) TextView mSupportLibSummary;
    @InjectView(R.id.materialdialog) TextView mMaterialDialog;
    @InjectView(R.id.materialdialog_summary) TextView mMaterialDialogSummary;
    @InjectView(R.id.snackbar) TextView mSnackbar;
    @InjectView(R.id.snackbar_summary) TextView mSnackbarSummary;
    @InjectView(R.id.materialripple) TextView mMaterialRipple;
    @InjectView(R.id.materialripple_summary) TextView mMaterialRippleSummary;
    @InjectView(R.id.butterknife) TextView mButterKnife;
    @InjectView(R.id.butterknife_summary) TextView mButterKnifeSummary;

    private Typeface mediumFont;

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

        // Allow strings to use HTML and hyperlinks
        mediumFont = TypefaceHelper.get(getActivity().getApplicationContext(), "Roboto-Medium");

        mSupportLib.setTypeface(mediumFont);
        mSupportLib.setMovementMethod(LinkMovementMethod.getInstance());
        mSupportLib.setText(Html.fromHtml(getResources().getString(R.string.pref_license_supportlib)));

        mMaterialDialog.setTypeface(mediumFont);
        mMaterialDialog.setMovementMethod(LinkMovementMethod.getInstance());
        mMaterialDialog.setText(Html.fromHtml(getResources().getString(R.string.pref_license_materialdialog)));

        mSnackbar.setTypeface(mediumFont);
        mSnackbar.setMovementMethod(LinkMovementMethod.getInstance());
        mSnackbar.setText(Html.fromHtml(getResources().getString(R.string.pref_license_snackbar)));

        mMaterialRipple.setTypeface(mediumFont);
        mMaterialRipple.setMovementMethod(LinkMovementMethod.getInstance());
        mMaterialRipple.setText(Html.fromHtml(getResources().getString(R.string.pref_license_materialripple)));

        mButterKnife.setTypeface(mediumFont);
        mButterKnife.setMovementMethod(LinkMovementMethod.getInstance());
        mButterKnife.setText(Html.fromHtml(getResources().getString(R.string.pref_license_butterknife)));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (sp.getBoolean("darkTheme", true)) {
            mCardViewSupport.setCardBackgroundColor(getResources().getColor(R.color.card_dark_background));
            mCardViewButter.setCardBackgroundColor(getResources().getColor(R.color.card_dark_background));
            mCardViewDialog.setCardBackgroundColor(getResources().getColor(R.color.card_dark_background));
            mCardViewRipple.setCardBackgroundColor(getResources().getColor(R.color.card_dark_background));
            mCardViewSnackbar.setCardBackgroundColor(getResources().getColor(R.color.card_dark_background));
            mSupportLibSummary.setTextColor(getResources().getColor(R.color.primary_text_color_dark));
            mButterKnifeSummary.setTextColor(getResources().getColor(R.color.primary_text_color_dark));
            mMaterialDialogSummary.setTextColor(getResources().getColor(R.color.primary_text_color_dark));
            mMaterialRippleSummary.setTextColor(getResources().getColor(R.color.primary_text_color_dark));
            mSnackbarSummary.setTextColor(getResources().getColor(R.color.primary_text_color_dark));
        } else {
            mCardViewSupport.setCardBackgroundColor(getResources().getColor(R.color.card_light_background));
            mCardViewButter.setCardBackgroundColor(getResources().getColor(R.color.card_light_background));
            mCardViewDialog.setCardBackgroundColor(getResources().getColor(R.color.card_light_background));
            mCardViewRipple.setCardBackgroundColor(getResources().getColor(R.color.card_light_background));
            mCardViewSnackbar.setCardBackgroundColor(getResources().getColor(R.color.card_light_background));
            mSupportLibSummary.setTextColor(getResources().getColor(R.color.primary_text_color));
            mButterKnifeSummary.setTextColor(getResources().getColor(R.color.primary_text_color));
            mMaterialDialogSummary.setTextColor(getResources().getColor(R.color.primary_text_color));
            mMaterialRippleSummary.setTextColor(getResources().getColor(R.color.primary_text_color));
            mSnackbarSummary.setTextColor(getResources().getColor(R.color.primary_text_color));
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}