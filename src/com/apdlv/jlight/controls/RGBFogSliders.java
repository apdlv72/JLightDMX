package com.apdlv.jlight.controls;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static javax.swing.SwingConstants.VERTICAL;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.ColorSlider;

@SuppressWarnings("serial")
public class RGBFogSliders extends JPanel implements ChangeListener, KeyListener, MouseListener {
	
	protected JSlider r;
	protected JSlider g;
	protected JSlider b;
	protected ChangeListener listener;
	protected JButton button;
	protected boolean keyDown;

	public RGBFogSliders(String name) {

		JPanel sliderPanel = new JPanel();

		r   = new ColorSlider(RED,   "R",   VERTICAL, 0, 255, 0);
		g   = new ColorSlider(GREEN, "G",   VERTICAL, 0, 255, 0);
		b   = new ColorSlider(BLUE,  "B",   VERTICAL, 0, 255, 0);

		sliderPanel.add(r);
		sliderPanel.add(g);
		sliderPanel.add(b);

		r.addChangeListener(this);
		g.addChangeListener(this);
		b.addChangeListener(this);

		setLayout(new BorderLayout());
		button = new JButton(name);
		button.addChangeListener(this);
		button.addKeyListener(this);
		button.addMouseListener(this);

		add(button, BorderLayout.NORTH);
		add(sliderPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void setToolTipText(String text) {
		button.setToolTipText(text);
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

	public int getRGB() {
		int rv = (r.getValue() << 16);
		int gv = (g.getValue() << 8);
		int bv = b.getValue();
		int wrgb = rv | gv | bv;
		return wrgb;
	}

	public boolean isSelected() {
		return button.isSelected();
	}

	public void setRGB(int wrgb) {
		r.setValue((wrgb >> 16) & 0xff);
		g.setValue((wrgb >>  8) & 0xff);
		b.setValue((wrgb >>  0) & 0xff);
	}

	public void setSelected(boolean sel) {
		button.setSelected(sel);
	}

	public void darker() {
		r.setValue(cap(8*r.getValue()/10, 0, 255));		
		g.setValue(cap(8*g.getValue()/10, 0, 255));		
		b.setValue(cap(8*b.getValue()/10, 0, 255));		
	}

	public void lighter() {
		r.setValue(r.getValue()<10 ? 10 : cap(12*r.getValue()/10, 0, 255));		
		g.setValue(g.getValue()<10 ? 10 : cap(12*g.getValue()/10, 0, 255));		
		b.setValue(b.getValue()<10 ? 10 : cap(12*b.getValue()/10, 0, 255));		
	}

	private int cap(int i, int min, int max) {
		return i<min ? min : i>max ? max : i;
	}

	public boolean isDark() {
		return r.getValue()<1 && g.getValue()<1 && b.getValue()<1;
	}

	public byte getFog() {
		return keyDown ? (byte)0xff: 0;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource()==button) {			
			keyDown = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource()==button) {			
			keyDown = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource()==button) {			
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getSource()==button) {			
			keyDown = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getSource()==button) {			
			keyDown = false;
		}
	}

	public void setFog(boolean b) {
		keyDown = b;
	}
}