package com.amodtech.colabandroid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class VideoChunkDistributeTask extends AsyncTask<String, String, String> {
	/* This Class is an AsynchTask to send a video file chunk to a helper app over a 
	 * socket connection.
	 * 
	 */
	
	private VideoChunkDisributionTaskListener thisTaskListener;
	private int chunkNumber;
	
	public VideoChunkDistributeTask(VideoChunkDisributionTaskListener ourListener) {
		//Constructor
		Log.d("VideoChunkDistributeTask","constructor");
		
		//Set the listener
		thisTaskListener = ourListener;
	}
	
    @Override
    protected String doInBackground(String... params) {
    	//This the key method that is executed in the Asynch task -it sends the video chunk to the helper app
    	//over a socket connection.
    	Log.d("VideoChunkDistributeTask","doInBackground");
    	
    	//Get the local video path from the parameters
    	String videoChunkFileName;
    	String helperIPAddress = null;
    	final int helperPort = 8080;
    	if (params.length == 3) {
	    	videoChunkFileName = params[0];
	    	chunkNumber = Integer.parseInt(params[1]);
	    	helperIPAddress = params[2];
    	} else {
    		//One or all of the params are not present - log an error and return
    		Log.d("VideoChunkDistributeTask doInBackground","One or all of the params are not present");
    		return null;
    	}
    	String compressedChunkFileName = "CompressedChunk" + chunkNumber + ".mp4";
    	
    	//Send the video file to helper over a Socket connection so he helper can compress the video file 
    	Socket helperSocket = null;
    	   
    	try {
    	    helperSocket = new Socket(helperIPAddress, helperPort);
    	    BufferedOutputStream helperSocketBOS = new BufferedOutputStream(helperSocket.getOutputStream());
    	    byte[] buffer = new byte[4096];
    	    
    	    //Write the video chunk to the output stream
    	    //Open the file
    	    File videoChunkFile = new File(videoChunkFileName);
    	    BufferedInputStream chunkFileIS = new BufferedInputStream(new FileInputStream(videoChunkFile));
    	    
    	    //First send a long with the file length - wrap the BufferedOutputStream  in a DataOuputStream to
    	    //allow us send a long directly
    	    DataOutputStream helperSocketDOS = new DataOutputStream(
    	    	     new BufferedOutputStream(helperSocket.getOutputStream()));
    	    long chunkLength = videoChunkFile.length();
    	    helperSocketDOS.writeLong(chunkLength);
    	    
    	    //Now loop through the video chunk file sending it to the helper via the socket - note this will simply 
    	    //do nothing if the file is empty
    	    int readCount;
    	    for (readCount = chunkFileIS.read(buffer); readCount < chunkLength; readCount = chunkFileIS.read(buffer)) {
    	    	//write the buffer to the output stream of the socket
    	    	helperSocketDOS.write(buffer, 0, readCount);
    	    }
    	    
    	    chunkFileIS.close();
    	    helperSocketDOS.flush();
    	} catch (UnknownHostException e) {
    		Log.d("VideoChunkDistributeTask doInBackground","unknown host");
    	    e.printStackTrace();
    	} catch (IOException e) {
    		Log.d("VideoChunkDistributeTask doInBackground","IO exceptiont");
    	    e.printStackTrace();
    	} finally{
    		//Tidy up
    	    if(helperSocket != null){
    	    	try {
    	    		helperSocket.close();
    	    	} catch (IOException e) {
    	    		Log.d("VideoChunkDistributeTask doInBackground","Error closing socket");
    	    		e.printStackTrace();
    	    	}
    	    }
    	}
    	
    	//File has been sent - now need to wait for a response
    	try {
			BufferedInputStream helperSocketBIS = new BufferedInputStream(helperSocket.getInputStream());
			DataInputStream helperSocketDIS = new DataInputStream(helperSocketBIS);
			
			//Read in the file size of the compressedChunk - XXXXX if this does not block then use a readFully to read in a byte
			//which indicates the compression is complete first
			long compressedChunkFileSize = helperSocketDIS.readLong();
			
			//Now read the full compressed chunk over the socket and store it in a new file
			File compressedChunkFile = new File(compressedChunkFileName);
			BufferedOutputStream compressedChunkFileBOS = new BufferedOutputStream(new FileOutputStream(compressedChunkFile));

			byte[] buffer = new byte[4096];
			int responseReadCount;
    	    for (responseReadCount = helperSocketDIS.read(buffer); responseReadCount < compressedChunkFileSize; responseReadCount = helperSocketDIS.read(buffer)) {
    	    	//write the buffer to the output stream of the socket
    	    	compressedChunkFileBOS.write(buffer, 0, responseReadCount);
    	    }
    	    
    	    //Tidy up
    	    helperSocketDIS.close();
    	    compressedChunkFileBOS.close();
    	    helperSocket.close();
    	    
    	} catch (IOException e) {
    		Log.d("VideoChunkDistributeTask doInBackground","IO exception getting response");
    	    e.printStackTrace();
    	} finally{
    		//Tidy up
    	    if(helperSocket != null){
    	    	try {
    	    		helperSocket.close();
    	    	} catch (IOException e) {
    	    		Log.d("VideoChunkDistributeTask doInBackground","Error closing socket");
    	    		e.printStackTrace();
    	    	}
    	    }
    	}

    	//return the name of the compressed chunk file
    	return "compressedChunkFileName";
    }
    
    @Override
    protected void onPostExecute(String compressedFileName) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onCompressedChunkReady(chunkNumber, compressedFileName);
    }

}

