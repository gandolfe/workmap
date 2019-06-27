package com.nfsw.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.nfsw.interfaces.BrowseInterface;
import com.nfsw.interfaces.LoginCallBack;
import com.nfsw.interfaces.NetWorkCallBack;
import com.nfsw.interfaces.PostCallBack;
import com.nfsw.interfaces.SignCallBack;
import com.nfsw.locationmap.SignFragment;
import com.nfsw.utils.DBHandler;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class NetService extends Service {

	
	private static final int SERVICE_ID = 3;
	
	Handler netHandler = null;
	
	private static final String TAG = "NetService";
	
	private LoginCallBack callback ;
	private NetWorkCallBack networkcallback;
	private SignCallBack signcallback;
	private BrowseInterface browsecallback;
	private PostCallBack postcallback;
	MyBinder myBinder;
	String mac;
	@Override
	public void onCreate() {
		super.onCreate();
		netThread.start();
		myBinder = new MyBinder();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		startForeground(SERVICE_ID, new Notification()); 
		return super.onStartCommand(intent, flags, startId);
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return myBinder;
	}
	
	public class MyBinder extends Binder{
		public void postLoginInfo(String uid,String psd){
			proxy_postLoginInfo(uid,psd);
		}
		
		public void setLoginCallBack(LoginCallBack imple){
			callback = imple;
		}
		
		public void setNetWorkCallBack(NetWorkCallBack imple){
			networkcallback = imple;
		}
		
		public void setSignCallBack(SignCallBack signcall){
			signcallback = signcall;
		}
		
		public void setBrowseCallback(BrowseInterface browsecall){
			browsecallback = browsecall;
		}
		
		public void setPostCallback(PostCallBack postcall){
			postcallback = postcall;
		}
		
		public void getCheckPlan(String workerNo,String token){
			proxy_getCheckPlan(workerNo, token);
		}
		
		public void DeviceLogout(String workerNo,String token){
			proxy_DeviceLogout(workerNo,token);
		}
		
		public void pointSign(String strCheckPointNo,String strWorkerNo, String strCheckPointPosition,String strDevicePosition,
			 String fDistanceToCheckPoint,String strDeviceSignDatetime,String strDeviceUploadDatetime,String strToken){
			proxy_CheckPointSign(strCheckPointNo, strWorkerNo, strCheckPointPosition, strDevicePosition,
					fDistanceToCheckPoint, strDeviceSignDatetime, strDeviceUploadDatetime, strToken);
		}
		
		public void getCheckTrack(String strWorkerNo,String strToken){
			proxy_GetWorkerCkeckTrack(strWorkerNo, strToken);
		}
		
		public void UploadExceptionData(String strWorkerNo, String strDevicePosition,String strExPosName,
	            String strDeviceDatetime,String strExceptionPosition, String TypeNo, String Type, 
	            String strExcepDesc, String picName,String picBytes, String voiceName, String voiceBytes,String strToken){
			
			proxy_UploadExceptionData(strWorkerNo, strDevicePosition, strExPosName,strDeviceDatetime, strExceptionPosition, TypeNo, Type, strExcepDesc, picName, picBytes, voiceName, voiceBytes, strToken);
		}
	}
	
	
	public void proxy_postLoginInfo(String uid,String psd){
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("uid", uid);
		data.putString("pwd", psd);
		msg.setData(data);
		msg.what = 0x1;
		netHandler.sendMessage(msg);
	}
	
	public void proxy_getCheckPlan(String workerNo,String token){
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("workerNo", workerNo);
		data.putString("token", token);
		msg.setData(data);
		msg.what = 0x2;
		netHandler.sendMessage(msg);
	}
	
	public void proxy_DeviceLogout(String workerNo,String token){
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("workerNo", workerNo);
		data.putString("token", token);
		msg.setData(data);
		msg.what = 0x4;
		netHandler.sendMessage(msg);
	}
	
	public void proxy_CheckPointSign(String strCheckPointNo,String strWorkerNo, String strCheckPointPosition,String strDevicePosition,
			 String fDistanceToCheckPoint,String strDeviceSignDatetime,String strDeviceUploadDatetime,String strToken){
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("strCheckPointNo", strCheckPointNo);
		data.putString("strWorkerNo", strWorkerNo);
		data.putString("strCheckPointPosition", strCheckPointPosition);
		data.putString("strDevicePosition", strDevicePosition);
		data.putString("fDistanceToCheckPoint", fDistanceToCheckPoint);
		data.putString("strDeviceSignDatetime", strDeviceSignDatetime);
		data.putString("strDeviceUploadDatetime", strDeviceUploadDatetime);
		data.putString("strToken", strToken);
		msg.setData(data);
		msg.what = 0x5;
		netHandler.sendMessage(msg);
	}
	
	public void proxy_GetWorkerCkeckTrack(String strWorkerNo, String strToken){
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("strWorkerNo", strWorkerNo);
		data.putString("strToken", strToken);
		msg.setData(data);
		msg.what = 0x6;
		netHandler.sendMessage(msg);
	}
	
	public void proxy_UploadExceptionData(String strWorkerNo, String strDevicePosition,String strExPosName,
            String strDeviceDatetime,String strExceptionPosition, String TypeNo, String Type, 
            String strExcepDesc, String picName,String picBytes, String voiceName, String voiceBytes,String strToken){
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("strWorkerNo", strWorkerNo);
		data.putString("strDevicePosition", strDevicePosition);
		data.putString("strExPosName",strExPosName);
		data.putString("strDeviceDatetime", strDeviceDatetime);
		data.putString("strExceptionPosition", strExceptionPosition);
		data.putString("TypeNo", TypeNo);
		data.putString("Type", Type);
		data.putString("strExcepDesc", strExcepDesc);
		data.putString("picName", picName);
		data.putString("picBytes", picBytes);
		data.putString("voiceName", voiceName);
		data.putString("voiceBytes", voiceBytes);
		data.putString("strToken", strToken);
		msg.setData(data);
		msg.what = 0x7;
		netHandler.sendMessage(msg);
	}
	
	/**
	 * 收发网络数据的线程
	 */
	Thread netThread = new Thread(){
		@Override
		public void run() {
			Looper.prepare();
			netHandler = new Handler(){
				public void dispatchMessage(Message msg) {
					Bundle data = msg.getData();
					switch(msg.what){
					case 0x1: //发送登录验证数据
						
						String macid = getMac();
						String uid = data.getString("uid");
						String pwd = data.getString("pwd");
						checkInfo(macid,uid,pwd);
						break;
					case 0x2://获取巡检计划
						String workerNo = data.getString("workerNo");
						String token = data.getString("token");
						GetCheckPlan(workerNo,token);
						break;
					case 0x3: //获取移动轨迹
						break;
					case 0x4: //登出
						String macid1 = getMac();
						String workerNo1 = data.getString("workerNo");
						String token1 = data.getString("token");
						DeviceLogout(macid1,workerNo1,token1);
						break;
					case 0x5: //签到
						String strCheckPointNo = data.getString("strCheckPointNo");
						String strWorkerNo = data.getString("strWorkerNo");
						String strCheckPointPosition = data.getString("strCheckPointPosition");
						String strDevicePosition = data.getString("strDevicePosition");
						String fDistanceToCheckPoint = data.getString("fDistanceToCheckPoint");
						String strDeviceSignDatetime = data.getString("strDeviceSignDatetime");
						String strDeviceUploadDatetime = data.getString("strDeviceUploadDatetime");
						String strToken = data.getString("strToken");
						CheckPointSign(strCheckPointNo, strWorkerNo, strCheckPointPosition, strDevicePosition,
								fDistanceToCheckPoint, strDeviceSignDatetime, strDeviceUploadDatetime, strToken);
						break;
					case 0x6: //
						String strWorkerNo0x6 = data.getString("strWorkerNo");
						String strToken0x6 = data.getString("strToken");
						GetWorkerCkeckTrack(strWorkerNo0x6, strToken0x6);
						break;
					case 0x7:
						String strWorkerNo0x7 = data.getString("strWorkerNo");
						String strDeviceMAC0x7 = getMac();
						String strDevicePosition0x7 =data.getString("strDevicePosition");
						String strExPosName = data.getString("strExPosName");
			            String strDeviceDatetime0x7 =data.getString("strDeviceDatetime");
			            String strExceptionPosition0x7 =data.getString("strExceptionPosition");
			            String TypeNo = data.getString("TypeNo");
			            String Type = data.getString("Type"); 
			            String strExcepDesc = data.getString("strExcepDesc"); 
			            String picName = data.getString("picName"); 
			            String picBytes =data.getString("picBytes");
			            String voiceName = data.getString("voiceName");
			            String voiceBytes = data.getString("voiceBytes");
			            String strToken0x7 = data.getString("strToken");
			            UploadExceptionData(strWorkerNo0x7, strDeviceMAC0x7, strDevicePosition0x7,strExPosName, strDeviceDatetime0x7,
			            		strExceptionPosition0x7, TypeNo, Type, strExcepDesc, picName, picBytes, voiceName, 
			            		voiceBytes, strToken0x7);
					}
				};
			};
			Looper.loop();
		}
	};
	
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
			 try {  
	                return loadFileAsString("/sys/class/net/eth0/address")  
	                        .toUpperCase().substring(0, 17);  
	            } catch (Exception e) {  
	                e.printStackTrace();  
	                  
	            }  
		}
		
		
		if(macSerial == null || "".equals(macSerial)){
			WifiManager wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);  
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
	     
	 public static String loadFileAsString(String fileName) throws Exception {  
         FileReader reader = new FileReader(fileName);    
         String text = loadReaderAsString(reader);  
         reader.close();  
         return text;  
     }  
   public static String loadReaderAsString(Reader reader) throws Exception {  
         StringBuilder builder = new StringBuilder();  
         char[] buffer = new char[4096];  
         int readLength = reader.read(buffer);  
         while (readLength >= 0) {  
             builder.append(buffer, 0, readLength);  
             readLength = reader.read(buffer);  
         }  
         return builder.toString();  
     }  
	

	/**
	 * 检查登录信息
	 * @param param
	 */
	 public void checkInfo(String mac,String username,String pwd) {  
	        // 命名空间  
	        String nameSpace = "http://nfswit.cc/"; 
	        
	        // 调用的方法名称  
	        String methodName = "DeviceLogin";  
	        
	        // EndPoint  
	        String endPoint = "http://182.247.238.98:82/WebService1.asmx";  
	       
	        // SOAP Action  
	        String soapAction = "http://nfswit.cc/DeviceLogin";
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        //指定参数
	        rpc.addProperty("strMAC", mac);   
	        rpc.addProperty("strWorkerNo", username);
	        rpc.addProperty("strPwd", pwd);
	        rpc.addProperty("strLoginPosition", "123_587");
	        rpc.addProperty("strIsManager", "N");
	  
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
//	        SoapFault object = (SoapFault) envelope.bodyIn;  
	        
	        if(object==null){
	        	Log.i(TAG, "return object is null!");
	        	return;
	        }
	        if(object instanceof SoapFault){
	        	Log.i(TAG, "SoapFault refult is :"+  object.toString());
	        	callback.loginFailed("服务器无法处理请求！");
	        	return;
	        }else if(object instanceof SoapObject){
	        	  // 获取返回的结果  
	        	Log.i(TAG, "SoapObject refult is :"+  object.toString());
	        	
	        	try{
	        		SoapObject result = (SoapObject)object;
		        	PropertyInfo info = new PropertyInfo();
		        	result.getPropertyInfo(0, info);
		        	String str = result.getProperty(0).toString();
		        	Log.i(TAG, str);
		        	JSONObject jsonobj =(JSONObject)JSONValue.parse(str);
		        	String code = (String)jsonobj.get("code");
		        	
			        if(code.equals("0")){
			        	//success
			        	callback.loginSuccess((String)jsonobj.get("data"));
			        }else{
			        	//failed
			        	callback.loginFailed((String)jsonobj.get("data"));
			        	callback.loginFailed(code);
			        }
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        	
	        }
	      
	    }
	 
	/**
	 * 
	 */
	 public void GetCheckPlan(String workerNo,String token){
		Log.i(TAG, "go  to getCheckPlan!");
		
		 // 命名空间  
	        String nameSpace = "http://nfswit.cc/"; 
	        
	        // 调用的方法名称  
	        String methodName = "GetCheckPlan";  
	        
	        // EndPoint  
	        String endPoint = "http://182.247.238.98:82/WebService1.asmx";  
	       
	        // SOAP Action  
	        String soapAction = "http://nfswit.cc/GetCheckPlan";
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        //指定参数
	        rpc.addProperty("strWorkerNo", workerNo);
	        rpc.addProperty("strToken", token);
	  
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
//	        SoapFault object = (SoapFault) envelope.bodyIn;  
	        
	        if(object==null){
	        	Log.i(TAG, "return object is null!");
	        	return;
	        }
	        if(object instanceof SoapFault){
	        	Log.i(TAG, "SoapFault refult is :"+  object.toString());
	        	return;
	        }else if(object instanceof SoapObject){
	        	  // 获取返回的结果  
	        	Log.i(TAG, "SoapObject refult is :"+  object.toString());
	        	
	        	try{
	        		SoapObject result = (SoapObject)object;
//		        	PropertyInfo info = new PropertyInfo();
//		        	result.getPropertyInfo(0, info);
	        		SoapPrimitive spri = (SoapPrimitive) result.getProperty(0);
	        		int count = spri.getAttributeCount();
	        		String str0 = (String)spri.getValue();
	        		
		        	if(signcallback!=null){
		        		signcallback.checkPlanResult(str0);
		        	}
		        	if(browsecallback!=null){
		        		browsecallback.checkPlanResult(str0);
		        	}
		        	
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        }
	 }
	 
	 
	 
	 /**
	  * 获取MAC移动轨迹
	  */
	 public void GetDeviceMovementTrack(String strDeviceMAC,String strStartTime,String strEndTime,String strToken){
		 Log.i(TAG, "strDeviceMAC:"+strDeviceMAC+";strStartTime:"+strStartTime+";strEndTime:"+strEndTime+";strToken:"+strToken);
		    // 命名空间  
	        String nameSpace = "http://nfswit.cc/"; 
	        
	        // 调用的方法名称  
	        String methodName = "GetDeviceMovementTrack";  
	        
	        // EndPoint  
	        String endPoint = "http://182.247.238.98:82/WebService1.asmx";  
	       
	        // SOAP Action  
	        String soapAction = "http://nfswit.cc/GetDeviceMovementTrack";
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        //指定参数
	        rpc.addProperty("strDeviceMAC", strDeviceMAC);
	        rpc.addProperty("strStartTime", strStartTime);
	        rpc.addProperty("strEndTime", strEndTime);
	        rpc.addProperty("strToken", strToken);
	  
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
	        
	        if(object==null){
	        	Log.i(TAG, "return object is null!");
	        	return;
	        }
	        if(object instanceof SoapFault){
	        	Log.i(TAG, "SoapFault refult is :"+  object.toString());
	        	return;
	        }else if(object instanceof SoapObject){
	        	  // 获取返回的结果  
	        	Log.i(TAG, "trail refult is :"+  object.toString());
	        	
	        	try{
	        		SoapObject result = (SoapObject)object;
		        	PropertyInfo info = new PropertyInfo();
		        	result.getPropertyInfo(0, info);
		        	String str = result.getProperty(0).toString();
		        	Log.i(TAG, str);
		        	
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        	
	        }
	 }
	 
	 
	 /**
	  * 登出
	  */
	 public void DeviceLogout(String strDeviceMAC,String strWorkerNo,String strToken){
		 Log.i(TAG, "strDeviceMAC:"+strDeviceMAC+";strWorkerNo:"+strWorkerNo+";strToken:"+strToken);
		    // 命名空间  
	        String nameSpace = "http://nfswit.cc/"; 
	        
	        // 调用的方法名称  
	        String methodName = "DeviceLogout";  
	        
	        // EndPoint  
	        String endPoint = "http://182.247.238.98:82/WebService1.asmx";  
	       
	        // SOAP Action  
	        String soapAction = "http://nfswit.cc/DeviceLogout";
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        //指定参数
	        rpc.addProperty("strDeviceMAC", strDeviceMAC);
	        rpc.addProperty("strWorkerNo", strWorkerNo);
	        rpc.addProperty("strToken", strToken);
	  
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
	        
	        if(object==null){
	        	Log.i(TAG, "return object is null!");
	        	return;
	        }
	        if(object instanceof SoapFault){
	        	Log.i(TAG, "SoapFault refult is :"+  object.toString());
	        	return;
	        }else if(object instanceof SoapObject){
	        	  // 获取返回的结果  
	        	Log.i(TAG, "trail refult is :"+  object.toString());
	        	
	        	try{
	        		SoapObject result = (SoapObject)object;
		        	PropertyInfo info = new PropertyInfo();
		        	result.getPropertyInfo(0, info);
		        	String str = result.getProperty(0).toString();
		        	Log.i(TAG, str);
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        	
	        }
	 }
	 
	 
	 /**
	  * 签到
	  */
	 public void CheckPointSign(String strCheckPointNo,String strWorkerNo, String strCheckPointPosition,String strDevicePosition,
			 String fDistanceToCheckPoint,String strDeviceSignDatetime,String strDeviceUploadDatetime,String strToken){
		   
		 	// 命名空间  
	        String nameSpace = "http://nfswit.cc/"; 
	        
	        // 调用的方法名称  
	        String methodName = "CheckPointSign";  
	        
	        // EndPoint  
	        String endPoint = "http://182.247.238.98:82/WebService1.asmx";  
	       
	        // SOAP Action  
	        String soapAction = "http://nfswit.cc/CheckPointSign";
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        //指定参数
	        rpc.addProperty("strCheckPointNo", strCheckPointNo);
	        rpc.addProperty("strWorkerNo", strWorkerNo);
	        rpc.addProperty("strCheckPointPosition", strCheckPointPosition);
	        rpc.addProperty("strDevicePosition", strDevicePosition);
	        rpc.addProperty("fDistanceToCheckPoint", fDistanceToCheckPoint);
	        rpc.addProperty("strDeviceSignDatetime", strDeviceSignDatetime);
	        rpc.addProperty("strDeviceUploadDatetime", strDeviceUploadDatetime);
	        rpc.addProperty("strToken", strToken);
	  
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
	        } catch (ConnectException e) {  
	        	Log.i(TAG, "connect exception!");
	            e.printStackTrace();  
	        }  catch(Exception e){
	        	e.printStackTrace();  
	        	Log.i(TAG, "exist exception!");
	        }
	  
	        // 获取返回的数据  
	        Object object = envelope.bodyIn;   
	        
	        if(object==null){
	        	Log.i(TAG, "return object is null!");
	        	//判断是否有网络
	        	ConnectivityManager cm = (ConnectivityManager)NetService.this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
	        	State state_wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	        	State state_net = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	        	if(state_wifi!=null && state_net!=null && state_wifi !=State.CONNECTED && state_net!=State.CONNECTED){
	        		DBHandler.getInstance(NetService.this.getApplicationContext()).saveSignedData(strCheckPointNo, strWorkerNo, 
	        		strCheckPointPosition, strDevicePosition, fDistanceToCheckPoint, strDeviceSignDatetime, strDeviceUploadDatetime, strToken);
	        	}
	        	
	        	return;
	        }
	        if(object instanceof SoapFault){
	        	Log.i(TAG, "SoapFault refult is :"+  object.toString());
	        	
	        	return;
	        }else if(object instanceof SoapObject){
	        	  // 获取返回的结果  
	        	Log.i(TAG, "trail refult is :"+  object.toString());
	        	
	        	try{
	        		SoapObject result = (SoapObject)object;
		        	PropertyInfo info = new PropertyInfo();
		        	result.getPropertyInfo(0, info);
		        	String str = result.getProperty(0).toString();
		        	Log.i(TAG, str);
		        	signcallback.signResult(str,strCheckPointNo);
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        	
	        }
	 }
	 
	 /**
	  * 巡检轨迹
	  */
	 public void GetWorkerCkeckTrack(String strWorkerNo, String strToken){
		    // 命名空间  
	        String nameSpace = "http://nfswit.cc/"; 
	        
	        // 调用的方法名称  
	        String methodName = "GetWorkerPeriodCkeckTrack";  
	        
	        // EndPoint  
	        String endPoint = "http://182.247.238.98:82/WebService1.asmx";  
	       
	        // SOAP Action  
	        String soapAction = "http://nfswit.cc/GetWorkerPeriodCkeckTrack";
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        //指定参数
	        rpc.addProperty("strWorkerNo", strWorkerNo);
	        rpc.addProperty("strToken", strToken);
	  
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
	        
	        if(object==null){
	        	Log.i(TAG, "return object is null!");
	        	return;
	        }
	        if(object instanceof SoapFault){
	        	Log.i(TAG, "SoapFault refult is :"+  object.toString());
	        	return;
	        }else if(object instanceof SoapObject){
	        	  // 获取返回的结果  
	        	Log.i(TAG, "check track refult is :"+  object.toString());
	        	
	        	try{
	        		SoapObject result = (SoapObject)object;
		        	PropertyInfo info = new PropertyInfo();
		        	result.getPropertyInfo(0, info);
		        	String str = result.getProperty(0).toString();
		        	Log.i(TAG, str);
		        	browsecallback.GetWorkerCkeckTrack(str);
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        }
	 }
	 
	 /**
	  * 上报异常
	  */
	 public void UploadExceptionData(String strWorkerNo,String strDeviceMAC, String strDevicePosition,String strExPosName,
             String strDeviceDatetime,String strExceptionPosition, String TypeNo, String Type, 
             String strExcepDesc, String picName,String picBytes, String voiceName, String voiceBytes,String strToken){
		    // 命名空间  
	        String nameSpace = "http://nfswit.cc/"; 
	        
	        // 调用的方法名称  
	        String methodName = "UploadExceptionData";  
	        
	        // EndPoint  
	        String endPoint = "http://182.247.238.98:82/WebService1.asmx";  
	       
	        // SOAP Action  
	        String soapAction = "http://nfswit.cc/UploadExceptionData";
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        //指定参数
	        rpc.addProperty("strWorkerNo", strWorkerNo);
	        rpc.addProperty("strDeviceMAC", strDeviceMAC);
	        rpc.addProperty("strExceptionPosition", strExceptionPosition);
	        rpc.addProperty("strExPosName", strExPosName);
	        rpc.addProperty("strDevicePosition", strDevicePosition);
	        rpc.addProperty("strDeviceDatetime", strDeviceDatetime);
	        rpc.addProperty("TypeNo", TypeNo);
	        rpc.addProperty("Type", Type);
	        rpc.addProperty("strExcepDesc", strExcepDesc);
	        
	        if(picName==null){
	        	rpc.addProperty("picName","");
	        }else{
	        	rpc.addProperty("picName",picName);
	        }
	        
	        if(picBytes==null){
	        	rpc.addProperty("picBytes","");
	        }else{
	        	rpc.addProperty("picBytes",picBytes);
	        }
	        
	        if(voiceName==null){
	        	rpc.addProperty("voiceName","");
	        }else{
	        	rpc.addProperty("voiceName",voiceName);
	        }
	        
	        if(voiceBytes==null){
	        	rpc.addProperty("voiceBytes","");
	        }else{
	        	rpc.addProperty("voiceBytes",voiceBytes);
	        }
	        
	        rpc.addProperty("strToken", strToken);
	  
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
	        
	        if(object==null){
	        	Log.i(TAG, "return object is null!");
	        	//判断是否有网络
	        	ConnectivityManager cm = (ConnectivityManager)NetService.this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
	        	State state_wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	        	State state_net = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	        	if(state_wifi!=null && state_net!=null && state_wifi !=State.CONNECTED && state_net!=State.CONNECTED){
	        		DBHandler.getInstance(NetService.this.getApplicationContext()).saveExceptionData(strWorkerNo, strDeviceMAC,
	        				strDevicePosition, strExPosName, strDeviceDatetime, strExceptionPosition, TypeNo, Type, strExcepDesc,
	        				picName, picBytes, voiceName, voiceBytes, strToken);
	        	}
	        	return;
	        }
	        if(object instanceof SoapFault){
	        	Log.i(TAG, "SoapFault refult is :"+  object.toString());
	        	//判断是否有网络
	        	ConnectivityManager cm = (ConnectivityManager)NetService.this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
	        	State state_wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	        	State state_net = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	        	if(state_wifi!=null && state_net!=null && state_wifi !=State.CONNECTED && state_net!=State.CONNECTED){
	        		DBHandler.getInstance(NetService.this.getApplicationContext()).saveExceptionData(strWorkerNo, strDeviceMAC,
	        				strDevicePosition, strExPosName, strDeviceDatetime, strExceptionPosition, TypeNo, Type, strExcepDesc,
	        				picName, picBytes, voiceName, voiceBytes, strToken);
	        	}
	        	return;
	        }else if(object instanceof SoapObject){
	        	  // 获取返回的结果  
	        	Log.i(TAG, "check track refult is :"+  object.toString());
	        	
	        	try{
	        		SoapObject result = (SoapObject)object;
		        	PropertyInfo info = new PropertyInfo();
		        	result.getPropertyInfo(0, info);
		        	String str = result.getProperty(0).toString();
		        	Log.i(TAG, str);
		        	if(postcallback!=null){
		        		postcallback.postExceptionResult(str);
		        	}
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        }
	 }
}
