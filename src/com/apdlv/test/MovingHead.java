package com.apdlv.test;

import static java.awt.Color.BLUE;
import static java.awt.Color.GRAY;
import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class MovingHead extends JPanel implements DmxEffectInterface, ActionListener {
	
	private int dmxAddr;
	private MySlider[] sliders;
	private XYView position;
	private String effect;
	
	@Override
	public Insets getInsets() {
		return new Insets(8, 20, 8, 20);
	}
	
	public MovingHead(int dmxAddr, String ... channels) {
		this.dmxAddr = dmxAddr;
		this.sliders = new MySlider[channels.length];
		
		JPanel anims = new JPanel();
		anims.setLayout(new GridLayout(6, 1));
//		anims.add(newButton("Bounce"));
		anims.add(newButton("Wheels"));
		anims.add(newButton("Waves"));
//		anims.add(newButton("Zigzag"));
		anims.add(newButton("Steady"));
		anims.add(newButton("Off"));
		add(anims);
		
		this.position = new XYView(65535, 65535);
		add(position);
		
		for (int i=0; i<sliders.length; i++) {
			
			int max = 255;
			String name = channels[i];
			switch (name) {
			case "Vert":
				continue;
//				max = 65535;
//				sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
//				break;
			case "Horz":
				continue;
//				max = 65535;
//				sliders[i] = new MySlider(name, HORIZONTAL, 0, max, 0);
//				break;
			case "Red":
				sliders[i] = new ColorSlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(Color.RED.darker());
				break;
			case "Green":
				sliders[i] = new ColorSlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(Color.GREEN.darker());
				break;
			case "Blue":
				sliders[i] = new ColorSlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(BLUE.darker());
				break;
			case "White":
				sliders[i] = new ColorSlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(GRAY);
				break;
			default:
				sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
				break;
			}
			
			JLabel label = new JLabel("  " + channels[i] + "  ");			
			add(new LabeledPanel(label, sliders[i]));
		}
	}

	private JButton newButton(String title) {
		JButton b = new JButton(title);
		b.addActionListener(this);
		return b;
	}
	
	@Override
	public void loop(long count, DmxPacket packet) {
		if (null!=this.effect) {
			switch (this.effect) {
			case "wheels":
				XYView.animateTwoWheels(position, count);
				break;
			case "waves":
				XYView.animateInterference(position, count);
				break;
			case "off":
				if (sliders[0].getValue()>0) {
					sliders[0].setValue(0);
				}
				break;
			}
		}

		int addr = dmxAddr;
		for (int i=0; i<sliders.length; i++) {
			MySlider slider = sliders[i];
			boolean _16bit = slider.getMaximum() > 255;
			int value = sliders[i].getValue();
			byte hi = (byte) ((value >> 8) & 0xff);
			byte lo = (byte) ((value >> 0) & 0xff);
			if (_16bit) {
				packet.data[addr + 0] = hi;
				packet.data[addr + 1] = lo;
			} else {
				packet.data[dmxAddr+i] = lo;
			}			
			addr += _16bit ? 2 : 1;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o instanceof JButton) {
			JButton b = (JButton)o;
			this.effect = b.getText().toLowerCase();
		}
	}
}