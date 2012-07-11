package com.alenribic.atodo.model;

public class TodoEntryError {
	
	private String message;
	private MetaInfo srcInfo;
	
	public TodoEntryError() {}

	public TodoEntryError(String message, MetaInfo srcInfo) {
		super();
		this.message = message;
		this.srcInfo = srcInfo;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public MetaInfo getSrcInfo() {
		return srcInfo;
	}

	public void setSrcInfo(MetaInfo srcInfo) {
		this.srcInfo = srcInfo;
	}
}
