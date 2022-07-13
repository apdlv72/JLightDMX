package com.apdlv.jlight.dmx;

public class DmxPacket {
	public byte[] data  = new byte[512];
	private boolean beat;
	private long loopCount;	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<200; i++) {
			byte b = data[i];
			int n = (int)(b & 0xff);
			sb.append(n).append(" ");
		}
		return sb.toString();
	}

	public void setBeat(boolean b) {
		beat = b;
	}

	public boolean isBeat() {
		return beat;
	}

	public void setLoopCount(long loopCount) {
		this.loopCount = loopCount;
	}

	public long getLoopCount() {
		return loopCount;
	}	
}