package com.nfsw.data;

import android.content.Context;
import android.content.SharedPreferences;

public class UserUtils {

	private static final String USER = "userinfo";
	private static final String ISLOGIN = "login";
	private Context ct;
	private static UserUtils utils = null;
	private SharedPreferences shares;
	
	private UserUtils(){
	}
	public static UserUtils getInstance(Context context){
		if(utils==null){
			utils = new UserUtils();
			utils.ct = context;
			utils.shares = utils.ct.getSharedPreferences(USER, Context.MODE_PRIVATE);
		}
		return utils;
	}
	
	/**
	 * get whether this app has logined
	 * @return
	 */
	public boolean isLogined(){
		boolean islogin =  shares.getBoolean(ISLOGIN, false);
		return islogin;
	}
	public void setLogin(boolean islogin){
		shares.edit().putBoolean(ISLOGIN, islogin).commit();
	}
	
}
