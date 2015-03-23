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

public class LicenseFragment extends Fragment {

    /** Inflate the license layout */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.license_layout, container, false);
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

        // Applying Roboto-Medium font
        ((TextView) view.findViewById(R.id.supportlib)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.materialdialog)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.snackbar)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.switchprefcompat)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.materialripple)).setTypeface(tf);

        // Allow strings to use HTML and hyperlinks
        ((TextView) view.findViewById(R.id.supportlib)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.supportlib)).setText(Html.fromHtml(getResources().getString(R.string.pref_license_supportlib)));

        ((TextView) view.findViewById(R.id.materialdialog)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.materialdialog)).setText(Html.fromHtml(getResources().getString(R.string.pref_license_materialdialog)));

        ((TextView) view.findViewById(R.id.snackbar)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.snackbar)).setText(Html.fromHtml(getResources().getString(R.string.pref_license_snackbar)));

        ((TextView) view.findViewById(R.id.switchprefcompat)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.switchprefcompat)).setText(Html.fromHtml(getResources().getString(R.string.pref_license_switchprefcompat)));

        ((TextView) view.findViewById(R.id.materialripple)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.materialripple)).setText(Html.fromHtml(getResources().getString(R.string.pref_license_materialripple)));
    }
}