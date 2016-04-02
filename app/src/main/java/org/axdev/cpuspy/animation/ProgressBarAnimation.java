package org.axdev.cpuspy.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

public class ProgressBarAnimation extends Animation {
    private ProgressBar mProgressBar;
    private int mTo;
    private int mFrom;
    private long mStepDuration;

    /**
     * @param fullDuration - time required to fill progress from 0% to 100%
     */
    public ProgressBarAnimation(ProgressBar progressBar, long fullDuration) {
        super();
        mProgressBar = progressBar;
        mStepDuration = fullDuration / progressBar.getMax();
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        }

        if (progress > mProgressBar.getMax()) {
            progress = mProgressBar.getMax();
        }

        mTo = progress;

        mFrom = mProgressBar.getProgress();
        setDuration(Math.abs(mTo - mFrom) * mStepDuration);
        mProgressBar.startAnimation(this);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float value = mFrom + (mTo - mFrom) * interpolatedTime;
        mProgressBar.setProgress((int) value);
    }
}