//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
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
        this.setThemeAttributes();
        this.setTypeface();
        this.startAnimation();

        final ActionBar supportActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (supportActionBar != null) { supportActionBar.setDisplayHomeAsUpEnabled(true); }

        // Use custom Typeface for action bar title on KitKat devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (supportActionBar != null) {
                supportActionBar.setTitle(R.string.pref_title_about);
                supportActionBar.setElevation(0);
            }
        } else {
            final SpannableString s = new SpannableString(getResources().getString(R.string.pref_title_about));
            s.setSpan(new TypefaceSpan(getActivity(), TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            if (supportActionBar != null) {
                supportActionBar.setTitle(s);
            }
        }

        /** Set OnClickListener for buttons */
        githubButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urlgithub));
                startActivity(i);
            }
        });

        paypalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urldonate));
                startActivity(i);
            }
        });

        xdaButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Urlxda));
                startActivity(i);
            }
        });
    }

    /** Extend background and animate cardview */
    private void startAnimation() {
        final Animation slideDown = AnimationUtils.loadAnimation(getActivity(),
                R.anim.abc_slide_in_top);

        slideDown.setDuration(600);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final View view = getView();
                    if (view != null) {
                        int cx = view.getWidth() / 2;
                        int cy = view.getHeight() / 2;
                        int radius = view.getWidth();

                        Animator anim =
                                ViewAnimationUtils.createCircularReveal(mAboutCardView, cx, cy, 0, radius);

                        anim.setDuration(500);
                        anim.setInterpolator(new AccelerateDecelerateInterpolator());
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                mAboutCardView.setVisibility(View.VISIBLE);
                            }
                        });
                        anim.start();
                    }
                } else {
                    mAboutCardView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
        });

        mAboutView.startAnimation(slideDown);
    }

    /** Set typeface and allow hyperlinks */
    private void setTypeface() {
        final Typeface mediumFont = TypefaceHelper.get(getActivity(), TypefaceHelper.MEDIUM_FONT);

        mHeaderDeveloper.setTypeface(mediumFont);
        mHeaderContrib.setTypeface(mediumFont);

        // Allow strings to use hyperlinks
        mIconCreator.setMovementMethod(LinkMovementMethod.getInstance());
        mDeveloper.setMovementMethod(LinkMovementMethod.getInstance());
        mOrigDev.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /** Set UI elements for dark and light themes */
    private void setThemeAttributes() {
        final ColorStateList dark = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_dark));
        final ColorStateList light = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_light));

        mAboutCardView.setCardBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.card_dark_background : R.color.card_light_background));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            githubButton.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
            paypalButton.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
            xdaButton.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
        } else {
            final Drawable githubDrawable = DrawableCompat.wrap(githubButton.getDrawable());
            githubButton.setImageDrawable(githubDrawable);
            DrawableCompat.setTintList(githubDrawable, (ThemeUtils.DARKTHEME ? dark : light));

            final Drawable paypalDrawable = DrawableCompat.wrap(paypalButton.getDrawable());
            paypalButton.setImageDrawable(paypalDrawable);
            DrawableCompat.setTintList(paypalDrawable, (ThemeUtils.DARKTHEME ? dark : light));

            final Drawable xdaDrawable = DrawableCompat.wrap(xdaButton.getDrawable());
            xdaButton.setImageDrawable(xdaDrawable);
            DrawableCompat.setTintList(xdaDrawable, (ThemeUtils.DARKTHEME ? dark : light));
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}