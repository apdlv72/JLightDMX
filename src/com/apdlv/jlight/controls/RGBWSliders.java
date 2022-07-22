package com.apdlv.jlight.controls;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static javax.swing.SwingConstants.VERTICAL;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.ColorSlider;
import com.apdlv.jlight.components.MySlider;

@SuppressWarnings("serial")
public class RGBWSliders extends JPanel implements ChangeListener {
	
	JSlider r;
	JSlider g;
	JSlider b;
	JSlider w;
	ChangeListener listener;
	private JCheckBox check;

	public RGBWSliders(String name) {
		this(name, null);
	}

	public RGBWSliders(String name, MySlider dimmer) {

		JPanel sliderPanel = new JPanel();

		r = new ColorSlider(RED,   "Red",   VERTICAL, 0, 255, 0);
		g = new ColorSlider(GREEN, "Green", VERTICAL, 0, 255, 0);
		b = new ColorSlider(BLUE,  "Blue",  VERTICAL, 0, 255, 0);
		w = new ColorSlider(WHITE, "White",  VERTICAL, 0, 255, 0);		
//		r.setBackground(RED.darker());
//		g.setBackground(GREEN.darker());
//		b.setBackground(BLUE.darker());
//		w.setBackground(WHITE);

		if (null!=dimmer) {
			sliderPanel.add(dimmer);	
		}
		sliderPanel.add(r);
		sliderPanel.add(g);
		sliderPanel.add(b);
		sliderPanel.add(w);

		r.addChangeListener(this);
		g.addChangeListener(this);
		b.addChangeListener(this);
		w.addChangeListener(this);

		setLayout(new BorderLayout());
		check = new JCheckBox(name);
		check.addChangeListener(this);

		add(check, BorderLayout.NORTH);
		add(sliderPanel, BorderLayout.CENTER);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (null != listener) {
			listener.stateChanged(e);
		}
	}

	public void addChangeListener(ChangeListener l) {
		listener = l;
	}

	public int getWRGB() {
		int wv = (w.getValue() << 24);
		int rv = (r.getValue() << 16);
		int gv = (g.getValue() << 8);
		int bv = b.getValue();
		int wrgb = wv | rv | gv | bv;
		//System.err.println("getWRGB: wrgb: " + wrgb + ", r: " + rv + ", g: " + gv + ", b: " + bv + ", w: " + wv);
		return wrgb;
	}

	public boolean isSelected() {
		return check.isSelected();
	}

	public void setWRGB(int wrgb) {
		w.setValue((wrgb >> 24) & 0xff);
		r.setValue((wrgb >> 16) & 0xff);
		g.setValue((wrgb >>  8) & 0xff);
		b.setValue((wrgb >>  0) & 0xff);
	}

	public void setSelected(boolean sel) {
		check.setSelected(sel);
	}

	public void darker() {
		r.setValue(cap(8*r.getValue()/10, 0, 255));		
		g.setValue(cap(8*g.getValue()/10, 0, 255));		
		b.setValue(cap(8*b.getValue()/10, 0, 255));		
		w.setValue(cap(8*w.getValue()/10, 0, 255));		
	}

	public void lighter() {
		r.setValue(r.getValue()<10 ? 10 : cap(12*r.getValue()/10, 0, 255));		
		g.setValue(g.getValue()<10 ? 10 : cap(12*g.getValue()/10, 0, 255));		
		b.setValue(b.getValue()<10 ? 10 : cap(12*b.getValue()/10, 0, 255));		
		w.setValue(w.getValue()<10 ? 10 : cap(12*w.getValue()/10, 0, 255));		
	}

	private int cap(int i, int min, int max) {
		return i<min ? min : i>max ? max : i;
	}

	public boolean isDark() {
		return r.getValue()<1 && g.getValue()<1 && b.getValue()<1 && w.getValue()<1;
	}
}