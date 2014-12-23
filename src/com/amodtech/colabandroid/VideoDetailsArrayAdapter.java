package com.amodtech.colabandroid;

import java.util.ArrayList;
import java.util.List;

import com.amodtech.colabandroid.R;
import com.amodtech.colabandroid.videoContent.VideoItem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoDetailsArrayAdapter extends ArrayAdapter<VideoItem> {
	//This is an ArrayAdapter for VideoInfo Arrays. It creates and populates
	//a row view for a given element in the VideoInfo Array
	
	private final Context context;
	private final ArrayList<VideoItem> values;
	
	public VideoDetailsArrayAdapter(Context context, List<VideoItem> values) {
	  super(context, R.layout.activity_item_list_row, values);
	  this.context = context;
	  this.values = (ArrayList<VideoItem>) values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//This method creates and populates a row view for a given element in the VideoInfo Array
		
		Log.d("VideoDetailsArrayAdapter","getView");
		//Inflate the view
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.activity_item_list_row, parent, false);
		
		//Set the video title and Thumbnail based on this particular video
		TextView videoTitle = (TextView) rowView.findViewById(R.id.video_title);
		ImageView videoThumbnail = (ImageView) rowView.findViewById(R.id.video_thumbnail);
		videoTitle.setText(values.get(position).videoPath);
		videoThumbnail.setImageBitmap(values.get(position).videoThumbnail);
		//videoThumbnail.setImageResource(R.drawable.car);
		return rowView;
	}
}
