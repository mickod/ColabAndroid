package com.amodtech.colabandroid;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.amodtech.colabandroid.FfmpegJNIWrapper;

public class VideoCompressionTask extends AsyncTask<String, Void, String> {
	/* This Class is an AsynchTask to compress a video on a backgroud thread
	 * 
	 */
	
    private final WeakReference<String> videoPath;

    public VideoCompressionTask(String videoFilePath) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
    	videoPath = new WeakReference<String>(videoFilePath);
    }

    // Decode image in background.
    @Override
    protected String doInBackground(String... params) {
    	String argv[] = {"ffmpeg", "-i", Environment.getExternalStorageDirectory() + "/DCIM/Camera/BigBuckBunny_320x180.mp4", "-strict", "experimental", "-acodec", "aac", Environment.getExternalStorageDirectory() + "/CompressedBBB_320x180_aac.mp4"};
    	Log.d("CompressionActivity onCreate","Calling ffmpegWrapper");
    	int ffmpegWrapperreturnCode = FfmpegJNIWrapper.ffmpegWrapper(argv);
    	Log.d("CompressionActivity onCreate","ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
    	return("DONE");
    }

}

