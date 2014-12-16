package org.axdev.cpuspy.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.axdev.cpuspy.R;

public class AboutFragment extends Fragment {

    private TextView iconText;
    private TextView devText;
    private TextView origText;

    /** Inflate the license layout */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_layout, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        iconText = ((TextView) getView().findViewById(R.id.iconcreator));
        devText = ((TextView) getView().findViewById(R.id.developer));
        origText = ((TextView) getView().findViewById(R.id.origdev));

        iconText.setMovementMethod(LinkMovementMethod.getInstance());
        devText.setMovementMethod(LinkMovementMethod.getInstance());
        origText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.pref_title_about);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
