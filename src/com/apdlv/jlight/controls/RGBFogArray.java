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
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class RGBFogArray extends JPanel implements ChangeListener, DmxControlInterface {
	
	enum MODE {
		RGB,      // 12ch light bar: 4 x (r, g, b)
		RGBMS,    // 5ch light bar: r, g, b, master, strobe
		RGBWMSPS, // common china par spot: R, G, B, White, Strobe, Master, Program, Speed
	}

	protected int dmxAddr;
	protected RGBFogSliders sliders[];

	private Controls controls;
	private RGBFogSliders master;
	private int index;


	@Override
	public Insets getInsets() {
		return new Insets(0, 10, 0, 0);
	}
	
	private Integer beforeStrobeValue;

	class Controls extends JPanel implements ChangeListener, MouseListener {
		private JCheckBox only;
		private JCheckBox chase;
		private JCheckBox reverse;
		private JCheckBox rand;
		private JCheckBox fade;
		private JSlider speed;
		private JCheckBox toggle;
		private boolean chaseWasSelected;
		private JButton speedButton;

		public Controls() {
			setLayout(new GridLayout(10, 1));
			
			toggle = new JCheckBox("Toggle");
			toggle.setSelected(true);
			toggle.setToolTipText("Reset any other channels when changing value");
			add(toggle);
			
			add(speedButton = newButton("Speed:"));
			speedButton.setToolTipText("Temporarily enable full speed chase");
			
			add(speed = new MySlider("Speed", HORIZONTAL, 10, 40, 25));
			speed.setToolTipText("Set chase speed");
			
			add(only = new JCheckBox("Light only"));
			only.setToolTipText("Light up without fog");

			add(chase = new JCheckBox("Chase"));
			chase.setToolTipText("Round robin chase effect");
			
//			add(sound = new JCheckBox("Sound"));
//			sound.setToolTipText("Enable sound activated mode");
			
			add(reverse = new JCheckBox("Reverse"));
			reverse.setToolTipText("Reverse chase direction");
			
			add(rand = new JCheckBox("Random"));
			rand.setToolTipText("Use random colors rather than master value");
			
			add(fade = new JCheckBox("Fade"));
			fade.setToolTipText("Set previous/next spot to faded value of curent one");

			speed.addChangeListener(this);
			chase.addChangeListener(this);
			chaseWasSelected = false;
//			all.addChangeListener(this);
			rand.addChangeListener(this);
			reverse.addChangeListener(this);
			fade.addChangeListener(this);
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
						RGBFogSliders sl = sliders[i];
						sl.setRGB(0);
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
			if (s==speedButton) {
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
			if (s==speedButton) {
				speed.setValue(speed.getMinimum());
				chase.setSelected(false);
			} 
		}
	}

	int rainbow[];
	private RGBWSpotArray linked;
	private JCheckBox link;

	public RGBFogArray(int dmxAddr, int strobeAddr, int count) {
		this(dmxAddr, count, null);
	}
	
	public RGBFogArray(int dmxAddr, int count, RGBWSpotArray linked) {
		
		this.dmxAddr = dmxAddr;
		this.linked = linked;
		
		rainbow = computeRainbow(256);
		
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		sliders = new RGBFogSliders[count];
		
		if (null!=linked) {
			add(link = new JCheckBox("Link"));
		}
		master = new RGBFogSliders("Master") {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (e.getSource()==button) {
					for (RGBFogSliders s : sliders) {
						s.keyDown = true;						
					}
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if (e.getSource()==button) {
					for (RGBFogSliders s : sliders) {
						s.keyDown = false;
					}
				}
			}
			@Override
			public void stateChanged(ChangeEvent e) {
				super.stateChanged(e);
				Object o = e.getSource();
				if (r==o || g==o || b==o) {
					int rgb = getRGB();
					for (RGBFogSliders s : sliders) {
						s.setRGB(rgb);
					}
				}
			}
		};
		master.setToolTipText("Let master control all spots");
		master.addChangeListener(this);
		controls = new Controls();
		
		add(controls);
		//add(new LabeledPanel(new JLabel("Dim"), dimmer));
		add(master);
		add(new JLabel("→"));
		for (int i = 0; i < count; i++) {
			sliders[i] = new RGBFogSliders("Minion"+(i+1));
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
	
	public void loop(long count, DmxPacket packet) {
		
		int len = sliders.length; // sliders.length;
		
		if (null!=linked && link.isSelected()) {
			int srcIdx = 0;			
			int srcLen = linked.sliders.length;
			for (RGBFogSliders target : this.sliders) {
				RGBWSliders source = linked.sliders[srcIdx % srcLen];
				int wrgb = source.getWRGB();
				target.setRGB(wrgb);
				srcIdx++;
			}
		}

		int speed = controls.speed.getMinimum()+(controls.speed.getMaximum()-controls.speed.getValue());
		
		boolean only = controls.only.isSelected();

		byte fog[] = new byte[sliders.length];
		for (int i=0; i<sliders.length; i++) {
			fog[i] = sliders[i].getFog();
		}
		
		if (controls.chase.isSelected()) {
		
			int color = master.getRGB();
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
				RGBFogSliders s = sliders[i];
				if (i == index) {
					s.setRGB(color);
					if (!only) {
						fog[i] = color>0 ? (byte)0xff : 1;
					}
				} else if (fade && (i0 == i || i2 == i)) {
					s.setRGB(faded);
					if (!only) {
						fog[i] = 0;
					}
				} else {
					s.setRGB(0);
					if (!only) {
						fog[i] = 0;
					}
				}
				s.repaint();
			}
		}
		
		int n = sliders.length;
		
		for (int i=0; i<n; i++) {
			
			int rgb = sliders[i].getRGB();
			
			int ri = ((rgb >> 16) & 0xff);
			int gi = ((rgb >>  8) & 0xff);
			int bi = ((rgb >>  0) & 0xff);
			
			byte r = (byte) ri;
			byte g = (byte) gi;
			byte b = (byte) bi;
			
			int index = dmxAddr + 8*i;

//			if (i==0) {
//				System.out.println("i=0, fog=" + fog + ", only=" + only + ", rgb=" + rgb);
//			}
			
			if (fog[i]==0 && !only) {
				r = g = b = 0;
			}
			
			packet.data[index+0] = (byte)0xff; // dimmer always 100%
			packet.data[index+1] = r; 
			packet.data[index+2] = g; 
			packet.data[index+3] = b; 
			packet.data[index+4] = fog[i];
			packet.data[index+5] = 0; // ch6: strobe: unused
			packet.data[index+6] = 0; // ch7: fade/sound mode: unused 
			packet.data[index+7] = 0; // speed for ch7: unused
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
		
		boolean toggle = controls.toggle.isSelected();
		boolean setColor = false;
		if (null==beforeStrobeValue) {
			
			if (src==master.r) {
				if (toggle && master.r.getValue()>0) {
					master.g.setValue(0);
					master.b.setValue(0);				
				}
				setColor = true;
			}
			if (src==master.g && master.g.getValue()>0) {
				if (toggle) {
					master.r.setValue(0);
					master.b.setValue(0);
				}
				setColor = true;
			}
			if (src==master.b && master.b.getValue()>0) {
				if (toggle) {
					master.r.setValue(0);
					master.g.setValue(0);
				}
				setColor = true;
			}
		} else {
			setColor = true;
		}

		if (setColor) {
			int rgb = master.getRGB();				
			for (int i=0; i<sliders.length; i++) {
				RGBFogSliders s = sliders[i];
				if (s.isSelected()) {
					s.setRGB(rgb);
				}
			}
		}
	}

	public void toggleRandom() {
		controls.rand.setSelected(!controls.rand.isSelected());
	}

	public void toggleReverse() {
		controls.reverse.setSelected(!controls.reverse.isSelected());
	}

	public void toggleFade() {
		controls.fade.setSelected(!controls.fade.isSelected());
	}

	public void setChase(boolean chase) {
		controls.chase.setSelected(chase);		
	}

	public void setMaster(boolean value) {
		master.setSelected(value);		
	}

	public void setRandom(boolean b) {
		controls.rand.setSelected(b);
	}

	public void setReverse(boolean b) {
		controls.reverse.setSelected(b);
	}

	public void setFade(boolean b) {
		controls.fade.setSelected(b);
	}

	public void setRandomColor() {
		int color = rand.nextInt();
		master.setRGB(color);
	}

	public RGBFogSliders get(int i) {
		return sliders[i];
	}
}


