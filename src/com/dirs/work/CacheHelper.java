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
	
	public  Map<Integer,ImageView> ViewCache = new HashMap<Integer, ImageView>();
	public  Map<Integer,Bitmap> bitmapCache = new HashMap<Integer, Bitmap>();
	public  Map<Integer,ImageView> Releaseed = new HashMap<Integer, ImageView>();
}
