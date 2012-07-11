package com.alenribic.atodo.model;

import java.util.Date;

public class MetaInfo {

	private String srcName;
	private Integer srcLine;
	private Integer srcColumn;
	private Date modTime;
	
	public MetaInfo() {}
	
	public MetaInfo(String srcName, Integer srcLine, 
			Integer srcColumn, Date modTime) {
		super();
		this.srcName = srcName;
		this.srcLine = srcLine;
		this.srcColumn = srcColumn;
		this.modTime = modTime;
	}
	
	public String getSrcName() {
		return srcName;
	}
	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}
	public Integer getSrcLine() {
		return srcLine;
	}
	public void setSrcLine(Integer srcLine) {
		this.srcLine = srcLine;
	}
	public Integer getSrcColumn() {
		return srcColumn;
	}
	public void setSrcColumn(Integer srcColumn) {
		this.srcColumn = srcColumn;
	}
	public Date getModTime() {
		return modTime;
	}
	public void setModTime(Date modTime) {
		this.modTime = modTime;
	}	
}
