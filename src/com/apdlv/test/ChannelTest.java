package com.apdlv.test;

import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
class ChannelTest extends JPanel implements DmxEffectInterface, ChangeListener {
	
	private JSlider valueSlider;
	private JLabel addressLabel;
	private MySlider addressSlider;
	private JLabel valueLabel;
	private int oldAddress;

	@Override
	public Insets getInsets() {
		return new Insets(8, 30, 8, 30);
	}
	
	public ChannelTest() {
		
		addressLabel  = new JLabel("Address");
		addressSlider = new MySlider("-1", VERTICAL, -1, 511, -1);
		addressSlider.addChangeListener(this);
		
		valueLabel  = new JLabel("Value");
		valueSlider = new MySlider("-1", VERTICAL, -1, 255, -1);
		valueSlider.addChangeListener(this);
		
		add(new LabeledPanel(addressLabel, addressSlider));		
		add(new LabeledPanel(valueLabel, valueSlider));		
		
		oldAddress = addressSlider.getValue(); 
	}

	@Override
	public void loop(long count, DmxPacket packet) {
		int address = addressSlider.getValue();
//		if (oldAddress!=address) {
//			if (oldAddress>-1) {
//				packet.data[oldAddress] = 0;
//			}
//			oldAddress = address;
//		}
		
		int value = valueSlider.getValue();
		if (address>-1 && value>-1) {
			packet.data[address] = (byte)(value & 0xff);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if (src == addressSlider) {
			
			addressLabel.setText(String.format("  %3d  ", addressSlider.getValue()));
		}
		else if (src == valueSlider) {
			valueLabel.setText(String.format("  %3d  ", valueSlider.getValue()));
		}		
	}
}