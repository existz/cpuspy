package org.axdev.cpuspy.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.axdev.cpuspy.R;

public class LicenseFragment extends Fragment {

    /** Inflate the license layout */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.license_layout, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Medium.ttf");

        // Applying Roboto-Medium font
        ((TextView) getView().findViewById(R.id.supportlib)).setTypeface(tf);
        ((TextView) getView().findViewById(R.id.materialdialog)).setTypeface(tf);
        ((TextView) getView().findViewById(R.id.snackbar)).setTypeface(tf);

        // Allow strings to use HTML and hyperlinks
        ((TextView) getView().findViewById(R.id.supportlib)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) getView().findViewById(R.id.supportlib)).setText(Html.fromHtml(getResources().getString(R.string.pref_license_supportlib)));

        ((TextView) getView().findViewById(R.id.materialdialog)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) getView().findViewById(R.id.materialdialog)).setText(Html.fromHtml(getResources().getString(R.string.pref_license_materialdialog)));

        ((TextView) getView().findViewById(R.id.snackbar)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) getView().findViewById(R.id.snackbar)).setText(Html.fromHtml(getResources().getString(R.string.pref_license_snackbar)));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.pref_title_license);
    }

}