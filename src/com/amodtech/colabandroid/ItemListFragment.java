package com.amodtech.colabandroid;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager.LoaderCallbacks;



import com.amodtech.colabandroid.videoContent.VideoContent;
import com.amodtech.colabandroid.videoContent.VideoItem;

/**
 * A list fragment representing a list of Items. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ItemListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    
    /**
     * The Array of Video data - this is the Array associtaded with this ListFragment
     */
    private VideoContent videosArray;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d("ItemListFragment onCreate","");
        
        //Create the Array of Videos in the Gallery - note that doing this every time is not very
        //efficient, especially as we are generating Thumbnails but lets see how it works for now
        videosArray = new VideoContent(getActivity()); 

        //Set the list adapter for this ListFragment to be a VideoDetailsArrayadpter using the video row layout 
        //and set the list to the newly created Array of Videos
        VideoDetailsArrayAdapter videoDetailsArrayAdapter = new VideoDetailsArrayAdapter(getActivity(),
        		videosArray.ITEMS);
        setListAdapter(videoDetailsArrayAdapter);
        
        //Init the loader which will load the video list into the listarray
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
    	//Loader callback - creates the query to get the videos from media store. The loader will run the query in the 
    	//background
    	
    	Log.d("ItemListFragment", "onCreateLoader");
        String[] projection = { MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID };
        return  new CursorLoader(this.getActivity(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, 
                    null, // Return all rows
                    null, null);
        
    }

    @Override
    public void onLoadFinished(Loader<Cursor> videoCursorLoader, Cursor videoCursor) {
    	//Loader callback - adds the videos to the video array and creates the thumbnails when the loader has
    	//loaded all the videos into the cursor

    	Log.d("ItemListFragment", "onLoadFinished");
	    if (videoCursor != null) {
	    	Log.d("ItemListFragment", "onLoadFinished: videocursor is not null: " + videoCursor);
	    	int titleColumn_index = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
	    	int pathColumn_index = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
	    	int idColumn_index = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
	    	videoCursor.moveToFirst();
	    	int i = videoCursor.getCount();
	    	Log.d("ItemListFragment", "onLoadFinished: videocursor count: " + i);
	        while (videoCursor.moveToNext()) {
	        	//Create the Thumbnail for this video
	        	Log.d("ItemListFragment", "onLoadFinished: Creating Thumbnail");
	        	String videoTitle = videoCursor.getString(titleColumn_index);
	        	String videoPath = videoCursor.getString(pathColumn_index);
	        	long videoID = videoCursor.getLong(idColumn_index);
	        	Bitmap thisVideoThumbnail = MediaStore.Video.Thumbnails.getThumbnail(this.getActivity().getContentResolver(), videoID, MediaStore.Images.Thumbnails.MINI_KIND, null);
	        	Log.d("VideoContent refresh ","thisVideoThumbnail: " + thisVideoThumbnail);
	        	if (thisVideoThumbnail == null) {
	        		Log.d("VideoContent refresh ","VideoThumbnail is null!!!");
	        	}
	        	VideoItem newVideoItem = new VideoItem(videoID, videoTitle, videoPath, thisVideoThumbnail);
	        	//Add the new video item to the list
	        	videosArray.addItem(newVideoItem);
	        }
	        videoCursor.close();
	    }  else {
        	Log.d("ItemListFragment", "onLoadFinished: videocursor is null");
	    }
	    
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
     // If the Cursor is being placed in a CursorAdapter, you should use the
     // swapCursor(null) method to remove any references it has to the
     // Loader's data.
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(videosArray.ITEMS.get(position).videoTitle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
    public VideoItem getVideoDetails(String videoTitle) {
    	//Return the video Item corresponding to this videoID
    	return videosArray.ITEM_MAP.get(videoTitle);
    }
}
