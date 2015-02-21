package com.amodtech.colabandroid;

public interface VideoUploadTaskListener {
	
	public void onUploadFinished(int result);
	// called when the compression task has completed
	
	public void onUploadPorgress(Long perCentComplete);

}
