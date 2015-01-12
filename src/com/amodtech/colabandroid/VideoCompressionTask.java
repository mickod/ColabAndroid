package com.amodtech.colabandroid;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.amodtech.colabandroid.FfmpegJNIWrapper;

public class VideoCompressionTask extends AsyncTask<String, String, String> {
	/* This Class is an AsynchTask to compress a video on a background thread
	 * 
	 */
	
	private CompressionTaskListener thisTaskListener;
	
	public VideoCompressionTask(CompressionTaskListener ourListener) {
		//Set the listener
		thisTaskListener = ourListener;
	}

    @Override
    protected String doInBackground(String... videoPath) {
    	//Compress the video in the background
    	Log.d("VideoCompressionTask","doInBackground");
    	
    	//Report the compressed file path
    	publishProgress(Environment.getExternalStorageDirectory() + "/CompressedBBB_320x180_aac.mp4");
    	
    	String argv[] = {"ffmpeg", "-i", Environment.getExternalStorageDirectory() + "/DCIM/Camera/BigBuckBunny_320x180.mp4", "-strict", "experimental", "-acodec", "aac", Environment.getExternalStorageDirectory() + "/CompressedBBB_320x180_aac.mp4"};
    	Log.d("VideoCompressionTask","Calling ffmpegWrapper");
    	int ffmpegWrapperreturnCode = FfmpegJNIWrapper.ffmpegWrapper(argv);
    	Log.d("VideoCompressionTask","ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
    	return("DONE");
    }
    
    @Override
    protected void onProgressUpdate(String... compressedFilePath) {
    	thisTaskListener.onCompressionPorgressUpdate(compressedFilePath[0]);
    }
    
    @Override
    protected void onPostExecute(String result) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onCompressionFinished("Compessed Path...");
    }

}

