package com.nfsw.interfaces;

import net.minidev.json.JSONObject;

public interface LoginCallBack {
	/**
	 * 登录成功
	 * @param uid
	 */
	public void loginSuccess(String strdata);
	
	
	/**
	 * 登录失败
	 */
	public void loginFailed(String detail);
}
