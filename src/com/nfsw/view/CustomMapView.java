package com.nfsw.view;

import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.MapView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class CustomMapView extends MapView {

	public CustomMapView(Context arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public CustomMapView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public CustomMapView(Context arg0, AMapOptions arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public CustomMapView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		touchCallBack.doTouch();
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		
		return super.onTouchEvent(event);
	}
	
	TouchCallBack touchCallBack;
	
	public interface TouchCallBack{
		public void doTouch();
	}
	public void setTouchCallBack(TouchCallBack callback){
		touchCallBack = callback;
	}

}
