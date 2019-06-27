package com.nfsw.locationmap;

import com.yangyunsheng.locationmap.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class WelcomeActivity extends Activity {
	boolean islogin;
	Handler handler;
	private static final String TABLE = "MYTABLE";
	private static final String ISLOGIN = "login";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String TOKEN = "token";
	private SharedPreferences shares;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		shares = getSharedPreferences(TABLE, Context.MODE_PRIVATE);
		islogin =  shares.getBoolean(ISLOGIN, false);
		MyApplication app = (MyApplication) getApplicationContext();
		
		app.workerNo = shares.getString(USERNAME, "");
		app.pwd = shares.getString(PASSWORD, "");
		app.token = shares.getString(TOKEN, "");
		if(islogin){
			loginHandler.sendEmptyMessage(0x1);
		}else{
			loginHandler.sendEmptyMessage(0x2);
		}
	}
	
	
	private Handler loginHandler = new Handler(){

		@Override
		public void dispatchMessage(Message msg) {
			switch(msg.what){
			case 0x01:
				//已经登录
				Intent intent1 = new Intent();
				intent1.putExtra("sourceflag", 1);
				intent1.setClass(WelcomeActivity.this, MainActivity.class);
				startActivity(intent1);
				WelcomeActivity.this.finish();
				break;
			case 0x02:
				//未登录
				Intent intent2 = new Intent();
				intent2.setClass(WelcomeActivity.this, LoginActivity.class);
				startActivity(intent2);
				WelcomeActivity.this.finish();
				break;
			}
		}
	};

}
