//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.ThemeUtils;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.utils.Utils;

import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;

public class CreditsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.credits_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Resources res = getResources();

        final ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);

        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(res.getString(R.string.pref_about_header_credits));
        } else {
            final SpannableString s = new SpannableString(res.getString(R.string.pref_about_header_credits));
            s.setSpan(new TypefaceSpan(getActivity(), TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }

        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(getActivity());

        final TextView mCreditsHeader = ButterKnife.findById(getActivity(), R.id.credits_header);
        final TextView mTranslatorsHeader = ButterKnife.findById(getActivity(), R.id.translator_header);
        mCreditsHeader.setTypeface(robotoMedium);
        mTranslatorsHeader.setTypeface(robotoMedium);

        final ListView mListView1 = ButterKnife.findById(getActivity(), R.id.credits_list);
        final ListView mListView2 = ButterKnife.findById(getActivity(), R.id.translator_list);

        final List<String[]> creditList = new LinkedList<>();
        creditList.add(new String[]{"Icons", "Eduardo Pratti"});
        creditList.add(new String[]{"Creator", "Brandon Valosek"});
        mListView1.setAdapter(new ArrayAdapter<String[]>(
                getActivity(),
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                creditList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // Must always return just a View.
                final View view = super.getView(position, convertView, parent);

                // If you look at the android.R.layout.simple_list_item_2 source, you'll see
                // it's a TwoLineListItem with 2 TextViews - mText1 and mText2.
                //TwoLineListItem listItem = (TwoLineListItem) view;
                final String[] entry = creditList.get(position);
                final TextView mText1 = ButterKnife.findById(view, android.R.id.text1);
                final TextView mText2 = ButterKnife.findById(view, android.R.id.text2);
                mText1.setText(entry[0]);
                mText2.setText(entry[1]);
                mText2.setTextColor(ContextCompat.getColor(getActivity(), ThemeUtils.isDarkTheme ?
                        R.color.secondary_text_color_dark : R.color.secondary_text_color_light));
                return view;
            }
        });

        final List<String[]> translatorList = new LinkedList<>();
        translatorList.add(new String[]{"Bengali (India)", "suhridkhan"});
        translatorList.add(new String[]{"French", "orlith"});
        translatorList.add(new String[]{"German", "AhMaizeBalls"});
        translatorList.add(new String[]{"Hungarian", "wechy77"});
        translatorList.add(new String[]{"Italian", "sossio18"});
        translatorList.add(new String[]{"Portuguese (Brazil)", "joaomarcosgabaldi"});
        translatorList.add(new String[]{"Portuguese (Portugal)", "Marco Marinho"});
        translatorList.add(new String[]{"Russian", "gaich"});
        translatorList.add(new String[]{"Swedish", "Carl"});
        mListView2.setAdapter(new ArrayAdapter<String[]>(
                getActivity(),
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                translatorList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // Must always return just a View.
                final View view = super.getView(position, convertView, parent);

                // If you look at the android.R.layout.simple_list_item_2 source, you'll see
                // it's a TwoLineListItem with 2 TextViews - mText1 and mText2.
                //TwoLineListItem listItem = (TwoLineListItem) view;
                final String[] entry = translatorList.get(position);
                final TextView mText1 = ButterKnife.findById(view, android.R.id.text1);
                final TextView mText2 = ButterKnife.findById(view, android.R.id.text2);
                mText1.setText(entry[0]);
                mText2.setText(entry[1]);
                mText2.setTextColor(ContextCompat.getColor(getActivity(), ThemeUtils.isDarkTheme ?
                        R.color.secondary_text_color_dark : R.color.secondary_text_color_light));
                return view;
            }
        });

        mListView1.setDivider(null);
        mListView1.setDividerHeight(0);

        mListView2.setDivider(null);
        mListView2.setDividerHeight(0);

        Utils.setDynamicHeight(mListView1);
        Utils.setDynamicHeight(mListView2);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.translate_menu, menu);
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
            case R.id.menu_help_translate:
                openURL("https://cpuspy.oneskyapp.com");
        }
        return super.onOptionsItemSelected(item);
    }

    private void openURL(String s) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(s));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("CpuSpy", "Error opening: " + s);
        }
    }
}