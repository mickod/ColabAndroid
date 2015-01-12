package com.amodtech.colabandroid;

public interface CompressingProgressTaskListener {
	
	public void onCompressingProgressFinished(Void params);
	// called when the compression task has completed
	
	public void onCompressingPorgressTaskUpdate(Long compressingFileSize);

}
