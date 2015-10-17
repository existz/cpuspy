package org.axdev.cpuspy.views;

import android.content.Context;
import android.preference.Preference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.ThemedActivity;

@SuppressWarnings("NullableProblems")
public class CpuSpyPreference extends Preference {

    private View mView;
    private int color;

    public CpuSpyPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CpuSpyPreference(Context context) {
        this(context, null, 0);
    }

    public CpuSpyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_custom);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mView = view;
        invalidateColor();
    }

    public void setColor(int color) {
        this.color = color;
        invalidateColor();
    }

    private void invalidateColor() {
        if (mView != null) {
            BorderCircleView circle = (BorderCircleView) mView.findViewById(R.id.circle);
            if (this.color != 0) {
                circle.setVisibility(View.VISIBLE);
                circle.setBackgroundColor(color);
                if (ThemedActivity.mIsDarkTheme) {
                    circle.setBorderColor(ContextCompat.getColor(getContext(), android.R.color.white));
                } else {
                    circle.setBorderColor(ContextCompat.getColor(getContext(), android.R.color.black));
                }
            } else {
                circle.setVisibility(View.GONE);
            }
        }
    }
}