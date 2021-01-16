package com.example.top10appdownloaders;

import android.util.Log;

import java.util.List;

public class EntryData {
    int menuResource;
    int feedLimit = 10;
    List<FeedEntry> applications  = null;
    private static final String TAG = "EntryData";

    public EntryData(int menuResource, int feedLimit) {

        Log.d(TAG, "EntryData: creating new EntryData \n" +
                " menuResource = " + menuResource + " feedLimit = " + feedLimit);

        if(feedLimit != 10 && feedLimit != 25){
            try {
                throw new Exception("error feed limit out of bounds");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.feedLimit = feedLimit;
        this.menuResource = menuResource;
    }

    public void setApplications(List<FeedEntry> applications) {
        this.applications = applications;
    }

    public List<FeedEntry> getApplications() {
        return applications;
    }

    public int getMenuResource() {
        return menuResource;
    }

    public boolean isNull(){
        return (applications == null);
    }

    public int getFeedLimit() {
        return feedLimit;
    }

}
