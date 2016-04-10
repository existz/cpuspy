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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.axdev.cpuspy.adapters.RecyclerViewImageAdapter;
import org.axdev.cpuspy.R;
import org.axdev.cpuspy.data.RecyclerViewImageData;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;
import org.axdev.cpuspy.widget.RecyclerLinearLayoutManager;

import butterknife.BindString;
import butterknife.ButterKnife;

public class LicenseFragment extends Fragment {

    @BindString(R.string.pref_title_license) String mStringLicense;

    private Context mContext;
    private SharedPreferences sp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.license_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = this.getActivity();
        final TextView mLicenseHeader = ButterKnife.findById(getActivity(), R.id.license_header);
        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(mContext);

        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        final int accentColor = sp.getInt("accent_color", ContextCompat.getColor(mContext, R.color.material_blue_500));
        mLicenseHeader.setTypeface(robotoMedium);
        mLicenseHeader.setTextColor(accentColor);

        final RecyclerView mLicenseRecyclerView = ButterKnife.findById(view, R.id.license_list);
        mLicenseRecyclerView.setNestedScrollingEnabled(false);
        final RecyclerViewImageData itemsData[] = {
                new RecyclerViewImageData(R.drawable.android, "Android Support Library", "Android Open Source Project"),
                new RecyclerViewImageData(R.drawable.jared_rummler, "Android Processes", "Jared Rummler"),
                new RecyclerViewImageData(R.drawable.jake_wharton, "Butter Knife", "Jake Wharton"),
                new RecyclerViewImageData(R.drawable.henning_dodenhof, "CircleImageView", "Henning Dodenhof"),
                new RecyclerViewImageData(R.drawable.anderweb, "Discrete Seekbar", "AnderWeb"),
                new RecyclerViewImageData(R.drawable.chainfire, "libsuperuser", "Chainfire"),
                new RecyclerViewImageData(R.drawable.aidan_follestad, "Material Dialogs", "Aidan Follestad"),
                new RecyclerViewImageData(R.drawable.fabien_devos, "NanoTasks", "Fabien Devos"),
                new RecyclerViewImageData(R.drawable.square, "Picasso", "Square Inc."),
                new RecyclerViewImageData(R.drawable.william_mora, "Snackbar", "William Mora")
        };

        final RecyclerLinearLayoutManager mLinearLayoutManager = new RecyclerLinearLayoutManager(mContext);
        final RecyclerViewImageAdapter mLicenseRecyclerViewAdapter = new RecyclerViewImageAdapter(itemsData);
        mLicenseRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Activity activity = getActivity();
                final int primaryColor = sp.getInt("primary_color", ContextCompat.getColor(mContext, R.color.material_blue_500));

                switch (position) {
                    case 0:
                        Utils.openChromeTab(activity, "http://developer.android.com/tools/support-library/index.html", primaryColor);
                        break;
                    case 1:
                        Utils.openChromeTab(activity, "https://github.com/jaredrummler/AndroidProcesses", primaryColor);
                        break;
                    case 2:
                        Utils.openChromeTab(activity, "https://github.com/JakeWharton/butterknife", primaryColor);
                        break;
                    case 3:
                        Utils.openChromeTab(activity, "https://github.com/hdodenhof/CircleImageView", primaryColor);
                        break;
                    case 4:
                        Utils.openChromeTab(activity, "https://github.com/AnderWeb/discreteSeekBar", primaryColor);
                        break;
                    case 5:
                        Utils.openChromeTab(activity, "https://github.com/Chainfire/libsuperuser", primaryColor);
                        break;
                    case 6:
                        Utils.openChromeTab(activity, "https://github.com/afollestad/material-dialogs", primaryColor);
                        break;
                    case 7:
                        Utils.openChromeTab(activity, "https://github.com/fabiendevos/nanotasks", primaryColor);
                        break;
                    case 8:
                        Utils.openChromeTab(activity, "https://github.com/square/picasso", primaryColor);
                        break;
                    case 9:
                        Utils.openChromeTab(activity, "https://github.com/nispok/snackbar", primaryColor);
                        break;
                }
            }
        });
        mLicenseRecyclerView.setLayoutManager(mLinearLayoutManager);
        mLicenseRecyclerView.setAdapter(mLicenseRecyclerViewAdapter);
        mLicenseRecyclerView.setItemAnimator(new DefaultItemAnimator());

        final ActionBar mActionBar = ((AppCompatActivity) mContext).getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);

        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(mStringLicense);
        } else {
            final SpannableString s = new SpannableString(mStringLicense);
            s.setSpan(new TypefaceSpan(mContext, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}