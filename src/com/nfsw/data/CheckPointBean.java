package com.nfsw.data;

public class CheckPointBean {
	private String CheckPointNo;
	private String CheckPointName ="";
	private String CheckPointLng;
	private String CheckPointLat;
	private String CheckPointAsl;
	private int checkFlag = 0;  //0 not checked ; 1 checked
	
	
	public CheckPointBean() {
		super();
	}
	public CheckPointBean(String checkPointNo, String checkPointName, String checkPointLng, String checkPointLat,
			String checkPointAsl) {
		super();
		CheckPointNo = checkPointNo;
		CheckPointName = checkPointName;
		CheckPointLng = checkPointLng;
		CheckPointLat = checkPointLat;
		CheckPointAsl = checkPointAsl;
	}
	
	public CheckPointBean(String checkPointNo, String checkPointName, String checkPointLng, String checkPointLat,int checkflag) {
		super();
		CheckPointNo = checkPointNo;
		CheckPointName = checkPointName;
		CheckPointLng = checkPointLng;
		CheckPointLat = checkPointLat;
		checkFlag = checkflag;
	}
	
	public int getCheckFlag() {
		return checkFlag;
	}
	public void setCheckFlag(int checkFlag) {
		this.checkFlag = checkFlag;
	}
	public String getCheckPointNo() {
		return CheckPointNo;
	}
	public void setCheckPointNo(String checkPointNo) {
		CheckPointNo = checkPointNo;
	}
	public String getCheckPointName() {
		return CheckPointName;
	}
	public void setCheckPointName(String checkPointName) {
		CheckPointName = checkPointName;
	}
	public String getCheckPointLng() {
		return CheckPointLng;
	}
	public void setCheckPointLng(String checkPointLng) {
		CheckPointLng = checkPointLng;
	}
	public String getCheckPointLat() {
		return CheckPointLat;
	}
	public void setCheckPointLat(String checkPointLat) {
		CheckPointLat = checkPointLat;
	}
	public String getCheckPointAsl() {
		return CheckPointAsl;
	}
	public void setCheckPointAsl(String checkPointAsl) {
		CheckPointAsl = checkPointAsl;
	}
	
	
}
