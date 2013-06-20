package com.dirs.work;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private LoadImageHelper mLoadHelper = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bundle mBundle = this.getIntent().getExtras();
		boolean isZoom = mBundle.getBoolean("Mode");
		String Size = mBundle.getString("Size");
		int num = mBundle.getInt("Number");
		String str = "图片是否缩放:" + isZoom + " 图片质量:" + Size + " 显示数量:" + num;
		this.setTitle(str);
		ListView lv = (ListView)findViewById(R.id.list);
		List<String> data = new ArrayList<String>();
		//在这里指定文件路径
		for(int i=0;i<num;++i){
			data.add(Size +"/test" + i +".jpg");
		}
		mLoadHelper = LoadImageHelper.getInstance(isZoom, getApplicationContext());
		ImageAdapter adapter = new ImageAdapter(data,getApplicationContext(),isZoom);
		lv.setAdapter(adapter);
		//开启加载线程
		mLoadHelper.start();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//停止加载线程
		mLoadHelper.stop();
	}
	
}
