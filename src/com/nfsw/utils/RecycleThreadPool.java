package com.nfsw.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class RecycleThreadPool{

	private static final String TAG = "RecycleThread";

	private static RecycleThreadPool instance;
	
	public Context ct;
	/**
	 * 线程池
	 */
	private ExecutorService mThreadPool;
	
	public static RecycleThreadPool getInstance(Context ct){
		if(instance==null){
			synchronized(RecycleThreadPool.class){
				if(instance==null){
					
					instance = new RecycleThreadPool(ct);
				}
			}
		}
		return instance;
	}
	
	private RecycleThreadPool(Context ct){
		this.ct = ct;
		init();
	}
	
	private void init(){
		mThreadPool = Executors.newSingleThreadExecutor();
	}
	
	public void startRecycleSend(){
		mThreadPool.execute(r);
	}
	
	Runnable r = new Runnable() {
		
		@Override
		public void run() {
			Log.i(TAG, "start recycle send！");
			
			DBHandler.getInstance(ct).querySigned();
			DBHandler.getInstance(ct).queryException();
			DBHandler.getInstance(ct).queryCMD();
		}
	};
	
	
	String strStatusDesc;
	
	public void postCMD(String strStatusDesc){
		this.strStatusDesc = strStatusDesc;
		mThreadPool.execute(rr);
	}
	
	Runnable rr = new Runnable() {
		
		@Override
		public void run() {
			Log.i(TAG, "send CMD！");
			
			String strDeviceMAC = getMac();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			Date date = new Date();
			calendar.setTime(date);
			String strDeviceDatetime = sdf.format(date);
			
			UploadDeviceStatus(strDeviceMAC,strStatusDesc,strDeviceDatetime);
		}
	};
	
	/**
	  * 上报状态
	  */
	 private void UploadDeviceStatus(String strDeviceMAC,  String strStatusDesc,String strDeviceDatetime){
		    // 命名空间  
	        String nameSpace = "http://nfswit.cc/"; 
	        
	        // 调用的方法名称  
	        String methodName = "UploadDeviceStatus";  
	        
	        // EndPoint  
	        String endPoint = "http://182.247.238.98:82/WebService1.asmx";  
	       
	        // SOAP Action  
	        String soapAction = "http://nfswit.cc/UploadDeviceStatus";
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        //指定参数
	        rpc.addProperty("strDeviceMAC", strDeviceMAC);
	        rpc.addProperty("strStatusDesc", strStatusDesc);
	        rpc.addProperty("strDeviceDatetime", strDeviceDatetime);
	        
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
	        	ConnectivityManager cm = (ConnectivityManager)ct.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	        	State state_wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	        	State state_net = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	        	if(state_wifi!=null && state_net!=null && state_wifi !=State.CONNECTED && state_net!=State.CONNECTED){
	        		DBHandler.getInstance(ct).saveCMDData(strDeviceMAC, strStatusDesc, strDeviceDatetime);
	        	}
	        	return;
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
				 try {  
		                return loadFileAsString("/sys/class/net/eth0/address")  
		                        .toUpperCase().substring(0, 17);  
		            } catch (Exception e) {  
		                e.printStackTrace();  
		                  
		            }  
			}
			
			
			if(macSerial == null || "".equals(macSerial)){
				WifiManager wifiMgr = (WifiManager)ct.getSystemService(Context.WIFI_SERVICE);  
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
	
}
