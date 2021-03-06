package com.dirs.work;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * 自定义Adapter，用于在Item上显示Bitmap
 */
public class ImageAdapter extends BaseAdapter {

	private List<String> data;
	private Context context;
	private LayoutInflater mInflater;
	private CacheHelper mCache = null;
	


	public ImageAdapter(List<String> list, Context c, boolean b) {
		// TODO Auto-generated constructor stub
		data = list;
		context = c;
		mInflater = LayoutInflater.from(context);
		mCache = CacheHelper.getInstance();
		LoadImageHelper.getInstance(b);
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
		Log.d("debug", "当前位置:" + position);
		View mView = mCache.ViewCache.get(position);
		//判断当前滑动到的位置是否已经被释放，如果已被释放则重新加载图片
		if(mCache.Recycled.containsKey(position)){
			Log.d("debug","当前Item已被释放，加入载入队列");
			mCache.Recycled.get(position);
			Map<Integer,String> map = new HashMap<Integer, String>();
			map.put(position,data.get(position));
			mCache.push(map);
			//将其从已释放列表中移除，并加入未释放列表
			mCache.Recycled.remove(position);
		}else{
			if (mView == null) {
				Log.d("debug","新Item，加入载入队列");
				mView = mInflater.inflate(R.layout.list_item, null);
				ImageView mImage;
				mImage = (ImageView) mView.findViewById(R.id.image);
				mImage.setImageResource(R.drawable.loading);
				//添加到View集合中
				mCache.ViewCache.put(position, mView);
				Map<Integer,String> map = new HashMap<Integer, String>();
				map.put(position,data.get(position));
				//添加至加载队列
				mCache.push(map);
			}
		}
		return mView;
	}
}
