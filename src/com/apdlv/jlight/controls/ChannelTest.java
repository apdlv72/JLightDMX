package com.apdlv.jlight.controls;

import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class ChannelTest extends JPanel implements DmxControlInterface, ChangeListener {
	
	private MySlider[] valueSliders;
	private JLabel addressLabel;
	private MySlider addressSlider;
	private JLabel[] valueLabels;

	@Override
	public Insets getInsets() {
		return new Insets(17, 20, 18, 20);
	}
	
	public ChannelTest() {
		Font font = new Font("Monospaced",Font.PLAIN, 10);
		
		addressLabel  = new JLabel("Addr");
		addressLabel.setFont(font);
		addressSlider = new MySlider("-1", VERTICAL, -1, 511, -1);
		addressSlider.addChangeListener(this);
		add(new LabeledPanel(addressLabel, addressSlider));		
		
		valueLabels  = new JLabel[16];
		valueSliders = new MySlider[16];
		for (int i=0; i<16; i++) {
			valueSliders[i] = new MySlider("x", VERTICAL, -1, 255, -1);
			valueSliders[i].addChangeListener(this);
			valueLabels[i] = new JLabel(" - ");
			valueLabels[i].setFont(font);
			add(new LabeledPanel(valueLabels[i], valueSliders[i]));		
		}				
	}

	@Override
	public void loop(long count, DmxPacket packet) {
		int address = addressSlider.getValue();		
		if (address>-1) {
			for (int i=0; i<16; i++) {
				int value = valueSliders[i].getValue();
				if (value>-1) {
					packet.data[address+i] = (byte)(value & 0xff);
				}
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if (src == addressSlider) {			
			int address = addressSlider.getValue();
			String text = address<0 ? "Addr" : String.format("%4d", address); 
			addressLabel.setText(text);
		} else {		
			for (int i=0; i<16; i++) {		
				if (src == valueSliders[i]) {
					int value = valueSliders[i].getValue();
					String text = value<0 ? " - " : String.format("%3d", value); 
					valueLabels[i].setText(text);
				}
			}
		}
	}
}