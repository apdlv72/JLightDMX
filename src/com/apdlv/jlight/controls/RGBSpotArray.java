package com.apdlv.jlight.controls;

import static java.awt.Color.BLUE;
import static java.awt.Color.DARK_GRAY;
import static java.awt.Color.GREEN;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.RED;
import static javax.swing.SwingConstants.HORIZONTAL;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.apdlv.jlight.components.ColorButton;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.components.MyUi;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class RGBSpotArray extends JPanel implements ChangeListener, DmxControlInterface {

	protected int dmxAddr;
	private int strobeAddr;
	private int index;
	private RGBSliders master;
	private RGBSliders sliders[];
	private Controls controls;
	private Controls2 controls2;
	protected boolean enableStrobe;

	public RGBSpotArray(int dmxAddr, int strobeAddr, int count) {

		this.dmxAddr = dmxAddr;
		this.strobeAddr = strobeAddr;
		
		rainbow = computeRainbow(256);
		
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		sliders = new RGBSliders[count];
		master = new RGBSliders("Master");
		master.addChangeListener(this);		
		controls = new Controls();
		controls2 = new Controls2();
		
		add(controls);
		add(master);
		add(new JLabel("→"));
		for (int i = 0; i < count; i++) {
			sliders[i] = new RGBSliders("" + (i + 1));
			add(sliders[i]);
		}
		add(controls2);
		
		enableStrobe = true;
	}

	public Insets getInsets() {
		return new Insets(0, 10, 0, 10);
	}
	

	class Controls2 extends JPanel implements ActionListener {
		private final Color OFF = new Color(0, 0, 0);
		private final Color LOW = new Color(10, 10, 10);
		private final Color WARM = new Color(255, 180, 107);
		private final Color NORM = new Color(255, 209, 163);
		private final Color COLD = new Color(254, 249, 255);
		private JButton cold;
		private JButton norm;
		private JButton warm;
		private JButton red;
		private JButton blue;
		private JButton green;
		private JButton low;
		private JButton off;
		private JButton plus;
		private JButton minus;

		public Controls2() {
			setLayout(new GridLayout(7,1));
			//add(new JLabel("Bar"));
			cold = colorButton("Cold", COLD);
			//norm = colorButton("Norm", NORM);
			warm = colorButton("Warm", WARM);
			red  = colorButton("Red", RED);
			//green = colorButton("Green", GREEN);			
			blue = colorButton("Blue", BLUE);			
			low = colorButton("Low", LIGHT_GRAY);			
			off = colorButton("Off", DARK_GRAY);
			JPanel p = new JPanel();
			p.add(minus = newJButton("-", Color.WHITE, false));			
			p.add(plus  = newJButton("+", Color.WHITE, false));
			//p.setBorder(BorderFactory.createLineBorder(RED));
			add(p);
		}

		private JButton colorButton(String name, Color color) {
			ColorButton butt = new ColorButton(name);
			butt.addActionListener(this);
			butt.setForeground(color);
			add(butt);
			return butt;
		}

		private JButton newJButton(String name, Color color, boolean doAdd) {
			ColorButton butt = new ColorButton(name);
			butt.addActionListener(this);
			butt.setForeground(color);
			if (doAdd) {
				add(butt);
			}
			return butt;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();
			// https://andi-siess.de/rgb-to-color-temperature/
			if (s==cold) {
				setColor(COLD);
			} else if (s==norm) {
				setColor(NORM);
			} else if (s==warm) {
				setColor(WARM);
			} else if (s==red) {
				setColor(RED);
			} else if (s==green) {
				setColor(GREEN);
			} else if (s==blue) {
				setColor(BLUE);
			} else if (s==low) {
				setColor(LOW);
			} else if (s==off) {
				setColor(OFF);
			} else if (s==plus) {
				int len = sliders.length;
				sliders[len-1].lighter();
			} else if (s==minus) {
				int len = sliders.length;
				sliders[len-1].darker();
			}
		}

		private void setColor(Color c) {
			setColor(c.getRed(), c.getGreen(), c.getBlue());
		}

		private void setColor(int r, int g, int b) {
			int len = sliders.length;
			int color = (r<<16) | (g<<8) | b;
			sliders[len-1].setRGB(color);
		}
	}

	class Controls extends JPanel implements ChangeListener, MouseListener {
		JCheckBox all;
		JCheckBox chase;
		JCheckBox reverse;
		JCheckBox rand;
		JCheckBox fade;
		JSlider speed;
		JSlider strobeSlider;
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
			
			add(strobeSlider = new JSlider(0, 210));
			strobeSlider.setUI(new MyUi());
			strobeSlider.setValue(0);
			add(chase = new JCheckBox("Chase"));
			add(all = new JCheckBox("All"));
			add(reverse = new JCheckBox("Reverse"));
			add(rand = new JCheckBox("Random"));
			add(fade = new JCheckBox("Fade"));

			speed.addChangeListener(this);
			chase.addChangeListener(this);
			chaseWasSelected = false;
			all.addChangeListener(this);
			rand.addChangeListener(this);
			reverse.addChangeListener(this);
			fade.addChangeListener(this);
			strobeSlider.addChangeListener(this);
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
					for (int i=0; i<sliders.length-1; i++) {
						RGBSliders sl = sliders[i];
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
			if (s==strobeButton) {
				strobeSlider.setValue(strobeSlider.getMaximum()-1);
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
				strobeSlider.setValue(strobeSlider.getMinimum());
			} else if (s==speedButton) {
				speed.setValue(speed.getMinimum());
				chase.setSelected(false);
			}
		}
	}

	int rainbow[]; 

	public void loop(long count, DmxPacket packet) {
		int len = sliders.length; // sliders.length;
		if (!controls.all.isSelected()) {
			len -= 1;
		}
		int speed = controls.speed.getMinimum()+(controls.speed.getMaximum()-controls.speed.getValue());
		
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
				RGBSliders s = sliders[i];
				if (i == index) {
					s.setRGB(color);
				} else if (fade && (i0 == i || i2 == i)) {
					s.setRGB(faded);
				} else {
					s.setRGB(0);
				}
				s.repaint();
			}
		}
		
		for (int i=0; i<sliders.length; i++) {
			int rgb = sliders[i].getRGB();
			byte r = (byte) ((rgb >> 16) & 0xff);
			byte g = (byte) ((rgb >>  8) & 0xff);
			byte b = (byte) ((rgb >>  0) & 0xff);
			int index = dmxAddr+3*i+0;
			packet.data[index+0] = r; 
			packet.data[index+1] = g; 
			packet.data[index+2] = b; 
		}
		if (strobeAddr>-1 && enableStrobe) {
			packet.data[strobeAddr-1] = (byte) (controls.strobeSlider.getValue() & 0xff);
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
			for (int i=0; i<sliders.length-1; i++) {
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

		if (setColor) {
			int rgb = master.getRGB();				
			for (int i=0; i<sliders.length; i++) {
				RGBSliders s = sliders[i];
				if (s.isSelected()) {
					s.setRGB(rgb);
				}
			}
		}
	}
}
