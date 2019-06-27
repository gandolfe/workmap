package com.nfsw.locationmap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.nfsw.data.CheckGroupBean;
import com.nfsw.data.CheckLineBean;
import com.nfsw.data.CheckPointBean;
import com.nfsw.data.JSONHelper;
import com.nfsw.interfaces.SignCallBack;
import com.nfsw.service.NetService;
import com.nfsw.utils.DBHandler;
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
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class SignFragment extends Fragment implements LocationSource,AMapLocationListener{
	
	NetService.MyBinder myBinder;
	MapView mMapView;
	private AMap aMap;
	private Context context;
	MyApplication application;
	String workerNo;
	String token;
	double latitud;
	double longtitud;
	SharedPreferences shares;
	private View view;
	DBHandler dbhandler;
	
	List<CheckGroupBean> plandata;
	CheckLineBean mLines = new CheckLineBean();
	List<CheckPointBean> current = new ArrayList<CheckPointBean>();
	int lineindex = 0;
	HashMap< Integer, Marker> innerMarker = new HashMap<Integer, Marker>();
	Circle lastcircle =null;
	
	private OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	
	private static final int STROKE_COLOR = Color.argb(60, 3, 145, 255);
	private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
	private static final int RADIUS = 50;
	private static final String TAG = "SignFragment";
	private static final String TABLE = "MYTABLE";
	private static final String ISLOGIN = "login";
	
	private static final int COUNTER = 3;
	int count = COUNTER;
	
	public SignFragment(Context ct,NetService.MyBinder binder){
		context = ct;
		application = (MyApplication)ct.getApplicationContext();
		workerNo = application.workerNo;
		token = application.token;
		
		if(binder!=null){
			myBinder = binder;
			myBinder.setSignCallBack(signcallback);
			myBinder.getCheckPlan(workerNo, token);
		}
	}
	
	public void setBinder(NetService.MyBinder binder){
		myBinder = binder;
		myBinder.setSignCallBack(signcallback);
		myBinder.getCheckPlan(workerNo, token);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.sign_fragment, null);
		mMapView = (MapView) view.findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		aMap = mMapView.getMap();
		
		shares = context.getSharedPreferences(TABLE, context.MODE_PRIVATE);
		
		dbhandler = DBHandler.getInstance(context.getApplicationContext());
		mLines.setLinePoints(dbhandler.getPlandata());
		for(int i=0;i<mLines.getLinePoints().size();i++ ){
			Log.i(TAG, "getCheckPointNo:"+mLines.getLinePoints().get(i).getCheckPointNo());
		}
		handler.sendEmptyMessage(0x3);
		return view;
	}
	
	
	SignCallBack signcallback = new SignCallBack() {
		
		@Override
		public void checkPlanResult(String result) {
			if(plandata==null){
				plandata =new ArrayList<CheckGroupBean>();
			}
			JSONHelper.CheckplanResolve(result, handler, plandata, mLines);
			
			if(mLines.getLinePoints().size()>0){
				application.data = mLines;
			}
			
			Log.i(TAG, " checkPlanResult save the plandata!");
			dbhandler.deletOldData();
			for(int i=0;i< (mLines.getLinePoints().size()>=20?20:mLines.getLinePoints().size());i++){
				CheckPointBean tempbean =mLines.getLinePoints().get(i);
				dbhandler.savePlanData(tempbean.getCheckPointNo(), tempbean.getCheckPointName(), tempbean.getCheckPointLng(), 
						tempbean.getCheckPointLat(), String.valueOf(tempbean.getCheckFlag()));
			}
			
		}

		@Override
		public void signResult(String result, String checkPointNo) {
			JSONObject jsonobj = (JSONObject)JSONValue.parse(result);
			String code = (String) jsonobj.get("code");
			if(code.equals("0")){
				//sign success
				for(int i=0;i<current.size();i++){
					if(current.get(i).getCheckPointNo().equals(checkPointNo)){
						current.get(i).setCheckFlag(1);
						
						innerMarker.clear();
						aMap.clear(true);
						if(lastcircle!=null){
							lastcircle.setVisible(false);
							lastcircle=null;
						}
						CircleOptions coption = new CircleOptions();
						coption.fillColor(FILL_COLOR).center(new LatLng(latitud, longtitud)).radius(RADIUS)
						.strokeWidth(1).strokeColor(STROKE_COLOR);
						lastcircle = aMap.addCircle(coption);
						break;
					}
				}
				resetMarkers();
				handler.sendEmptyMessage(0x4);
			}else if(code.equals("1")){
				//sign failed
				handler.sendEmptyMessage(0x5);
			}
		}
	};
	
	Handler handler = new Handler(){
		public void dispatchMessage(android.os.Message msg) {
			switch(msg.what){
			case 0x1:
				String detail =(String)msg.getData().get("detial");
				Toast.makeText(SignFragment.this.getActivity(),detail , Toast.LENGTH_SHORT).show();
				break;
			case 0x2:
				shares.edit().putBoolean(ISLOGIN, false).commit();
				Intent intent = new Intent(SignFragment.this.getActivity(), LoginActivity.class);
				startActivity(intent);
				SignFragment.this.getActivity().finish();
				break;
			case 0x3:
				setMarkers(lineindex);
				break;
			case 0x4:
				Toast.makeText(getActivity(), "签到成功！", Toast.LENGTH_SHORT).show();
				break;
			case 0x5:
				Toast.makeText(getActivity(), "签到失败！", Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};
	
	
	private void init(){
		if(aMap!=null){
			Log.i(TAG, "setOnMapTouchListener");
			aMap.setLocationSource(this);// 设置定位监听
			aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
			aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
			// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
			aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
			etupLocationStyle();
			
			double latdata = 24.9341100000;
			double lngdata = 102.8199100000;
			
			aMap.moveCamera(CameraUpdateFactory
					.newLatLngZoom(new LatLng(latdata, lngdata), 20));
		}
	}
	
	private void etupLocationStyle(){
		// 自定义系统定位蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		// 自定义定位蓝点图标
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
				fromResource(R.drawable.user_location_pointer));
		// 自定义精度范围的圆形边框颜色
		myLocationStyle.strokeColor(Color.argb(0, 0, 0,0));
		//自定义精度范围的圆形边框宽度
		myLocationStyle.strokeWidth(0);
		// 设置圆形的填充颜色
		myLocationStyle.radiusFillColor(Color.argb(0, 0, 0,0));
		// 将自定义的 myLocationStyle 对象添加到地图上
		aMap.setMyLocationStyle(myLocationStyle);
	}
	

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null
					&& amapLocation.getErrorCode() == 0) {
				
				Log.i(TAG, "获取定位精确度："+amapLocation.getAccuracy());
				Log.i(TAG, "location type:"+amapLocation.getLocationType());
				
				 longtitud = amapLocation.getLongitude();
				 latitud = amapLocation.getLatitude();
			
//				aMap.clear();
				 
				setMarkers(lineindex);
				 
				if(lastcircle!=null){
					lastcircle.setVisible(false);
					lastcircle=null;
				}
				CircleOptions coption = new CircleOptions();
				coption.fillColor(FILL_COLOR).center(new LatLng(latitud, longtitud)).radius(RADIUS)
				.strokeWidth(1).strokeColor(STROKE_COLOR);
				lastcircle = aMap.addCircle(coption);
					
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
				Log.i(TAG, "reset zoom and marker!");
//				aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
				aMap.moveCamera(CameraUpdateFactory
						.newLatLngZoom(new LatLng(latitud, longtitud), 20));
				
				
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
				Log.e("AmapErr",errText);
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
			mLocationOption.setInterval(2000);
			
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

			mlocationClient.startLocation();
		}
	}
	
	
	public void signResult(){
		if(mLines.getLinePoints().size()<=0){
			return;
		}
		if(aMap==null){
			return;
		}
		
		CheckLineBean checkline = mLines;
		if(checkline==null && myBinder!=null){
			innerMarker.clear();
			aMap.clear();
			myBinder.getCheckPlan(workerNo, token);
			return;
		}
		
		LatLng mypoint = new LatLng(latitud, longtitud);
		current= checkline.getLinePoints();
		
	}
	
	public void setMarkers(int index){
		if(mLines.getLinePoints().size()<=0){
			return;
		}
		if(aMap==null){
			return;
		}
		
		CheckLineBean checkline = mLines;
		if(checkline==null && myBinder!=null){
			innerMarker.clear();
			aMap.clear();
			myBinder.getCheckPlan(workerNo, token);
			return;
		}
		
		LatLng mypoint = new LatLng(latitud, longtitud);
		current= checkline.getLinePoints();
		
		for(int i=0;i<current.size();i++){
			MarkerOptions options = new MarkerOptions();
			Log.i(TAG, "lat,lng:"+current.get(i).getCheckPointLat()+","+current.get(i).getCheckPointLng()+"--"+current.get(i).getCheckPointName());
			Double lat =0.0;
			Double lng =0.0;
			try{
				lat = Double.valueOf(current.get(i).getCheckPointLat());
				lng = Double.valueOf(current.get(i).getCheckPointLng());
			}catch(Exception e){
				continue;
			}
			
			
			float distance = twopointsDistance(mypoint,new LatLng(lat, lng));
			if(Math.abs(distance)>RADIUS){
				if(innerMarker.containsKey(i)){
					aMap.clear(true);
					innerMarker.clear();
					resetMarkers();
					return;
				}
				continue;
			}else{
				if(!innerMarker.containsKey(i)){
					aMap.clear(true);
					innerMarker.clear();
					resetMarkers();
					return;
				}
				//marker has already in the map
				continue;
			}
			
//			if(Math.abs(distance)<=RADIUS && current.get(i).getCheckFlag()==1){
//				//inner the area
//				options.icon(new BitmapDescriptorFactory().fromResource(R.drawable.marker_green));
//				options.snippet(getResources().getString(R.string.already_sign));
//			}else if(Math.abs(distance)<=RADIUS && current.get(i).getCheckFlag()==0){
//				//outer
//				options.icon(new BitmapDescriptorFactory().fromResource(R.drawable.marker_yellow));
//				options.snippet(getResources().getString(R.string.sure_sign));
//			}
//			options.position(new LatLng(lat,lng));
//			Marker mark = aMap.addMarker(options);
//			mark.setObject(i);
//			//save the marker
//			innerMarker.put(i, mark);
			
		}
		
//		aMap.setInfoWindowAdapter(windowAdapter);
//		
//		aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
//			
//			@Override
//			public boolean onMarkerClick(Marker marker) {
//
//				if(marker.isInfoWindowShown()){
//					marker.hideInfoWindow();
//					return true;
//				}else{
//					marker.showInfoWindow();
//				}
//				return false;
//			}
//		});
//		
//		aMap.setOnInfoWindowClickListener(listener);
	}
	
	private void resetMarkers(){
		LatLng mypoint = new LatLng(latitud, longtitud);
		
		for(int i=0;i<current.size();i++){
			MarkerOptions options = new MarkerOptions();
			Log.i(TAG, "lat,lng:"+current.get(i).getCheckPointLat()+","+current.get(i).getCheckPointLng()+"--"+current.get(i).getCheckPointName());
			Double lat =0.0;
			Double lng =0.0;
			try{
				lat = Double.valueOf(current.get(i).getCheckPointLat());
				lng = Double.valueOf(current.get(i).getCheckPointLng());
			}catch(Exception e){
				continue;
			}
			
			float distance = twopointsDistance(mypoint,new LatLng(lat, lng));
			
			if(Math.abs(distance)<=RADIUS && current.get(i).getCheckFlag()==1){
				//inner the area
				options.icon(new BitmapDescriptorFactory().fromResource(R.drawable.plan_point_new));
				options.snippet(getResources().getString(R.string.already_sign));
			}else if(Math.abs(distance)<=RADIUS && current.get(i).getCheckFlag()==0){
				//outer
				options.icon(new BitmapDescriptorFactory().fromResource(R.drawable.plan_point_signed_new));
				options.snippet(getResources().getString(R.string.sure_sign));
			}else{
				continue;
			}
			options.position(new LatLng(lat,lng));
			Marker mark = aMap.addMarker(options);
			mark.setObject(i);
			//save the marker
			innerMarker.put(i, mark);
			
		}
		
		aMap.setInfoWindowAdapter(windowAdapter);
		
		aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
			
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
		});
		
		aMap.setOnInfoWindowClickListener(listener);
	}
	
		
	OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {
		 
	    @Override
	    public void onInfoWindowClick(Marker arg0) {
	        
	    	int i = (Integer) arg0.getObject();
	    	CheckPointBean lickPoint = current.get(i);
	    	if(lickPoint.getCheckFlag()==0){
	    		
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar calendar = Calendar.getInstance();
				Date date = new Date();
				calendar.setTime(date);
				String strDeviceSignDatetime = sdf.format(date);
				
				
				double point_lat = Double.parseDouble(lickPoint.getCheckPointLat());
				double point_lng = Double.parseDouble(lickPoint.getCheckPointLng());
				
				float  fDistanceToCheckPoint =twopointsDistance( new LatLng(latitud, longtitud),new LatLng(point_lat,point_lng));
				String mfDistanceToCheckPoint = String.valueOf(fDistanceToCheckPoint);
	    		
	    		myBinder.pointSign(lickPoint.getCheckPointNo(), workerNo, 
	    				""+lickPoint.getCheckPointLng()+","+lickPoint.getCheckPointLat(),
	    				""+longtitud+","+latitud, mfDistanceToCheckPoint, strDeviceSignDatetime, strDeviceSignDatetime, token);
	    	}
	    	arg0.hideInfoWindow();
	    }
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
	
	
	
	
	public float twopointsDistance(LatLng startLatlng,LatLng endLatlng){
			
		return AMapUtils.calculateLineDistance(startLatlng, endLatlng) ;
			
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		init();
		if(aMap!=null){
			aMap.clear();
			innerMarker.clear();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
		deactivate();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		
		Log.i(TAG, "save the plandata!");
		dbhandler.deletOldData();
		for(int i=0;i< (current.size()>=20?20:current.size());i++){
			CheckPointBean tempbean = current.get(i);
			dbhandler.savePlanData(tempbean.getCheckPointNo(), tempbean.getCheckPointName(), tempbean.getCheckPointLng(), 
					tempbean.getCheckPointLat(), String.valueOf(tempbean.getCheckFlag()));
		}
		
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
