package com.amodtech.colabandroid;

import java.net.Socket;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class VideoChunkDistributeTask extends AsyncTask<String, String, String> {
	/* This Class is an AsynchTask to divide a video into a number of chunks and 
	 * distribute the chunks to helper apps to compress the video
	 * 
	 */
	
	private VideoUploadTaskListener thisTaskListener;
	
	public VideoChunkDistributeTask(VideoUploadTaskListener ourListener) {
		//Constructor
		Log.d("VideoChunkDistributeTask","constructor");
		
		//Set the listener
		thisTaskListener = ourListener;
	}
	
    @Override
    protected String doInBackground(String... params) {
    	//Compress the video and return it
    	Log.d("VideoChunkDistributeTask","doInBackground");
    	
    	//Get the local video path from the parameters
    	String fileToCompress;
    	int chunkNumber;
    	if (params.length == 1) {
	    	fileToCompress = params[0];
	    	chunkNumber = Integer.parseInt(params[1]);
    	} else {
    		//One or all of the params are not present - log an error and return
    		Log.d("VideoChunkDistributeTask doInBackground","One or all of the params are not present");
    		return null;
    	}
    	
    	//Send the video file to helper to be compressed
    	Socket socket = null;
    	   
    	try {
    	    socket = new Socket(dstAddress, dstPort);
    	    
    	    ByteArrayOutputStream byteArrayOutputStream = 
    	                  new ByteArrayOutputStream(1024);
    	    byte[] buffer = new byte[1024];
    	    
    	    int bytesRead;
    	    InputStream inputStream = socket.getInputStream();
    	    
    	    /*
    	     * notice:
    	     * inputStream.read() will block if no data return
    	     */
    	             while ((bytesRead = inputStream.read(buffer)) != -1){
    	                 byteArrayOutputStream.write(buffer, 0, bytesRead);
    	                 response += byteArrayOutputStream.toString("UTF-8");
    	             }

    	   } catch (UnknownHostException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	    response = "UnknownHostException: " + e.toString();
    	   } catch (IOException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	    response = "IOException: " + e.toString();
    	   }finally{
    	    if(socket != null){
    	     try {
    	      socket.close();
    	     } catch (IOException e) {
    	      // TODO Auto-generated catch block
    	      e.printStackTrace();
    	     }
    	    }
    	   }
    	   return null;
    	  }

    	  @Override
    	  protected void onPostExecute(Void result) {
    	   textResponse.setText(response);
    	   super.onPostExecute(result);
    	  }
    	  
    	 }
    
    @Override
    protected void onPostExecute(String compressedFile) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onUploadFinished();
    }

}

