package com.apdlv.jlight.controls;

import static javax.swing.SwingConstants.HORIZONTAL;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.components.MyUi;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class RGBWSpotArray extends JPanel implements ChangeListener, DmxControlInterface {

	private int dmxAddr;
	private int strobeAddr;
	Controls controls;
	RGBWSliders master;
	RGBWSliders sliders[];
	int index;
//	private Controls2 controls2;

	@Override
	public Insets getInsets() {
		return new Insets(0, 10, 0, 0);
	}
	
	class Controls extends JPanel implements ChangeListener, MouseListener {
		JCheckBox sound;
		JCheckBox chase;
		JCheckBox reverse;
		JCheckBox rand;
		JCheckBox fade;
		JSlider speed;
		JSlider strobe;
		JCheckBox toggle;
		private boolean chaseWasSelected;
		private JButton strobeButton;
		private JButton speedButton;

		public Controls() {
			setLayout(new GridLayout(10, 1));
			
			toggle = new JCheckBox("Toggle");
			toggle.setSelected(true);
			add(toggle);
			
			add(speedButton = newButton("Speed:"));
			add(speed = new MySlider("Speed", HORIZONTAL, 1, 20, 10));
			add(strobeButton = newButton("Strobe:"));
			
			add(strobe = new JSlider(0, 210));
			strobe.setUI(new MyUi());
			strobe.setValue(0);
			add(chase = new JCheckBox("Chase"));
			add(sound = new JCheckBox("Sound"));
			add(reverse = new JCheckBox("Reverse"));
			add(rand = new JCheckBox("Random"));
			add(fade = new JCheckBox("Fade"));

			speed.addChangeListener(this);
			chase.addChangeListener(this);
			chaseWasSelected = false;
//			all.addChangeListener(this);
			rand.addChangeListener(this);
			reverse.addChangeListener(this);
			fade.addChangeListener(this);
			strobe.addChangeListener(this);
		}
		
		private JButton newButton(String name) {
			JButton b = new JButton(name);
			b.addMouseListener(this);
			return b;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			Object s = e.getSource();
			if (chase == s) {
				if (chaseWasSelected && !chase.isSelected()) {
					for (int i=0; i<sliders.length; i++) {
						RGBWSliders sl = sliders[i];
						sl.setWRGB(0);
					}
				}
				chaseWasSelected = chase.isSelected();
			}
		}
		
		private int darker(int rgb) {
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = (rgb >> 0) & 0xff;
			r /= 2;
			g /= 2;
			b /= 2;
			return (r << 16) | (g << 8) | b;
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent e) {
			Object s = e.getSource();
			if (s==strobeButton) {
				strobe.setValue(strobe.getMaximum()-1);
			} else if (s==speedButton) {
				speed.setValue(speed.getMaximum()-1);
				chase.setSelected(true);
				if (master.isDark()) {
					rand.setSelected(true);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			Object s = e.getSource();
			if (s==strobeButton) {
				strobe.setValue(strobe.getMinimum());
			} else if (s==speedButton) {
				speed.setValue(speed.getMinimum());
				chase.setSelected(false);
			}
		}
	}

	int rainbow[]; 

	public RGBWSpotArray(int dmxAddr, int strobeAddr, int count) {

		this.dmxAddr = dmxAddr;
		this.strobeAddr = strobeAddr;
		
		rainbow = computeRainbow(256);
		
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		sliders = new RGBWSliders[count];
		master = new RGBWSliders("Master");
		master.addChangeListener(this);		
		controls = new Controls();
//		controls2 = new Controls2();
		
		add(controls);
		add(master);
		add(new JLabel("→"));
		for (int i = 0; i < count; i++) {
			sliders[i] = new RGBWSliders("" + (i + 1));
			add(sliders[i]);
		}
//		add(controls2);
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
		int len = sliders.length; // sliders.length;
		
		if (packet.isBeat()) {
			if (controls.sound.isSelected()) {
				
				int[] colors = null;
				int index = 0;
				
				if (controls.rand.isSelected()) {
					 colors = randomColors;
					 index = (lastIndex + 1) % randomColors.length;							 
				} else { 				
					colors = circleColors;
					index = rand.nextInt(colors.length);
					while (index == lastIndex) {
						index = rand.nextInt(colors.length);
					}
				}
				lastIndex = index;
				
				int dice = colors[index];
				int r = (1 & dice)>0 ? 255 : 0;
				int g = (2 & dice)>0 ? 255 : 0;
				int b = (4 & dice)>0 ? 255 : 0;				
				int w = (8 & dice)>0 ? 255 : 0;
				int wrgb = (w << 24) | (r << 16) | (g << 8) | b;				
				//System.out.println("BOOM dice=" + dice + " wrgb=" + wrgb);				
				master.setWRGB(wrgb);
				
				if (!controls.chase.isSelected()) {
					for (int i=sliders.length-1; i>0; i--) {
						sliders[i].setWRGB(sliders[i-1].getWRGB());
					}
					sliders[0].setWRGB(master.getWRGB());
				}				
			}
		}
		
//		if (!controls.all.isSelected()) {
//			len -= 1;
//		}
		int speed = controls.speed.getMinimum()+(controls.speed.getMaximum()-controls.speed.getValue());
		
		if (controls.chase.isSelected()) {
		
			int color = master.getWRGB();
			if (controls.rand.isSelected()) {
				if (randomColor<0 || rnd.nextInt(5)==0) {
					randomColor = createRandomColor(count);
				}
				color = randomColor;
			}
		
			int faded = controls.darker(color);
			boolean fade = controls.fade.isSelected();
		
			int count1 = (int) (count % speed);
			if (0 == count1) {
				if (controls.reverse.isSelected()) {
					index = (index - 1 + len) % len;
				} else {
					index = (index + 1) % len;
				}
			}
		
			int i2 = (index + 1) % len;
			int i0 = (index - 1 + len) % len;
		
			for (int i = 0; i < len; i++) {
				RGBWSliders s = sliders[i];
				if (i == index) {
					s.setWRGB(color);
				} else if (fade && (i0 == i || i2 == i)) {
					s.setWRGB(faded);
				} else {
					s.setWRGB(0);
				}
				s.repaint();
			}
		}
		
		for (int i=0; i<sliders.length; i++) {
			
			int rgb = sliders[i].getWRGB();
			
			byte w = (byte) ((rgb >> 24) & 0xff);
			byte r = (byte) ((rgb >> 16) & 0xff);
			byte g = (byte) ((rgb >>  8) & 0xff);
			byte b = (byte) ((rgb >>  0) & 0xff);
			
			int index = dmxAddr + 8*i;
			//System.err.println("Slider " + i + ": index=" + index);
			
			packet.data[index+0] = (byte)0xff; // ch1: total dimming
			packet.data[index+1] = r; 
			packet.data[index+2] = g; 
			packet.data[index+3] = b; 
			packet.data[index+4] = w;
			packet.data[index+5] = 0; // ch6: strobe: unused
			packet.data[index+6] = 0; // ch7: fade/sound mode: unused 
			packet.data[index+7] = 0; // speed for ch7: unused 
		}
		if (strobeAddr>-1) {
			packet.data[strobeAddr-1] = (byte) (controls.strobe.getValue() & 0xff);
		}
	}

	private int randomColor = -1;
	private Random rnd = new Random();

	private int createRandomColor(long count) {		
		//int index = rnd.nextInt(rainbow.length);
		int index = (int) ((count/1) % rainbow.length);				
		return rainbow[index];
	}
	
    int[] computeRainbow(int count) {
    	int rainbow[] = new int[count];
        double a = 360.0 / count;
        double ar = 0;
        double ag = 120;
        double ab = 240;
        for (int i=0; i<count; i++) {
            double rr = deg2rad(i*a + ar);
            double rg = deg2rad(i*a + ag);
            double rb = deg2rad(i*a + ab);
            double sr = Math.sin(rr);
            double sg = Math.sin(rg);
            double sb = Math.sin(rb);
            int r = clamp255D(127.5 + 127.5*sr);
            int g = clamp255D(127.5 + 127.5*sg);
            int b = clamp255D(127.5 + 127.5*sb);
            rainbow[i] = (((r & 0xff))<<16) | (((g & 0xff))<<8) | (((b & 0xff)));
        }
        return rainbow;
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
		//System.out.println("stateChanged: src=" + src);
		
		if (src instanceof JCheckBox) {
			JCheckBox c = (JCheckBox) src;
			boolean sel = c.isSelected();
			for (int i=0; i<sliders.length; i++) {
				sliders[i].setSelected(sel);
			}
			return;
		}
	
		boolean toggle = controls.toggle.isSelected();
		boolean setColor = false;
		if (src==master.r) {
			if (toggle && master.r.getValue()>0) {
				master.g.setValue(0);
				master.b.setValue(0);				
				master.w.setValue(0);				
			}
			setColor = true;
		}
		if (src==master.g && master.g.getValue()>0) {
			if (toggle) {
				master.r.setValue(0);
				master.b.setValue(0);
				master.w.setValue(0);				
			}
			setColor = true;
		}
		if (src==master.b && master.b.getValue()>0) {
			if (toggle) {
				master.r.setValue(0);
				master.g.setValue(0);
				master.w.setValue(0);				
			}
			setColor = true;
		}
		if (src==master.w && master.w.getValue()>0) {
			if (toggle) {
				master.r.setValue(0);
				master.g.setValue(0);
				master.b.setValue(0);				
			}
			setColor = true;
		}

		if (setColor) {
			int rgb = master.getWRGB();				
			for (int i=0; i<sliders.length; i++) {
				RGBWSliders s = sliders[i];
				if (s.isSelected()) {
					s.setWRGB(rgb);
				}
			}
		}
	}
}