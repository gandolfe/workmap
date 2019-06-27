package com.nfsw.locationmap;

import java.util.List;

import com.nfsw.data.CheckLineBean;
import com.nfsw.service.KeepLiveService;
import com.nfsw.service.SchedulReceiver;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class MyApplication extends Application {
	
	
	public String workerNo;
	public String pwd;
	public String token;
	public String name;
	public CheckLineBean data; 

	@Override
	public void onCreate() {
		super.onCreate();
		startAlarm();
	}
	
	
	@SuppressLint("NewApi")
	public void startAlarm(){
		
		Log.i("MyApplication", "adk version:"+android.os.Build.VERSION.SDK_INT+"");
		
		if(android.os.Build.VERSION.SDK_INT<=20){
			
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			Intent myintent = new Intent(this, SchedulReceiver.class);
			myintent.setAction("com.nfsw.action.ACTION_LOCATION_ALARM");
			PendingIntent pendSender = PendingIntent.getBroadcast(this, 0, myintent, 0);
			am.cancel(pendSender);
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime(), pendSender);
			Log.i("SchedulReceiver", "MainActivity start Alarm!" );
			
		}else{
			JobScheduler mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
			
			JobInfo.Builder builder = new JobInfo.Builder( 1,
	                 new ComponentName( getPackageName(), KeepLiveService.class.getName() ) );

	         
	         builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
	         builder.setRequiresDeviceIdle(false);
	         builder.setRequiresCharging(false);
	         builder.setMinimumLatency(10000);
	         builder.setOverrideDeadline(20000); 


	         if( mJobScheduler.schedule( builder.build() ) <= 0 ) {
	             //If something goes wrong
	        	 Log.i("KeepLiveService", "JobScheduler is error!");
	         }else {
	        	 Log.i("KeepLiveService", "JobScheduler go to run!");
	         }
		}
		
		
//		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//		Intent myintent = new Intent(this, SchedulReceiver.class);
//		myintent.setAction("com.nfsw.action.ACTION_LOCATION_ALARM");
//		PendingIntent pendSender = PendingIntent.getBroadcast(this, 0, myintent, PendingIntent.FLAG_UPDATE_CURRENT);
//		am.cancel(pendSender);
//		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime(), pendSender);
//		Log.i("SchedulReceiver", "MyApplication start Alarm!" );
	}
	
}
