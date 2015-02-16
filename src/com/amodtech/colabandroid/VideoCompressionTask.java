package com.amodtech.colabandroid;

import java.io.File;

import com.amodtech.yaandroidffmpegwrapper.FfmpegJNIWrapper;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

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
    protected String doInBackground(String... params) {
    	//Compress the video in the background
    	Log.d("VideoCompressionTask","doInBackground");
    	
    	//Get the the path of the video to compress
    	String videoPath;
    	String videoFileName;
    	File videoFileToCompress;
    	if (params.length == 1) {
    		videoPath = params[0];
    		videoFileToCompress = new File(videoPath);
    		videoFileName = videoFileToCompress.getName();
    	} else {
    		//One or all of the params are not present - log an error and return
    		Log.d("VideoCompressionTask","doInBackground wrong number of params");
    		return null;
    	}
    	
    	//Make sure the video to compress actually exists
    	if(!videoFileToCompress.exists()) {
    		Log.d("VideoCompressionTask","doInBackground video file to compress does not exist");
    		return null;
    	}
    	
    	
    	//Report the compressed file path
    	String compressedFilePath = Environment.getExternalStorageDirectory() + "/Comp_" + videoFileName;
    	Log.d("VideoCompressionTask","doInBackground compressedFilePath: " + compressedFilePath);
    	publishProgress(compressedFilePath);
    	
    	//If the compressed file already exists then delete it first and let this task create a new one
    	File compressedVideoFile = new File(compressedFilePath);
    	if(compressedVideoFile.exists()) {
    		compressedVideoFile.delete();
    	}
    	
    	String argv[] = {"ffmpeg", "-i", videoPath, "-strict", "experimental", "-acodec", "aac", compressedFilePath};
    	Log.d("VideoCompressionTask","doInBackground Calling ffmpegWrapper");
    	int ffmpegWrapperreturnCode = FfmpegJNIWrapper.ffmpegWrapper(argv);
    	Log.d("VideoCompressionTask","doInBackground ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
    	return(compressedFilePath);
    }
    
    @Override
    protected void onProgressUpdate(String... compressedFilePath) {
    	thisTaskListener.onCompressionPorgressUpdate(compressedFilePath[0]);
    }
    
    @Override
    protected void onPostExecute(String compFilePath) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onCompressionFinished(compFilePath);
    }

}

