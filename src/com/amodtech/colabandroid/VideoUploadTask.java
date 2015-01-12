package com.amodtech.colabandroid;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class VideoUploadTask extends AsyncTask<String, String, Integer> {
	/* This Class is an AsynchTask to upload a video to a server on a background thread
	 * 
	 */
	
	private VideoUploadTaskListener thisTaskListener;
	private String serverURL;
	private String videoPath;
	
	public VideoUploadTask(VideoUploadTaskListener ourListener) {
		//Constructor
		Log.d("VideoUploadTask","constructor");
		
		//Set the listener
		thisTaskListener = ourListener;
	}

    @Override
    protected Integer doInBackground(String... params) {
    	//Upload the video in the background
    	Log.d("VideoUploadTask","doInBackground");
    	
    	//Get the Server URL and the local video path from the parameters
    	if (params.length == 2) {
	    	serverURL = params[0];
	    	videoPath = params[1];
    	} else {
    		//One or all of the params are not present - log an error and return
    		Log.d("VideoUploadTask doInBackground","One or all of the params are not present");
    		return -1;
    	}
    	
    	//Create a new Multipart HTTP request to upload the video
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(serverURL);

        //Create a Multipart entity and add the parts to it
        try {
        	Log.d("VideoUploadTask doInBackground","Building the request");
	        FileBody filebodyVideo = new FileBody(new File(videoPath));
	        StringBody title;
			title = new StringBody("Filename: " + videoPath);
	        StringBody description = new StringBody("ColabAndroid test Video");
	        MultipartEntity reqEntity = new MultipartEntity();
	        reqEntity.addPart("videoFile", filebodyVideo);
	        reqEntity.addPart("title", title);
	        reqEntity.addPart("description", description);
	        httppost.setEntity(reqEntity);
		} catch (UnsupportedEncodingException e1) {
			//Log the error
			Log.d("VideoUploadTask doInBackground","UnsupportedEncodingException error when setting StringBody for title or description");
			e1.printStackTrace();
			return -1;
		}

        //Send the request to the server
        HttpResponse serverResponse = null;
		try {
			Log.d("VideoUploadTask doInBackground","Sending the Request");
			serverResponse = httpclient.execute( httppost );
		} catch (ClientProtocolException e) {
			//Log the error
			Log.d("VideoUploadTask doInBackground","ClientProtocolException");
			e.printStackTrace();
		} catch (IOException e) {
			//Log the error
			Log.d("VideoUploadTask doInBackground","IOException");
			e.printStackTrace();
		}
        
        //Check the response code
		Log.d("VideoUploadTask doInBackground","Checking the response code");
		if (serverResponse != null) {
			Log.d("VideoUploadTask doInBackground","ServerRespone" + serverResponse.getStatusLine());
	        HttpEntity responseEntity = serverResponse.getEntity( );
	        if (responseEntity != null) {
	        	//log the response code and consume the content
	        	Log.d("VideoUploadTask doInBackground","responseEntity is not null");
	        	try {
					responseEntity.consumeContent( );
				} catch (IOException e) {
					//Log the (further...) error...
					Log.d("VideoUploadTask doInBackground","IOexception consuming content");
					e.printStackTrace();
				}
	        } 
		} else {
			//Log that response code was null
			Log.d("VideoUploadTask doInBackground","serverResponse = null");
		}

        //Shut down the connection manager
        httpclient.getConnectionManager( ).shutdown( );  	
    	return 1;
    }
    
    @Override
    protected void onPostExecute(Integer result) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onUploadFinished();
    }

}


