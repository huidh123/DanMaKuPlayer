package com.example.javaBean;

public class NetMessage {
	private int code;
	private String error;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public NetMessage(int code, String error) {
		super();
		this.code = code;
		this.error = error;
	}
}
