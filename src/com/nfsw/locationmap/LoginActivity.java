package com.nfsw.locationmap;



import com.nfsw.interfaces.LoginCallBack;
import com.nfsw.service.NetService;
import com.nfsw.service.NetService.MyBinder;
import com.nfsw.view.MonIndicator;
import com.nfsw.view.SmoothCheckBox;
import com.yangyunsheng.locationmap.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import net.minidev.json.JSONObject;

public class LoginActivity extends Activity implements OnClickListener{
	
	
	private static final String TABLE = "MYTABLE";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String TAG = "LoginActivity";
	private static final String ISLOGIN = "login";
	private static final String TOKEN = "token";
	
	
	private TextView company_text;
	private TextView platformtext;
	private EditText editName;
	private EditText editPWD;
	private SmoothCheckBox checkbox;
	private Button sure;
	private Button cancel;
	
	private WindowManager windowManager;
	public SharedPreferences shares; 
	private boolean isRemenber;
	private String username;
	private String password;
	public String taken;
	MyApplication application;
	
	NetService.MyBinder myBinder;
	private MonIndicator monIndicator;
	private RelativeLayout loadingLayout;
	private boolean isLoading = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_activity);
		application = (MyApplication)getApplicationContext();
		
//		Intent intent = new Intent(NETWORK_ACTION);
		Intent intent = new Intent(this, com.nfsw.service.NetService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		
		windowManager = (WindowManager) getApplication()
				.getSystemService(Context.WINDOW_SERVICE);
		shares = this.getSharedPreferences(TABLE, Context.MODE_PRIVATE);
		isRemenber = shares.getBoolean("isremenber", false);
		initView();
	}
	
	private void initView(){
		
		company_text = (TextView)findViewById(R.id.companytext);
		platformtext = (TextView)findViewById(R.id.platformtext);
		AssetManager mgr = getAssets();
		Typeface tf = Typeface.createFromAsset(mgr, "fonts/simsun.ttf");
		company_text.setTypeface(tf);
		platformtext.setTypeface(tf);
		company_text.getPaint().setFakeBoldText(true);
		platformtext.getPaint().setFakeBoldText(true);
		
		editName = (EditText)findViewById(R.id.name_edit);
		editPWD = (EditText)findViewById(R.id.pwd_edit);
		checkbox = (SmoothCheckBox)findViewById(R.id.remenber_pwd);
		checkbox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
				isRemenber = isChecked;
				Log.i(TAG, "isremenber:"+isChecked);
				shares.edit().putBoolean("isremenber", isChecked).commit();
			}
		});
		if(isRemenber){
			Log.i(TAG, "isremenber_:"+isRemenber);
			checkbox.setChecked(isRemenber);
			username = shares.getString(USERNAME, "");
			application.workerNo = username;
			password = shares.getString(PASSWORD, "");
			editName.setText(username);
			editPWD.setText(password);
		}
		sure = (Button)findViewById(R.id.login_sure);
		cancel = (Button)findViewById(R.id.login_cancel);
		sure.setOnClickListener(this);
		cancel.setOnClickListener(this);
		
	}
	
	ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myBinder = (MyBinder)service;
			myBinder.setLoginCallBack(callback);
		}
	};
	
	/**
	 * 登录操作中的服务器返回结果的回调
	 */
	LoginCallBack callback = new LoginCallBack() {
		
		@Override
		public void loginSuccess(String dataobj) {
			taken = dataobj;
			application.token = taken;
			Message msg = new Message();
			msg.what = 0x01;
			msg.obj = taken;
			loginHandler.sendMessage(msg);
		}

		@Override
		public void loginFailed(String detail) {
			Message msg = new Message();
			msg.what = 0x02;
			msg.obj = detail;
			loginHandler.sendMessage(msg);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.login_sure:
			String userid = editName.getText().toString();
			String pwdstr = editPWD.getText().toString();
			Log.i(TAG, "userid:"+userid+"; pwdstr:"+pwdstr);
			if(userid==null||pwdstr==null){
				Toast.makeText(this, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
				return;
			}
			if(userid.length()<=0||pwdstr.length()<=0){
				Toast.makeText(this, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
				return;
			}
			
			startLoading();
			
			application.workerNo = userid;
			application.pwd = pwdstr;
			//如果是记住密码
			if(isRemenber){
				shares.edit().putString(USERNAME, userid).putString(PASSWORD, pwdstr).commit();
			}
			
			myBinder.postLoginInfo(userid, pwdstr);
			break;
		case R.id.login_cancel:
			finish();
			break;
		}
	}
	
	/**
	 * 开始gif动画
	 */
	private void startLoading(){
		if(loadingLayout==null){
			LayoutInflater  inflayter = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			loadingLayout = (RelativeLayout) inflayter.inflate(R.layout.loading_view, null);
			monIndicator =(MonIndicator) loadingLayout.findViewById(R.id.monIndicator);
			monIndicator.setColors(new int[]{0xFFff1493, 0xFFff1493, 0xFFff1493, 0xFFff1493, 0xFFff1493});
		}
		if(!isLoading){
			windowManager.addView(loadingLayout, getDialogParmas());
			isLoading = true;
		}
		Message msg = new Message();
		msg.what = 0x3;
		loginHandler.sendMessageDelayed(msg,1000*10);
		Log.i(TAG, "go run set!");
	}
	
	private Handler loginHandler = new Handler(){

		@Override
		public void dispatchMessage(Message msg) {
			switch(msg.what){
			case 0x01:
				//成功
				Log.i(TAG, "login success!");
				loginHandler.removeMessages(0x3);
				if(isLoading){
					windowManager.removeView(loadingLayout);
					isLoading = false;
				}
				
				shares.edit().putBoolean(ISLOGIN, true).commit();
				shares.edit().putString(USERNAME, application.workerNo).commit();
				shares.edit().putString(PASSWORD, application.pwd).commit();
				shares.edit().putString(TOKEN, application.token).commit();
				
				Intent intent = new Intent(LoginActivity.this,MainActivity.class );
				String tokens = (String) msg.obj;
				intent.putExtra("token",tokens);
				intent.putExtra("sourceflag", 2);
				startActivity(intent);
				LoginActivity.this.finish();
				break;
			case 0x02:
				//失败
				loginHandler.removeMessages(0x3);
				if(isLoading){
					windowManager.removeView(loadingLayout);
					isLoading = false;
				}
				
				shares.edit().putBoolean(ISLOGIN, false).commit();
				
				String mess = (String) msg.obj;
				Toast.makeText(LoginActivity.this, mess, Toast.LENGTH_SHORT).show();
				break;
			case 0x03:
				if(isLoading){
					windowManager.removeView(loadingLayout);
					isLoading = false;
				}
				Toast.makeText(LoginActivity.this, "登录超时!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	private WindowManager.LayoutParams getDialogParmas() {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.format = PixelFormat.TRANSLUCENT;
		params.x = 0;
		params.y = 0;
		params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		return params;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
	}
	
}
