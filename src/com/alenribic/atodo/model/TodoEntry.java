package com.alenribic.atodo.model;

public class TodoEntry {
	
	private String subject;
	private String action;
	private String[] labels;
	private String[] users;
	private String priority;
	private Float timeSpent;
	private MetaInfo srcInfo;
	
	public TodoEntry() {}
	
	public TodoEntry(String subject, String action, String[] labels,
			String[] users, String priority, Float timeSpent,
			MetaInfo srcInfo) {
		super();
		this.subject = subject;
		this.action = action;
		this.labels = labels;
		this.users = users;
		this.priority = priority;
		this.timeSpent = timeSpent;
		this.srcInfo = srcInfo;
	}
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String[] getLabels() {
		return labels;
	}
	public void setLabels(String[] labels) {
		this.labels = labels;
	}
	public String[] getUsers() {
		return users;
	}
	public void setUsers(String[] users) {
		this.users = users;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public Float getTimeSpent() {
		return timeSpent;
	}
	public void setTimeSpent(Float timeSpent) {
		this.timeSpent = timeSpent;
	}
	public MetaInfo getSrcInfo() {
		return srcInfo;
	}
	public void setSrcInfo(MetaInfo srcInfo) {
		this.srcInfo = srcInfo;
	}
}
