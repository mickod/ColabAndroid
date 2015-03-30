package com.amodtech.colabandroid;

import java.io.File;

import com.amodtech.yaandroidffmpegwrapper.FfmpegJNIWrapper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class VideoCompressionTask extends AsyncTask<String, String, String> {
	/* This Class is an AsynchTask to compress a video on a background thread
	 * 
	 */
	
	private CompressionTaskListener thisTaskListener;
	private Context appContext;
	
	public VideoCompressionTask(Context appCon, CompressionTaskListener ourListener) {
		//Set the listener
		thisTaskListener = ourListener;
		this.appContext = appCon;
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
    	String argv2[] = {"ffmpeg", "-i", videoPath, "-strict", "experimental", "-acodec", "aac", compressedFilePath};
    	String argv3[] = {"ffmpeg", "-version"};
    	String argv4[] = {"ffmpeg", "-i", videoPath,  "-i", Environment.getExternalStorageDirectory() + "/beetle.png", "-filter_complex",  "\"overlay=10:10\"", 
    				compressedFilePath};
    	Log.d("VideoCompressionTask","doInBackground Calling ffmpegWrapper");
    	int ffmpegWrapperreturnCode = FfmpegJNIWrapper.call_ffmpegWrapper(appContext, argv);
    	Log.d("VideoCompressionTask","doInBackground ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
    	//XXXX REMOVE Log.d("VideoCompressionTask","doInBackground Calling ffmpegWrapper for second time");
    	//XXXX REMOVEcompressedVideoFile = new File(compressedFilePath);
    	//XXXX REMOVEcompressedVideoFile.delete();
    	//XXXX REMOVEffmpegWrapperreturnCode = FfmpegJNIWrapper.call_ffmpegWrapper(appContext, argv2);
    	//XXXX REMOVELog.d("VideoCompressionTask","doInBackground ffmpegWrapperreturnCode (secodn time): " + ffmpegWrapperreturnCode);
    	return(compressedFilePath);
    }
    
    @Override
    protected void onProgressUpdate(String... compressedFilePath) {
    	Log.d("VideoCompressionTask","onProgressUpdate");
    	thisTaskListener.onCompressionPorgressUpdate(compressedFilePath[0]);
    }
    
    @Override
    protected void onPostExecute(String compFilePath) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onCompressionFinished(compFilePath);
    }

}

