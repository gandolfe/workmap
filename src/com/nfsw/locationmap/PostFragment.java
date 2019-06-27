package com.nfsw.locationmap;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.nfsw.data.CheckLineBean;
import com.nfsw.data.CheckPointBean;
import com.nfsw.interfaces.PostCallBack;
import com.nfsw.service.NetService;
import com.nfsw.utils.MediaManager;
import com.nfsw.view.AudioRecorderButton;
import com.nfsw.view.AudioRecorderButton.AudioFinishRecorderListener;
import com.nfsw.view.CustomDialog;
import com.nfsw.view.PostImg_CustomDialog;
import com.yangyunsheng.locationmap.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class PostFragment extends Fragment implements LocationSource,AMapLocationListener,View.OnClickListener,OnGeocodeSearchListener{

	MapView mapview;
	private AMap aMap;
	NetService.MyBinder myBinder;
	private Context context;
	MyApplication application;
	CheckLineBean mLines = null;
	List<CheckPointBean> current = null;
	String workerNo;
	String token;
	double pointer_lat;
	double pointer_lng;
	double location_lat;
	double location_lng;
	
	RelativeLayout type_layout;
	ImageView type_img;
	TextView type_name;
	ImageView send_btn;
	EditText edit;
	ImageView post_img;
	TextView address_text;
	AudioRecorderButton record_btn ;
	FrameLayout voice_play;
	View voice_loud;
	
	private OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	private static final String TAG = "PostFragment";
	
	PopupWindow moreWorker;
	View mMenuView2;
	ListView moreList;
	String [] typeArray = {"覆盖","破损","塌陷","异物","沉降(管道)","堵塞","错接","雨污合流","缺失","下沉(检查井)"};
	int typeIndex;
	
	private static final int IMAGE = 1;
	private static final int CAMERA = 2;
	String photo_path;
	String photo_name;
	String photo_str;
	CustomDialog customDialog = null;
	Bitmap camera_bitmap = null;
	
	GeocodeSearch geocoderSearch ;
	String strExPosName = "";
	
	String voice_filepath = null;
	
	boolean isplaying = true;
	
	public PostFragment(Context ct,NetService.MyBinder binder){
		context = ct;
		application = (MyApplication)ct.getApplicationContext();
		workerNo = application.workerNo;
		token = application.token;
		myBinder = binder;
		mLines = application.data;
		myBinder.setPostCallback(postcallback);
			
	}
	
	PostCallBack postcallback = new PostCallBack() {
		
		@Override
		public void postExceptionResult(String result) {
			
			handler.removeMessages(0x5);
			if(customDialog!=null){
				customDialog.dismiss();
				customDialog =null;
			}
			
			JSONObject jsonobj = (JSONObject)JSONValue.parse(result);
			String code = (String) jsonobj.get("code");
			if(code.equals("0")){
				handler.sendEmptyMessage(0x1);
			}else{
				handler.sendEmptyMessage(0x2);
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.post_fragment, null);
		
		type_layout = (RelativeLayout) view.findViewById(R.id.type_select);
		type_layout.setOnClickListener(this);
		type_img = (ImageView) view.findViewById(R.id.type_tab);
		type_name = (TextView) view.findViewById(R.id.type_name);
		type_name.setText(typeArray[typeIndex]);
		send_btn = (ImageView) view.findViewById(R.id.send_btn);
		send_btn.setOnClickListener(this);
		edit = (EditText) view.findViewById(R.id.exception_des);
		post_img = (ImageView)view.findViewById(R.id.image_view);
		post_img.setOnClickListener(this);
		address_text = (TextView)view.findViewById(R.id.address_name);
		record_btn = (AudioRecorderButton)view.findViewById(R.id.record_btn);
		record_btn.setAudioFinishRecorderListener(mAudioFinishListener);
		voice_play = (FrameLayout)view.findViewById(R.id.voice_btn);
		voice_loud = (View)view.findViewById(R.id.id_recoder_anim);
		
		
		
		mapview = (MapView) view.findViewById(R.id.map);
		mapview.onCreate(savedInstanceState);
		aMap = mapview.getMap();
		aMap.clear();
		
		geocoderSearch = new GeocodeSearch(context);
		geocoderSearch.setOnGeocodeSearchListener(this);
		
		init();
		return view;
	}
	
	AudioFinishRecorderListener mAudioFinishListener = new AudioFinishRecorderListener() {
		
		@Override
		public void onFinish(float seconds, String filePath) {
			
			voice_play.setVisibility(View.VISIBLE);
			voice_play.setOnClickListener(PostFragment.this);
			
			voice_filepath = filePath;
			
			Log.i(TAG, "voice_filepath:"+voice_filepath);
		}
	};
	
	private void init(){
		if(aMap!=null){
			Log.i(TAG, "setOnMapTouchListener");
			aMap.setLocationSource(this);// 设置定位监听
			aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
			aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
			// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
			aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
			
			double latdata = 24.9341100000;
			double lngdata = 102.8199100000;
			
			aMap.moveCamera(CameraUpdateFactory
					.newLatLngZoom(new LatLng(latdata, lngdata), 20));
		}
	}
	
	
	Handler handler = new Handler(){
		public void dispatchMessage(android.os.Message msg) {
			switch(msg.what){
			case 0x1:
				edit.setText("");
				Toast.makeText(context, "上传成功！", Toast.LENGTH_SHORT).show();
				post_img.setImageResource(R.drawable.post_img_selector);
				post_img.setOnClickListener(PostFragment.this);
				post_img.setOnLongClickListener(null);
				break;
			case 0x2:
				Toast.makeText(context, "上传失败！", Toast.LENGTH_SHORT).show();
				break;
			case 0x5:
				customDialog.dismiss();
				customDialog =null;
				Toast.makeText(context, "上传失败！", Toast.LENGTH_SHORT).show();
				break;
			case 0x6:
				String address = msg.getData().getString("address");
				strExPosName = address;
				if(address!=null){
					address_text.setText(address);
				}
				break;
			case 0x7:
				address_text.setText("位置名获取失败");
				break;
			}
		};
	};
	
	public void setMarkers(){
		if(mLines==null){
			return;
		}
		
		if(mLines.getLinePoints().size()<=0){
			return;
		}
		if(aMap==null){
			return;
		}
		
		current= mLines.getLinePoints();
		
		for(int i=0;i<current.size();i++){
			MarkerOptions options = new MarkerOptions();
			double lat = Double.valueOf(current.get(i).getCheckPointLat());
			double lng = Double.valueOf(current.get(i).getCheckPointLng());
			options.icon(new BitmapDescriptorFactory().fromResource(R.drawable.plan_point_new));
			options.position(new LatLng(lat,lng));
			Marker mark = aMap.addMarker(options);
			mark.setObject(i);
		}
		
	}
	
	/**
	 * 点击右上角菜单弹出框
	 * @param context
	 * @param v
	 */
	public void showMore( View v) {// 类型选择弹出框	
		if (moreWorker == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mMenuView2 = inflater.inflate(R.layout.type_list, null);
			moreList = (ListView) mMenuView2.findViewById(R.id.type_list);
			moreList.setSelector(getResources().getDrawable(R.drawable.listviewclick));
			List<Map<String, Object>> moreData = new ArrayList<Map<String, Object>>();
			
			ArrayAdapter adapter = new ArrayAdapter<String>(context, R.layout.worker_list_item,typeArray);
			moreList.setAdapter(adapter);
			// 创建一个PopuWidow对象
//			DisplayMetrics dm=new DisplayMetrics();
//			this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//		    int width =dm.widthPixels;
//		    int height = dm.heightPixels;
		    moreWorker = new PopupWindow(mMenuView2,v.getWidth(), RelativeLayout.LayoutParams.WRAP_CONTENT, true);
		    type_img.setImageResource(R.drawable.type_tab_up);
		    type_img.invalidate();
		}
		
		moreWorker.setOnDismissListener(new PopupWindow.OnDismissListener() {
			
			@Override
			public void onDismiss() {
				type_img.setImageResource(R.drawable.type_tab_down);
			}
		});
		
		// 设置SelectPicPopupWindow弹出窗体可点击
		moreWorker.setFocusable(true);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		moreWorker.setAnimationStyle(R.style.morestyle);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0000000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		moreWorker.setBackgroundDrawable(dw);
		// 设置允许在外点击消失
		moreWorker.setOutsideTouchable(true);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		moreWorker.setBackgroundDrawable(new BitmapDrawable());
		moreWorker.showAsDropDown(v);
		moreList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				typeIndex = arg2;
				type_name.setText(typeArray[typeIndex]);
				moreWorker.dismiss();				
			}
		});
	}
	
	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		mapview.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		mapview.onPause();
		deactivate();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapview.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapview.onDestroy();
	}

	@Override
	public void onLocationChanged(AMapLocation arg0) {
		
		aMap.clear();
		setMarkers();
		
		location_lat = arg0.getLatitude();
		location_lng = arg0.getLongitude();
		pointer_lat = arg0.getLatitude();
		pointer_lng = arg0.getLongitude();
		Log.i(TAG, "location lat,lng:"+location_lat+","+location_lng);
		aMap.moveCamera(CameraUpdateFactory
				.newLatLngZoom(new LatLng(location_lat, location_lng), 18));
		
		MarkerOptions options_lc = new MarkerOptions();
		new BitmapDescriptorFactory();
		options_lc.position(new LatLng(location_lat, location_lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location_pointer));
		aMap.addMarker(options_lc);
		MarkerOptions options_pt = new MarkerOptions();
		options_pt.draggable(true);
		options_pt.position(new LatLng(location_lat, location_lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.exception_point));
		aMap.addMarker(options_pt);
		aMap.setOnMarkerDragListener(draglistener);
		
		//反地理编码
		LatLonPoint llp = new LatLonPoint(pointer_lat, pointer_lng);
		RegeocodeQuery query = new RegeocodeQuery(llp, 100,GeocodeSearch.AMAP);
		geocoderSearch.getFromLocationAsyn(query);
		
	}

	AMap.OnMarkerDragListener draglistener = new AMap.OnMarkerDragListener() {
		
		@Override
		public void onMarkerDragStart(Marker arg0) {
			Log.i(TAG, "drag start!");
		}
		
		@Override
		public void onMarkerDragEnd(Marker arg0) {
			Log.i(TAG, "drag end!");
			Projection p = aMap.getProjection();
			Point point = p.toScreenLocation(arg0.getPosition());
			Log.i(TAG, "point.y:"+point.y);
			point.y = point.y -30;
			Log.i(TAG, "point.y:"+point.y);
			
			LatLng ll =p.fromScreenLocation(point);
			pointer_lat = ll.latitude;
			pointer_lng = ll.longitude;
			
			Log.i(TAG, "pointer_lat:"+pointer_lat+";pointer_lng"+pointer_lng);
			
			//反地理编码
			LatLonPoint llp = new LatLonPoint(pointer_lat, pointer_lng);
			RegeocodeQuery query = new RegeocodeQuery(llp, 100,GeocodeSearch.AMAP);
			geocoderSearch.getFromLocationAsyn(query);
			
		}
		
		@Override
		public void onMarkerDrag(Marker arg0) {
			
		}
	};
	
	
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
			mLocationOption.setOnceLocation(true);
			mlocationClient.setLocationOption(mLocationOption);
			mlocationClient.startLocation();
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


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.type_select:
			showMore(type_layout);
			break;
		
		case R.id.image_view:
			
			final PostImg_CustomDialog dialog = new PostImg_CustomDialog(context, R.style.CustomDialog);
			dialog.setCameralistener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					Calendar calendar = Calendar.getInstance();
					Date date = new Date();
					calendar.setTime(date);
					String datetimestr = sdf.format(date);
					String temp_photo_name = datetimestr +".jpg";
					String  temp_photo_path = getDiskCacheDir() + File.separator + temp_photo_name;
					photo_path = temp_photo_path;
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(temp_photo_path)));  
					intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);  
					 
                    startActivityForResult(intent, CAMERA);  
					dialog.dismiss();
				}
			});
			
			dialog.setPhotoslistener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_PICK,
			                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			        startActivityForResult(intent, IMAGE);
					dialog.dismiss();
				}
			});
			
			dialog.show();
			
			break;
		case R.id.send_btn:
			String des =  edit.getText().toString();
			Log.i(TAG, "des str:"+des);
			if(des==null ||des.length()<=0){
				Toast.makeText(context, "描述不能为空！", Toast.LENGTH_SHORT).show();
				return;
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			Date date = new Date();
			calendar.setTime(date);
			String strDeviceSignDatetime = sdf.format(date);
			
			String voice_string = "";
			String voice_name = "";
			if(voice_filepath!=null){
				try {
					voice_string = encodeBase64File(voice_filepath);
					voice_name =  voice_filepath.substring(voice_filepath.lastIndexOf("/")+1);
					voice_name = voice_name.replace("-", "");
					Log.i(TAG, "voice_length:"+voice_string.length());
					Log.i(TAG, "voice_string:"+voice_string);
					Log.i(TAG, "voice_name:"+voice_name);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			myBinder.UploadExceptionData(workerNo, ""+location_lng+","+location_lat,strExPosName, strDeviceSignDatetime, 
					""+pointer_lng+","+pointer_lat, typeIndex+"", typeArray[typeIndex], des, photo_name,photo_str ,
					voice_name, voice_string, token);
			
			customDialog = new CustomDialog(context, "上传中...");
		    customDialog.show();
			
		    Message msg1 = new Message();
			msg1.what = 0x5;
		    handler.sendMessageDelayed(msg1, 1000*10);
			break;
			
		case R.id.voice_btn:
			if(isplaying){
				MediaManager.pause();
				MediaManager.release();
			}
			
			voice_loud.setBackgroundResource(R.drawable.adj);
			voice_loud.setBackgroundResource(R.drawable.play_anim);
			AnimationDrawable animation = (AnimationDrawable) voice_loud.getBackground();
			animation.start();
			
			MediaManager.playSound(voice_filepath, new MediaPlayer.OnCompletionListener() {

				public void onCompletion(MediaPlayer mp) {
					voice_loud.setBackgroundResource(R.drawable.adj);
					isplaying = false;
				}
			});
			break;
		}
	}
	
	
	public static String encodeBase64File(String path) throws Exception {
        File  file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return  Base64.encodeToString(buffer, Base64.NO_WRAP);
    }
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "case the onActivityResult requestCode:"+requestCode +";resultCode:"+resultCode);
		if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = context.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            photo_path = c.getString(columnIndex);
            if(photo_path!=null){
            	Log.i(TAG, "imagePath:"+photo_path);
            	int lastindex = photo_path.lastIndexOf(".");
            	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    			Calendar calendar = Calendar.getInstance();
    			Date date = new Date();
    			calendar.setTime(date);
    			String datetimestr = sdf.format(date);
    			photo_name = datetimestr +photo_path.substring(lastindex);
    			Log.i(TAG, "photo_name:"+photo_name);
            }else {
            	c.close();
            	return;
            }
            
            fillPhotoString(photo_path,4,1024*1024);
            
            c.close();
        }
		if(requestCode == CAMERA && resultCode == Activity.RESULT_OK){
			 
			int lastindex = photo_path.lastIndexOf(".");
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Calendar calendar = Calendar.getInstance();
			Date date = new Date();
			calendar.setTime(date);
			String datetimestr = sdf.format(date);
			photo_name = datetimestr +photo_path.substring(lastindex);
			Log.i(TAG, "photo_name:"+photo_name);
			 fillPhotoString(photo_path,8,1024*500);
			 
		}
	}
	
	private String getDiskCacheDir(){
    	String cachepath;
    	if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
    			|| !Environment.isExternalStorageRemovable()){
    		cachepath = context.getExternalCacheDir().getPath() + File.separator +"photo";
    	}else{
    		cachepath = context.getCacheDir().getPath()+ File.separator +"photo";
    	}
    	File fileDir = new File(cachepath);
    	if(!fileDir.exists()){
    		fileDir.mkdirs();
    	}
    	return cachepath;
    }
	
	private void fillPhotoString(String path, int scale, int maxSize) {

		Options opts = new Options();
		opts.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeFile(photo_path, opts);
		
		// 使用此流读取
				ByteArrayOutputStream baos;
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				try {
					byte[] imgBytes = baos.toByteArray();
					photo_str = Base64.encodeToString(imgBytes, Base64.NO_WRAP);
					Log.i(TAG, "photo_str size:" + photo_str.length());
					Log.i(TAG, photo_str);
					if (photo_str.length() > maxSize) {
						bitmap.recycle();
						fillPhotoString(path, scale * 2, maxSize);
					} else {
						if (bitmap != null) {
							int height = bitmap.getHeight();
							int width = bitmap.getWidth();
							int templength;
							int startx;
							int starty;
							if (height > width) {
								templength = width;
								startx =0;
								starty = (height-width)/2;
							} else {
								templength = height;
								starty =0;
								startx = (width-height)/2;
							}
							Bitmap new_bitmap = Bitmap.createBitmap(bitmap, startx, starty, templength, templength);
							bitmap.recycle();
							post_img.setImageBitmap(new_bitmap);
							post_img.invalidate();
							post_img.setOnClickListener(img_listener);
							post_img.setOnLongClickListener(img_Long_listener);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						baos.flush();
						baos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		
	}
	
	public void readPhotoStream(Bitmap bitmap, int scale, int maxSize) {
		// 使用此流读取
		ByteArrayOutputStream baos;
		baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		try {
			byte[] imgBytes = baos.toByteArray();
			photo_str = Base64.encodeToString(imgBytes, Base64.NO_WRAP);
			Log.i(TAG, "photo_str size:" + photo_str.length());
			Log.i(TAG, photo_str);
			if (photo_str.length() > maxSize) {
				readPhotoStream(bitmap, scale * 2, maxSize);
			} else {
				if (bitmap != null) {
					int height = bitmap.getHeight();
					int width = bitmap.getWidth();
					int templength;
					int startx;
					int starty;
					if (height > width) {
						templength = width;
						startx = 0;
						starty = (height - width) / 2;
					} else {
						templength = height;
						starty = 0;
						startx = (width - height) / 2;
					}
					Bitmap new_bitmap = Bitmap.createBitmap(bitmap, startx, starty, templength, templength);
					post_img.setImageBitmap(new_bitmap);
					post_img.invalidate();
					post_img.setOnClickListener(camera_listener);
					post_img.setOnLongClickListener(img_Long_listener);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				baos.flush();
				baos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public View.OnClickListener camera_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(context, ShowBigImgActivity.class);
			Bundle data = new Bundle();
			data.putParcelable("bitmap", camera_bitmap);
//			intent.putExtra("bitmap", camera_bitmap);
			intent.putExtras(data);
			context.startActivity(intent);
		}
	};
	
	public View.OnClickListener img_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(context, ShowBigImgActivity.class);
			intent.putExtra("path", photo_path);
			context.startActivity(intent);
		}
	};
	
	public OnLongClickListener img_Long_listener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			final PostImg_CustomDialog dialog = new PostImg_CustomDialog(context, R.style.CustomDialog);
			dialog.setCameralistener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
                    startActivityForResult(intent, CAMERA);  
					dialog.dismiss();
				}
			});
			
			dialog.setPhotoslistener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_PICK,
			                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			        startActivityForResult(intent, IMAGE);
					dialog.dismiss();
				}
			});
			
			dialog.show();
			return false;
		}
		
	};

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		
	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		
		if(rCode==1000){
			
			Message msg = new Message();
			Bundle data =new Bundle();
			
			String getNeighborhood = result.getRegeocodeAddress().getNeighborhood();
			if(getNeighborhood!=null && !getNeighborhood.isEmpty()){
				data.putString("address", getNeighborhood);
				msg.setData(data);
				msg.what = 0x6;
				handler.sendMessage(msg);
				Log.i(TAG, "getNeighborhood:"+getNeighborhood);
				return;
			}
			
			String getBuilding = result.getRegeocodeAddress().getBuilding();
			if(getBuilding!=null && !getBuilding.isEmpty()){
				data.putString("address", getBuilding);
				msg.setData(data);
				msg.what = 0x6;
				handler.sendMessage(msg);
				Log.i(TAG, "getBuilding:"+getBuilding);
				return;
			}
			
			
			String getFormatAddress = result.getRegeocodeAddress().getFormatAddress();
			if(getFormatAddress!=null){
				data.putString("address", getFormatAddress);
				msg.setData(data);
				msg.what = 0x6;
				handler.sendMessage(msg);
				Log.i(TAG, "getBuilding:"+getFormatAddress);
				return;
			}
			
			
		}else{
			handler.sendEmptyMessage(0x7);
		}
		
	}
	
}
