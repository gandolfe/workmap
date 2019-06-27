package com.nfsw.locationmap;

import java.util.List;

import com.nfsw.service.KeepLiveService;
import com.nfsw.service.NetService;
import com.nfsw.service.SchedulReceiver;
import com.nfsw.service.NetService.MyBinder;
import com.yangyunsheng.locationmap.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener{

	
	NetService.MyBinder myBinder;
	private FrameLayout frame;
	private TextView title_name;
	private TextView title_data1;
	private TextView title_data2;
	private RelativeLayout root_layout;
	private LinearLayout line;
	private RelativeLayout myactionbar;
	private LinearLayout tabs;
	private int screenhight;
	int root_h;
	int actionbar_h;
	int tabs_h;
	int line_h;
	
	private SignFragment frag_sign;
	private PostFragment frag_post;
	private BrowseFragment frag_browse;
	private SettingsFragment frag_settings;
	
	private RelativeLayout sign_layout;
	private RelativeLayout post_layout;
	private RelativeLayout browse_layout;
	private RelativeLayout settings_layout;
	
	private ImageView sign_img;
	private ImageView post_img;
	private ImageView browse_img;
	private ImageView settings_img;
	
	private TextView sign_text;
	private TextView post_text;
	private TextView browse_text;
	private TextView settings_text;
	
	int index = SIGN; // selected page index
	
	private static final String TAG = "MainActivity";
	private static final int SIGN = 0;
	private static final int POST = 1;
	private static final int BROWSE = 2;
	private static final int SETTINGS = 3;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		ninit();
		
		Log.i(TAG, "thread id:"+Thread.currentThread().getId());
		Intent intent = new Intent(this, com.nfsw.service.NetService.class);
		this.bindService(intent, conn, Context.BIND_AUTO_CREATE);
		
		initOnLayoutListener();
		
		
		Log.i(TAG, "adk version:"+android.os.Build.VERSION.SDK_INT+"");
		
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
			List<JobInfo> jobs = mJobScheduler.getAllPendingJobs();
			if(jobs.size()>=1){
				mJobScheduler.cancelAll();	
			}
			 
			
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
		
		
		
		
		
		
	}
	
	ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "bind thread id:"+Thread.currentThread().getId());
			
			myBinder = (MyBinder)service;
			if(frag_sign!=null){
				frag_sign.setBinder(myBinder);
			}
		}
	};
	
	
	private void initOnLayoutListener() {
        final ViewTreeObserver viewTreeObserver = this.getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
            	root_h = root_layout.getMeasuredHeight();
            	actionbar_h = myactionbar.getMeasuredHeight();
            	tabs_h = tabs.getMeasuredHeight();
            	line_h = line.getMeasuredHeight();
            	
            	int height0 = root_h - actionbar_h - tabs_h - line_h;
            	
            	android.widget.RelativeLayout.LayoutParams param_map0 = 
    					new android.widget.RelativeLayout.LayoutParams
    					(android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, height0);
    			param_map0.addRule(RelativeLayout.BELOW, R.id.actionbar_layout);
    			frame.setLayoutParams(param_map0);
            	
            	Log.i(TAG, "root_h:"+root_h);
            	Log.i(TAG, "line_h:"+line_h);
            	
                selectIndex(SIGN);
           // 移除GlobalLayoutListener监听     
              MainActivity.this.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }
	
	
	private void ninit(){
		root_layout = (RelativeLayout)findViewById(R.id.root_layout);
		frame = (FrameLayout)findViewById(R.id.content_fragment);
		title_name = (TextView)findViewById(R.id.title_name);
		title_data1 = (TextView)findViewById(R.id.title_data1);
		title_data2 = (TextView)findViewById(R.id.title_data2);
		
		line =(LinearLayout)findViewById(R.id.line);
		myactionbar = (RelativeLayout)findViewById(R.id.actionbar_layout);
		tabs = (LinearLayout)findViewById(R.id.tabs);
		
		sign_layout =(RelativeLayout)findViewById(R.id.sign_layout);
		post_layout =(RelativeLayout)findViewById(R.id.post_layout);
		browse_layout =(RelativeLayout)findViewById(R.id.browse_layout);
		settings_layout =(RelativeLayout)findViewById(R.id.settings_layout);
		
		sign_img =(ImageView)findViewById(R.id.sign_img);
		post_img =(ImageView)findViewById(R.id.post_img);
		browse_img =(ImageView)findViewById(R.id.browse_img);
		settings_img =(ImageView)findViewById(R.id.settings_img);
		
		sign_text = (TextView)findViewById(R.id.sign_text);
		post_text = (TextView)findViewById(R.id.post_text);
		browse_text = (TextView)findViewById(R.id.browse_text);
		settings_text = (TextView)findViewById(R.id.settings_text);
		
		
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenhight = metric.heightPixels;
		int onetabw = metric.widthPixels/4;
		Log.i(TAG, "height is:"+screenhight);
		
		android.widget.LinearLayout.LayoutParams lay_param1 = (android.widget.LinearLayout.LayoutParams) sign_layout.getLayoutParams();
		android.widget.LinearLayout.LayoutParams lay_param2 = (android.widget.LinearLayout.LayoutParams) post_layout.getLayoutParams();
		android.widget.LinearLayout.LayoutParams lay_param3 = (android.widget.LinearLayout.LayoutParams) browse_layout.getLayoutParams();
		android.widget.LinearLayout.LayoutParams lay_param4 = (android.widget.LinearLayout.LayoutParams) settings_layout.getLayoutParams();
		
		lay_param1.width = onetabw;
		lay_param2.width = onetabw;
		lay_param3.width = onetabw;
		lay_param4.width = onetabw;
		
		sign_layout.setLayoutParams(lay_param1);
		post_layout.setLayoutParams(lay_param2);
		browse_layout.setLayoutParams(lay_param3);
		settings_layout.setLayoutParams(lay_param4);
		
		tabs.invalidate();
		sign_layout.setOnClickListener(this);
		post_layout.setOnClickListener(this);
		browse_layout.setOnClickListener(this);
		settings_layout.setOnClickListener(this);
		
	}
	
	
	public SignFragment getFrag_sign() {
		return frag_sign;
	}


	public void setFrag_sign(SignFragment frag_sign) {
		this.frag_sign = frag_sign;
	}


	public PostFragment getFrag_post() {
		return frag_post;
	}


	public void setFrag_post(PostFragment frag_post) {
		this.frag_post = frag_post;
	}


	public BrowseFragment getFrag_browse() {
		return frag_browse;
	}


	public void setFrag_browse(BrowseFragment frag_browse) {
		this.frag_browse = frag_browse;
	}


	public SettingsFragment getFrag_settings() {
		return frag_settings;
	}


	public void setFrag_settings(SettingsFragment frag_settings) {
		this.frag_settings = frag_settings;
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.sign_layout:
			selectIndex(SIGN);
			break;
		case R.id.post_layout:
			selectIndex(POST);
			break;
		case R.id.browse_layout:
			selectIndex(BROWSE);
			break;
		case R.id.settings_layout:
			selectIndex(SETTINGS);
			break;
		}
	}
	
	
	private void selectIndex(int index){
		
		
		FragmentManager fm = getFragmentManager();  
        // 开启Fragment事务  
        FragmentTransaction transaction = fm.beginTransaction();
		
		switch(index){
		case SIGN:
			setdefaultColor();
			setdefaultImg();
			title_name.setText(R.string.sign);
			title_data1.setVisibility(View.INVISIBLE);
			title_data2.setVisibility(View.INVISIBLE);
			sign_img.setImageResource(R.drawable.sign_in_select);
			sign_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			myactionbar.setVisibility(View.VISIBLE);
			
			if(frag_sign==null){
				frag_sign = new SignFragment(this,myBinder);
			}
	        transaction.replace(R.id.content_fragment, frag_sign);  
	        transaction.commit();  
			break;
		case POST:
			setdefaultColor();
			setdefaultImg();
			title_name.setText(R.string.post);
			title_data1.setVisibility(View.INVISIBLE);
			title_data2.setVisibility(View.INVISIBLE);
			post_img.setImageResource(R.drawable.post_select);
			post_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			int height_map1 = root_h-actionbar_h-tabs_h-line_h;
			myactionbar.setVisibility(View.VISIBLE);
			
			android.widget.RelativeLayout.LayoutParams param_map1 = 
					new android.widget.RelativeLayout.LayoutParams
					(android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, height_map1);
			param_map1.addRule(RelativeLayout.BELOW, R.id.actionbar_layout);
			
			if(frag_post==null){
				frag_post = new PostFragment(this,myBinder);
			}
		  
	        transaction.replace(R.id.content_fragment, frag_post);  
	        transaction.commit(); 
			
			break;
		case BROWSE:
			setdefaultColor();
			setdefaultImg();
			title_name.setText(R.string.browse);
			title_data1.setVisibility(View.INVISIBLE);
			title_data2.setVisibility(View.VISIBLE);
			browse_img.setImageResource(R.drawable.browse_select);
			browse_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			int height_map2 = root_h-actionbar_h-tabs_h-line_h;
			myactionbar.setVisibility(View.VISIBLE);
			
			android.widget.RelativeLayout.LayoutParams param_map2 = 
					new android.widget.RelativeLayout.LayoutParams
					(android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, height_map2);
			param_map2.addRule(RelativeLayout.BELOW, R.id.actionbar_layout);
			
			if(frag_browse==null){
				frag_browse = new BrowseFragment(this,myBinder);
				frag_browse.setActionText(title_data2);
			}
			
			transaction.replace(R.id.content_fragment, frag_browse);  
		    transaction.commit(); 
			
			break;
		
		case SETTINGS:
			setdefaultColor();
			setdefaultImg();
			title_name.setText(R.string.settings);
			title_data1.setVisibility(View.INVISIBLE);
			title_data2.setVisibility(View.INVISIBLE);
			settings_img.setImageResource(R.drawable.settings_select);
			settings_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			myactionbar.setVisibility(View.VISIBLE);
			
			int height_setting = root_h-actionbar_h-tabs_h-line_h;
			
			android.widget.RelativeLayout.LayoutParams param_setting = 
					new android.widget.RelativeLayout.LayoutParams
					(android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, height_setting);
			param_setting.addRule(RelativeLayout.BELOW, R.id.actionbar_layout);
			
			if(frag_settings==null){
				frag_settings = new SettingsFragment(this,myBinder);
			}
			transaction.replace(R.id.content_fragment, frag_settings);  
		    transaction.commit(); 
			break;
		}
	}
	
	private void setdefaultColor(){
		sign_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));
		post_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));
		browse_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));
		settings_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));
	}
	private void setdefaultImg(){
		sign_img.setImageResource(R.drawable.sign_in_normal);
		post_img.setImageResource(R.drawable.post_normal);
		browse_img.setImageResource(R.drawable.browse_normal);
		settings_img.setImageResource(R.drawable.settings_normal);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		
	}
}
