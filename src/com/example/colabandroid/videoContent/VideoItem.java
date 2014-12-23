package com.example.colabandroid.videoContent;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class VideoItem  implements Parcelable {
	public long videoID;
    public String videoTitle;
    public String videoPath;
    public Bitmap videoThumbnail;

    public VideoItem(long id, String title, String content, Bitmap vidThumbnail) {
    	this.videoID = id;
        this.videoTitle = title;
        this.videoPath = content;
        this.videoThumbnail = vidThumbnail;
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// Put item contents into Parcel...
		dest.writeLong(videoID);
		dest.writeString(videoTitle);
		dest.writeString(videoPath);
		dest.writeValue(videoThumbnail);
		
	}
}
