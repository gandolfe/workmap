package com.nfsw.locationmap;

import java.util.ArrayList;
import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.utils.CameraUpdateUtil;
import com.amap.api.services.share.ShareSearch;
import com.nfsw.data.CheckGroupBean;
import com.nfsw.data.CheckLineBean;
import com.nfsw.data.CheckPointBean;
import com.nfsw.data.JSONHelper;
import com.nfsw.interfaces.LoginCallBack;
import com.nfsw.interfaces.NetWorkCallBack;
import com.nfsw.service.NetService;
import com.nfsw.service.NetService.MyBinder;
import com.nfsw.view.CustomMapView;
import com.yangyunsheng.locationmap.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class MainActivity_old extends Activity implements LocationSource,AMapLocationListener{
	
	
	NetService.MyBinder myBinder;
	MyApplication application;
	String workerNo;
	String token;
	List<CheckGroupBean> plandata;
	List<CheckLineBean> mLines = new ArrayList<CheckLineBean>();;
	int lineindex = 0;
	int sourceflag;
	SharedPreferences shares;
	double latitud;
	double longtitud;
	
	
	private AMap aMap;
	private CustomMapView mMapView = null;
	private OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	
	private static final int STROKE_COLOR = Color.argb(60, 3, 145, 255);
	private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
	private static final String TAG = "MainActivity";
	private static final String NETWORK_ACTION = "INTENT.ACTION.NETWORKSERVICE_WORKER";
	private static final String USERNAME = "username";
	private static final String TABLE = "MYTABLE";
	private static final String ISLOGIN = "login";
	private static final int RADIUS = 50;
	
	int count = 5;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		setContentView(R.layout.activity_main_old);
		shares = this.getSharedPreferences(TABLE, MODE_PRIVATE);
		
		application = (MyApplication)getApplicationContext();
		if(application.workerNo!=null){
			workerNo = application.workerNo;
		}
		if(application.token!=null){
			token = application.token;
		}
		
		sourceflag = getIntent().getIntExtra("sourceflag", 2);
		
		//地图控件引用
		mMapView = (CustomMapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		init();
		if(!mLines.isEmpty()){
			setMarkers(lineindex);
		}
		
//		Intent intent = new Intent(NETWORK_ACTION);
		Intent intent = new Intent(this, com.nfsw.service.NetService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		
	}
	
	
	OnMapTouchListener maptouchlistener = new OnMapTouchListener() {
		
		@Override
		public void onTouch(MotionEvent arg0) {
			Log.i(TAG, "count set 0");
			count = 0;
		}
	};
	
	
	ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myBinder = (MyBinder)service;
			myBinder.setNetWorkCallBack(callback);
			myBinder.setLoginCallBack(loginback);
			
			if(sourceflag==1){
				myBinder.postLoginInfo(workerNo, application.pwd);
			}
			
			myBinder.getCheckPlan(workerNo, token);
			
		}
	};
	
	LoginCallBack loginback = new LoginCallBack() {
		
		@Override
		public void loginSuccess(String token) {
			
		}
		
		@Override
		public void loginFailed(String detail) {
			if(detail.equals("101")){
				shares.edit().putBoolean(ISLOGIN, false).commit();
				Intent intent = new Intent(MainActivity_old.this, LoginActivity.class);
				startActivity(intent);
				MainActivity_old.this.finish();
			}
		}
	};
	
	
	NetWorkCallBack callback = new NetWorkCallBack() {
		
		@Override
		public void checkPlanResult(String result) {
			if(plandata==null){
				plandata =new ArrayList<CheckGroupBean>();
			}
//			JSONHelper.CheckplanResolve(result, handler, plandata, mLines);
			
//			if(plandata.size()>0){
//				application.data = mLines;
//			}
		}

		@Override
		public void checkPlanFailCode(String code) {
			
		}
	};
	
	Handler handler = new Handler(){
		public void dispatchMessage(android.os.Message msg) {
			switch(msg.what){
			case 0x1:
				String detail =(String)msg.getData().get("detial");
				Toast.makeText(MainActivity_old.this,detail , Toast.LENGTH_SHORT).show();
				break;
			case 0x2:
				shares.edit().putBoolean(ISLOGIN, false).commit();
				Intent intent = new Intent(MainActivity_old.this, LoginActivity.class);
				startActivity(intent);
				MainActivity_old.this.finish();
				break;
			case 0x3:
				setMarkers(lineindex);
				break;
			}
		};
	};
	
	
	private void init(){
		if(aMap==null){
			aMap = mMapView.getMap();
			aMap.setOnMapTouchListener(maptouchlistener);
			aMap.setLocationSource(this);// 设置定位监听
			aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
			aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
			// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
			aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
			etupLocationStyle();
		}
	}
	
	private void etupLocationStyle(){
		// 自定义系统定位蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		// 自定义定位蓝点图标
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
				fromResource(R.drawable.gps_point));
		// 自定义精度范围的圆形边框颜色
		myLocationStyle.strokeColor(0);
		//自定义精度范围的圆形边框宽度
		myLocationStyle.strokeWidth(0);
		// 设置圆形的填充颜色
		myLocationStyle.radiusFillColor(0);
		// 将自定义的 myLocationStyle 对象添加到地图上
		aMap.setMyLocationStyle(myLocationStyle);
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
//		deactivate();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		deactivate();
		mMapView.onDestroy();
		unbindService(conn);
	}

	
	/** 定位初始化以及启动定位*/
	@Override
	public void activate(OnLocationChangedListener arg0) {
		mListener = arg0;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			//设置定位监听
			mlocationClient.setLocationListener(this);
			//设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			//设置定位参数
			
			//设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
			mLocationOption.setInterval(5000);
			
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

			mlocationClient.startLocation();
		}
	}
	
	public void setMarkers(int index){
		if(index>=mLines.size()){
			return;
		}
		if(aMap==null){
			return;
		}
		CheckLineBean checkline = mLines.get(index);
		if(checkline==null){
			return;
		}
		
		
		LatLng mypoint = new LatLng(latitud, longtitud);
		List<CheckPointBean> checkpoints = checkline.getLinePoints();
		
		//test code
		checkpoints.add(new CheckPointBean("test001", "test001", "106.6072991755", "29.6782437309", "111"));
		checkpoints.add(new CheckPointBean("test002", "test002", "106.6143360000", "29.6847230000", "111"));
		checkpoints.add(new CheckPointBean("test002", "test002", "106.6138950000", "29.6842250000", "111"));
		
    	
		for(int i=0;i<checkpoints.size();i++){
			MarkerOptions options = new MarkerOptions();
			Double lat = Double.valueOf(checkpoints.get(i).getCheckPointLat());
			Double lng = Double.valueOf(checkpoints.get(i).getCheckPointLng());
			
			float distance = twopointsDistance(mypoint,new LatLng(lat, lng));
			Log.i(TAG, "dictance:"+distance);
			if(Math.abs(distance)<=RADIUS){
				//inner the area
				options.icon(new BitmapDescriptorFactory().fromResource(R.drawable.marker_blue));
			}else{
				//outer
				options.icon(new BitmapDescriptorFactory().fromResource(R.drawable.marker_red));
			}
			if(checkpoints.get(i).getCheckFlag()==1){
				//already checked
				options.icon(new BitmapDescriptorFactory().fromResource(R.drawable.marker_green));
			}
			options.title("title")
			.snippet(checkpoints.get(i).getCheckPointName())
			.position(new LatLng(lat,lng));
			Marker mark = aMap.addMarker(options);
			mark.setObject(i);
		}
		
		AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
		    // marker 对象被点击时回调的接口

			@Override
			public boolean onMarkerClick(Marker marker) {
				Log.i(TAG, "click marker index is:"+(Integer)marker.getObject());
				Toast.makeText(MainActivity_old.this, (Integer)marker.getObject(), Toast.LENGTH_SHORT).show();
				return true;
			}
		};
		
		aMap.setOnMarkerClickListener(markerClickListener);
	}
	
	public float twopointsDistance(LatLng startLatlng,LatLng endLatlng){
		
		return AMapUtils.calculateLineDistance(startLatlng, endLatlng) ;
		
	}
	
	
	/** 停止定位的相关处理*/
	@Override
	public void deactivate() {
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}

	/**定位的回调返回函数 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null
					&& amapLocation.getErrorCode() == 0) {
				
//				Log.i(TAG, "定位结果类型："+amapLocation.getLocationType());
//				Log.i(TAG, "GPS的当前状态："+amapLocation.getGpsAccuracyStatus());
//				Log.i(TAG, "经度："+amapLocation.getLongitude()+";维度："+amapLocation.getLatitude());
				Log.i(TAG, "获取定位精确度："+amapLocation.getAccuracy());
				
				 longtitud = amapLocation.getLongitude();
				 latitud = amapLocation.getLatitude();
				
				if(count<5){
					count++;
				}else{
					
					aMap.clear(true);
					CircleOptions coption = new CircleOptions();
					coption.fillColor(FILL_COLOR).center(new LatLng(latitud, longtitud)).radius(RADIUS)
					.strokeWidth(1).strokeColor(STROKE_COLOR);
					aMap.addCircle(coption);
					
					
					mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
					aMap.moveCamera(CameraUpdateFactory
							.newLatLngZoom(new LatLng(latitud, longtitud), 20));
//					aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(latitud, longtitud)));
//					aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
					setMarkers(lineindex);
				}
				
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
				Log.e("AmapErr",errText);
			}
		}
	}
	
	
}
