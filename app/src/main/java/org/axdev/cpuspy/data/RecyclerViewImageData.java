package org.axdev.cpuspy.data;

import android.graphics.drawable.Drawable;

public class RecyclerViewImageData {
    private Drawable drawable;
    private String title;
    private String summary;

    public RecyclerViewImageData(Drawable drawable, String title, String summary){
        this.drawable = drawable;
        this.title = title;
        this.summary = summary;
    }

    public Drawable getDrawable() { return drawable; }

    public void setDrawable(Drawable drawable) { this.drawable = drawable; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}