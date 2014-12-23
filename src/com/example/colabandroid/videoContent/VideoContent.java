package com.example.colabandroid.videoContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;

/**
 * Class representing the list of Video Content
 */
public class VideoContent {

    //An array of sample (video) items.
    public List<VideoItem> ITEMS = new ArrayList<VideoItem>();

    // A map of (video) items, by ID.
    public Map<String, VideoItem> ITEM_MAP = new HashMap<String, VideoItem>();
    
    //Context 
    private final Context mContext;
    
    public VideoContent(Context context) {
    	//Constructor
    	mContext = context;
    }

    public void addItem(VideoItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.videoTitle, item);
    }
}
