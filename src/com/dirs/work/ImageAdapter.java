package com.dirs.work;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageAdapter extends BaseAdapter {


	private List<String> data;
	private Context context;
	private boolean isZoom;
	private LayoutInflater mInflater;
	private CacheHelper mCache = CacheHelper.getInstance();
	
	private AsyncGetImage agi;
	
	public ImageAdapter(List<String> list, Context c, boolean b) {
		// TODO Auto-generated constructor stub
		data = list;
		context = c;
		isZoom = b;
		mInflater = LayoutInflater.from(context);
	}

	static class ViewHolder {
		public ImageView mImage;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (view == null) {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.list_item, null);
			holder.mImage = (ImageView) view.findViewById(R.id.image);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
			// holder.mImage.setImageResource(R.drawable.loading);
		}
		if(mCache.Releaseed.containsKey(position)){
			Log.d("debug","aaaa");
			agi = new AsyncGetImage(context, holder.mImage,isZoom);
			agi.execute(data.get(position),String.valueOf(position));
		}else{
			mCache.ViewCache.put(position,holder.mImage);
			agi = new AsyncGetImage(context, holder.mImage,isZoom);
			agi.execute(data.get(position),String.valueOf(position));
		}
		return view;
	}
}
