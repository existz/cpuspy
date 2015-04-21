//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.ThemeUtils;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AboutFragment extends Fragment {

    @InjectView(R.id.card_view_about) CardView mAboutCardView;
    @InjectView(R.id.btn_github) ImageButton githubButton;
    @InjectView(R.id.btn_paypal) ImageButton paypalButton;
    @InjectView(R.id.btn_xda) ImageButton xdaButton;
    @InjectView(R.id.about_header_developer) TextView mHeaderDeveloper;
    @InjectView(R.id.about_header_contrib) TextView mHeaderContrib;
    @InjectView(R.id.iconcreator) TextView mIconCreator;
    @InjectView(R.id.developer) TextView mDeveloper;
    @InjectView(R.id.origdev) TextView mOrigDev;
    @InjectView(R.id.about_background) View mAboutView;

    private final String Urlgithub="https://www.github.com/existz/cpuspy";
    private final String Urldonate="http://goo.gl/X2sA4D";
    private final String Urlxda="http://goo.gl/AusQy8";

    /** Inflate the About layout */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.about_layout, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Use custom Typeface for action bar title on KitKat devices
        if (Build.VERSION.SDK_INT == 19) {
            final SpannableString s = new SpannableString(getResources().getString(R.string.pref_title_about));
            s.setSpan(new TypefaceSpan(getActivity(), "Roboto-Medium.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(s);
        } else {
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_title_about);
            ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(0);
        }

        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Set typeface and allow hyperlinks */
        final Typeface mediumFont = TypefaceHelper.get(getActivity().getApplicationContext(), "Roboto-Medium");

        mHeaderDeveloper.setTypeface(mediumFont);
        mHeaderContrib.setTypeface(mediumFont);

        // Allow strings to use hyperlinks
        mIconCreator.setMovementMethod(LinkMovementMethod.getInstance());
        mDeveloper.setMovementMethod(LinkMovementMethod.getInstance());
        mOrigDev.setMovementMethod(LinkMovementMethod.getInstance());

        // Set UI elements for dark and light themes
        mAboutCardView.setCardBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.card_dark_background : R.color.card_light_background));
        githubButton.setColorFilter(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.drawable_color_dark : R.color.drawable_color_light));
        paypalButton.setColorFilter(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.drawable_color_dark : R.color.drawable_color_light));
        xdaButton.setColorFilter(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.drawable_color_dark : R.color.drawable_color_light));

        /** Set OnClickListener for buttons */
        githubButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urlgithub));
                startActivity(i);
            }
        });

        paypalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urldonate));
                startActivity(i);
            }
        });

        xdaButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urlxda));
                startActivity(i);
            }
        });

        /** Extend background and animate cardview sliding up */
        final Animation slideDown = AnimationUtils.loadAnimation(getActivity(),
                R.anim.abc_slide_in_top);

        slideDown.setDuration(600);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {}

            @Override
            public void onAnimationEnd(Animation arg0) {
                final Animation slideUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.slide_in_up);

                slideUp.setDuration(375);
                mAboutCardView.setVisibility(View.VISIBLE);
                mAboutCardView.startAnimation(slideUp);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {}
        });

        mAboutView.startAnimation(slideDown);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}