package org.axdev.cpuspy.views;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.ThemeSingleton;

import org.axdev.cpuspy.R;

public class CpuSpyPreferenceCategory extends PreferenceCategory {

    public CpuSpyPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CpuSpyPreferenceCategory(Context context) {
        this(context, null, 0);
    }

    public CpuSpyPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_category_custom);
        setSelectable(false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        ((TextView) view).setTextColor(ThemeSingleton.get().positiveColor);
    }
}