package org.axdev.cpuspy.activity;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.fragments.ProcessListFragment;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;

public class ProcessActivity extends ThemedActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getResources();
        final ActionBar mActionBar = getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);

       if (ThemedActivity.isLightAB(this)) {
           mActionBar.setHomeAsUpIndicator(R.drawable.ic_close);
       } else {
           mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_dark);
       }
        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionBar.setTitle(R.string.running_processes);
        } else {
            final SpannableString s = new SpannableString(res.getString(R.string.app_name_long));
            s.setSpan(new TypefaceSpan(this, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            mActionBar.setTitle(s);
        }

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(android.R.id.content, new ProcessListFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, getIntent());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
