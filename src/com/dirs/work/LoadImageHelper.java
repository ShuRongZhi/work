package com.dirs.work;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class LoadImageHelper {

	private static LoadImageHelper instance = null;
	//指定缩放模式
	private boolean isJava = true;
	//指定缩放图片的宽高
	private final int width = 300;
	private final int height = 300;
	//线程停止标志
	private boolean isNeedStop;
	//缓存助手类
	private CacheHelper mCache = null;
	//下载图片的线程
	private getImageThread mThread = null;
	//Jni助手类
	private JniHelper mJniHelper = null;
	//同步锁对象
	private Object lock = new Object();


	public static LoadImageHelper getInstance(boolean b) {
		if (instance == null) {
			instance = new LoadImageHelper(b);
		}
		return instance;

	}

	public void clear(){
		instance = null;
	}
	
	private LoadImageHelper(boolean b) {
		mThread = new getImageThread();
		mCache = CacheHelper.getInstance();
		mJniHelper = JniHelper.getInstance();
		isJava = b;
		isNeedStop = false;
	}

	public void start() {
		// 判断线程是否运行，如果没运行则运行，否则忽略
		if (!mThread.isAlive()) {
			Log.d("debug", "线程未运行，运行");
			isNeedStop = false;
			mThread.start();
		} else {
			Log.d("debug", "线程已运行");
		}
	}

	//停止下载线程
	public void stop() {
		isNeedStop = true;
	}

	//图片下载线程
	class getImageThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (!isNeedStop) {
				//如果停止标志不为true，则查询加载队列是否为空，如果为空休眠1秒
				if (!mCache.isListEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Map<Integer, String> map = mCache.pop();
				if (map != null) {
					// 取出键的值
					int position = map.entrySet().iterator().next().getKey();
					String image = map.get(position);
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
			Log.d("debug", "线程退出");
		}

	}

	// 图片下载函数，调用JniHelper提供getImage接口，获得图片对应的byte[]，如果下载失败返回null
	private synchronized boolean getImage(String iamge, int position) {
		//指定缩放模式
		mJniHelper.init(!isJava);
		byte[] buf = mJniHelper.getImage(iamge);
		if (buf != null) {
			try {
				Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
				if (bm != null) {
					// 判断是否缩放，如果要求缩放则创建一个缩放后的Bitmap加入Bitmap集合中，否则直接加入
					if (!isJava) {
						mCache.BitmapCache.put(position, bm);
					} else {
						Bitmap newbm = Bitmap.createScaledBitmap(bm, width,
								height, true);
						bm.recycle();
						mCache.BitmapCache.put(position, newbm);
					}
				}
			} catch (OutOfMemoryError oom) {
				Log.d("debug", "内存溢出!");
				// 捕获oom异常，给handler发送消息，要求释放内存
				updateUI.sendEmptyMessage(0);
				// 同步锁，等待内存释放完毕后再加载
				synchronized (lock) {
					try {
						// 等待内存释放后继续操作，超时10秒
						lock.wait(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d("debug", "aaaa");
					// 等内存释放完毕后尝试重新解码图片
					Bitmap bm = null;
					try {
						bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
						if (!isJava) {
							mCache.BitmapCache.put(position, bm);
						} else {
							Bitmap newbm = Bitmap.createScaledBitmap(bm, width,
									height, true);
							bm.recycle();
							mCache.BitmapCache.put(position, newbm);
						}
					} catch (OutOfMemoryError wtf) {
						Log.d("debug", "wtf??内存还是溢出了！！你大爷的虚拟机!!");
						wtf.printStackTrace();
						return false;
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
			int size = mCache.UnRecycle.size();
			Log.d("debug", "准备开始释放内存,未释放集合大小:" + size);
			/**
			 * 释放算法：遍历未释放的ImageView集合，将其设置为默认图片 并根据索引，在Bitmap集合中获取Bitmap，将其释放
			 * */
			@SuppressWarnings("rawtypes")
			Iterator iter = mCache.UnRecycle.entrySet().iterator();
			List<Integer> releaseList = new ArrayList<Integer>();
			ImageView iv = null;
			
			// 计数器，只释放内存溢出位置前X张图片之前的图片，比如在15这个位置溢出，那就只释放0-(15-X)的图片，留下X张图片
			// 避免突然所有图片都消失了
			int count = 0;
			while (iter.hasNext()) {
				// 判断是否应该退出
				//这里为留下前3张图片
				if (++count > (size - 3)) {
					break;
				}
				@SuppressWarnings("unchecked")
				Map.Entry<Integer, ImageView> entry = (Map.Entry<Integer, ImageView>) iter
						.next();
				int pos = entry.getKey();
				// 从未释放的ImageView中取出
				iv = mCache.UnRecycle.get(pos);
				try {
					// 首先将取出的ImageView控件设置默认图片，断开与Bitmap的引用，避免回收到依然被引用的Bitmap而FC
					iv.setImageResource(R.drawable.loading);
					// 取出索引对应的Bitmap
					Bitmap bm = mCache.BitmapCache.get(pos);
					if (bm != null) {
						if (!bm.isRecycled()) {
							Log.d("debug", "位置:" + pos + "的位图未释放，释放!");
							// 将已释放的索引添加到List中，循环结束后会从未释放集合中移除这些项
							releaseList.add(pos);
							// 将已释放的ImageView添加到已释放集合中
							mCache.Recycled.put(pos, iv);
							bm.recycle();
						}
					}else{
						Log.d("debug","取出位置:" + pos + " 的位图时取到NULL！");
					}
				} catch (Exception e) {
					Log.d("debug", "wtf???");
					releaseList.add(pos);
					mCache.Recycled.put(pos, iv);
					e.printStackTrace();
				}
			}
			// 从Bitmap集合与未释放集合中移除失效的项
			for (int i = 0; i < releaseList.size(); i++) {
				mCache.BitmapCache.remove(i);
				mCache.UnRecycle.remove(i);
			}
			Log.d("debug", "已释放集合大小:" + mCache.UnRecycle.size());
			lock.notify();
			Log.d("debug", "释放内存完成");
		}
	}

	// 界面更新方法
	Handler updateUI = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			// 下载图片失败
			case -1: 
				//将下载失败的位置设置为失败提示图片
				View err_v = mCache.ViewCache.get(msg.arg1);
				ImageView err_iv = (ImageView) err_v.findViewById(R.id.image);
				err_iv.setImageResource(R.drawable.download_error);
				break;
			// 需要释放内存
			case 0:
				recycle();
				break;
			// 下载图片成功，从Bitmap中取出并显示到ListView上
			case 1:
				int position = msg.arg1;
				View v = mCache.ViewCache.get(position);
				ImageView iv = (ImageView) v.findViewById(R.id.image);
				Bitmap bm = mCache.BitmapCache.get(position);
				iv.setImageBitmap(bm);
				//加载成功再将Item添加到未释放列表中
				mCache.UnRecycle.put(position,iv);
				break;
			}
		}

	};

}
