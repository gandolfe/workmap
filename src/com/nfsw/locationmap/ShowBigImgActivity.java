package com.nfsw.locationmap;


import com.nfsw.view.ZoomImageView;
import com.yangyunsheng.locationmap.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

public class ShowBigImgActivity extends Activity{

	ZoomImageView imageview; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_big_image);
		imageview = (ZoomImageView) findViewById(R.id.img_view);
		
		Intent srcintent = getIntent();
		String path = srcintent.getStringExtra("path");
		if(path==null){
		}else{
			Bitmap bm = BitmapFactory.decodeFile(path);
			imageview.setImageBitmap(bm);
			return;
		}
		
		Bundle src = getIntent().getExtras();
		if(src.containsKey("bitmap")){
			try{
				Bitmap camera_bitmap = (Bitmap)src.get("bitmap");
				imageview.setImageBitmap(camera_bitmap);
				Log.i("ShowBigImgActivity", "byte count:"+camera_bitmap.getByteCount());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
}
