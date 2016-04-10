//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.adapters.RecyclerViewImageAdapter;
import org.axdev.cpuspy.adapters.RecyclerViewAdapter;
import org.axdev.cpuspy.data.RecyclerViewData;
import org.axdev.cpuspy.data.RecyclerViewImageData;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;
import org.axdev.cpuspy.widget.RecyclerLinearLayoutManager;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

public class CreditsFragment extends Fragment {

    @BindDrawable(R.drawable.brandon_velosek) Drawable mBrandonVelosekDrawable;
    @BindDrawable(R.drawable.eduardo_pratti) Drawable mEduardoPrattiDrawable;
    @BindString(R.string.pref_about_header_credits) String mStringCredits;

    private SharedPreferences sp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.credits_layout, container, false);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context mContext = this.getActivity();

        final ActionBar mActionBar = ((AppCompatActivity) mContext).getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);

        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(mStringCredits);
        } else {
            final SpannableString s = new SpannableString(mStringCredits);
            s.setSpan(new TypefaceSpan(mContext, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }

        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(mContext);

        final TextView mCreditsHeader = ButterKnife.findById(getActivity(), R.id.credits_header);
        final TextView mTranslatorsHeader = ButterKnife.findById(getActivity(), R.id.translator_header);

        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        final int materialBlue500 = ContextCompat.getColor(mContext, R.color.material_blue_500);
        final int primaryColor = sp.getInt("primary_color", materialBlue500);
        final int accentColor = sp.getInt("accent_color", materialBlue500);
        mCreditsHeader.setTypeface(robotoMedium);
        mCreditsHeader.setTextColor(accentColor);
        mTranslatorsHeader.setTypeface(robotoMedium);
        mTranslatorsHeader.setTextColor(accentColor);

        final RecyclerView mCreditsRecyclerView = ButterKnife.findById(view, R.id.credits_list);
        mCreditsRecyclerView.setNestedScrollingEnabled(false);
        final RecyclerViewImageData creditsData[] = {
                new RecyclerViewImageData(mEduardoPrattiDrawable, "Icons", "Eduardo Pratti"),
                new RecyclerViewImageData(mBrandonVelosekDrawable, "Creator", "Brandon Valosek")
        };

        final RecyclerLinearLayoutManager mLinearLayoutManager = new RecyclerLinearLayoutManager(mContext);
        final RecyclerViewImageAdapter mCreditsRecyclerViewAdapter = new RecyclerViewImageAdapter(creditsData);
        mCreditsRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Activity activity = getActivity();
                switch (position) {
                    case 0:
                        Utils.openChromeTab(activity, "https://plus.google.com/+EduardoPratti", primaryColor);
                        break;
                    case 1:
                        Utils.openChromeTab(activity, "https://github.com/bvalosek", primaryColor);
                        break;
                }
            }
        });
        mCreditsRecyclerView.setLayoutManager(mLinearLayoutManager);
        mCreditsRecyclerView.setAdapter(mCreditsRecyclerViewAdapter);
        mCreditsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        final RecyclerView mTranslatorRecyclerView = ButterKnife.findById(view, R.id.translator_list);
        mTranslatorRecyclerView.setNestedScrollingEnabled(false);
        final RecyclerViewData translatorData[] = {
                new RecyclerViewData("Bengali (India)", "suhridkhan"),
                new RecyclerViewData("French", "M1ck, orlith"),
                new RecyclerViewData("German", "AhMaizeBalls"),
                new RecyclerViewData("Greek", "VasilisKos"),
                new RecyclerViewData("Hungarian", "wechy77"),
                new RecyclerViewData("Italian", "sossio18"),
                new RecyclerViewData("Portuguese (Brazil)", "joaomarcosgabaldi"),
                new RecyclerViewData("Portuguese (Portugal)", "Marco Marinho"),
                new RecyclerViewData("Russian", "gaich"),
                new RecyclerViewData("Simplified Chinese", "ContactFront"),
                new RecyclerViewData("Swedish", "Carl")
        };

        final RecyclerLinearLayoutManager mLinearLayoutManager2 = new RecyclerLinearLayoutManager(mContext);
        final RecyclerViewAdapter mTranslatorRecyclerViewAdapter = new RecyclerViewAdapter(translatorData);
        mTranslatorRecyclerViewAdapter.setOnItemClickListener(null);
        mTranslatorRecyclerView.setLayoutManager(mLinearLayoutManager2);
        mTranslatorRecyclerView.setAdapter(mTranslatorRecyclerViewAdapter);
        mTranslatorRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.translate_menu, menu);
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        final int primaryColor = sp.getInt("primary_color", ContextCompat.getColor(getActivity(), R.color.material_blue_500));
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
            case R.id.menu_help_translate:
                Utils.openChromeTab(getActivity(), "https://cpuspy.oneskyapp.com", primaryColor);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}