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
import java.nio.ByteBuffer;

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
	    	Log.d("VideoChunkDistributeTask doInBackground","videoChunkFileName: " + videoChunkFileName);
	    	Log.d("VideoChunkDistributeTask doInBackground","chunkNumber: " + chunkNumber);	
	    	Log.d("VideoChunkDistributeTask doInBackground","helperIPAddress: " + helperIPAddress);	
    	} else {
    		//One or all of the params are not present - log an error and return
    		Log.d("VideoChunkDistributeTask doInBackground","One or all of the params are not present");
    		return null;
    	}
    	
    	//Send the video file to helper over a Socket connection so he helper can compress the video file 
    	Socket helperSocket = null;
    	   
    	try {
    		Log.d("VideoChunkDistributeTask doInBackground","connecting to: " + helperIPAddress + ":" + helperPort);
    	    helperSocket = new Socket(helperIPAddress, helperPort);
    	    BufferedOutputStream helperSocketBOS = new BufferedOutputStream(helperSocket.getOutputStream());
    	    byte[] buffer = new byte[4096];
    	    
    	    //Write the video chunk to the output stream
    	    //Open the file
    	    ////XXXX Test
    	    //XXXX REMOVE String testFileName = Environment.getExternalStorageDirectory() +"/transtest.mp4";
    	    File videoChunkFile = new File(videoChunkFileName);
    	    BufferedInputStream chunkFileIS = new BufferedInputStream(new FileInputStream(videoChunkFile));
    	    
    	    //First send a long with the file length - wrap the BufferedOutputStream  in a DataOuputStream to
    	    //allow us send a long directly
    	    DataOutputStream helperSocketDOS = new DataOutputStream(
    	    	     new BufferedOutputStream(helperSocket.getOutputStream()));
    	    long chunkLength = videoChunkFile.length();
    	    helperSocketDOS.writeLong(chunkLength);
    	    Log.d("VideoChunkDistributeTask doInBackground","chunkLength: " + chunkLength);
    	    
    	    //Now loop through the video chunk file sending it to the helper via the socket - note this will simply 
    	    //do nothing if the file is empty
    	    int readCount = 0;
    	    int totalReadCount = 0;
    	    while(totalReadCount < chunkLength) {
    	    	//write the buffer to the output stream of the socket
    	    	readCount = chunkFileIS.read(buffer);
    	    	helperSocketDOS.write(buffer, 0, readCount);
    	    	totalReadCount += readCount;
    	    }
    	    
    	    Log.d("VideoChunkDistributeTask doInBackground","file sent");
    	    chunkFileIS.close();
    	    helperSocketDOS.flush();
    	} catch (UnknownHostException e) {
    		Log.d("VideoChunkDistributeTask doInBackground","unknown host");
    	    e.printStackTrace();
    	} catch (IOException e) {
    		Log.d("VideoChunkDistributeTask doInBackground","IO exceptiont");
    	    e.printStackTrace();
    	}
    	
    	//File has been sent - now need to wait for a response
    	try {
    		Log.d("VideoChunkDistributeTask doInBackground","Waiting for response");
			//BufferedInputStream helperSocketBIS = new BufferedInputStream(helperSocket.getInputStream());
			DataInputStream helperSocketDIS = new DataInputStream(helperSocket.getInputStream());
			
		    //The first part of the message should be the length of the file being transfered - read it first and
		    //then write from the second byte onwards to the buffer
		    //byte[] fileSizeBytes = new byte[Long.SIZE];
		    //helperSocketDIS.readFully(fileSizeBytes, 0, Long.SIZE);
		    //ByteBuffer bb = ByteBuffer.wrap(fileSizeBytes);
		    long fileSize = helperSocketDIS.readLong();
		    Log.d("VideoChunkDistributeTask doInBackground","Compressed chunk return fileSize: " + fileSize);
		    
		    //Now read in the rest of the file up to the final byte indicated by the size
		    byte[] bytes = new byte[4096];
		    String compressedChunkFileName = "CompressedChunk" + chunkNumber + ".mp4";
			File compressedChunkFile = new File(Environment.getExternalStorageDirectory() +  "/" + compressedChunkFileName);
			if(compressedChunkFile.exists()) {
				//Delete the file and create a new one
				boolean fileDeleted = compressedChunkFile.delete();
				if (!fileDeleted) {
					//log error and return
					Log.d("MainActivity SocketServerThread Run","compressedChunkFile: old file not deleted");
					return null;
				}
			}
			BufferedOutputStream compressedChunkFileBOS = new BufferedOutputStream(new FileOutputStream(compressedChunkFile));
		    long totalCount = 0;
		    int thisReadCount = 0;
		    while (totalCount < fileSize && (thisReadCount = helperSocketDIS.read(bytes)) != -1) {
		    	totalCount += thisReadCount;
		    	Log.d("VideoChunkDistributeTask doInBackground","totalCount received is: " + totalCount);
		    	compressedChunkFileBOS.write(bytes, 0, thisReadCount);
		    }
		    //Write the final buffer read in - this is necessary as thisReadCount will be set to -1 
		    //when the end of stream id detected even when it has read in some bytes while detecting the end
		    //of stream
		    compressedChunkFileBOS.write(bytes);
		    Log.d("VideoChunkDistributeTask doInBackground","video file received");
		    Log.d("VideoChunkDistributeTask doInBackground","totalCount: " + totalCount);
		    Log.d("VideoChunkDistributeTask doInBackground","thisReadCount: " + thisReadCount);

		    //Tidy up
		    compressedChunkFileBOS.flush();
		    compressedChunkFileBOS.close();
		    helperSocketDIS.close();			    
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

