package com.amodtech.colabandroid;

public interface VideoUploadTaskListener {
	
	public void onUploadFinished();
	// called when the compression task has completed
	
	public void onUploadPorgress(Long perCentComplete);

}
