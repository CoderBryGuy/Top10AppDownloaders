package com.example.top10appdownloaders;

import java.util.ArrayList;
import java.util.List;

public class FeedEntries {
    private static FeedEntries feedEntries =  null;
    List<EntryData> myFeedEntries;

    private FeedEntries() {        
        
        this.myFeedEntries = new ArrayList<EntryData>();
        //== feed limit 10 ==
        myFeedEntries.add(new EntryData(R.id.mnuFree, 10));
        myFeedEntries.add(new EntryData(R.id.mnuPaid, 10));
        myFeedEntries.add(new EntryData(R.id.mnuSongs, 10));

        //== feed limit 25==
        myFeedEntries.add(new EntryData(R.id.mnuFree, 25));
        myFeedEntries.add(new EntryData(R.id.mnuPaid, 25));
        myFeedEntries.add(new EntryData(R.id.mnuSongs, 25));

    }

    public static FeedEntries getInstance()
    {
        if (feedEntries == null)
            feedEntries = new FeedEntries();

        return feedEntries;
    }


}
