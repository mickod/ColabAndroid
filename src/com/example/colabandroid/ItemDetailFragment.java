package com.example.colabandroid;

import java.io.File;
import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.colabandroid.videoContent.VideoItem;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
	
    public static final String ARG_VIDEO_TITLE = "video_title";
    public static final String  ARG_SELECTED_VIDEO_ITEM = "selected_video_item";
    private VideoView videoPlayerView;
    private MediaController mediaController;
    private VideoItem selectedVideoItem;
    private View rootView;

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
    	vidFormat.setText("Unknown");
    	TextView vidFileSize = (TextView) rootView.findViewById(R.id.video_file_size);
    	File vidFile = new File(selectedVideoItem.videoPath);
    	String vidFileSizeString = new DecimalFormat("0.00").format(vidFile.length()/1000000.0);
    	vidFileSize.setText(vidFileSizeString + " MB");
    	
    	
        return rootView;
    }
}
