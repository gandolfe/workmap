package com.nfsw.view;

import com.yangyunsheng.locationmap.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class PostImg_CustomDialog extends Dialog{

	private Context context;
	private TextView camecattext;
	private TextView photostext;
	private View.OnClickListener cameralistener;
	private View.OnClickListener photoslistener;
	public PostImg_CustomDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		initview();
	}

	public PostImg_CustomDialog(Context context) {
		super(context);
		this.context = context;
		initview();
	}
	
	public void initview(){
		
		setContentView(R.layout.postimg_dialog);
		camecattext = (TextView)findViewById(R.id.camera);
		photostext = (TextView)findViewById(R.id.photos);
		
	}
	
	public void setCameralistener(View.OnClickListener listener){
		cameralistener = listener;
		camecattext.setOnClickListener(cameralistener);
	}
	
	public void setPhotoslistener(View.OnClickListener listener){
		photoslistener = listener;
		photostext.setOnClickListener(photoslistener);
	}
	
}
