package org.axdev.cpuspy.data;

public class ItemData {

    private String title;
    private String summary;

    public ItemData(String title, String summary){

        this.title = title;
        this.summary = summary;
    }

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