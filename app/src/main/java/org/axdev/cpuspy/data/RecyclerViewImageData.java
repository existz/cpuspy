package org.axdev.cpuspy.data;

public class RecyclerViewImageData {
    private int drawable;
    private String title;
    private String summary;

    public RecyclerViewImageData(int drawable, String title, String summary){
        this.drawable = drawable;
        this.title = title;
        this.summary = summary;
    }

    public int getDrawable() { return drawable; }

    public void setDrawable(int drawable) { this.drawable = drawable; }

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