package com.amodtech.colabandroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

public class CompressionActivity extends Activity {
	//This Activty is the file compression activity. It uses ffmpeg via the JNI and NDK to 
	//compress a video file

    static {
    	//Load the JNI file so this activity can use it
    	System.loadLibrary("colab_jni");
    }
 
    // A native method that returns a Java String to be displayed on the
    // TextView
    public native String getMessage();
    
    // Native methods for ffmpeg functions
	private static native int naInit(String pVideoFileName);
    private static native int[] naGetVideoRes();
    private static native int naFinish();
    private static native int naGetDuration();
    private static native int naGetFrameRate();
    private static native int ffmpegWrapper(String[] argv);
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("CompressionActivity onCreate","entry");
    	super.onCreate(savedInstanceState);
    	// Create a TextView.
    	TextView textView = new TextView(this);

    	//Set default text
    	textView.setText("Default text");
    	setContentView(textView);
    	Log.d("CompressionActivity onCreate","Setting default Text");
    	
    	// Retrieve the text from native method getMessage()
    	Log.d("CompressionActivity onCreate","Setting Text with native method");
    	Log.d("CompressionActivity onCreate","Text from native: " + getMessage());
    	textView.setText(getMessage());
    	setContentView(textView);
    	Log.d("CompressionActivity onCreate","Content View set");
        	   	
    	// Init a video file 
    	int videoInitReturnCode = naInit(Environment.getExternalStorageDirectory() + "/9_Sec_BBBunny_Trailer.mp4");
    	Log.d("CompressionActivity onCreate","videoInitReturnCode: " + videoInitReturnCode);
    	
    	//Test ffmpeg.c wrapper
    	String argv_X1[] = {"ffmpeg", "-i", "movie.mov", "-vcodec", "copy", "-acodec", "copy", "out.mp4"};
    	String argv_X2[] = {"ffmpeg", "-i", "video.avi"};
    	String argv_X3[] = {"ffmpeg", "-i", Environment.getExternalStorageDirectory() + "/9_Sec_BBBunny_Trailer.mp4"};
    	String argv_X4[] = {"ffmpeg", "-i", Environment.getExternalStorageDirectory() + "/9_Sec_BBBunny_Trailer.mp4", "-t", "00:00:04", "-c", "copy", Environment.getExternalStorageDirectory() + "/smallfile1.mp4", "-ss", "00:00:04", "-c", "copy", Environment.getExternalStorageDirectory() + "/smallfile2.mp4"};
    	String argv_X5[] = {"ffmpeg", "-i", Environment.getExternalStorageDirectory() + "/BigBuckBunny_320x180.mp4", "-acodec", "mp2", Environment.getExternalStorageDirectory() + "/CompressedBBB_320x180.mp4"};
    	String argv[] = {"ffmpeg", "-i", Environment.getExternalStorageDirectory() + "/DCIM/Camera/BigBuckBunny_320x180.mp4", "-strict", "experimental", "-acodec", "aac", Environment.getExternalStorageDirectory() + "/CompressedBBB_320x180_aac.mp4"};
    	Log.d("CompressionActivity onCreate","Calling ffmpegWrapper");
    	int ffmpegWrapperreturnCode = ffmpegWrapper(argv);
    	Log.d("CompressionActivity onCreate","ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
   	}
}
