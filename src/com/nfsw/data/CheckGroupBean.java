package com.nfsw.data;
import java.util.ArrayList;
import java.util.List;

public class CheckGroupBean {
	
	private String groupName;
	private List<CheckLineBean> lines;
	private int cycletype;
	
	
	public CheckGroupBean(String groupName, List<CheckLineBean> lines, int cycletype) {
		super();
		this.groupName = groupName;
		this.lines = lines;
		this.cycletype = cycletype;
	}
	public int getCycletype() {
		return cycletype;
	}
	public void setCycletype(int cycletype) {
		this.cycletype = cycletype;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public List<CheckLineBean> getLines() {
		return lines;
	}
	public void setLines(List<CheckLineBean> lines) {
		this.lines = lines;
	}
	public CheckGroupBean(String groupName, List<CheckLineBean> lines) {
		super();
		this.groupName = groupName;
		this.lines = lines;
	}
	public CheckGroupBean() {
		super();
	}
	
	public void addLine(CheckLineBean line){
		if(lines==null){
			lines = new ArrayList<CheckLineBean>();
		}
		lines.add(line);
	}
	
}
