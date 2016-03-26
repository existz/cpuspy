package org.axdev.cpuspy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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

import com.afollestad.materialdialogs.color.CircleView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.ThemedActivity;
import org.axdev.cpuspy.adapters.RecyclerViewAdapter;
import org.axdev.cpuspy.data.ItemData;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;
import org.axdev.cpuspy.widget.RecyclerLinearLayoutManager;
import org.axdev.cpuspy.views.DividerItemDecoration;

import butterknife.BindString;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DeveloperFragment extends Fragment {

    @BindString(R.string.pref_about_developer) String mStringDeveloper;
    @BindString(R.string.email_developer) String mStringEmailDev;
    @BindString(R.string.email_developer_summary) String mStringEmailDevSummary;
    @BindString(R.string.view_gplus) String mStringGPlus;
    @BindString(R.string.view_gplus_summary) String mStringGPlusSummary;
    @BindString(R.string.menu_donate) String mStringDonate;
    @BindString(R.string.donate_summary) String mStringDonateSummary;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.developer_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context mContext = getActivity();
        final ActionBar mActionBar = ((AppCompatActivity) mContext).getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setElevation(0);

        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(mStringDeveloper);
        } else {
            final SpannableString s = new SpannableString(mStringDeveloper);
            s.setSpan(new TypefaceSpan(mContext, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }

        final CircleImageView imageView = ButterKnife.findById(view, R.id.profile_image);
        imageView.setBorderColor(ContextCompat.getColor(mContext, ThemedActivity.mIsDarkTheme ?
                android.R.color.white : R.color.light_background));
        imageView.setBorderWidth((int) getResources().getDimension(ThemedActivity.mIsDarkTheme ?
                R.dimen.circleimage_border_medium : R.dimen.circleimage_border_large));

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        final int primaryColor = sp.getInt("primary_color", ContextCompat.getColor(mContext, R.color.material_blue_500));
        final int accentColor = sp.getInt("accent_color", ContextCompat.getColor(mContext, R.color.material_blue_500));

        final View mHeader = ButterKnife.findById(view, R.id.developer_header);
        mHeader.setBackgroundColor(primaryColor);

        final View mDivider = ButterKnife.findById(view, R.id.viewDivider);
        mDivider.setBackgroundColor(CircleView.shiftColorDown(primaryColor));

        final TextView contactTitle = ButterKnife.findById(view, R.id.developer_contact_title);
        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(mContext);
        contactTitle.setTypeface(robotoMedium);
        contactTitle.setTextColor(accentColor);

        final RecyclerView mDeveloperRecyclerView = ButterKnife.findById(view, R.id.developer_list);
        final ItemData creditsData[] = {
                new ItemData(mStringEmailDev, mStringEmailDevSummary),
                new ItemData(mStringGPlus, mStringGPlusSummary),
                new ItemData(mStringDonate, mStringDonateSummary)};

        final RecyclerLinearLayoutManager mLinearLayoutManager = new RecyclerLinearLayoutManager(mContext);
        mLinearLayoutManager.setScrollEnabled(false);
        final RecyclerViewAdapter mCreditsRecyclerViewAdapter = new RecyclerViewAdapter(creditsData);
        mCreditsRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Activity activity = getActivity();

                switch (position) {
                    case 0: // Email Developer
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("plain/text");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "robbeane@gmail.com" });
                        intent.putExtra(Intent.EXTRA_SUBJECT, "CPUSpy Material");
                        startActivity(Intent.createChooser(intent, mStringEmailDev));
                        break;
                    case 1: // Google Plus
                        Utils.openChromeTab(activity, "https://plus.google.com/+RobBeane", primaryColor);
                        break;
                    case 2: // Donate
                        Utils.openChromeTab(activity, "https://goo.gl/X2sA4D", primaryColor);
                        break;
                }
            }
        });
        mDeveloperRecyclerView.setLayoutManager(mLinearLayoutManager);
        mDeveloperRecyclerView.setAdapter(mCreditsRecyclerViewAdapter);
        mDeveloperRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mDeveloperRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, null));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
