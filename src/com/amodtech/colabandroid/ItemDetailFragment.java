package com.amodtech.colabandroid;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import android.os.PowerManager;

import com.amodtech.colabandroid.R;
import com.amodtech.colabandroid.videoContent.VideoItem;
import com.amodtech.yaandroidffmpegwrapper.FfmpegJNIWrapper;

/**
 * A fragment representing a single Video detail screen (from the standard Android
 * two pane project template). This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements CompressionTaskListener, 
OnClickListener, CompressingProgressTaskListener, VideoUploadTaskListener, VideoChunkDisributionTaskListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
	
    public static final String ARG_VIDEO_TITLE = "video_title";
    public static final String  ARG_SELECTED_VIDEO_ITEM = "selected_video_item";
    private final String colabServerURL = "http://ec2-52-16-55-251.eu-west-1.compute.amazonaws.com:3000" + "/web_video_upload";
    private final String helperIPAddresses[] = {"192.168.1.66", "192.168.1.171", "192.168.1.10"};
    private VideoView videoPlayerView;
    private MediaController mediaController;
    private VideoItem selectedVideoItem;
    private View rootView;
    private Button uploadButton;
    private Button colabUploadButton;
    private CompressingFileSizeProgressTask compressingProgressTask;
    private final int numberOfHelpers = 3;
    private String chunkFileNames[] = new String[numberOfHelpers];
    private long simpleCompressionStartTime = 0;
    private long colabCompressionStartTime = 0;
    private long uploadStartTime = 0;
    private long totalElapsedStartTime = 0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("ItemDetailFragment","onCreate");
        super.onCreate(savedInstanceState);
        
        //Keep screen on
        this.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (getArguments().containsKey(ARG_SELECTED_VIDEO_ITEM)) {
            // Load the video content specified by the fragment
            // arguments.
        	
        	//Get the video item details
        	selectedVideoItem = getArguments().getParcelable(ARG_SELECTED_VIDEO_ITEM);
        	Log.d("ItemDetailFragment","onCreate: selectedVideoItem" + selectedVideoItem);
        	if (selectedVideoItem == null) {
        		Log.d("ItemDetailFragment","onCreate: selectedVideoItem is null!!");
        	}
        	 	
        } else {
        	//No selected item passed
        	Log.d("ItemDetailFragment","onCreate: no selected Item argument received");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        
    	//Create the video player and set the video path
    	videoPlayerView = (VideoView) rootView.findViewById(R.id.video_area);
    	if (videoPlayerView == null) {
    		Log.d("ItemDetailFragment","onCreateView: videoPlayerView is null");
    		return null;
    	}
    	 
    	//Set the video path and make sure the first frame is shown instead of a black screen
    	videoPlayerView.setVideoPath(selectedVideoItem.videoPath); 
    	videoPlayerView.seekTo(100);
    	
    	//Set the MediaController (the video control bar) to match the size of the VideoView - this trick 
    	//from a StackOverflow answer makes sure it is sized correctly, but calling setAnchroView after the
    	//Video is actually loaded and hence knows it right sze.
    	final Context mContext = this.getActivity();
    	videoPlayerView.setOnPreparedListener(new OnPreparedListener() {
    	    @Override
    	    public void onPrepared(MediaPlayer mp) {
    	    		//Set the video duration at this point as it is unknown before this
	    	    	TextView vidDuration = (TextView) rootView.findViewById(R.id.video_duration);
	    	    	int duration = videoPlayerView.getDuration()/1000;
	    	    	String durationString = new DecimalFormat("0.00").format(duration/1000.0);
	    	    	vidDuration.setText(durationString + " secs");
    	    	
	    	    	//Add a listener for the size chnage to correctly set the controls
    	            mp.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() { 
    	                                    @Override
    	                                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    	                                        //Add Media Controller and set its position on the screen.
    	                                    	mediaController = new MediaController(mContext);
    	                                    	videoPlayerView.setMediaController(mediaController);
    	                                    	mediaController.setAnchorView(videoPlayerView);
    	                                    }
    	                                    });
    	                            }
    	});
    	
    	//Display the video info
    	TextView vidTitle = (TextView) rootView.findViewById(R.id.video_title);
    	vidTitle.setText(selectedVideoItem.videoTitle);
    	TextView vidFormat = (TextView) rootView.findViewById(R.id.video_format);
    	//XXXXvidFormat.setText(FfmpegJNIWrapper.getMessage());
    	TextView vidFileSize = (TextView) rootView.findViewById(R.id.video_file_size);
    	File vidFile = new File(selectedVideoItem.videoPath);
    	String vidFileSizeString = new DecimalFormat("0.00").format(vidFile.length()/1000000.0);
    	vidFileSize.setText(vidFileSizeString + " MB");
    	
    	//Add the button listeners
    	uploadButton = (Button) rootView.findViewById(R.id.upload_button);
    	colabUploadButton = (Button) rootView.findViewById(R.id.colab_upload_button);
    	uploadButton.setOnClickListener(this);
    	colabUploadButton.setOnClickListener(this);
    			
        return rootView;
    }
    
    @Override
    public void onClick(View v) {
		//Handle all button clicks on this fragment
    	Log.d("ItemDetailFragment","onClick");
    	
    	if(v == rootView.findViewById(R.id.upload_button)) {
    		//Simple upload Button - start timing and compression task
    		Log.d("ItemDetailFragment","onClick upload Button");
    		simpleCompressionStartTime = System.nanoTime();
    		totalElapsedStartTime = simpleCompressionStartTime;
    		clearTimingDisplays();
			VideoCompressionTask compressTask = new VideoCompressionTask(this.getActivity(), this);
			compressTask.execute(selectedVideoItem.videoPath);
		} else if (v == rootView.findViewById(R.id.colab_upload_button)) {
			//Colaborative upload button
			Log.d("ItemDetailFragment","onClick colaborative upload Button");
			
			//Delete any existing video chunk files and set the chunkfile names to zero
			File videoChunkDir = Environment.getExternalStorageDirectory();
			for(File chunkFile: videoChunkDir.listFiles()) {
				//Check for a file with fileChunk...
			    if(chunkFile.getName().startsWith("videoChunk_"))
			    	chunkFile.delete();
			}
			for (int i=0; i<numberOfHelpers; i++) {
				chunkFileNames[i] = null;
			}
			
			//Start timing
			colabCompressionStartTime = System.nanoTime();
			totalElapsedStartTime = colabCompressionStartTime;
			clearTimingDisplays();

			//Get the video duration first
	    	int videoDurationSecs = videoPlayerView.getDuration()/1000;
	    	Log.d("ItemDetailFragment","onClick colab upload video lenght: " + videoDurationSecs);
	    	
	    	//Now break into chunks and distribute
	    	VideoChunkDistributeTask distributionTaskArray[] = new VideoChunkDistributeTask[numberOfHelpers];
			int chunkSize = videoDurationSecs/(numberOfHelpers);
			int startSecs = 0;
    		for (int i=0; i<numberOfHelpers; i++) {
    			//Create video chunk
    			String startTime = convertSecsToTimeString(startSecs);
    			int endSecs = startSecs + chunkSize;
    			if (i ==  (numberOfHelpers -1)) {
    				//make sure the last chunk goes right to the 
    				//end (neither shorter or longer)
    				endSecs = videoDurationSecs;
    			}
    			String endTime = convertSecsToTimeString(endSecs);
    			Log.d("ItemDetailFragment","onClick startSecs: " + startSecs);
    			Log.d("ItemDetailFragment","onClick startTime: " + startTime);
    			Log.d("ItemDetailFragment","onClick endSecs: " + endSecs);
    			Log.d("ItemDetailFragment","onClick endTime: " + endTime);
    			
    			//Call ffmpeg to create this chunk of the video
    			String argv[] = {"ffmpeg", "-i", selectedVideoItem.videoPath, 
    					"-ss",startTime, "-t", endTime,
    					"-c","copy", Environment.getExternalStorageDirectory() +"/videoChunk_"+i+".mp4"};
    			Log.d("ItemDetailFragment","onClick colab upload argv: " + argv);
    			int ffmpegWrapperReturnCode = FfmpegJNIWrapper.call_ffmpegWrapper(this.getActivity(), argv);
    	    	Log.d("ItemDetailFragment","onClick colab upload breaking into chunks ffmpegWrapperreturnCode: " + ffmpegWrapperReturnCode);
    	    	
    			//Now distribute the video chunk to the helper for compression - to allow multiple AsynchTasks execute in parallel the 
    	    	//'executeonExecutor' call is required. It needs to be used with caution to avoid the usual synchronization issues and also 
    	    	//to avoid too many threads being created
    	    	distributionTaskArray[i] = new VideoChunkDistributeTask(this);
    	    	//XXXX REMOVE distributionTaskArray[i].execute(Environment.getExternalStorageDirectory() + "/videoChunk_"+i+".mp4", String.valueOf(i), helperIPAddresses[i]);	
    	    	distributionTaskArray[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Environment.getExternalStorageDirectory() + "/videoChunk_"+i+".mp4", String.valueOf(i), helperIPAddresses[i]);
    	    	Log.d("ItemDetailFragment","onClick distributed chunk filename: " + "videoChunk_"+i+".mp4");
    	    	
    	    	//Increment startSecs
    	    	startSecs = endSecs +1;
    		}
		}
	}
    
    public void onCompressionFinished(String compressedFilePath) {
    	//Called when the compression asynch task has finished
    	
    	//calculate the the compression time
    	long simpleCompressionEndTime = System.nanoTime();
    	long simpleCompressionTime = simpleCompressionEndTime - simpleCompressionStartTime;
    	TextView compressionTimeTextView = (TextView) rootView.findViewById(R.id.compress_time);
    	String compressTimeString = new DecimalFormat("0.000000").format(simpleCompressionTime/1000000000.0);
    	compressionTimeTextView.setText(compressTimeString);
    	
    	//Stop the compression progress monitoring asynchtask
    	compressingProgressTask.cancel(true);
    	
    	//Update the progress
    	TextView progressMessageTextView = (TextView) rootView.findViewById(R.id.prog_message);
    	progressMessageTextView.setText("Compressed: " + compressedFilePath);
    	
    	//Start the upload background task
    	Log.d("ItemDetailFragment","onCompressionFinished: starting uploadTask. compressedFilePath: " + compressedFilePath);
    	uploadStartTime = System.nanoTime();
    	VideoUploadTask uploadTask = new VideoUploadTask(this);
    	uploadTask.execute(colabServerURL, compressedFilePath);
    	progressMessageTextView.setText("Uploading file: " + compressedFilePath);
    }
    
    public void onCompressionPorgressUpdate(String compressedFilePath) {
    	//Listener method - called when the compression task generates a progress
    	//event
    	Log.d("ItemDetailFragment","onCompressionPorgressUpdate. compressedFilePath: " + compressedFilePath);
    	
    	//The progress event in this case is simply the path name of the file to be compressed
    	//Start a new asynchtask to check the file size of this file and udate the UI regularly
    	//durign the compression.
    	TextView progressMessageTextView = (TextView) rootView.findViewById(R.id.prog_message);
    	progressMessageTextView.setText(compressedFilePath);
    	
    	compressingProgressTask = new CompressingFileSizeProgressTask(this);
    	//To allow multiple AsynchTasks execute in parallel the following 'executeonExecutor' call is required. It needs to be
    	//used with caution to avoid the usual synchronization issues and also to avoid too many threads being created
    	compressingProgressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, compressedFilePath);
    }

	@Override
	public void onCompressingProgressFinished(Void params) {
		//Do nothing - not expectng this to be called as we should have killed the Asynchtask before this
		
	}

	@Override
	public void onCompressingPorgressTaskUpdate(Long compressingFileSize) {
		//Update the compressing file size
		Log.d("ItemDetailFragment","onCompressingPorgressTaskUpdate");
		
    	TextView progressMessageTextView = (TextView) rootView.findViewById(R.id.prog_message);
    	String vidFileSizeString = new DecimalFormat("0.00").format(compressingFileSize/1000000.0);
    	progressMessageTextView.setText(vidFileSizeString + " MB");
	}

	@Override
	public void onUploadFinished(int result) {
		//Called when the upload task is finished
		
		if (result > 0) {
			//Calculate upload time
	    	long uploadEndTime = System.nanoTime();
	    	long uploadTime = uploadEndTime - uploadStartTime;
	    	TextView uploadTimeTextView = (TextView) rootView.findViewById(R.id.upload_time);
	    	String uploadTimeString = new DecimalFormat("0.000000").format(uploadTime/1000000000.0);
	    	uploadTimeTextView.setText(uploadTimeString);
	    	uploadStartTime = 0;
	    	
	    	//Calculate the total elapsed time
	    	long elpasedTime = uploadEndTime - totalElapsedStartTime;
	    	TextView elpasedTimeTextView = (TextView) rootView.findViewById(R.id.total_elapsed_time);
	    	String elapsedTimeString = new DecimalFormat("0.000000").format(elpasedTime/1000000000.0);
	    	elpasedTimeTextView.setText(elapsedTimeString);
	    	totalElapsedStartTime = 0;	    	
	    	
			//Update the progress message
	    	TextView progressMessageTextView = (TextView) rootView.findViewById(R.id.prog_message);
	    	progressMessageTextView.setText("Video Uploaded");
		} else {
			//Update progress with an error message
	    	TextView progressMessageTextView = (TextView) rootView.findViewById(R.id.prog_message);
	    	progressMessageTextView.setText("Error uploading video");
		}
	}

	@Override
	public void onUploadPorgress(Long perCentComplete) {
		//XXXX
	}
	
	@Override
	public void onCompressedChunkReady(int chunkNumber, String compressedChunkFileName) {
		//Called when a chunk, compressed by an app helper, is ready
		Log.d("ItemDetailFragment onCompressedChunkReady","chunkNumber: " + chunkNumber);
		Log.d("ItemDetailFragment onCompressedChunkReady","compressedChunkFileName: " + compressedChunkFileName);
		
		//Add the file name to the chunk file names array
		if ( chunkNumber >= 0 && chunkNumber < numberOfHelpers) {
			chunkFileNames[chunkNumber] = compressedChunkFileName; 
		} else {
			//Invalid chunk number for some reason...
			Log.d("ItemDetailFragment onCompressedChunkReady","invlaid chunk number received");
			return;
		}
		
		//Check if we have all the chunks yet by - if not just return
		for (int j = 0; j < numberOfHelpers; j++) {
			if (chunkFileNames[j] == null) {
				Log.d("ItemDetailFragment onCompressedChunkReady","All chunks not yet received - returning");
				return;
			}
		}
		
		StringBuilder chunkFileNamesStringBuilder = new StringBuilder();
		//Build the list of files to conactonate for the ffmpeg command
		//First create a concat.txt file
		File concatFile = new File(Environment.getExternalStorageDirectory() +  "/" + "concat.txt");
		if(concatFile.exists()) {
			//Delete the file and create a new one
			boolean fileDeleted = concatFile.delete();
			if (!fileDeleted) {
				//log error and return
				Log.d("ItemDetailFragment onCompressedChunkReady","concatFile: old file not deleted");
				return;
			}
		}
		//Now add the chunk files to the concat.txt file
		BufferedWriter concatFileBW;
		try {
			concatFileBW = new BufferedWriter(new FileWriter(concatFile));
			for (int i=0; i<numberOfHelpers; i++) {
				concatFileBW.write("file '" + Environment.getExternalStorageDirectory() + "/" + chunkFileNames[i] + "'\n");
				//concatFileBW.write("file '" + Environment.getExternalStorageDirectory() + "/" + "Comp_20141217_231003.mp4" + "'\n"); //XXXX
				Log.d("ItemDetailFragment onCompressedChunkReady","writing to concat.txt: " +
						"file '" + Environment.getExternalStorageDirectory() + "/" + chunkFileNames[i]);
			}
			concatFileBW.flush();
			concatFileBW.close();
		} catch (IOException e) {
			//Log the error
			Log.d("ItemDetailFragment onCompressedChunkReady","concatFileBW: io Exception: " + e);
			e.printStackTrace();
			return;
		}
		
		//Use ffmpeg to concatonate the video files
		final String compressedConactFileName = "compressedConcatChunks.mp4";
		File compresseConcatFile = new File(Environment.getExternalStorageDirectory() +  "/" + compressedConactFileName);
		if(compresseConcatFile.exists()) {
			//Delete the file and create a new one
			boolean fileDeleted = compresseConcatFile.delete();
			if (!fileDeleted) {
				//log error and return
				Log.d("ItemDetailFragment onCompressedChunkReady","compresseConcatFile: old file not deleted");
				return;
			}
		}
    	String argv[] = {"ffmpeg", "-f", "concat", "-i", concatFile.getAbsolutePath(), "-codec", "copy", compresseConcatFile.getAbsolutePath() };
    	Log.d("ItemDetailFragment onCompressedChunkReady","Calling ffmpegWrapper");
    	int ffmpegWrapperreturnCode = FfmpegJNIWrapper.call_ffmpegWrapper(this.getActivity(), argv);
    	Log.d("ItemDetailFragment onCompressedChunkReady","ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
    	
    	//Calculate the total Colab Compress time
    	long colabCompressionEndTime = System.nanoTime();
    	long colabCompressionTime = colabCompressionEndTime - colabCompressionStartTime;
    	TextView compressionTimeTextView = (TextView) rootView.findViewById(R.id.compress_time);
    	String colabCompressTimeString = new DecimalFormat("0.000000").format(colabCompressionTime/1000000000.0);
    	compressionTimeTextView.setText(colabCompressTimeString);
    	
    	//Start task to upload file
    	Log.d("ItemDetailFragment","onCompressedChunkReady: starting uploadTask after all chunks received");
    	uploadStartTime = System.nanoTime();
    	VideoUploadTask uploadTask = new VideoUploadTask(this);
    	uploadTask.execute(compressedConactFileName);	
	}
	
	String convertSecsToTimeString(int timeSeconds) {
		//Convert number of seconds into hours:mins:seconds string
		int hours = timeSeconds / 3600;
		int mins = (timeSeconds % 3600) / 60;
		int secs = timeSeconds % 60;
		String timeString = String.format("%02d:%02d:%02d", hours, mins, secs);
		Log.d("ItemDetailFragment","convertSecsToTimeString timeSeconds: " + timeSeconds + " timeString: " + timeString);
		return timeString;
	}

	private void clearTimingDisplays() {
		//Clear all timing displays
		TextView compressionTimeTextView = (TextView) rootView.findViewById(R.id.compress_time);
    	compressionTimeTextView.setText("");
    	TextView uploadTimeTextView = (TextView) rootView.findViewById(R.id.upload_time);
    	uploadTimeTextView.setText("");
    	TextView elpasedTimeTextView = (TextView) rootView.findViewById(R.id.total_elapsed_time);
    	elpasedTimeTextView.setText("");
	}
    
}
