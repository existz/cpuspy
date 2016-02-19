package org.axdev.cpuspy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.ThemedActivity;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DeveloperFragment extends Fragment implements AdapterView.OnItemClickListener {

    @BindColor(R.color.secondary_text_color_dark) int mColorTextDark;
    @BindColor(R.color.secondary_text_color_light) int mColorTextLight;
    @BindString(R.string.pref_about_developer) String mStringDeveloper;
    @BindString(R.string.email_developer) String mStringEmailDev;
    @BindString(R.string.email_developer_summary) String mStringEmailDevSummary;
    @BindString(R.string.view_gplus) String mStringGPlus;
    @BindString(R.string.view_gplus_summary) String mStringGPlusSummary;
    @BindString(R.string.menu_donate) String mStringDonate;
    @BindString(R.string.donate_summary) String mStringDonateSummary;

    private int primaryColor;

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

        final ThemedActivity act = ((ThemedActivity) mContext);
        final int colorPrimary = act.primaryColor();
        final int colorAccent = act.accentColor();
        primaryColor = colorPrimary == 0 ? ContextCompat.getColor(mContext, R.color.primary) : colorPrimary;
        int accentColor = colorAccent == 0 ? ContextCompat.getColor(mContext, R.color.accent) : colorAccent;

        final View mHeader = ButterKnife.findById(view, R.id.developer_header);
        //noinspection ResourceAsColor
        mHeader.setBackgroundColor(primaryColor);

        final View mDivider = ButterKnife.findById(view, R.id.viewDivider);
        mDivider.setBackgroundColor(act.primaryColorDark());

        final TextView contactTitle = ButterKnife.findById(view, R.id.developer_contact_title);
        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(mContext);
        contactTitle.setTypeface(robotoMedium);
        //noinspection ResourceAsColor
        contactTitle.setTextColor(accentColor);

        final ListView mListView = ButterKnife.findById(getActivity(), R.id.developer_list);
        final List<String[]> developerList = new ArrayList<>();
        developerList.add(new String[]{mStringEmailDev, mStringEmailDevSummary});
        developerList.add(new String[]{mStringGPlus, mStringGPlusSummary});
        developerList.add(new String[]{mStringDonate, mStringDonateSummary});
        mListView.setAdapter(new ArrayAdapter<String[]>(
                mContext,
                R.layout.list_item_2,
                developerList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(
                            R.layout.list_item_2, parent, false);
                }

                // If you look at the android.R.layout.simple_list_item_2 source, you'll see
                // it's a TwoLineListItem with 2 TextViews - mText1 and mText2.
                //TwoLineListItem listItem = (TwoLineListItem) view;
                final String[] entry = developerList.get(position);
                final TextView mText1 = ButterKnife.findById(convertView, R.id.text1);
                final TextView mText2 = ButterKnife.findById(convertView, R.id.text2);

                mText1.setText(entry[0]);
                mText2.setText(entry[1]);

                mText2.setTextColor(ThemedActivity.mIsDarkTheme ? mColorTextDark : mColorTextLight);

                return convertView;
            }
        });

        Utils.setDynamicHeight(mListView);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Activity mContext = getActivity();
        switch (position) {
            case 0: // Email Developer
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "robbeane@gmail.com" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "CPUSpy Material");
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.email_developer)));
                break;
            case 1: // Google Plus
                Utils.openChromeTab(mContext, "https://plus.google.com/+RobBeane", primaryColor);
                break;
            case 2: // Donate
                Utils.openChromeTab(mContext, "https://goo.gl/X2sA4D", primaryColor);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
