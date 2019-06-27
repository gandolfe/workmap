package com.nfsw.data;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class JSONHelper {
	private static final String TAG = "JSONHelper";
	public static void CheckplanResolve(String jsonstr,Handler handler, List<CheckGroupBean> outdata,CheckLineBean outlins){
		try{
			Log.i(TAG,"jsonstr is:"+ jsonstr);
			JSONObject jsonobj =(JSONObject)JSONValue.parse(jsonstr);
        	String code = (String)jsonobj.get("code");
        	
        	if(code.equals("101")){
        		handler.sendEmptyMessage(0x2);
        		return;
        	}
        	
        	if(!code.equals("0")){
        		
        		Message mg = new Message();
        		Bundle data = new Bundle();
        		data.putString("detail", "error!");
        		mg.setData(data);
        		mg.what= 0x1;
        		handler.sendMessage(mg);
        		return;
        	}
        	
        	
        	
        	JSONArray array_data = (JSONArray)jsonobj.get("data");
        	for(int i=0;i<array_data.size();i++){
        		
        		JSONObject groupjson = (JSONObject) array_data.get(i);
        		String groupname = (String)groupjson.get("name");
        		JSONArray lines = (JSONArray)groupjson.get("Line");
        		CheckGroupBean grouptemp = new CheckGroupBean();
        		grouptemp.setGroupName(groupname);
        		
        		for(int j=0;j<lines.size();j++){
        			
        			JSONObject linejson = (JSONObject) lines.get(j);
	        		String linename = (String)linejson.get("name");
	        		JSONArray points = (JSONArray)linejson.get("CheckPoint");
	        		CheckLineBean templine = new CheckLineBean();
	        		templine.setLinename(linename);
	        		templine.setGroupname(groupname);
	        		outlins.setGroupname(groupname);
	        		outlins.setLinename(linename);
	        		
	        		for(int k=0;k<points.size();k++){
	        			
	        			JSONObject point = (JSONObject) points.get(k);
		        		String CheckPointNo = (String)point.get("CheckPointNo");
		        		String CheckPointName = (String)point.get("CheckPointName");
		        		String CheckPointLng = (String)point.get("CheckPointLng");
		        		String CheckPointLat = (String)point.get("CheckPointLat");
		        		String CheckPointAsl = (String)point.get("CheckPointAsl");
		        		
		        		CheckPointBean tempbean = 
		        				new CheckPointBean(CheckPointNo, CheckPointName, CheckPointLng, CheckPointLat, CheckPointAsl);

		        		if(point.containsKey("SignDate")){
		        			tempbean.setCheckFlag(1);
		        		};
		        		
		        		templine.addPoint(tempbean);
		        		outlins.addPoint(tempbean);
	        		}
	        		grouptemp.addLine(templine);
	        		
        		}
        		outdata.add(grouptemp);
        	}
        	
        	
        	handler.sendEmptyMessage(0x3);
        	
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public static void checkTrackResolve(String jsonstr,Handler handler,List<CheckTrackPointBean> outdata){
		try{
			Log.i(TAG,"checkTrackResolve jsonstr is:"+ jsonstr);
			JSONObject jsonobj =(JSONObject)JSONValue.parse(jsonstr);
        	String code = (String)jsonobj.get("code");
        	
        	if(code.equals("101")){
        		handler.sendEmptyMessage(0x2);
        		return;
        	}
        	
        	if(!code.equals("0")){
        		
        		Message mg = new Message();
        		Bundle data = new Bundle();
        		data.putString("detail", (String)jsonobj.get("data"));
        		mg.setData(data);
        		mg.what= 0x1;
        		handler.sendMessage(mg);
        		return;
        	}
        	
        	JSONArray array_data = (JSONArray)jsonobj.get("data");
        	for(int i=0;i<array_data.size();i++){
        		
        		JSONObject checkpointjson = (JSONObject) array_data.get(i);
        		String CheckPointNo = (String)checkpointjson.get("CheckPointNo");
        		String WorkerNo = (String)checkpointjson.get("WorkerNo");
        		String CheckPosition = (String)checkpointjson.get("CheckPosition");
        		String DevicePosition = (String)checkpointjson.get("DevicePosition");
        		CheckTrackPointBean tempbean = new CheckTrackPointBean(CheckPointNo, WorkerNo, CheckPosition, DevicePosition);
        		outdata.add(tempbean);
        	}
        	
        	handler.sendEmptyMessage(0x3);
        	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
