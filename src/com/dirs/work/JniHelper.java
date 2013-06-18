package com.dirs.work;

public class JniHelper {
	private static JniHelper instance = null;
	private JniHelper(){};
	
	public synchronized static JniHelper getInstance(){
		if(instance == null){
			instance = new JniHelper();
		}
		return instance;
	}
	static{
	    //载入so库
		System.loadLibrary("imageHelper");
	}
	//初始化操作，用来设置全局变量，在不同线程中必须分别执行init操作
	public synchronized native void init();
	//取得图片内存
	//限制同一时间只能有一个线程访问，避免对Native层的资源产生修改冲突
	public synchronized native byte[] getImage(String ImageID);
}
