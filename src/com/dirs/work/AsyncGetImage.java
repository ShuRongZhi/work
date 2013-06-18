package com.dirs.work;

import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


//异步加载类，负责从网络读取图片
public class AsyncGetImage extends AsyncTask<String, Integer, Bitmap> {
    //Jni助手类
	private ImageView mImage;
	private JniHelper mJniHelper = JniHelper.getInstance();
	private CacheHelper mCache = CacheHelper.getInstance();
	private Context context = null;
	//是否缩放
	private boolean isZoom;
	//设定缩放的宽高
	private final int height = 300;
	private final int width = 300;
	private int position;

	public AsyncGetImage(Context c, ImageView iv, boolean b) {
		this.context = c;
		this.mImage = iv;
		this.isZoom = b;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		// TODO Auto-generated method stub
		Log.d("debug", "doInBackGround");
		mJniHelper.init();
		position = Integer.parseInt(params[1]);
		if (!this.getImage(params[0])) {
			Log.d("debug", "下载失败");
			publishProgress(-1);
		} else {
			Log.d("debug","下载成功");
			publishProgress(1);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		// 设置默认图片
		Log.d("debug", "onPreExecute");
		this.mImage.setImageResource(R.drawable.loading);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		switch (values[0]) {
		case 0:
			// 回收内存
			this.recycle();
			break;
		case -1:
		    //加载图片失败
			Toast.makeText(context, "加载图片失败", Toast.LENGTH_LONG).show();
			break;
		case 1:
		    //加载图片成功，根据传递的position从ViewCache取得ImageView控件
		    //并从BitmapCache获取对应的位图，显示到ImageView上
			ImageView iv = mCache.ViewCache.get(position);
			Bitmap bm = mCache.bitmapCache.get(position);
			if (bm == null) {
				Log.d("debug", "BitmapCache取到空位图");
			} else {
				iv.setImageBitmap(bm);
			}
			break;
		}
	}
    
    //图片下载函数，传递图片名，调用Native层的getImage下载图片
	private boolean getImage(String image) {
		Log.d("debug", "getImage");
		byte[] buf = mJniHelper.getImage(image);
		//判断是否成功下载到图片
		if (buf == null) {
			Log.d("debug", "下载图片:" + image + "失败!");
			return false;
		} else {
			Bitmap bm = null;
			try {
				bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
			} catch (OutOfMemoryError oom) {
				oom.printStackTrace();
				Log.d("debug", "内存溢出");
				//提示onProgressUpdate需要回收内存
				publishProgress(0);
				try {
				    //等待2000毫秒
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} finally {
				if (bm != null) {
					Log.d("debug","finally!");
					/**
					*根据position，将位图放进BitmapCache相应的位置，在onProgressUpdate函数中取出
					*/
					if (!isZoom) {
						mCache.bitmapCache.put(position, bm);
					} else {
					    //将原图缩放到width*height大小，并释放原图的内存
						Bitmap newbm = Bitmap.createScaledBitmap(bm, width,
								height, true);
						bm.recycle();
						mCache.bitmapCache.put(position, newbm);
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}
    
    //内存释放函数，当发生内存溢出时，遍历map，将不可见的Bitmap给释放掉
	private void recycle() {
		Log.d("debug", "准备开始释放内存");
		Iterator iter = mCache.ViewCache.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer,ImageView> entry = (Map.Entry<Integer, ImageView>)iter.next();
			int pos = entry.getKey();
			ImageView iv = mCache.ViewCache.get(pos);
			iv.setImageResource(R.drawable.loading);
			Bitmap bm = mCache.bitmapCache.get(pos);
			mCache.Releaseed.put(pos,iv);
			if (bm != null) {
				if (!bm.isRecycled()) {
					bm.recycle();
				}
			}
		}
		Log.d("debug","内存释放完成");
	}
}
