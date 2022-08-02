package com.apdlv.jlight.controls;

import static com.apdlv.jlight.controls.RGBWAUSpotArray.MODE.*;
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

	enum MODE {
		CH7_MRGBWAU, // Master, R, G, B, White, Amber, UV (Setting: d100)
		CH10_MRGBWAUFPS // R, G, B, White, Amber, UV, Flash, Program, Speed
	}
	
	MODE mode = CH10_MRGBWAUFPS;
	
	private int dmxAddr;
	ColorSlider red;
	RGBWSliders sliders[];
	int index;
//	private Controls2 controls2;

	@Override
	public Insets getInsets() {
		return new Insets(12, 0, 13, 0);
	}
	
	private MySlider master;
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
	private MySlider flash;
	private MySlider program;
	private MySlider speed;
	private JCheckBox cbFlash;
	private JCheckBox cbProgram;
	private JCheckBox cbSpeed; 

	public RGBWAUSpotArray(int dmxAddr, RGBWSpotArray linked) {

		this.linked = linked;
		this.dmxAddr = dmxAddr;
		
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		master  = new MySlider(0, 255, 255);
		red     = new ColorSlider(RED, 0, 255, 0);
		green   = new ColorSlider(GREEN, 0, 255, 0);
		blue    = new ColorSlider(BLUE, 0, 255, 0);
		cold    = new ColorSlider(WHITE, 0, 255, 0);		
		warm    = new ColorSlider(ORANGE.brighter().brighter().brighter().brighter(), 0, 255, 0);
		uv      = new ColorSlider(MAGENTA.brighter().brighter().brighter(), 0, 255, 255);		
		flash   = new MySlider(0, 255, 0);
		program = new MySlider(0, 255, 0);
		speed   = new MySlider(0, 255, 0);
		
		add(link = new JCheckBox("Link"));
		add(new LabeledPanel(dim  =  new JButton("Dim"), master));
		dim.addMouseListener(this);
		add(new LabeledPanel(cbRed     = new JCheckBox("R"), red));
		add(new LabeledPanel(cbGreen   = new JCheckBox("G"), green));
		add(new LabeledPanel(cbBlue    = new JCheckBox("B"), blue));
		add(new LabeledPanel(cbCold    = new JCheckBox("C"), cold));
		add(new LabeledPanel(cbWarm    = new JCheckBox("W"), warm));
		add(new LabeledPanel(cbUV      = new JCheckBox("U"), uv));

		add(new LabeledPanel(cbFlash   = new JCheckBox("F"), flash));
		add(new LabeledPanel(cbProgram = new JCheckBox("P"), program));
		add(new LabeledPanel(cbSpeed   = new JCheckBox("S"), speed));
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
			RGBWSliders source = linked.sliders[0];
			int wrgb = source.getWRGB();
			int w = (wrgb >> 24) & 0xff;
			int r = (wrgb >> 16) & 0xff;
			int g = (wrgb >>  8) & 0xff;
			int b = (wrgb >>  0) & 0xff;
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
		
		int ofs1 =  0;
		int ofs2 = 10;
		if (mode==CH10_MRGBWAUFPS) {
			packet.data[dmxAddr+ofs1+1] = (byte)master.getValue();
			packet.data[dmxAddr+ofs2+1] = (byte)master.getValue();		
			ofs1++;
			ofs2++;
		}
		
		if (null!=linked && link.isSelected()) {
			RGBWSliders src1 = linked.sliders[0];
			RGBWSliders src2 = linked.sliders[2];
			copyRGBW(src1, packet, ofs1);
			copyRGBW(src2, packet, ofs2);
		} else {
			packet.data[dmxAddr+ofs1+1] = (byte)red.getValue();
			packet.data[dmxAddr+ofs1+2] = (byte)green.getValue();
			packet.data[dmxAddr+ofs1+3] = (byte)blue.getValue();
			packet.data[dmxAddr+ofs1+4] = (byte)cold.getValue();			
			
			packet.data[dmxAddr+ofs2+1] = (byte)red.getValue();
			packet.data[dmxAddr+ofs2+2] = (byte)green.getValue();
			packet.data[dmxAddr+ofs2+3] = (byte)blue.getValue();
			packet.data[dmxAddr+ofs2+4] = (byte)cold.getValue();			
		}
		
		packet.data[dmxAddr+ofs1+5] = (byte)warm.getValue();		
		packet.data[dmxAddr+ofs1+6] = (byte)uv.getValue();
		
		packet.data[dmxAddr+ofs2+5] = (byte)warm.getValue();		
		packet.data[dmxAddr+ofs2+6] = (byte)uv.getValue();
		
		switch (mode) {
		case CH10_MRGBWAUFPS:
			packet.data[dmxAddr+ofs1+7] = (byte)flash.getValue();			
			packet.data[dmxAddr+ofs1+8] = (byte)program.getValue();			
			packet.data[dmxAddr+ofs1+9] = (byte)speed.getValue();			

			packet.data[dmxAddr+ofs2+7] = (byte)flash.getValue();			
			packet.data[dmxAddr+ofs2+8] = (byte)program.getValue();			
			packet.data[dmxAddr+ofs2+9] = (byte)speed.getValue();			
			break;
		}
		
	}

	private void copyRGBW(RGBWSliders src, DmxPacket packet, int offs) {
		int wrgb = src.getWRGB();
		int w = (wrgb >> 24) & 0xff;
		int r = (wrgb >> 16) & 0xff;
		int g = (wrgb >>  8) & 0xff;
		int b = (wrgb >>  0) & 0xff;
		packet.data[dmxAddr+offs+1] = (byte)r;
		packet.data[dmxAddr+offs+2] = (byte)g;
		packet.data[dmxAddr+offs+3] = (byte)b;
		packet.data[dmxAddr+offs+4] = (byte)w;
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
//		if (src == dim) {
			//master.setValue(255-master.getValue());
//		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Object src = e.getSource();
//		if (src == dim) {
			//master.setValue(255-master.getValue());
//		}
	}
}