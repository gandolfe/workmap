package com.nfsw.data;

public class SignedBean {

	private String strCheckPointNo;
	private String strWorkerNo;
	private String strCheckPointPosition;
	private String strDevicePosition;
	private String fDistanceToCheckPoint;
	private String strDeviceSignDatetime;
	private String strToken;
	
	public SignedBean() {
		super();
	}
	public SignedBean(String strCheckPointNo, String strWorkerNo, String strCheckPointPosition,
			String strDevicePosition, String fDistanceToCheckPoint, String strDeviceSignDatetime, String strToken) {
		super();
		this.strCheckPointNo = strCheckPointNo;
		this.strWorkerNo = strWorkerNo;
		this.strCheckPointPosition = strCheckPointPosition;
		this.strDevicePosition = strDevicePosition;
		this.fDistanceToCheckPoint = fDistanceToCheckPoint;
		this.strDeviceSignDatetime = strDeviceSignDatetime;
		this.strToken = strToken;
	}
	public String getStrCheckPointNo() {
		return strCheckPointNo;
	}
	public void setStrCheckPointNo(String strCheckPointNo) {
		this.strCheckPointNo = strCheckPointNo;
	}
	public String getStrWorkerNo() {
		return strWorkerNo;
	}
	public void setStrWorkerNo(String strWorkerNo) {
		this.strWorkerNo = strWorkerNo;
	}
	public String getStrCheckPointPosition() {
		return strCheckPointPosition;
	}
	public void setStrCheckPointPosition(String strCheckPointPosition) {
		this.strCheckPointPosition = strCheckPointPosition;
	}
	public String getStrDevicePosition() {
		return strDevicePosition;
	}
	public void setStrDevicePosition(String strDevicePosition) {
		this.strDevicePosition = strDevicePosition;
	}
	public String getfDistanceToCheckPoint() {
		return fDistanceToCheckPoint;
	}
	public void setfDistanceToCheckPoint(String fDistanceToCheckPoint) {
		this.fDistanceToCheckPoint = fDistanceToCheckPoint;
	}
	public String getStrDeviceSignDatetime() {
		return strDeviceSignDatetime;
	}
	public void setStrDeviceSignDatetime(String strDeviceSignDatetime) {
		this.strDeviceSignDatetime = strDeviceSignDatetime;
	}
	public String getStrToken() {
		return strToken;
	}
	public void setStrToken(String strToken) {
		this.strToken = strToken;
	}
	
	
}
