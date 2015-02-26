package com.amodtech.colabandroid;

import java.io.File;
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
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

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
    private VideoView videoPlayerView;
    private MediaController mediaController;
    private VideoItem selectedVideoItem;
    private View rootView;
    private Button uploadButton;
    private CompressingFileSizeProgressTask compressingProgressTask;
    private final int numberOfHelpers = 2;
    private String chunkFileNames[] = new String[numberOfHelpers];

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
	    	    	int duration = videoPlayerView.getDuration();
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
    	uploadButton.setOnClickListener(this);
    			
        return rootView;
    }
    
    @Override
    public void onClick(View v) {
		//Handle all button clicks on this fragment
    	Log.d("ItemDetailFragment","onClick");
    	
    	if(v == rootView.findViewById(R.id.upload_button)) {
    		//Upload Button
    		Log.d("ItemDetailFragment","onClick upload Button");
			VideoCompressionTask compressTask = new VideoCompressionTask(this.getActivity(), this);
			compressTask.execute(selectedVideoItem.videoPath);
    		//XXXX REMOVE Log.d("ItemDetailFragment","onClick: starting uploadTask");
        	//XXXX REMOVEVideoUploadTask uploadTask = new VideoUploadTask(this);
        	//XXXX REMOVE uploadTask.execute(colabServerURL, Environment.getExternalStorageDirectory() + "/DCIM/Camera/BBB_trailer.mp4");	
		} else if (v == rootView.findViewById(R.id.colab_upload_button)) {
    		Log.d("ItemDetailFragment","onClick colaborative upload Button");
			//Colaborative upload button - first divide the video into chunks using ffmpeg
			//Get the video duration first
			String argv[] = {"ffmpeg", "-i", Environment.getExternalStorageDirectory() + "/DCIM/Camera/BigBuckBunny_320x180.mp4"};
			Log.d("ItemDetailFragment","onClick colab upload: Calling ffmpegWrapper");
	    	int ffmpegWrapperreturnCode = FfmpegJNIWrapper.call_ffmpegWrapper(this.getActivity(), argv);
	    	Log.d("ItemDetailFragment","onClick colab upload video lenght ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
	    	
	    	//Now break into chunks and distribute
	    	VideoChunkDistributeTask distributionTaskArray[] = new VideoChunkDistributeTask[numberOfHelpers];
    		for (int i=0; i<numberOfHelpers; i++) {
    			//Create video chunk
    			String argv1[] = {"ffmpeg", "-i", Environment.getExternalStorageDirectory() + "/DCIM/Camera/BigBuckBunny_320x180.mp4", 
    					"-ss","00:00:00", "-t", "00:50:00",
    					"-c","copy", Environment.getExternalStorageDirectory() +"videoChunk"+i+".mp4"};
    			ffmpegWrapperreturnCode = FfmpegJNIWrapper.call_ffmpegWrapper(this.getActivity(), argv1);
    	    	Log.d("ItemDetailFragment","onClick colab upload breaking into chunks ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
    	    	
    			//Now distribute the video chunk to the helper for compression
    	    	distributionTaskArray[i] = new VideoChunkDistributeTask(this);
    	    	distributionTaskArray[i].execute(Environment.getExternalStorageDirectory() + "videoChunk"+i+".mp4", String.valueOf(i));	
    		}
		}
	}
    
    public void onCompressionFinished(String compressedFilePath) {
    	//Called when the compression asynch task has finished
    	
    	//Stop the compression progress monitoring asynchtask
    	compressingProgressTask.cancel(true);
    	
    	//Update the progress
    	TextView progressMessageTextView = (TextView) rootView.findViewById(R.id.prog_message);
    	progressMessageTextView.setText("Compressed: " + compressedFilePath);
    	
    	//Start the upload background task
    	Log.d("ItemDetailFragment","onCompressionFinished: starting uploadTask. compressedFilePath: " + compressedFilePath);
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
		
		//Add the file name to the chunk file names array
		if ( chunkNumber > 0 && chunkNumber < numberOfHelpers) {
			chunkFileNames[chunkNumber] = compressedChunkFileName; 
		} else {
			//Invalid chunk number for some reason...
			Log.d("ItemDetailFragment onCompressedChunkReady","invlaid chunk number received");
			return;
		}
		
		//Check if we have all the chunks yet by - if not just return
		for (int j = 0; j < numberOfHelpers; j++) {
			if (chunkFileNames[j] == null) {
				return;
			}
		}
		
		StringBuilder chunkFileNamesStringBuilder = new StringBuilder();
		//Build the list of files to conactonate for the ffmpeg command
		for (int i=0; i<numberOfHelpers; i++) {
			//add this chunk file name to the chunkNameString
			chunkFileNamesStringBuilder.append(chunkFileNames[i]);
		}
		String chunkNamesString = chunkFileNamesStringBuilder.toString();
		
		//Use ffmpeg to concatonate the video files
		final String compressedConactFileName = "compressedConcatChunks.mp4";
    	String argv[] = {"ffmpeg", "-i", "concat:\"" + chunkNamesString+ "\"", "-codec", "copy", compressedConactFileName };
    	Log.d("ItemDetailFragment onCompressedChunkReady","Calling ffmpegWrapper");
    	int ffmpegWrapperreturnCode = FfmpegJNIWrapper.call_ffmpegWrapper(this.getActivity(), argv);
    	Log.d("ItemDetailFragment onCompressedChunkReady","ffmpegWrapperreturnCode: " + ffmpegWrapperreturnCode);
    	
    	//Start task to upload file
    	Log.d("ItemDetailFragment","onCompressedChunkReady: starting uploadTask after all chunks received");
    	VideoUploadTask uploadTask = new VideoUploadTask(this);
    	uploadTask.execute(compressedConactFileName);	
	}
    
}
