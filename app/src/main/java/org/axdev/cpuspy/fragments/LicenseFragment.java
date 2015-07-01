//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.app.ListFragment;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;

public class LicenseFragment extends ListFragment implements AdapterView.OnItemClickListener {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.license_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.licenses, android.R.layout.simple_list_item_1);

        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

        /** Use custom Typeface for action bar title on KitKat devices */
        final ActionBar supportActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (supportActionBar != null) { supportActionBar.setDisplayHomeAsUpEnabled(true); }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (supportActionBar != null) {
                supportActionBar.setTitle(R.string.pref_title_license);
            }
        } else {
            final SpannableString s = new SpannableString(getResources().getString(R.string.pref_title_license));
            s.setSpan(new TypefaceSpan(getActivity(), TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            if (supportActionBar != null) {
                supportActionBar.setTitle(s);
            }
        }

        TextView mLicenseHeader = (TextView) getActivity().findViewById(R.id.license_header);
        setMediumTypeface(mLicenseHeader);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        switch (position) {
            case 0:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://developer.android.com/tools/support-library/index.html"));
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/JakeWharton/butterknife"));
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/afollestad/material-dialogs"));
                startActivity(intent);
                break;
            case 3:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/balysv/material-ripple"));
                startActivity(intent);
                break;
            case 4:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/nispok/snackbar"));
                startActivity(intent);
                break;
        }
    }

    private void setMediumTypeface(TextView tv) {
        // Applying Roboto-Medium font
        Typeface mediumFont;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediumFont = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        } else {
            mediumFont = TypefaceHelper.get(getActivity(), TypefaceHelper.MEDIUM_FONT);
        }

        tv.setTypeface(mediumFont);
    }
}