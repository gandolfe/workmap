package com.nfsw.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

public class ConnectBroadCastReceiver extends BroadcastReceiver{

	private static final String TAG = "ConnectBroadCastReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		
		State wifistate = null;
		State mobilestate = null;
		
		ConnectivityManager cm =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifistate = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		mobilestate = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		
		if(wifistate!=null && mobilestate!=null && wifistate!=State.CONNECTED && mobilestate==State.CONNECTED){
			//mobile network
			Log.i(TAG, "change to the mobile network!");
			RecycleThreadPool.getInstance(context.getApplicationContext()).startRecycleSend();
			RecycleThreadPool.getInstance(context.getApplicationContext()).postCMD("mobile_network");
			
		}else if (wifistate!=null && mobilestate!=null && wifistate!=State.CONNECTED && mobilestate!=State.CONNECTED){
			//no network
			Log.i(TAG, "change no network!");
			RecycleThreadPool.getInstance(context.getApplicationContext()).postCMD("no_network");
			
		}else if(wifistate!=null && wifistate==State.CONNECTED){
			//wifi network
			Log.i(TAG, "change to wifi network!");
			RecycleThreadPool.getInstance(context.getApplicationContext()).startRecycleSend();
			RecycleThreadPool.getInstance(context.getApplicationContext()).postCMD("wifi_network");
		}
	}

}
