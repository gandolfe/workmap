package com.nfsw.service;

import com.nfsw.utils.RecycleThreadPool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShutDownBroadCastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("SchedulReceiver", "ShutDownBroadCastReceiver receiver!");
		RecycleThreadPool.getInstance(context.getApplicationContext()).postCMD("system_shutdown");
	}

}
