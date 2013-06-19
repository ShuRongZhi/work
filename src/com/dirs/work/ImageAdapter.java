package com.dirs.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	private JniHelper mJniHelper;
	
	//设定缩放的宽高
	private final int width = 300;
	private final int height = 300;
	//同步锁对象
	private Object lock = new Object();
	
	//存放所有View
	private Map<Integer, View> ViewCache = new HashMap<Integer, View>();
	//存放已加载至内存的Bitmap
	private Map<Integer, Bitmap> BitmapCache = new HashMap<Integer, Bitmap>();
	//存放未释放的ImageView
	private Map<Integer, ImageView> UnRecycle = new HashMap<Integer, ImageView>();
	//存放已释放的ImageView
	private Map<Integer, ImageView> Recycled = new HashMap<Integer, ImageView>();

	public ImageAdapter(List<String> list, Context c, boolean b) {
		// TODO Auto-generated constructor stub
		data = list;
		context = c;
		isZoom = b;
		mInflater = LayoutInflater.from(context);
		mJniHelper = JniHelper.getInstance();
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
		View mView = ViewCache.get(position);
		//判断当前滑动到的位置是否已经被释放，如果已被释放则重新加载图片
		if(Recycled.containsKey(position)){
			Log.d("debug","Item已被释放,重新加载");
			ImageView iv = Recycled.get(position);
			new getImageThread(data.get(position),position).start();
			//将其从已释放列表中移除，并加入未释放列表
			Recycled.remove(position);
			UnRecycle.put(position,iv);
		}else{
			if (mView == null) {
				Log.d("debug","新Item");
				mView = mInflater.inflate(R.layout.list_item, null);
				ImageView mImage;
				mImage = (ImageView) mView.findViewById(R.id.image);
				mImage.setImageResource(R.drawable.loading);
				//添加到View集合和未释放列表中
				ViewCache.put(position, mView);
				UnRecycle.put(position,mImage);
				new getImageThread(data.get(position), position).start();
			}
		}
		return mView;
	}

	//获取图片线程，要求的参数为图片ID和索引位置
	class getImageThread extends Thread {
		private int position;
		private String image;

		public getImageThread(String str, int pos) {
			this.image = str;
			this.position = pos;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			//调用getImge下载图片
			if (!getImage(image, position)) {
				Log.d("debug", "下载图片失败");
				Message msg = new Message();
				msg.what = -1;
				msg.arg1 = position;
				updateUI.sendMessage(msg);
			} else {
				Message msg = new Message();
				msg.what = 1;
				msg.arg1 = position;
				updateUI.sendMessage(msg);
			}
		}

	}
	
	//图片下载函数，调用JniHelper提供getImage接口，获得图片对应的byte[]，如果下载失败返回null
	private synchronized boolean getImage(String iamge, int position) {
		mJniHelper.init();
		byte[] buf = mJniHelper.getImage(iamge);
		if (buf != null) {
			try {
				Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
				if (bm != null) {
					//判断是否缩放，如果要求缩放则创建一个缩放后的Bitmap加入Bitmap集合中，否则直接加入
					if (!isZoom) {
						BitmapCache.put(position, bm);
					} else {
						Bitmap newbm = Bitmap.createScaledBitmap(bm, width,
								height, true);
						bm.recycle();
						BitmapCache.put(position, newbm);
					}
				}
			} catch (OutOfMemoryError oom) {
				Log.d("debug", "内存溢出!");
				//捕获oom异常，给handler发送消息，要求释放内存
				updateUI.sendEmptyMessage(0);
				//同步锁，等待内存释放完毕后再加载
				synchronized (lock) {
					try {
						//等待内存释放后继续操作，超时10秒
						lock.wait(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d("debug", "aaaa");
					//等内存释放完毕后尝试重新解码图片
					Bitmap bm = BitmapFactory.decodeByteArray(buf, 0,
							buf.length);
					if (!isZoom) {
						BitmapCache.put(position, bm);
					} else {
						Bitmap newbm = Bitmap.createScaledBitmap(bm, width,
								height, true);
						bm.recycle();
						BitmapCache.put(position, newbm);
					}

				}
			}
		} else {
			return false;
		}
		return true;
	}

	private void recycle() {
		synchronized (lock) {
			int size = UnRecycle.size();
			Log.d("debug", "准备开始释放内存,未释放集合大小:" + size);
			/**
			 * 释放算法：遍历未释放的ImageView集合，将其设置为默认图片
			 * 并根据索引，在Bitmap集合中获取Bitmap，将其释放
			 * */
			Iterator iter = UnRecycle.entrySet().iterator();
			List<Integer> releaseList = new ArrayList<Integer>();
			ImageView iv = null;
			//计数器，只释放内存溢出位置前一张图片之前的图片，比如在15这个位置溢出，那就只释放0-13的图片，留下一张图片
			//避免突然所有图片都消失了
			int count = 0;
			while (iter.hasNext()) {
				//判断是否应该退出
				if(++count > (size-1)){
					break;
				}
				Map.Entry<Integer, ImageView> entry = (Map.Entry<Integer, ImageView>) iter
						.next();
				int pos = entry.getKey();
				//从未释放的ImageView中取出
				iv = UnRecycle.get(pos);
				try {
					//首先将取出的ImageView控件设置默认图片，断开与Bitmap的引用，避免回收到依然被引用的Bitmap而FC
					iv.setImageResource(R.drawable.loading);
					//取出索引对应的Bitmap
					Bitmap bm = BitmapCache.get(pos);
					if (bm != null) {
						if (!bm.isRecycled()) {
							Log.d("debug", "位置:" + pos + "的位图未释放，释放!");
							//将已释放的索引添加到List中，循环结束后会从未释放集合中移除这些项
							releaseList.add(pos);
							//将已释放的ImageView添加到已释放集合中
							Recycled.put(pos,iv);
							bm.recycle();
						}
					}
				} catch (Exception e) {
					Log.d("debug","wtf???");
					releaseList.add(pos);
					Recycled.put(pos,iv);
					e.printStackTrace();
				}
			}
			//从Bitmap集合与未释放集合中移除失效的项
			for(int i=0;i<releaseList.size();i++){
				Log.d("debug","i Value is:" + i);
				BitmapCache.remove(i);
				UnRecycle.remove(i);
			}
			Log.d("debug", "已释放集合大小:" + UnRecycle.size());
			lock.notify();
			Log.d("debug", "释放内存完成");
		}
	}
	
	//界面更新方法
	Handler updateUI = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			//下载图片失败
			case -1:
				Toast.makeText(context, "加载图片失败!", Toast.LENGTH_SHORT).show();
				View err_v = ViewCache.get(msg.arg1);
				ImageView err_iv = (ImageView) err_v.findViewById(R.id.image);
				err_iv.setImageResource(R.drawable.download_error);
				break;
			//需要释放内存
			case 0:
				recycle();
				break;
			//下载图片成功，从Bitmap中取出并显示到ListView上
			case 1:
				int position = msg.arg1;
				View v = ViewCache.get(position);
				ImageView iv = (ImageView) v.findViewById(R.id.image);
				Bitmap bm = BitmapCache.get(position);
				iv.setImageBitmap(bm);
				break;
			}
		}

	};

}
