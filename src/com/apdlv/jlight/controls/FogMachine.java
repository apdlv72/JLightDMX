package com.apdlv.jlight.controls;

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

import com.apdlv.jlight.components.ColorButton;
import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class FogMachine extends JPanel implements DmxControlInterface, MouseListener, ChangeListener, ActionListener {
	
	private int dmxAddr;
	private JButton powerButton;
	private JSlider powerSlider;
	private JButton fogButton;
	private JSlider fogSlider;
	private JSlider laserSlider;
	private JButton laserButton;
	private JSlider speedSlider;
	private JButton speedButton;
	
	@Override
	public Insets getInsets() {
		return new Insets(10, 36, 15, 35);
	}
	
	public FogMachine(int dmxChannel) {
		this.dmxAddr = dmxChannel;
		
		int orient = VERTICAL; 
		
		powerButton = new JButton("Power");
		powerButton.addActionListener(this);
		powerSlider = new MySlider("Power", orient, 0, 1, 0);
		powerSlider.addChangeListener(this);
		
		fogButton  = new JButton("Fog");
		fogButton.addMouseListener(this);
		fogSlider   = new MySlider("Fog", orient, 0, 1, 0);

		laserButton = new ColorButton("Laser");
		laserButton.addMouseListener(this);
		laserSlider = new MySlider("Laser", orient, 0, 255, 0);
		
		speedButton = new ColorButton("Speed");
		speedButton.addMouseListener(this);
		speedSlider = new MySlider("Speed", orient, 0, 255, 0);

		fogButton.setEnabled(false);
		fogSlider.setEnabled(false);
		
		laserButton.setForeground(Color.GREEN);
		add(new LabeledPanel(powerButton, powerSlider));		
		add(new LabeledPanel(fogButton, fogSlider));		
		add(new LabeledPanel(laserButton, laserSlider));		
		add(new LabeledPanel(speedButton, speedSlider));		
	}

	@Override
	public void loop(long count, DmxPacket packet) {
		int power = this.powerSlider.getValue();
		int fog   = this.fogSlider.getValue();
		int laser = this.laserSlider.getValue();		
		int speed = this.speedSlider.getValue();		
		
		packet.data[dmxAddr+0] = 0==power ? (byte)0x00 : (byte)0xff;
		packet.data[dmxAddr+1] = 0==fog   ? (byte)0x00 : (byte)0xff;
		packet.data[dmxAddr+2] = (byte) (laser & 0xff);		
		packet.data[dmxAddr+3] = (byte) (speed & 0xff);		
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
		if (s==powerButton) {
			powerSlider.setValue(powerSlider.getMaximum()-powerSlider.getValue());
		}
	}
	
	public void setLaser(boolean b) {
		laserSlider.setValue(b ? laserSlider.getMaximum() : laserSlider.getMinimum());
	}

	public void toggleLaser() {
		boolean set = laserSlider.getValue()>127;
		laserSlider.setValue(set ? laserSlider.getMinimum() : laserSlider.getMaximum());
	}
}