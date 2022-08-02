package com.apdlv.jlight;

import static java.awt.Color.BLACK;
import static java.awt.Color.GRAY;
import static java.awt.Color.WHITE;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_Q;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static javax.swing.BorderFactory.createLineBorder;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;

import com.apdlv.jlight.components.SelfMaintainedBackground;
import com.apdlv.jlight.components.SelfMaintainedForeground;
import com.apdlv.jlight.controls.AutoPilot;
import com.apdlv.jlight.controls.ChannelDebug;
import com.apdlv.jlight.controls.ChannelTest;
import com.apdlv.jlight.controls.DmxControlInterface;
import com.apdlv.jlight.controls.FogMachine;
import com.apdlv.jlight.controls.LaserHead;
import com.apdlv.jlight.controls.LightBar;
import com.apdlv.jlight.controls.MovingHead;
import com.apdlv.jlight.controls.RGBWAUSpotArray;
import com.apdlv.jlight.controls.RGBWSpotArray;
import com.apdlv.jlight.controls.Settings;
import com.apdlv.jlight.controls.Settings.SettingsListener;
import com.apdlv.jlight.controls.SoundControl;
import com.apdlv.jlight.dmx.ArtNetLib;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class JLightDMX extends JFrame implements SettingsListener {
	
	private static final int ADDR_RGBW_SPOTS = 64-1; // ch 64 -> index 63. 4 spots -> till 96
	private static final int ADDR_RGBWAU_SPOTS = 100-2;	
	private static final int ADDR_FOGGER = 128; // channel 129
	private static final int ADDR_STROBE = 200;
	private static final int ADDR_LASER = 255;
	private static final int ADDR_MOVING1 = 300-1; // channels 300 - 311
	private static final int ADDR_LIGHTBAR = 210-1; // channels 210 - 221

	Settings settings;

	private boolean doSend;

	public static void main(String[] args) {
		String name = UIManager.getCrossPlatformLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(name);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			System.err.println("Failed to set " + name);
		}

		JLightDMX main = new JLightDMX();
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.processPackets();
	}
	
	void processPackets() {
		ArtNetLib artnet = new ArtNetLib("255.255.255.255");
		DmxPacket packet = new DmxPacket();

		int fps = 40;
		int millis = (int) Math.round(1000.0/fps);
		
		long loopCount = 0;
		long lastSent = 0;
		String last = "";
		while (true) {
			try {
				packet.clear();
				packet.setLoopCount(loopCount++);				
				for (DmxControlInterface control : controls) {				
					control.loop(loopCount, packet);
				}

				long now = currentTimeMillis();
				long next = lastSent + millis; 
				long delay = Math.max(0, next - now);
				sleep(delay);

				lastSent = currentTimeMillis();
				if (doSend) {
					artnet.sendArtDmxPacket(packet.data, (byte) 0, (byte) 0, (byte) 0);
				} else {
					String line = packet.toString();
					if (!line.equals(last)) {
						System.out.println(loopCount + ": " + line);
						last = line;
					}
				}
			} catch (Exception e) {
				//System.out.println("processPackets: " + e);
				e.printStackTrace();
			}
		}
		
	}
	
	public JLightDMX() {
		
		Border empty = BorderFactory.createEmptyBorder();
		
		this.setLayout(new BorderLayout());
		this.setBackground(BLACK);
		
		Dimension screenSize = Toolkit. getDefaultToolkit(). getScreenSize();
		
		JPanel panel = new JPanel();
		panel.setSize(screenSize);
		panel.setBackground(BLACK);
		
		Dimension prefSize = new Dimension(screenSize.width-40, 2*screenSize.height);		
		panel.setPreferredSize(prefSize);				
		panel.setLayout(new FlowLayout());
		
		settings = new Settings(this, panel, null, null);
		settings.addSettingsListener(this);
		
		SoundControl sound = new SoundControl();	
//		LevelControl sound = new LevelControl();
//		sound.start();
		
		RGBWSpotArray rgbwSpots = new RGBWSpotArray(ADDR_RGBW_SPOTS, 200, 4);
		RGBWAUSpotArray rgbwauSpots = new RGBWAUSpotArray(ADDR_RGBWAU_SPOTS, rgbwSpots); 
		AutoPilot pilot = new AutoPilot(rgbwSpots);
		FogMachine fogger = new FogMachine(ADDR_FOGGER);
		// RGBWSpot spot = new RGBWSpot(ADDR_RGBW_SPOTS2, "Master", "Red", "Green", "Blue", "White", "Prgrm", "Flash");
		LaserHead lasers = new LaserHead(ADDR_LASER, 
				"Laser"  , // 50 values/step 
				"Pattern" , // 10 values/step 
				"X-Swap", 
				"Y-Pos", 
				"Speed" // Inverse - 255 is slowest 
				); 
		LightBar lightbar = new LightBar(ADDR_LIGHTBAR, ADDR_STROBE, rgbwSpots);
		MovingHead moving  = new MovingHead(ADDR_MOVING1, "XPos", "YPos", "Speed", "Color", "Pattern", "Strobe", "Light", "Progr");
		channelDebugControl = new ChannelDebug();
		channelTestControl = new ChannelTest();
		
		addControl(panel, settings);
		addControl(panel, sound);
		addControl(panel, pilot);
		addControl(panel, rgbwSpots);
		addControl(panel, rgbwauSpots);		
		addControl(panel, lightbar);
		addControl(panel, fogger);
		addControl(panel, lasers);
		//addControl(panel, spot);
		addControl(panel, channelTestControl);
		addControl(panel, channelDebugControl);
		addControl(panel, moving);
		
		// order matters below because it determines, which control will have it's loop method called first:
		// sound comes first (for beat detection)
		controls.add(sound);
		controls.add(pilot);
		controls.add(rgbwSpots);
		controls.add(rgbwauSpots);
		controls.add(lightbar);
		controls.add(fogger);
		//controls.add(spot);
		controls.add(moving);
		//controls.add(moving2);
		controls.add(lasers);
		controls.add(channelTestControl);
		// settings comes but last (for blackout)
		controls.add(settings);
		controls.add(channelDebugControl);
						
		this.setPreferredSize(screenSize);
		JScrollPane scroll = new JScrollPane(panel);
		scroll.setBorder(empty);
		scroll.setBackground(BLACK);
		this.add(scroll);

		setColors(panel);		
		setScrollbarColors(scroll.getVerticalScrollBar());
		setScrollbarColors(scroll.getHorizontalScrollBar());		
				
		addEscapeListener(this, settings);
		
		this.pack();
		Dimension size = panel.getSize();
		Rectangle bounds = moving.getBounds();
		
		int maxY = bounds.y + bounds.height + 400;
		size.height = maxY;
		size.width -= 100;
		panel.setPreferredSize(size);		
		panel.setSize(size);		
	}
	
	public static void addEscapeListener(final JFrame frame, Settings settings) {
	    ActionListener escListener = new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {	        	
	        	settings.toggleFullscreen();
	        }
	    };
	    ActionListener quitListener = new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {	        	
	        	System.exit(0);
	        }
	    };

	    JRootPane root = frame.getRootPane();
	    root.registerKeyboardAction(escListener,
	            getKeyStroke(VK_ESCAPE, 0),
	            WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(quitListener,
	            getKeyStroke(VK_Q, CTRL_DOWN_MASK),
	            WHEN_IN_FOCUSED_WINDOW);
	}
	
	private void setScrollbarColors(JScrollBar bar) {
		bar.setBackground(BLACK);
		bar.setUI(new BasicScrollBarUI() {
			Color gray = GRAY.darker().darker().darker();
		    @Override
		    protected void configureScrollBarColors() {		    	
		        this.thumbColor = GRAY.darker().darker();
		        this.thumbDarkShadowColor = gray;
		        this.thumbHighlightColor = gray;
		        this.thumbLightShadowColor = gray;		        		
		    }		    
		    @Override
		    protected JButton createDecreaseButton(int orientation) {
		    	return createZeroButton();	    
		    }
		    @Override
		    protected JButton createIncreaseButton(int orientation) {
	            return createZeroButton();
		    }
		    @Override
		    protected void paintIncreaseHighlight(Graphics g) {
		    	//super.paintIncreaseHighlight(g);
		    }
		    @Override
		    protected void paintDecreaseHighlight(Graphics g) {
		    	super.paintDecreaseHighlight(g);
		    }
		    private JButton createZeroButton() {
		        JButton jbutton = new JButton();
		        jbutton.setPreferredSize(new Dimension(0, 0));
		        jbutton.setMinimumSize(new Dimension(0, 0));
		        jbutton.setMaximumSize(new Dimension(0, 0));
		        return jbutton;
		    }
		});
	}

	List<DmxControlInterface> controls = new ArrayList<>();

	private ChannelDebug channelDebugControl;

	private ChannelTest channelTestControl;
	
	private static void addControl(JPanel panel, JPanel p) {
		if (null!=p) {
			panel.add(p);
			Border border = createLineBorder(WHITE);
			p.setBorder(border);
		}
	}
	
	private static void setColors(Component c) {
		if (c instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox) c;
		}
		try {
			Font font = new Font("Monaco", Font.PLAIN, 20);
			// c.setFont(font);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!(c instanceof SelfMaintainedBackground)) {
			c.setBackground(BLACK);
		}
		if (!(c instanceof SelfMaintainedForeground)) {
			c.setForeground(WHITE);
		}
		if (c instanceof JPanel) {
			JPanel p = (JPanel) c;
			Component[] all = p.getComponents();
			for (Component x : all) {
				setColors(x);
			}
		}
	}

	private static void setBorder(JPanel p, Border border) {
		if (null!=p) {
			p.setBorder(border);
		}
	}

	@Override
	public void setSending(boolean sending) {
		this.doSend = sending;
		System.err.println("setSending: " + sending);
	}

	@Override
	public void setDebug(boolean selected) {
		channelTestControl.setVisible(selected);
		channelDebugControl.setVisible(selected);
	}
}
