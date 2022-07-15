package com.apdlv.jlight.controls;

import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apdlv.jlight.components.ColorSlider;
import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.components.XYView;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class MovingHead extends JPanel implements DmxControlInterface, ActionListener {
	
	private int dmxAddr;
	private MySlider[] sliders;
	private XYView position;
	private String effect;
	
	@Override
	public Insets getInsets() {
		return new Insets(17, 20, 18, 20);
	}
	
	public MovingHead(int dmxAddr, String ... channels) {
		this.dmxAddr = dmxAddr;
		this.sliders = new MySlider[channels.length];
		
		JPanel anims = new JPanel();
		anims.setLayout(new GridLayout(6, 1));
		anims.add(newButton("Bounce"));
		anims.add(newButton("Wheels"));
		anims.add(newButton("Waves"));
//		anims.add(newButton("Zigzag"));
		anims.add(newButton("Steady"));
		anims.add(newButton("Off"));
		add(anims);
		
		this.position = new XYView(65535, 65535);
		add(position);
		
		Color c = Color.BLACK;
		for (int i=0; i<sliders.length; i++) {
			
			int max = 255;
			String name = channels[i];
			switch (name) {
			case "XPos":
				sliders[i] = new MySlider(name, VERTICAL, 0, 65535, 0);
				sliders[i].setBackground(c);
				break;
			case "YPos":
				sliders[i] = new MySlider(name, VERTICAL, 0, 65535, 0);
				sliders[i].setBackground(c);
				break;
			case "Speed":
				sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(c);
				break;
			case "Color":
				//sliders[i] = new ColorSlider(name, VERTICAL, 0, max, 0);
				sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(c);
				break;
			case "Pattern":
				sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(c);
				break;
			case "Strobe":
				sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(c);
				break;
			case "Light":
				sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(c);
				break;
			case "Progr":
				sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
				sliders[i].setBackground(c);
				break;
			default:
				//sliders[i] = new MySlider(name, VERTICAL, 0, max, 0);
				//sliders[i].setBackground(GRAY);
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
	
	boolean wasOff = false;
	
	@Override
	public void loop(long count, DmxPacket packet) {
		if (null!=this.effect) {
			boolean isOff = false;
			switch (this.effect) {
			case "bounce":
				position.animateBounce(count);
				break;
			case "wheels":
				position.animateTwoWheels(count);
				break;
			case "waves":
				position.animateInterference(count);
				break;
			case "off":
				if (!wasOff) {
					position.setIdlePosition();
					if (sliders[0].getValue()>0) {
						sliders[0].setValue(0);
					}
				}
				isOff = true;
				break;
			}
			wasOff = isOff;
		}

		if (false) {
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
					packet.data[addr] = lo;
				}			
				addr += _16bit ? 2 : 1;
			}
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