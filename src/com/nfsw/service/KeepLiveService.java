package com.nfsw.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@SuppressLint("NewApi")
public class KeepLiveService extends JobService {

	
	private static final String TAG = "KeepLiveService";
	private static final String TABLE = "MYTABLE";
	Context  ct = null;
	JobParameters param = null;
	
	String longitudestr;
	String latitudestr;
	String timestr;
	
	//声明AMapLocationClient类对象
	AMapLocationClient mLocationClient = null;
	//声明AMapLocationClientOption对象
	public AMapLocationClientOption mLocationOption = null;
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ct = this.getApplicationContext();
		Log.i("KeepLiveService", "start Command!");
		return super.onStartCommand(intent, flags, startId);
		
	}
	
	@Override
	public boolean onStartJob(JobParameters params) {
		
		Log.i("KeepLiveService", "onStartJob!");
		param = params;
		mJobHandler.sendMessage( Message.obtain( mJobHandler, 1, params ) );
	    return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		// TODO Auto-generated method stub
		Log.i("KeepLiveService", "onStopJob!");
		return false;
	}
	
	
	private Handler mJobHandler = new Handler( new Handler.Callback() {
        @Override
        public boolean handleMessage( Message msg ) {
        	Log.i("KeepLiveService", "run once time!");
        	
        	//初始化定位
    		mLocationClient = new AMapLocationClient(getApplicationContext());
    		//设置定位回调监听
    		mLocationClient.setLocationListener(mLocationListener);
    		//初始化AMapLocationClientOption对象
    		mLocationOption = new AMapLocationClientOption();
    		//设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
    		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
    		//获取一次定位结果：
    		//该方法默认为false。
    		mLocationOption.setOnceLocation(true);
    		mLocationOption.setOnceLocationLatest(true);
    		//给定位客户端对象设置定位参数
    		mLocationClient.setLocationOption(mLocationOption);
    		//启动定位
    		mLocationClient.startLocation();
        	
            return true;
        }
    } ); 
	
	
	//声明定位回调监听器
			public AMapLocationListener mLocationListener = new AMapLocationListener(){

				@Override
				public void onLocationChanged(AMapLocation amapLocation) {
							if(amapLocation==null){
								Log.i(TAG, "amapLocation is null!");
								return;
							}
							if(amapLocation.getErrorCode()!=0){
								Log.i(TAG, "amapLocation has exception errorCode:"+amapLocation.getErrorCode());
								return;
							}
							
							double longitude = amapLocation.getLongitude();//获取经度
							double latitude = amapLocation.getLatitude();//获取纬度
							
							Log.i(TAG, "net_location:longitude:"+longitude +";latitude:"+latitude);
							longitudestr = String.valueOf(longitude);
							latitudestr = String.valueOf(latitude);
							
							SharedPreferences shares= KeepLiveService.this.getSharedPreferences(TABLE, Context.MODE_PRIVATE);
							Double src_longitude = (double) shares.getFloat("longitude", 0);
							Double src_latitude = (double) shares.getFloat("latitude", 0);
							Log.i(TAG, "share_location:longitude:"+longitude +";latitude:"+latitude);
							LatLng old_point = new LatLng(src_latitude, src_longitude);
							LatLng now_point = new LatLng(latitude, longitude);
							float distance = AMapUtils.calculateLineDistance(old_point, now_point);
							Log.i(TAG, "distance:"+distance);
							if(distance<-10){
								jobFinished( param, false );
								resetSchedul();
								return;
							}
							
							shares.edit().putFloat("longitude",(float)longitude).commit();
							shares.edit().putFloat("latitude",(float)latitude).commit();
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date = new Date(amapLocation.getTime());
							timestr = df.format(date);
							Log.i(TAG, "longitude,latitude:"+longitude+","+latitude);
							Log.i(TAG, "time:"+timestr);
							
							Executors.newSingleThreadExecutor().execute(run);
				}
						
			};
			
			Runnable run  = new Runnable() {
				
				@Override
				public void run() {
					upDatePosition(getMac(),longitudestr+","+latitudestr,timestr,timestr);
					
					jobFinished( param, false );
					
					resetSchedul();
				}
			};	
			
			public void upDatePosition(String mac, String position, String recordTime, String reportTime) {
				Log.i(TAG, mac+";"+position+";"+recordTime);
				// 命名空间
				String nameSpace = "http://nfswit.cc/";

				// 调用的方法名称
				String methodName = "UpdateDevicePosition";

				// EndPoint
				String endPoint = "http://182.247.238.98:82/WebService1.asmx";

				// SOAP Action
				String soapAction = "http://nfswit.cc/UpdateDevicePosition";

				// 指定WebService的命名空间和调用的方法名
				SoapObject rpc = new SoapObject(nameSpace, methodName);

				// 指定参数
				rpc.addProperty("strDeviceMAC", mac);
				rpc.addProperty("strDevicePosition", position);
				rpc.addProperty("strDeviceRecordTime", recordTime);
				rpc.addProperty("strDeviceReportTime", reportTime);

				// 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				envelope.implicitTypes = true;
				envelope.bodyOut = rpc;
				// 设置是否调用的是dotNet开发的WebService
				envelope.dotNet = true;
				// 等价于envelope.bodyOut = rpc;
				envelope.setOutputSoapObject(rpc);

				HttpTransportSE transport = new HttpTransportSE(endPoint);
				try {
					// 调用WebService
					transport.call(soapAction, envelope);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// 获取返回的数据
				Object object = envelope.bodyIn;
				// SoapFault object = (SoapFault) envelope.bodyIn;

				if (object == null) {
					Log.i(TAG, "return object is null!");
					return;
				}
				if (object instanceof SoapFault) {
					Log.i(TAG, "SoapFault refult is :" + object.toString());
					return;
				} else if (object instanceof SoapObject) {
					// 获取返回的结果
					Log.i(TAG, "SoapObject refult is :" + object.toString());

					try {
						SoapObject result = (SoapObject) object;
						PropertyInfo info = new PropertyInfo();
						result.getPropertyInfo(0, info);
						String str = result.getProperty(0).toString();
						Log.i(TAG, str);
						JSONObject jsonobj = (JSONObject) JSONValue.parse(str);
						String code = (String) jsonobj.get("code");
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
			
			private String getMac() {
				String macSerial = null;
				String str = "";

				try {
					Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
					InputStreamReader ir = new InputStreamReader(pp.getInputStream());
					LineNumberReader input = new LineNumberReader(ir);

					for (; null != str;) {
						str = input.readLine();
						if (str != null) {
							macSerial = str.trim();// 去空格
							break;
						}
					}
				} catch (IOException ex) {
					// 赋予默认值
					ex.printStackTrace();
				}
				
				if(macSerial == null || "".equals(macSerial)){
					WifiManager wifiMgr = (WifiManager)KeepLiveService.this.getSystemService(Context.WIFI_SERVICE);  
					WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());  
					if (null != info) {  
						macSerial = info.getMacAddress();  
					}  
					Log.i(TAG, "info.getMacAddress() mac:"+macSerial);
				}
				
				if(macSerial == null || "".equals(macSerial)){
					
					macSerial = getLocalMacAddressFromBusybox();
					Log.i(TAG, "getLocalMacAddressFromBusybox mac:"+macSerial);
				}
				
				return macSerial;
			}

			
			  public static String getLocalMacAddressFromBusybox(){   
			         String result = "";     
			         String Mac = "";
			         result = callCmd("busybox ifconfig","HWaddr");
			          
			         //如果返回的result == null，则说明网络不可取
			         if(result==null){
			             return "网络出错，请检查网络";
			         }
			          
			         //对该行数据进行解析
			         //例如：eth0      Link encap:Ethernet  HWaddr 00:16:E8:3E:DF:67
			         if(result.length()>0 && result.contains("HWaddr")==true){
			             Mac = result.substring(result.indexOf("HWaddr")+6, result.length()-1);
			             Log.i("test","Mac:"+Mac+" Mac.length: "+Mac.length());
			              
			      
			             result = Mac;
			             Log.i("test",result+" result.length: "+result.length());            
			         }
			         return result;
			     }   
			     
			     private static String callCmd(String cmd,String filter) {   
			         String result = "";   
			         String line = "";   
			         try {
			             Process proc = Runtime.getRuntime().exec(cmd);
			             InputStreamReader is = new InputStreamReader(proc.getInputStream());   
			             BufferedReader br = new BufferedReader (is);   
			              
			             //执行命令cmd，只取结果中含有filter的这一行
			             while ((line = br.readLine ()) != null && line.contains(filter)== false) {   
			                 //result += line;
			                 Log.i("test","line: "+line);
			             }
			              
			             result = line;
			             Log.i("test","result: "+result);
			         }   
			         catch(Exception e) {   
			             e.printStackTrace();   
			         }   
			         return result;   
			     }

			
	
	
	private void resetSchedul(){
		JobScheduler mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
		JobInfo.Builder builder = new JobInfo.Builder( 1,
                new ComponentName( getPackageName(), KeepLiveService.class.getName() ) );

        
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);
        builder.setMinimumLatency(20000);
        builder.setOverrideDeadline(30000); 


        if( mJobScheduler.schedule( builder.build() ) <= 0 ) {
            //If something goes wrong
       	 Log.i("KeepLiveService", "JobScheduler is error!");
        }else {
       	 Log.i("KeepLiveService", "JobScheduler go to run!");
        }
	}
	

}
