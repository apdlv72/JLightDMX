package com.apdlv.jlight.controls;

import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class LaserHead extends JPanel implements DmxControlInterface {
	
	private int dmxAddr;
	private JSlider[] sliders;
	private String[] channels;
	
	@Override
	public Insets getInsets() {
		return new Insets(8, 20, 8, 20);
	}
	
	public LaserHead(int dmxAddr, String ... channels) {
		this.dmxAddr = dmxAddr;
		this.channels = channels;
		this.sliders = new JSlider[channels.length];
		
		for (int i=0; i<sliders.length; i++) {
			String name = channels[i];
			sliders[i] = new MySlider(name, VERTICAL, 0, 255, 0);
			JLabel label = new JLabel("  " + name + "  ");			
			add(new LabeledPanel(label, sliders[i]));
		}
	}
	
	@Override
	public void loop(long count, DmxPacket packet) {
		for (int i=0; i<sliders.length-2; i++) {
			int v = sliders[i].getValue();
			if (i==4) v=255-v;
			byte value = (byte) (v & 0xff);
			packet.data[dmxAddr+i] = value;
		}
	}
}
