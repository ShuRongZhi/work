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

public class AsyncGetImage extends AsyncTask<String, Integer, Bitmap> {
	private ImageView mImage;
	private JniHelper mJniHelper = JniHelper.getInstance();
	private CacheHelper mCache = CacheHelper.getInstance();
	private Context context = null;
	private boolean isZoom;
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
			Toast.makeText(context, "加载图片失败", Toast.LENGTH_LONG).show();
			break;
		case 1:
			ImageView iv = mCache.ViewCache.get(position);
			Bitmap bm = mCache.bitmapCache.get(position);
			if (bm == null) {
				Log.d("debug", "aaa");
			} else {
				iv.setImageBitmap(bm);
			}
			break;
		}
	}

	private boolean getImage(String image) {
		Log.d("debug", "getImage");
		byte[] buf = mJniHelper.getImage(image);
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
				publishProgress(0);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} finally {
				if (bm != null) {
					Log.d("debug","finally!");
					if (!isZoom) {
						mCache.bitmapCache.put(position, bm);
					} else {
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
	}
}
