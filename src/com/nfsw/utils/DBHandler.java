package com.nfsw.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.nfsw.data.CheckPointBean;
import com.nfsw.locationmap.MyApplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

public class DBHandler {
	
	private SQLiteDatabase mDB = null;
	
	private static DBHandler instance;
	
	private static final String TAG = "DBHandler";
	MyApplication app;
	
	public static DBHandler getInstance(Context ct){
		if(instance==null){
			instance = new DBHandler(ct);
		}
		return instance;
	}
	
	private DBHandler(Context ct){
		app = (MyApplication)ct;
		String userID = app.workerNo;
		mDB = new DBHelper(ct, userID).getWritableDatabase();
	}
	
	
	public void saveSignedData(String strCheckPointNo,String strWorkerNo, String strCheckPointPosition,String strDevicePosition,
			 String fDistanceToCheckPoint,String strDeviceSignDatetime,String strDeviceUploadDatetime,String strToken){
		
		//check if the data is beyonged 500
		Cursor cs =null;
		try{
			String checksql = "select count(*) from ab_signed";
			cs = mDB.rawQuery(checksql,null);
			if(cs==null){
				return;
			}
			if(cs.moveToNext()){
				int count = cs.getInt(0);
				
				if(count>500){
					mDB.execSQL("delete from ab_signed;",null );
				}
			}
		
		}catch(Exception e){
			
		}finally{
			if(cs!=null){
				try{
					cs.close();
				}catch(Exception e){
					
				}
			}
		}
		
		
		
		
		String insertsql = "insert into `ab_signed` values (?,?,?,?,?,?,?);";
		String args[] = {strCheckPointNo,strWorkerNo,strCheckPointPosition,strDevicePosition,fDistanceToCheckPoint,
				strDeviceSignDatetime,strToken};
		mDB.execSQL(insertsql,args );
		
	}
	
	
	public void saveExceptionData(String strWorkerNo,String strDeviceMAC, String strDevicePosition,String strExPosName,
            String strDeviceDatetime,String strExceptionPosition, String TypeNo, String Type, 
            String strExcepDesc, String picName,String picBytes, String voiceName, String voiceBytes,String strToken){
		
		String insertsql = "insert into `ab_exception` values (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		String args[] = {strWorkerNo,strDeviceMAC,strDevicePosition,strExPosName,strDeviceDatetime,strExceptionPosition,
				TypeNo,Type,strExcepDesc,picName,picBytes,voiceName,voiceBytes,strToken};
		mDB.execSQL(insertsql,args );
		
	}
	
	public void saveCMDData(String strDeviceMAC, String strStatusDesc,String strDeviceDatetime){
		
		String insertsql = "insert into `ab_cmd` values (?,?,?);";
		String args[] = {strDeviceMAC,strStatusDesc,strDeviceDatetime};
		mDB.execSQL(insertsql,args );
		
	}
	
	public void savePlanData(String CheckPointNo,String CheckPointName,String CheckPointLng,String CheckPointLat,String checkFlag){
		String insertsql = "insert into `ab_plandata` values (?,?,?,?,?);";
		String args[] = {CheckPointNo,CheckPointName,CheckPointLng,CheckPointLat,checkFlag};
		mDB.execSQL(insertsql,args );
		Log.i(TAG, "CheckPointNo :"+CheckPointNo);
	}
	
	public void deletOldData(){
		String deletsql = "delete from `ab_plandata`";
		mDB.execSQL(deletsql);
	}
	
	
	
	public void querySigned(){
		String sqlstr = "select * from ab_signed;";
		Cursor cursor = null;
		
		try{
			
			cursor = mDB.rawQuery(sqlstr,null);
			if(cursor==null){
				return;
			}
			
			while(cursor.moveToNext()){
				String strCheckPointNo = cursor.getString(cursor.getColumnIndex("strCheckPointNo"));
				String strWorkerNo = cursor.getString(cursor.getColumnIndex("strWorkerNo"));
				String strCheckPointPosition = cursor.getString(cursor.getColumnIndex("strCheckPointPosition"));
				String strDevicePosition = cursor.getString(cursor.getColumnIndex("strDevicePosition"));
				String fDistanceToCheckPoint = cursor.getString(cursor.getColumnIndex("fDistanceToCheckPoint"));
				String strDeviceSignDatetime = cursor.getString(cursor.getColumnIndex("strDeviceSignDatetime"));
			
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar calendar = Calendar.getInstance();
				Date date = new Date();
				calendar.setTime(date);
				String strDevicepostDatetime = sdf.format(date);
				
				CheckPointSign(strCheckPointNo, strWorkerNo, strCheckPointPosition, strDevicePosition, fDistanceToCheckPoint,
						strDeviceSignDatetime,strDevicepostDatetime , app.token);
			
			}
			
			//delete all data of database
			String deletestr = "delete from ab_signed;";
			mDB.execSQL(deletestr);
		}catch(Exception e){
			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
		}
	
	}
	
	
	public void queryException(){
		
		String sqlstr = "select * from ab_exception;";
		Cursor cursor = null;
		try{
			cursor = mDB.rawQuery(sqlstr, null);
			if(cursor==null){
				return;
			}
			
			while(cursor.moveToNext()){
				
				String strWorkerNo = cursor.getString(cursor.getColumnIndex("strWorkerNo"));
				String strDeviceMAC = cursor.getString(cursor.getColumnIndex("strDeviceMAC"));
				String strDevicePosition = cursor.getString(cursor.getColumnIndex("strDevicePosition"));
				String strExPosName = cursor.getString(cursor.getColumnIndex("strExPosName"));
				String strDeviceDatetime = cursor.getString(cursor.getColumnIndex("strDeviceDatetime"));
				String strExceptionPosition = cursor.getString(cursor.getColumnIndex("strExceptionPosition"));
				String TypeNo = cursor.getString(cursor.getColumnIndex("TypeNo"));
				String Type = cursor.getString(cursor.getColumnIndex("Type"));
				String strExcepDesc = cursor.getString(cursor.getColumnIndex("strExcepDesc"));
				String picName = cursor.getString(cursor.getColumnIndex("picName"));
				String picBytes = cursor.getString(cursor.getColumnIndex("picBytes"));
				String voiceName = cursor.getString(cursor.getColumnIndex("voiceName"));
				String voiceBytes = cursor.getString(cursor.getColumnIndex("voiceBytes"));
			
				Log.i(TAG, strWorkerNo+";"+strDeviceMAC+";"+strDevicePosition+";"+strExPosName+";"+strDeviceDatetime+";"+strExcepDesc);
				Log.i(TAG, "picBytes:"+picBytes);
				Log.i(TAG, "voiceBytes:"+voiceBytes);
				
				UploadExceptionData(strWorkerNo, strDeviceMAC, strDevicePosition, strExPosName, strDeviceDatetime,
						strExceptionPosition, TypeNo, Type, strExcepDesc, picName, picBytes, voiceName, voiceBytes, app.token);
				
			}
			
			//delete all data of database
			String deletestr = "delete from ab_exception;";
			mDB.execSQL(deletestr);
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(cursor!=null){
				cursor.close();
			}
		}
		
	}
	
	public void queryCMD(){
		
		String sqlstr = "select * from ab_cmd;";
		Cursor cursor = null;
		try{
			cursor = mDB.rawQuery(sqlstr, null);
			if(cursor==null){
				return;
			}
			
			while(cursor.moveToNext()){
				
				String strDeviceMAC = cursor.getString(cursor.getColumnIndex("strDeviceMAC"));
				String strStatusDesc = cursor.getString(cursor.getColumnIndex("strStatusDesc"));
				String strDeviceDatetime = cursor.getString(cursor.getColumnIndex("strDeviceDatetime"));
			
				Log.i(TAG, "strDeviceMAC:"+strDeviceMAC+";"+strStatusDesc+":"+strStatusDesc);
				
				UploadDeviceStatus(strDeviceMAC, strStatusDesc, strDeviceDatetime);
				
			}
			
			//delete all data of database
			String deletestr = "delete from ab_cmd;";
			mDB.execSQL(deletestr);
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(cursor!=null){
				cursor.close();
			}
		}
		
	}
	
	public List<CheckPointBean> getPlandata(){
		List<CheckPointBean> linePoints = new ArrayList<CheckPointBean>();
		
		String sqlstr = "select * from ab_plandata;";
		Cursor cursor = null;
		try{
			cursor = mDB.rawQuery(sqlstr, null);
			if(cursor==null){
				return linePoints;
			}
			
			while(cursor.moveToNext()){
				
				String checkpointno = cursor.getString(cursor.getColumnIndex("checkpointno"));
				String checkpointname = cursor.getString(cursor.getColumnIndex("checkpointname"));
				String checkpointlng = cursor.getString(cursor.getColumnIndex("checkpointlng"));
				String checkpointlat = cursor.getString(cursor.getColumnIndex("checkpointlat"));
				String checkflag = cursor.getString(cursor.getColumnIndex("checkflag"));
			
				CheckPointBean tempbean = new CheckPointBean(checkpointno, checkpointname, 
						checkpointlng, checkpointlat, Integer.parseInt(checkflag));
				linePoints.add(tempbean);
				
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(cursor!=null){
				cursor.close();
			}
		}
		
		return linePoints;
	}
	
	
	
	
	
	
	/**
	  * 上报异常
	  */
	 private void UploadExceptionData(String strWorkerNo,String strDeviceMAC, String strDevicePosition,String strExPosName,
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
	  
	 }
	
	/**
	  * 签到
	  */
	 private void CheckPointSign(String strCheckPointNo,String strWorkerNo, String strCheckPointPosition,String strDevicePosition,
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
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	  
	 }
	 
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
	        
	 }



}
