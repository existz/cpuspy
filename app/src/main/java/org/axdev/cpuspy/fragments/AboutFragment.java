package org.axdev.cpuspy.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.axdev.cpuspy.R;

public class AboutFragment extends Fragment {

    /** Inflate the About layout */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_layout, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Medium.ttf");

        // Applying Roboto-Medium font
        ((TextView) getView().findViewById(R.id.about_header_developer)).setTypeface(tf);
        ((TextView) getView().findViewById(R.id.about_header_contrib)).setTypeface(tf);

        // Allow strings to use hyperlinks
        ((TextView) getView().findViewById(R.id.iconcreator)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) getView().findViewById(R.id.developer)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) getView().findViewById(R.id.origdev)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.pref_title_about);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(0);
        }
    }
}