package com.amodtech.colabandroid;

public interface VideoChunkDisributionTaskListener {
	/* Listener interface for task whihc distributes video chunks
	 * to helpers apps for compression
	 */
	
	public void onCompressedChunkReady(String compressedChunkFileName);
	// called when the compressed video chunk is returned from helper


}
