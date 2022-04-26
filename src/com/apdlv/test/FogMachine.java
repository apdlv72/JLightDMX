package com.apdlv.test;

import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
class FogMachine extends JPanel implements DmxEffectInterface, MouseListener, ChangeListener, ActionListener {
	
	private int dmxAddr;
	private JSlider powerSlider;
	private JSlider fogSlider;
	private JSlider laserSlider;
	private JButton fogButton;
	private JButton laserButton;
	private JButton powerLabel;

	@Override
	public Insets getInsets() {
		return new Insets(10, 30, 10, 30);
	}
	
	public FogMachine(int dmxAddr) {
		this.dmxAddr = dmxAddr;
		
		int orient = VERTICAL; 
		
		powerLabel = new JButton("Power");
		powerLabel.addActionListener(this);
		powerSlider = new MySlider("Power", orient, 0, 1, 0);
		powerSlider.addChangeListener(this);
		
		fogButton  = new JButton("Fog");
		fogButton.addMouseListener(this);
		fogSlider   = new MySlider("Fog", orient, 0, 1, 0);

		laserButton = new ColorButton("Laser");
		laserButton.addMouseListener(this);
		laserSlider = new MySlider("Laser", orient, 0, 25, 0);
		
		fogButton.setEnabled(false);
		fogSlider.setEnabled(false);
		
		laserButton.setForeground(Color.GREEN);
		add(new LabeledPanel(powerLabel, powerSlider));		
		add(new LabeledPanel(fogButton, fogSlider));		
		add(new LabeledPanel(laserButton, laserSlider));		
	}

	@Override
	public void loop(long count, DmxPacket packet) {
		int power = this.powerSlider.getValue();
		int fog   = this.fogSlider.getValue();
		int laser = 10 * this.laserSlider.getValue();		
		
		packet.data[dmxAddr+0] = 0==power ? (byte)0x00 : (byte)0xff;
		packet.data[dmxAddr+1] = 0==fog   ? (byte)0x00 : (byte)0xff;
		packet.data[dmxAddr+2] = (byte) (laser & 0xff);		
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		Object src = e.getSource();
		if (src == fogButton) {
			fogSlider.setValue(1);
		} else if (src == laserButton) {
			laserSlider.setValue(laserSlider.getMaximum());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Object src = e.getSource();
		if (src == fogButton) {
			fogSlider.setValue(0);
		} else if (src == laserButton) {
			laserSlider.setValue(laserSlider.getMinimum());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if (src == powerSlider) {
			fogButton.setEnabled(powerSlider.getValue()>0);
			fogSlider.setEnabled(powerSlider.getValue()>0);
		}		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s==powerLabel) {
			powerSlider.setValue(powerSlider.getMaximum()-powerSlider.getValue());
		}
	}	
}