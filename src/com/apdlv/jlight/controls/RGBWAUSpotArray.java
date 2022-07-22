package com.apdlv.jlight.controls;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.MAGENTA;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.ColorSlider;
import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class RGBWAUSpotArray extends JPanel implements ChangeListener, DmxControlInterface, MouseListener {

	private int dmxAddr;
	ColorSlider red;
	RGBWSliders sliders[];
	int index;
//	private Controls2 controls2;

	@Override
	public Insets getInsets() {
		return new Insets(12, 0, 13, 0);
	}
	
	private MySlider dimmer;
	private ColorSlider green;
	private ColorSlider blue;
	private ColorSlider cold;
	private ColorSlider warm;
	private ColorSlider uv;
	private JButton dim;
	private JCheckBox cbRed;
	private JCheckBox cbGreen;
	private JCheckBox cbBlue;
	private JCheckBox cbCold;
	private JCheckBox cbWarm;
	private JCheckBox cbUV;
	private JCheckBox link;
	private RGBWSpotArray linked; 

	public RGBWAUSpotArray(int dmxAddr, RGBWSpotArray linked) {

		this.linked = linked;
		this.dmxAddr = dmxAddr;
		
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		dimmer = new MySlider(0, 255, 0);
		red    = new ColorSlider(RED, 0, 255, 0);
		green  = new ColorSlider(GREEN, 0, 255, 0);
		blue   = new ColorSlider(BLUE, 0, 255, 0);
		cold   = new ColorSlider(WHITE, 0, 255, 0);		
		warm   = new ColorSlider(ORANGE.brighter().brighter().brighter().brighter(), 0, 255, 0);
		uv = new ColorSlider(MAGENTA.brighter().brighter().brighter(), 0, 255, 255);

		add(new LabeledPanel(link = new JCheckBox("Link"), dimmer));
		add(new LabeledPanel(dim  =  new JButton("Dim"), dimmer));
		add(new LabeledPanel(cbRed   = new JCheckBox(""), red));
		add(new LabeledPanel(cbGreen = new JCheckBox(""), green));
		add(new LabeledPanel(cbBlue  = new JCheckBox(""), blue));
		add(new LabeledPanel(cbCold  = new JCheckBox(""), cold));
		add(new LabeledPanel(cbWarm  = new JCheckBox(""), warm));
		add(new LabeledPanel(cbUV    = new JCheckBox(""), uv));
		
		dim.addMouseListener(this);
	}
	
	Random rand = new Random();
	
	int randomColors[] = {
			0b0001, 0b0010, 0b0100, 0b1000, // r g b w
			0b0011, 0b0110, 0b0101, // rg, gb, rb 
			0b1001, 0b1010, 0b1100, // wr wg wb
			0b0111, // rgb
	};
	int circleColors[] = {
			0b0100,
			0b0010, 
			0b0001, 
	};
	
	int lastIndex = 0;

	public void loop(long count, DmxPacket packet) {
		
		if (null!=linked && link.isSelected()) {
			RGBWSliders master = linked.master;
			int wrgb = master.getWRGB();
			int w = (wrgb >> 24) & 0xff;
			int r = (wrgb >> 16) & 0xff;
			int g = (wrgb >>  8) & 0xff;
			int b = (wrgb >>  0) & 0xff;
			//System.err.println("First: " + wrgb + ", r: " + r + ", g: " + g + ", b: " + b + ", w: " + w);
			red.setValue(r);
			green.setValue(g);
			blue.setValue(b);
			cold.setValue(w);
		}
		
		if (packet.isBeat()) {
			toggle(cbRed, red);
			toggle(cbGreen, green);
			toggle(cbBlue, blue);
			toggle(cbCold, cold);
			toggle(cbWarm, warm);
			toggle(cbUV, uv);
		}
		
		packet.data[dmxAddr+0] = (byte)dimmer.getValue();
		packet.data[dmxAddr+1] = (byte)red.getValue();
		packet.data[dmxAddr+2] = (byte)green.getValue();
		packet.data[dmxAddr+3] = (byte)blue.getValue();
		packet.data[dmxAddr+4] = (byte)cold.getValue();
		packet.data[dmxAddr+5] = (byte)warm.getValue();		
		packet.data[dmxAddr+6] = (byte)uv.getValue();		
	}

    private void toggle(JCheckBox cb, ColorSlider cs) {
    	if (cb.isSelected()) {
    		cs.setValue(cs.getMaximum()-cs.getValue());
    	}
	}

	int clamp255D(double v) {
        int i = (int) Math.round(v);
        return i<0 ? 0 : i>255 ? 255 : i;
    }
    
    double deg2rad(double deg) {
        return Math.PI*deg/180;
    }


	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Object src = e.getSource();
		if (src == dim) {
			dimmer.setValue(255-dimmer.getValue());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Object src = e.getSource();
		if (src == dim) {
			dimmer.setValue(255-dimmer.getValue());
		}
	}
}