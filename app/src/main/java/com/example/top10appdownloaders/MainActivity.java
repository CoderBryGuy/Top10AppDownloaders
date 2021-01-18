package com.example.top10appdownloaders;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listApps;
    private String feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
    private int currentMenuId = R.id.mnuFree;
    private final FeedEntries feedEntries = FeedEntries.getInstance();
    private final String URL_ID = "urlId";
    private final String FEED_LIMIT_ID = "feedLimitId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listApps = (ListView)findViewById(R.id.xmlListView);

        if(savedInstanceState != null){
            feedURL = savedInstanceState.getString(URL_ID, "onRestoreInstanceState() failed");
            feedLimit = savedInstanceState.getInt(FEED_LIMIT_ID, 10);
        }
        downloadURL(String.format(feedURL, feedLimit));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(URL_ID, feedURL);
        outState.putInt(FEED_LIMIT_ID, feedLimit);
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
    }

//    @Override
//    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        feedURL = savedInstanceState.getString(URL_ID, "onRestoreInstanceState() failed");
//        feedLimit = savedInstanceState.getInt(FEED_LIMIT_ID, 10);
//        Log.d(TAG, "onRestoreInstanceState: ");
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.feeds_menu, menu);
       if(feedLimit == 10){
           menu.findItem(R.id.mnu10).setChecked(true);
       }else {
           menu.findItem(R.id.mnu25).setChecked(true);
       }
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.mnuFree:
                setCurrentMenuId(R.id.mnuFree);
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.mnuPaid:
                setCurrentMenuId(R.id.mnuPaid);
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                setCurrentMenuId(R.id.mnuSongs);
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnu10:
            case R.id.mnu25:
                if(!item.isChecked()){
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + "setting feedLimit to " + feedLimit);
                }else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + "feed limit unchanged");
                }
                break;
            case R.id.menuRefresh:
                for (EntryData data : feedEntries.myFeedEntries
                ) {
                    if(data.getMenuResource() == currentMenuId && data.getFeedLimit() == feedLimit && !data.isNull()){
                        data.nullifyApplications();
                        Log.d(TAG, "onOptionsItemSelected: Refresh called, nullify data.getApplications()");
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        downloadURL(String.format(feedURL, feedLimit));
        return true;

    }

    private void setCurrentMenuId(int id) {
        currentMenuId = id;
    }

    private void downloadURL(String feedURL) {

        for (EntryData data : feedEntries.myFeedEntries
             ) {
            if(data.getMenuResource() == currentMenuId && data.getFeedLimit() == feedLimit && !data.isNull()){
                setFeedAdapter(data);
                Log.d(TAG, "downloadURL: XML doc already exists. Retrieving data.getApplications()");
                return;
            }
        }

        Log.d(TAG, "downloadURL:  starting async task");
        DownloadData downloadData = new DownloadData();
        downloadData.execute(feedURL);
        Log.d(TAG, "downloadURL: done");
    }

    public void setFeedAdapter(EntryData data){
        FeedAdapter<FeedEntry> arrayAdapter = new FeedAdapter<>(getApplicationContext(), R.layout.list_record, data.getApplications());
        listApps.setAdapter(arrayAdapter);
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: the response code was " + response);
//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while(true){

                    charsRead = reader.read(inputBuffer);
                    if(charsRead < 0){
                        break;
                    }
                    if(charsRead > 0){
                        xmlResult.append(String.copyValueOf(inputBuffer, 0 , charsRead));
                    }
                }

                reader.close();
                return xmlResult.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data" + e.getMessage());
            }catch(SecurityException e){
                Log.e(TAG, "downloadXML: security exception? Needs permission?" + e.getMessage());
                e.printStackTrace();
            }
          return null;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
//            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            for (EntryData data : feedEntries.myFeedEntries
                 ) {
                if((data.getMenuResource() == currentMenuId) && (data.getFeedLimit() == feedLimit) && data.isNull()){
                    data.setApplications(parseApplications.getApplications());
                    MainActivity.this.setFeedAdapter(data);
//                    setFeedAdapter(data);
                    Log.d(TAG, "onPostExecute: setting data.setApplications()");
                    return;
                }
//                else if(data.getMenuResource() == currentMenuId && data.getFeedLimit() == feedLimit && !data.isNull()){
//                    setFeedAdapter(data);
//                    Log.d(TAG, "onPostExecute: retrieving data.getApplications()");
//                }
            }

        }


//        public void setFeedAdapter(EntryData data){
//            FeedAdapter<FeedEntry> arrayAdapter = new FeedAdapter<>(getApplicationContext(), R.layout.list_record, data.getApplications());
//            listApps.setAdapter(arrayAdapter);
//        }


    }
}