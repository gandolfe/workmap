package com.nfsw.data;

import java.util.ArrayList;
import java.util.List;

public class CheckLineBean {

	private String linename;
	private String groupname;
	private List<CheckPointBean> linePoints;
	public CheckLineBean(String linename, List<CheckPointBean> linePoints) {
		super();
		this.linename = linename;
		this.linePoints = linePoints;
	}
	public CheckLineBean() {
		super();
	}
	
	public String getGroupname() {
		return groupname;
	}
	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}
	public String getLinename() {
		return linename;
	}
	public void setLinename(String linename) {
		this.linename = linename;
	}
	public List<CheckPointBean> getLinePoints() {
		return linePoints;
	}
	public void setLinePoints(List<CheckPointBean> linePoints) {
		this.linePoints = linePoints;
	}
	public void addPoint(CheckPointBean point){
		if(linePoints==null){
			linePoints = new ArrayList<CheckPointBean>();
		}
		linePoints.add(point);
	}
	
}
