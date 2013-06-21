package com.dirs.work;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

public class CacheHelper {
	private static CacheHelper instance = null;
	
	private CacheHelper(){};
	
	public static CacheHelper getInstance(){
		if(instance == null){
			instance = new CacheHelper();
		}
		return instance;
	}
	
	//存放所有View
	public Map<Integer, View> ViewCache = new HashMap<Integer, View>();
	//存放已加载至内存的Bitmap
	public Map<Integer, Bitmap> BitmapCache = new HashMap<Integer, Bitmap>();
	//存放未释放的ImageView
	public Map<Integer, ImageView> UnRecycle = new HashMap<Integer, ImageView>();
	//存放已释放的ImageView
	public Map<Integer, ImageView> Recycled = new HashMap<Integer, ImageView>();
	//存放加载列队
	private LinkedList<Map<Integer,String>> LoadList = new LinkedList<Map<Integer,String>>();
	
	//往加载列队中压入消息
	public void push(Map<Integer,String> map){
		LoadList.addFirst(map);
	}
	
	//从加载队列中取出消息
	public Map<Integer,String> pop(){
		if(LoadList.size() != 0){
			return LoadList.poll();
		}else{
			return null;
		}
	}
	
	public boolean isListEmpty(){
		if(LoadList.size() == 0){
			return true;
		}else{
			return false;
		}
	}
	
	public void clearCache(){
		LoadList.clear();
		ViewCache.clear();
		BitmapCache.clear();
		UnRecycle.clear();
		Recycled.clear();
	}
}
