package com.nfsw.service;

import com.nfsw.locationmap.MyApplication;
import com.nfsw.utils.RecycleThreadPool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadCastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		MyApplication app = (MyApplication)context.getApplicationContext();
		Log.i("BootBroadCastReceiver", "receive bootBroadCast!");
		app.startAlarm();
		RecycleThreadPool.getInstance(context.getApplicationContext()).postCMD("system_boot_completed");
	}
}
