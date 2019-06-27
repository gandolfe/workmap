package com.nfsw.data;

public class CheckTrackPointBean{

	private String CheckPointNo;
	private String WorkerNo;
	private String CheckPosition;
	private String DevicePosition;

	public CheckTrackPointBean() {
		super();
	}
	public CheckTrackPointBean(String checkPointNo, String workerNo, String checkPosition, String devicePosition) {
		super();
		CheckPointNo = checkPointNo;
		WorkerNo = workerNo;
		CheckPosition = checkPosition;
		DevicePosition = devicePosition;
	}
	public String getCheckPointNo() {
		return CheckPointNo;
	}
	public void setCheckPointNo(String checkPointNo) {
		CheckPointNo = checkPointNo;
	}
	public String getWorkerNo() {
		return WorkerNo;
	}
	public void setWorkerNo(String workerNo) {
		WorkerNo = workerNo;
	}
	public String getCheckPosition() {
		return CheckPosition;
	}
	public void setCheckPosition(String checkPosition) {
		CheckPosition = checkPosition;
	}
	public String getDevicePosition() {
		return DevicePosition;
	}
	public void setDevicePosition(String devicePosition) {
		DevicePosition = devicePosition;
	}
	
	
}
