package com.apdlv.jlight.controls;

import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Checkbox;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class LaserHead extends JPanel implements DmxControlInterface, ChangeListener {
	
	private int dmxAddr;
	private JSlider[] sliders;
	@SuppressWarnings("unused")
	private String[] channels;
	private JCheckBox[] labels;
	
	@Override
	public Insets getInsets() {
		return new Insets(13, 30, 14, 30);
	}
	
	public LaserHead(int dmxAddr, String ... channels) {
		this.dmxAddr = dmxAddr;
		this.channels = channels;
		this.sliders = new JSlider[channels.length];
		this.labels = new JCheckBox[channels.length];
		
		for (int i=0; i<sliders.length; i++) {
			String name = channels[i];
			sliders[i] = new MySlider(name, VERTICAL, 0, 255, 0);
			labels[i] = new JCheckBox(name);
			add(new LabeledPanel(labels[i], sliders[i]));
		}
	}
	
	@Override
	public void loop(long count, DmxPacket packet) {
		for (int i=0; i<sliders.length; i++) {
			int v = sliders[i].getValue();			
			
			if (packet.isBeat()) {
				if (labels[i].isSelected()) {
					System.out.println("*BEAT*");
					v = 255-v;
					sliders[i].setValue(v);
				}
			}
			
			if (i==4) {
				v=255-v;
			}
			byte value = (byte) (v & 0xff);
			packet.data[dmxAddr+i] = value;
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
	}
}
