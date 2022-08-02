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
	
	enum MODE {
		RGB,      // 12ch light bar: 4 x (r, g, b)
		RGBMS,    // 5ch light bar: r, g, b, master, strobe
		RGBWMSPS, // common china par spot: R, G, B, White, Strobe, Master, Program, Speed
	}

	protected int dmxAddr;
	protected RGBWSliders sliders[];
	protected boolean enableStrobe;

	private int strobeAddr;
	private Controls controls;
	private RGBWSliders master;
	private int index;
	private int lastIndex = 0;


	@Override
	public Insets getInsets() {
		return new Insets(0, 10, 0, 0);
	}
	
	private Integer beforeStrobeValue;

	class Controls extends JPanel implements ChangeListener, MouseListener {
		private JCheckBox sound;
		private JCheckBox chase;
		private JCheckBox reverse;
		private JCheckBox rand;
		private JCheckBox fade;
		private JSlider speed;
		private JSlider strobe;
		private JCheckBox toggle;
		private boolean chaseWasSelected;
		private JButton strobeButton;
		private JButton wStrobeButton;
		private JButton speedButton;

		public Controls(boolean enableStrobe) {
			setLayout(new GridLayout(10, 1));
			
			toggle = new JCheckBox("Toggle");
			toggle.setSelected(true);
			toggle.setToolTipText("Reset any other channels when changing value");
			add(toggle);
			
			add(speedButton = newButton("Speed:"));
			speedButton.setToolTipText("Temporarily enable full speed chase");
			
			add(speed = new MySlider("Speed", HORIZONTAL, 1, 20, 10));
			speed.setToolTipText("Set chase speed");
			
			if (enableStrobe) {
				strobeButton = newButton("Strobe:");
				strobeButton.setToolTipText("Temorarily switch on full speed strobe");
				
				wStrobeButton = newButton("WStrobe:");
				JPanel strobeButtons = new JPanel(new FlowLayout(0,0,0));
				strobeButtons.setBorder(BorderFactory.createEmptyBorder());
				strobeButtons.add(strobeButton);
				strobeButtons.add(wStrobeButton);
				add(strobeButtons);
				
				wStrobeButton.setToolTipText("Temorary full speed white strobe");
				wStrobeButton.addMouseListener(this);
				
				add(strobe = new JSlider(0, 210, 0));
				strobe.setToolTipText("Set strobe speed");
				strobe.setUI(new MyUi());
			}
			
			add(chase = new JCheckBox("Chase"));
			chase.setToolTipText("Round robin chase effect");
			
			add(sound = new JCheckBox("Sound"));
			sound.setToolTipText("Enable sound activated mode");
			
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
			if (enableStrobe) {
				strobe.addChangeListener(this);
			}
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
			int max = strobe.getMaximum()-1;
			if (s==strobeButton) {
				strobe.setValue(max);
			} else if (s==speedButton) {
				speed.setValue(speed.getMaximum()-1);
				chase.setSelected(true);
				if (master.isDark()) {
					rand.setSelected(true);
				}
			} else if (s == wStrobeButton) {
				strobe.setValue(max);
				if (null==beforeStrobeValue) {
					beforeStrobeValue = master.getWRGB();
					//System.out.println("Value before strobing: " + beforeStrobeValue);
					master.setWRGB(0xffffffff);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			Object s = e.getSource();
			int min = strobe.getMinimum();
			if (s==strobeButton) {
				strobe.setValue(min);
			} else if (s==speedButton) {
				speed.setValue(speed.getMinimum());
				chase.setSelected(false);
			} else if (s == wStrobeButton) {
				strobe.setValue(min);
				if (null!=beforeStrobeValue) {
					master.setWRGB(beforeStrobeValue);				
					beforeStrobeValue = null;
				}
			}
		}

		private void sysout(String string) {
			// TODO Auto-generated method stub
			
		}
	}

	int rainbow[];
	private MySlider dimmer;
	private int channels;
	private MODE mode;
	private RGBWSpotArray linked;
	private JCheckBox link;
	private JCheckBox masterCheck; 

	public RGBWSpotArray(int dmxAddr, int strobeAddr, int count) {
		this(dmxAddr, strobeAddr, count, MODE.RGBWMSPS, null,  true);
	}
	
	public RGBWSpotArray(int dmxAddr, int strobeAddr, int count, MODE mode, RGBWSpotArray linked, boolean enableStrobe) {
		
		this.mode = mode;
		switch (this.mode) {
			case RGBWMSPS:
				channels = 8;
				break;
			case RGBMS:
				channels = 5;
				break;
			case RGB:
				channels = 3;
				break;
		}

		this.dmxAddr = dmxAddr;
		this.strobeAddr = strobeAddr;
		this.linked = linked;
		this.enableStrobe = enableStrobe;
		
		rainbow = computeRainbow(256);
		
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		sliders = new RGBWSliders[count];
		dimmer = new MySlider(0, 255, 255);
		
		boolean withWhite = true;
		if (mode==MODE.RGB) {
			withWhite = false;
		}
		
		if (null!=linked) {
			add(link = new JCheckBox("Link"));
		}
		master = new RGBWSliders("Master", dimmer, withWhite);
		master.setToolTipText("Let master control all spots");
		master.addChangeListener(this);
		masterCheck = master.getCheck();
		controls = new Controls(enableStrobe);
		
		add(controls);
		//add(new LabeledPanel(new JLabel("Dim"), dimmer));
		add(master);
		add(new JLabel("→"));
		for (int i = 0; i < count; i++) {
			sliders[i] = new RGBWSliders("" + (i + 1), withWhite);
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
			for (RGBWSliders target : this.sliders) {
				RGBWSliders source = linked.sliders[srcIdx % srcLen];
				int wrgb = source.getWRGB();
				target.setWRGB(wrgb);
				srcIdx++;
			}
		}

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
				int masterColor = master.getWRGB();
				
				if (master.isSelected()) {
					//System.out.println("setting " + sliders.length + " sliders to master");
					for (int i=sliders.length-1; i>=0; i--) {
						sliders[i].setWRGB(masterColor);
					}					
				}
				else if (!controls.chase.isSelected()) {
					//System.out.println("forwarding slider colors");
					for (int i=sliders.length-1; i>0; i--) {
						sliders[i].setWRGB(sliders[i-1].getWRGB());
					}
					sliders[0].setWRGB(masterColor);
				} 
				
			}
		}
		
		int speed = controls.speed.getMinimum()+(controls.speed.getMaximum()-controls.speed.getValue());
		
		if (controls.chase.isSelected()) {
		
			int color = master.getWRGB();
			if (controls.rand.isSelected() && !controls.sound.isSelected()) {
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
		
		int  dimi = dimmer.getValue();
		byte dimb = (byte)dimi;
				
		for (int i=0; i<sliders.length; i++) {
			
			int rgb = sliders[i].getWRGB();
			
			int ri = ((rgb >> 16) & 0xff);
			int gi = ((rgb >>  8) & 0xff);
			int bi = ((rgb >>  0) & 0xff);
			
			byte w = (byte) ((rgb >> 24) & 0xff);
			byte r = (byte) ri;
			byte g = (byte) gi;
			byte b = (byte) bi;
			
			int index = dmxAddr + channels*i;
			
			switch (mode) {
			case RGBWMSPS: 
				packet.data[index+0] = dimb;
				packet.data[index+1] = r; 
				packet.data[index+2] = g; 
				packet.data[index+3] = b; 
				packet.data[index+4] = w;
				packet.data[index+5] = 0; // ch6: strobe: unused
				packet.data[index+6] = 0; // ch7: fade/sound mode: unused 
				packet.data[index+7] = 0; // speed for ch7: unused
				break;
			case RGBMS:
				packet.data[index+0] = r;
				packet.data[index+1] = g; 
				packet.data[index+2] = b; 
				packet.data[index+3] = dimb; 
				packet.data[index+4] = 0; // strobe: unused
				break;
			case RGB:
				// TODO: software dimming
				packet.data[index+0] = (byte) (dimi*ri/255);
				packet.data[index+1] = (byte) (dimi*gi/255); 
				packet.data[index+2] = (byte) (dimi*bi/255); 
				break;
			}
		}
		
		if (strobeAddr>-1 && enableStrobe) {
			int strobe = controls.strobe.getValue();
			packet.data[strobeAddr-1] = (byte) (strobe & 0xff);
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
		
		if (src == masterCheck) {
			boolean sel = masterCheck.isSelected();
			for (int i=0; i<sliders.length; i++) {
				sliders[i].setSelected(sel);
			}
			return;
		}
	
		boolean toggle = controls.toggle.isSelected();
		boolean setColor = false;
		if (!controls.sound.isSelected() && null==beforeStrobeValue) {
			
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
		} else {
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

	public void setSound(boolean b) {
		controls.sound.setSelected(b);
	}

	public void setRandomColor() {
		int color = rand.nextInt();
		master.setWRGB(color);
	}
}


