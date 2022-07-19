package com.apdlv.jlight.controls;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.apdlv.jlight.components.ColorSlider;
import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.components.MyUi;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class RGBWSpot extends JPanel implements DmxControlInterface {

	private int dmxAddr;
	private JSlider[] sliders;
	private Controls controls;

	@Override
	public Insets getInsets() {
		return new Insets(8, 20, 8, 20);
	}

	class Controls extends JPanel {

		JCheckBox strobe;
		JSlider speed;

		public Controls() {
			strobe = new JCheckBox("Strobe");
			speed = new JSlider(VERTICAL, 0, 20, 0);
			add(strobe);
			add(speed);
			speed.setUI(new MyUi());
		}
	}

	public RGBWSpot(int dmxAddr, String... channels) {
		this.dmxAddr = dmxAddr;
		this.sliders = new JSlider[channels.length];

		add(controls = new Controls());

		for (int i = 0; i < sliders.length; i++) {

			String name = channels[i];
			switch (name) {
			case "Red":
				sliders[i] = new ColorSlider(RED, name, VERTICAL, 0, 255, 0);
				// sliders[i].setBackground(RED.darker());
				break;
			case "Green":
				sliders[i] = new ColorSlider(GREEN, name, VERTICAL, 0, 255, 0);
				// sliders[i].setBackground(GREEN.darker());
				break;
			case "Blue":
				sliders[i] = new ColorSlider(BLUE, name, VERTICAL, 0, 255, 0);
				// sliders[i].setBackground(BLUE.darker());
				break;
			default:
				sliders[i] = new MySlider(name, VERTICAL, 0, 255, 0);
				break;
			}

			JLabel label = new JLabel("  " + channels[i] + "  ");
			add(new LabeledPanel(label, sliders[i]));
		}
	}

	@Override
	public void loop(long count, DmxPacket packet) {
		if (controls.strobe.isSelected()) {
			int speed = 2 + controls.speed.getValue() * 10;
			int div = (int) (count % speed);
			if (0 == div) {
				// System.out.println("strobe: 255 -> " + dmxAddr);
				sliders[0].setValue(255);
				repaint();
			} else {
				// System.out.println("strobe: 0 -> " + dmxAddr);
				sliders[0].setValue(0);
			}
		}

		for (int i = 0; i < sliders.length; i++) {
			int value = sliders[i].getValue();
			byte bvalue = (byte) (value & 0xff);
			packet.data[dmxAddr + i] = bvalue;
		}
	}
}