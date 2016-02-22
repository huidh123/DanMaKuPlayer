package org.libsdl.app;

public class VedioFrameData {
	private byte[] frameData;
	private VedioFrameInfo frameInfo;

	public byte[] getFrameData() {
		return frameData;
	}

	public void setFrameData(byte[] frameData) {
		this.frameData = frameData;
	}

	public VedioFrameInfo getFrameInfo() {
		return frameInfo;
	}

	public void setFrameInfo(VedioFrameInfo frameInfo) {
		this.frameInfo = frameInfo;
	}

	public VedioFrameData(byte[] frameData, VedioFrameInfo frameInfo) {
		super();
		this.frameData = frameData;
		this.frameInfo = frameInfo;
	}
}