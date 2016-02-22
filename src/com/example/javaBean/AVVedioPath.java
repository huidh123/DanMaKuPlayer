package com.example.javaBean;

public class AVVedioPath {
	private String img;
	private String cid;
	private String src;
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public AVVedioPath(String img, String cid, String src) {
		super();
		this.img = img;
		this.cid = cid;
		this.src = src;
	}
	
}
