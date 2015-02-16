package com.amodtech.colabandroid;

import java.io.File;
import android.os.AsyncTask;
import android.util.Log;

public class CompressingFileSizeProgressTask extends AsyncTask<String, Long, Void> {
	/* This Class is an AsynchTask to compress a video on a background thread
	 * 
	 */
	
	private CompressingProgressTaskListener thisTaskListener;
	File compressingFile;
	
	public CompressingFileSizeProgressTask(CompressingProgressTaskListener ourListener) {
		//Set the listener
		Log.d("CompressingFileSizeProgressTask","constructor");
		thisTaskListener = ourListener;
	}

    @Override
    protected Void doInBackground(String... compressingFilePath) {
    	//In the background, check file size every second and report progress
    	Log.d("CompressingFileSizeProgressTask","doInBackground. compressingFilePath: " + compressingFilePath[0]);
    	
    	//Loop continuously, checking and reporting the compressing file size every second
    	Long compressingFileSize;
    	compressingFile = new File(compressingFilePath[0]);
    	Log.d("CompressingFileSizeProgressTask","doInBackground. got handle to file");
    	while(true) {
	    	try {
	    		//Sleep for one second and then check and report file size
	    		Log.d("CompressingFileSizeProgressTask","doInBackground. about to sleep");
	            Thread.sleep(1000);
	            Log.d("CompressingFileSizeProgressTask","woke up");
	        	compressingFileSize = compressingFile.length();
	        	Log.d("CompressingFileSizeProgressTask","doInBackground. about to publis progress");
	        	publishProgress(compressingFileSize);
	        	Log.d("CompressingFileSizeProgressTask","doInBackground. about to publis progress");
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	            return null;
	        }
    	}
    }
    
    @Override
    protected void onProgressUpdate(Long... compressingFileSize) {
    	//Report progress - size of the compressing file in this case
    	Log.d("CompressingFileSizeProgressTask","onProgressUpdate");
    	thisTaskListener.onCompressingPorgressTaskUpdate(compressingFileSize[0]);
    }
    
    @Override
    protected void onPostExecute(Void params) {
    	//Do nothing
    }

}


