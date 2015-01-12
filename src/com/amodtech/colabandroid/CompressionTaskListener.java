package com.amodtech.colabandroid;

public interface CompressionTaskListener {
	/*
	 * This class is a  listener for events from the compression asynch task
	 */
	
	public void onCompressionFinished(String compressedFilePath);
	// called when the compression task has completed
	
	public void onCompressionPorgressUpdate(int compressedFileSize);

}
