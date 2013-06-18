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

/**
* 自定义Adapter，用于在Item上显示Bitmap
*/
public class ImageAdapter extends BaseAdapter {


	private List<String> data;
	private Context context;
	private boolean isZoom;
	private LayoutInflater mInflater;
	private CacheHelper mCache = CacheHelper.getInstance();
	
	//获取图片的异步操作类
	private AsyncGetImage mAsync;
	
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
		//判断当前的Item上图片是否被释放，如果释放则重新加载
		if(mCache.Releaseed.containsKey(position)){
			Log.d("debug","图片已被释放，重新加载");
			mAsync = new AsyncGetImage(context, holder.mImage,isZoom);
			mAsync.execute(data.get(position),String.valueOf(position));
		}else{
		    //开始异步加载图片
			mCache.ViewCache.put(position,holder.mImage);
			mAsync = new AsyncGetImage(context, holder.mImage,isZoom);
			mAsync.execute(data.get(position),String.valueOf(position));
		}
		return view;
	}
}
