package org.axdev.cpuspy.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import org.axdev.cpuspy.R;

public class AboutFragment extends Fragment {

    private final String Urlgithub="https://www.github.com/existz/cpuspy";
    private final String Urldonate="http://goo.gl/X2sA4D";
    private final String Urlxda="http://goo.gl/AusQy8";

    /** Inflate the About layout */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set title and fix elevation for layout header
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.pref_title_about);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Medium.ttf");

        // Applying Roboto-Medium font
        ((TextView) view.findViewById(R.id.about_header_developer)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.about_header_contrib)).setTypeface(tf);

        // Allow strings to use hyperlinks
        ((TextView) view.findViewById(R.id.iconcreator)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.developer)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.origdev)).setMovementMethod(LinkMovementMethod.getInstance());

        // Set OnClickListener for buttons
        ImageButton githubButton = (ImageButton) view.findViewById(R.id.btn_github);
        githubButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urlgithub));
                startActivity(i);
            }
        });

        ImageButton paypalButton = (ImageButton) view.findViewById(R.id.btn_paypal);
        paypalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urldonate));
                startActivity(i);
            }
        });

        ImageButton xdaButton = (ImageButton) view.findViewById(R.id.btn_xda);
        xdaButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urlxda));
                startActivity(i);
            }
        });
    }
}