package com.nfsw.locationmap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.nfsw.data.CheckGroupBean;
import com.nfsw.data.CheckLineBean;
import com.nfsw.data.CheckPointBean;
import com.nfsw.data.CheckTrackPointBean;
import com.nfsw.data.JSONHelper;
import com.nfsw.interfaces.BrowseInterface;
import com.nfsw.service.NetService;
import com.yangyunsheng.locationmap.R;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class BrowseFragment extends Fragment implements LocationSource,AMapLocationListener{

	NetService.MyBinder myBinder;
	private Context context;
	MapView mapview;
	private AMap aMap;
	View view;
	TextView action_text;
	
	MyApplication application;
	String workerNo;
	String token;
	SharedPreferences shares;
	private static final String TAG = "BrowseFragment";
	private static final String TABLE = "MYTABLE";
	private static final String ISLOGIN = "login";
	
	private OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	double latitud;
	double longtitud;
	private static final int RADIUS = 10;
	ArrayList<MarkerOptions> optiions = new ArrayList<MarkerOptions>();
	
	private static final int STROKE_COLOR = Color.argb(60, 3, 145, 255);
	private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
	
	List<CheckGroupBean> plandata;
	CheckLineBean mLines = new CheckLineBean();
	List<CheckTrackPointBean> tracklist = new ArrayList<CheckTrackPointBean>();
	int lineindex = 0;
	
	int count = 2;
	
	public BrowseFragment(Context ct,NetService.MyBinder binder){
		context = ct;
		myBinder = binder;
		myBinder.setBrowseCallback(browsecall);
		
		application = (MyApplication)ct.getApplicationContext();
		workerNo = application.workerNo;
		token = application.token;
		mLines = application.data;
		if(mLines==null){
			myBinder.getCheckPlan(workerNo, token);
		}
	}
	
	public void setActionText(TextView textview){
		action_text = textview;
	}
	
	private void get_checkTrack(){
		
		myBinder.getCheckTrack(workerNo,token);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			view = inflater.inflate(R.layout.browse_fragment, null);
			mapview = (MapView) view.findViewById(R.id.map);
			mapview.onCreate(savedInstanceState);
			aMap = mapview.getMap();
			init();
			if(mLines!=null){
				aMap.clear(true);
				setMarkers(lineindex);
				refreshAction_text();
			}
			
			count = 2;
			get_checkTrack();
		
		return view;
	}
	
	private void refreshAction_text(){
		if(mLines.getLinePoints()==null){
			return;
		}
		List<CheckPointBean> tempdatas = mLines.getLinePoints();
		int checkednum=0;
		int unchecknum=0;
		for(int i =0;i< tempdatas.size();i++){
			if(tempdatas.get(i).getCheckFlag()==0){
				unchecknum++;
			}else{
				checkednum++;
			}
		}
		action_text.setTextSize(11);
		action_text.setText("已巡检:("+checkednum+") 未巡检:("+unchecknum+")");
	}
	
	private void init(){
		if(aMap!=null){
			Log.i(TAG, "setOnMapTouchListener");
			aMap.setOnMapTouchListener(maptouchlistener);
			aMap.setLocationSource(this);// 设置定位监听
			aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
			aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
			// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
			aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
			etupLocationStyle();
			
			double latdata = 24.9341100000;
			double lngdata = 102.8199100000;
			
			aMap.moveCamera(CameraUpdateFactory
					.newLatLngZoom(new LatLng(latdata, lngdata), 20));
		}
	}
	
	OnMapTouchListener maptouchlistener = new OnMapTouchListener() {
		
		@Override
		public void onTouch(MotionEvent arg0) {
			Log.i(TAG, "count set 0");
			count = 0;
		}
	};
	
	private void etupLocationStyle(){
		// 自定义系统定位蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		// 自定义定位蓝点图标
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
				fromResource(R.drawable.user_location_pointer));
		// 自定义精度范围的圆形边框颜色
		myLocationStyle.strokeColor(0);
		//自定义精度范围的圆形边框宽度
		myLocationStyle.strokeWidth(0);
		// 设置圆形的填充颜色
		myLocationStyle.radiusFillColor(0);
		// 将自定义的 myLocationStyle 对象添加到地图上
		aMap.setMyLocationStyle(myLocationStyle);
		Log.i(TAG, "etupLocationStyle");
	}
	
	BrowseInterface browsecall = new BrowseInterface() {
		
		@Override
		public void GetWorkerCkeckTrack(String result) {
			tracklist.clear();
			JSONHelper.checkTrackResolve(result, handler, tracklist);
		}

		@Override
		public void checkPlanResult(String result) {
			JSONHelper.CheckplanResolve(result, handler, plandata, mLines);
		}
	};
	
	Handler handler = new Handler(){
		public void dispatchMessage(android.os.Message msg) {
			switch(msg.what){
			case 0x1:
				String detail =(String)msg.getData().get("detial");
				break;
			case 0x2:
				shares.edit().putBoolean(ISLOGIN, false).commit();
				Intent intent = new Intent(BrowseFragment.this.getActivity(), LoginActivity.class);
				startActivity(intent);
				BrowseFragment.this.getActivity().finish();
				break;
			case 0x3:
				aMap.clear(true);
				updateCheckpoints();
				setMarkers(lineindex);
				setLines();
				refreshAction_text();
				break;
			case 0x4:
				aMap.clear(true);
				setMarkers(lineindex);
				setLines();
				refreshAction_text();
				break;
		
			}
		}

	};
	

	
	
	private void updateCheckpoints(){
		
		List<CheckPointBean>  temp = mLines.getLinePoints();
		for(int i=0 ;i<tracklist.size();i++){
			
			String no = tracklist.get(i).getCheckPointNo();
			for(int j = 0;j<temp.size();j++){
				if(temp.get(j).getCheckPointNo().equals(no)){
					temp.get(j).setCheckFlag(1);
					break;
				}
			}
		
		}
		
	}
	
	
	private void setMarkers(int index) {
		if(mLines.getLinePoints().size()<=0){
			return;
		}
		if(aMap==null){
			return;
		}
		
		optiions.clear();
		CheckLineBean checkline = mLines;
		if(checkline==null){
			return;
		}
		
		aMap.setInfoWindowAdapter(windowAdapter);
		
		List<CheckPointBean> checks = checkline.getLinePoints();
    	
		for(int i=0;i<checks.size();i++){
			MarkerOptions options = new MarkerOptions();
			Double lat = Double.valueOf(checks.get(i).getCheckPointLat());
			Double lng = Double.valueOf(checks.get(i).getCheckPointLng());
			
			
			if(checks.get(i).getCheckFlag()==1){
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.plan_point_signed_new));
			}else if(checks.get(i).getCheckFlag()==0){
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.plan_point_new));
			}
			Log.i(TAG, "lat:"+lat+";lng:"+lng+";CheckFlag:"+checks.get(i).getCheckFlag());
			
			
			options.position(new LatLng(lat,lng)).snippet(checks.get(i).getCheckPointName());
			optiions.add(options);
		}
		
		aMap.addMarkers(optiions, false);
		aMap.setOnMarkerClickListener(markerListener);
	
	};
	
	InfoWindowAdapter windowAdapter = new InfoWindowAdapter() {
		
		@Override
		public View getInfoWindow(Marker arg0) {
			
			View infowindow = getActivity().getLayoutInflater().inflate(R.layout.infowindow, null);
			TextView content = (TextView) infowindow.findViewById(R.id.content_text);
			content.setText(arg0.getSnippet());
			
			return infowindow;
		}
		
		@Override
		public View getInfoContents(Marker arg0) {
			return null;
		}
	};
	
	OnMarkerClickListener markerListener = new OnMarkerClickListener() {
		
		@Override
		public boolean onMarkerClick(Marker marker) {
			
			if(marker.isInfoWindowShown()){
				marker.hideInfoWindow();
				return true;
			}else{
				marker.showInfoWindow();
			}
			return false;
		}
	};
	
	private void setLines() {
		
		List<LatLng> latlngs = new ArrayList<LatLng>();
		for(int i =0;i<tracklist.size();i++){
			String device_pst = tracklist.get(i).getDevicePosition();
			String [] pstarray = device_pst.split(",");
			double lati = Double.parseDouble(pstarray[1]);
			double lngi =Double.parseDouble(pstarray[0]);
			Log.i(TAG, "line lati:"+lati +";lngi:"+lngi);
			latlngs.add(new LatLng(lati, lngi));
		}
		aMap.addPolyline(new PolylineOptions().addAll(latlngs).width(8).transparency(0.6f).color(Color.argb(111, 2, 2, 2)));
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
		 longtitud = amapLocation.getLongitude();
		 latitud = amapLocation.getLatitude();
		
		 if(count<2){
				Log.i(TAG, "count ++!");
				count++;
			}else{
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
				Log.i(TAG, "reset zoom and marker!");
				aMap.clear(true);
				aMap.moveCamera(CameraUpdateFactory
						.newLatLngZoom(new LatLng(latitud, longtitud), 18));
				setMarkers(lineindex);
				setLines();
				refreshAction_text();
			}
		}
	}

	@Override
	public void activate(OnLocationChangedListener arg0) {
		Log.i(TAG, "activate");
		mListener = arg0;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(context);
			mLocationOption = new AMapLocationClientOption();
			//设置定位监听
			mlocationClient.setLocationListener(this);
			//设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			//设置定位参数
			
			//设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
			mLocationOption.setInterval(4000);
			
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

			mlocationClient.startLocation();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mapview.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mapview.onPause();
		deactivate();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapview.onSaveInstanceState(outState);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapview.onDestroy();
	}
	
	@Override
	public void deactivate() {
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}
}
