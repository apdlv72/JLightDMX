package com.apdlv.jlight.controls;

import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.components.XYView;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class MovingHead extends JPanel implements DmxControlInterface, ActionListener {
	
	private static final String STEADY = "steady";
	private static final String WHEELS = "wheels";
	private static final String WAVES = "waves";
	private static final String OFF = "off";
	private static final String BOUNCE = "bounce";
	private int dmxAddr;
	private MySlider[] sliders;
	private XYView position;
	private String effect;
	private JCheckBox music;
	
	@Override
	public Insets getInsets() {
		return new Insets(17, 20, 18, 20);
	}
	
	public MovingHead(int dmxAddr, String ... channels) {
		this.dmxAddr = dmxAddr;
		this.sliders = new MySlider[channels.length];
		
		JPanel anims = new JPanel();
		anims.setLayout(new GridLayout(6, 1));
		//anims.add(this.music = new JCheckBox("Music", true));
		anims.add(newButton(BOUNCE));
		anims.add(newButton(WHEELS));
		anims.add(newButton(WAVES));
//		anims.add(newButton("Zigzag"));
		anims.add(newButton(STEADY));
		anims.add(newButton(OFF));
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
	
	Random rand = new Random();
	
	@Override
	public void loop(long count, DmxPacket packet) {
		
//		if (packet.isBeat() && music.isSelected()) {
//			sliders[2].setValue(rand.nextInt(10)*25);
//		}
		
		if (null!=this.effect) {
			boolean isOff = false;
			switch (this.effect) {
			case BOUNCE:
				position.animateBounce(count);
				break;
			case WHEELS:
				position.animateTwoWheels(count);
				break;
			case WAVES:
				position.animateInterference(count);
				break;
			case OFF:
				if (!wasOff) {
					position.setIdlePosition();
					if (sliders[0].getValue()>0) {
						sliders[0].setValue(0);
					}
				}
				isOff = true;
				break;
			case STEADY:
				break;
			default:				
			}
			if (!isOff) {
				int[] xy = position.getXY();
				sliders[0].setValue(xy[0]);
				sliders[1].setValue(xy[1]);
			}			
			wasOff = isOff;
		}

		
		if (true) {
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
			setEffect(b.getText().toLowerCase());
		}
	}

	private void setEffect(String name) {
		this.effect = name.toLowerCase();
	}

	public void toggleMoving() {
		
		MySlider light = getSlider("Light");
		MySlider color = getSlider("Color");
		if (light.getValue()>0) {
			light.setValue(light.getMinimum());
			setEffect(OFF);
		} else {
			light.setValue(light.getMaximum());
			color.setValue(new Random().nextInt(10)*25);
			setEffect(BOUNCE);
		}
	}

	private MySlider getSlider(String name) {
		for (MySlider s : sliders) {
			String n = s.getName();
			if (n.equals(name)) {
				return s;
			}
		}
		return null;
	}
}