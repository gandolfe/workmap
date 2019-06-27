package com.nfsw.locationmap;


import com.nfsw.service.NetService;
import com.nfsw.service.NetService.MyBinder;
import com.yangyunsheng.locationmap.R;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.TextView;

public class SettingsFragment extends Fragment {

	
	Button exitimg ;
	Context ct;
	MyApplication myapp;
	NetService.MyBinder myBinder;
	TextView worker_no;
	TextView worker_name;
	private static final String TABLE = "MYTABLE";
	private static final String ISLOGIN = "login";
	public SettingsFragment(Context context,NetService.MyBinder binder){
		ct = context;
		myapp = (MyApplication)ct.getApplicationContext();
		
		if(binder!=null){
			myBinder = binder;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.settings_fragment, null);
		
		worker_no = (TextView)view.findViewById(R.id.worker_no_data);
		worker_no.setText(myapp.workerNo);
		worker_name = (TextView)view.findViewById(R.id.worker_name_data);
		worker_name.setText(myapp.workerNo);
		exitimg = (Button)view.findViewById(R.id.sure_exit);
		exitimg.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsFragment.this.getActivity());
				builder.setMessage("是否退出？")
				.setPositiveButton("退出", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						myBinder.DeviceLogout(myapp.workerNo, myapp.token);
						
						SharedPreferences shares = ct.getSharedPreferences(TABLE, ct.MODE_PRIVATE);
						shares.edit().putBoolean(ISLOGIN, false).commit();
						Intent intent = new Intent(ct, LoginActivity.class);
						ct.startActivity(intent);
						
						dialog.dismiss();
						SettingsFragment.this.getActivity().finish();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
					}
				}).create().show();
				
			}
		});
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
