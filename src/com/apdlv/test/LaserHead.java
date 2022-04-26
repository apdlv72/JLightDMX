package com.apdlv.test;

import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

@SuppressWarnings("serial")
class LaserHead extends JPanel implements DmxEffectInterface {
	
	private int dmxAddr;
	private JSlider[] sliders;
	
	@Override
	public Insets getInsets() {
		return new Insets(8, 20, 8, 20);
	}
	
	public LaserHead(int dmxAddr, String ... channels) {
		this.dmxAddr = dmxAddr;
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
			packet.data[dmxAddr+i] = (byte) (sliders[i].getValue() & 0xff);
		}
	}
}
