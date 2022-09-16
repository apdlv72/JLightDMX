package com.apdlv.jlight.controls;

import java.awt.Insets;

import javax.swing.event.ChangeListener;

import com.apdlv.jlight.dmx.DmxPacket;

/*
 * 2ch: (bad) effects + speed
 * 3ch: r, g, b
 * 5ch: r, g, b, master, strobe
 * 12ch: rgb1, rgb2, rgb3, rgb4 (4 segments)
 */

@SuppressWarnings("serial")
public class LightBar extends RGBWSpotArray implements DmxControlInterface, ChangeListener {
		
	final MODE mode = RGBWSpotArray.MODE.RGB;
	
	@Override
	public Insets getInsets() {
		return new Insets(13, 30, 14, 30);
	}
	
	public LightBar(int dmxAddr, int strobeAddr, RGBWSpotArray linked) {
		// 8 -> have two adjacent light bars with addr and addr+12
		super(dmxAddr, strobeAddr, 8, RGBWSpotArray.MODE.RGB, linked, false);
		enableStrobe = false;
		//controls.all.setSelected(true);		
	}
	
	@Override
	public void loop(long count, DmxPacket packet) {
		
		// TODO support 5ch mode (might be interesting for hardware strobing)
		if (RGBWSpotArray.MODE.RGB==mode) {
			super.loop(count, packet);
			return;
		}
		
		int a = dmxAddr;
		for (int i=0; i<sliders.length; i++) {
			int rgb = sliders[i].getWRGB();
			
			byte r = (byte) ((rgb >> 16) & 0xff);
			byte g = (byte) ((rgb >>  8) & 0xff);
			byte b = (byte) ((rgb >>  0) & 0xff);
			
			packet.data[a+0] = r;
			packet.data[a+1] = g;
			packet.data[a+2] = b;
			a+=3;
		}
	}

	public void setLink(boolean b) {
		link.setSelected(b);
	}	
}
