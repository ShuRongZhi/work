package com.dirs.work;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
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
	//存放所有ImageView控件
	public  Map<Integer,ImageView> ViewCache = new HashMap<Integer, ImageView>();
	//存放所有Bitmap控件
	public  Map<Integer,Bitmap> bitmapCache = new HashMap<Integer, Bitmap>();
	//存放已被释放的ImageView控件
	public  Map<Integer,ImageView> Releaseed = new HashMap<Integer, ImageView>();
}
