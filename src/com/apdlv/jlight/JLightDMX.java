package com.apdlv.jlight;

import static java.awt.Color.BLACK;
import static java.awt.Color.GRAY;
import static java.awt.Color.WHITE;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.*;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static javax.swing.BorderFactory.createLineBorder;
import static javax.swing.JComponent.*;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import com.apdlv.jlight.controls.RGBFogArray;
import com.apdlv.jlight.controls.RGBWAUSpotArray;
import com.apdlv.jlight.controls.RGBWSpotArray;
import com.apdlv.jlight.controls.Settings;
import com.apdlv.jlight.controls.Settings.SettingsListener;
import com.apdlv.jlight.controls.SoundControl;
import com.apdlv.jlight.controls.Stroboscope;
import com.apdlv.jlight.dmx.ArtNetLib;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class JLightDMX extends JFrame implements SettingsListener, KeyListener {
	
	private static final int ADDR_RGB_SPOTS = 0; // ceiling: channel 1-27 (9*RGB)
	
	private static final int ADDR_RGBW_SPOTS = 64-1; // ch 64 -> index 63. 4 spots -> till 96
	private static final int ADDR_RGBWAU_SPOTS = 100-3;	// 2 * 5ch mode = 10 channels
	private static final int ADDR_FOGGER = 128; // 3 channels 129=power, 130=fog, 131=laser
	
	private static final int ADDR_XENON_FLASH = 132-1; // channels 130(relay)+131(square wave)
	
	private static final int ADDR_MINIONS = 140-1; // 140-164: 3 * 8ch (master, R, G, B, fog, strobe, effect) 
	
	private static final int ADDR_STROBE = 200; // master strob channel
	private static final int ADDR_LASER = 256-1;
	private static final int ADDR_MOVING1 = 1-1; // channels 300 - 311
	private static final int ADDR_LIGHTBAR = 210-1; // channels 210 - 221

	private Settings settings;

	private boolean doSend;

	private Stroboscope strobo;

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
					long t0 = System.currentTimeMillis();
					control.loop(loopCount, packet);
					long t1 = System.currentTimeMillis();
					long delta = t1-t0;
					String name = control.getClass().getSimpleName();
					//System.err.println(name + ": " + delta + " ms");
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
		
		soundControl = new SoundControl();
		
		rgbwSpots = new RGBWSpotArray(ADDR_RGBW_SPOTS, 200, 4);
		rgbwauSpots = new RGBWAUSpotArray(ADDR_RGBWAU_SPOTS, rgbwSpots); 
		pilot = new AutoPilot(rgbwSpots);
		fogger = new FogMachine(ADDR_FOGGER);
		lasers = new LaserHead(ADDR_LASER, 
				"Laser"  , // 50 values/step 
				"Pattern" , // 10 values/step 
				"X-Swap", 
				"Y-Pos", 
				"Speed" // Inverse - 255 is slowest 
				); 
		lightbar = new LightBar(ADDR_LIGHTBAR, ADDR_STROBE, rgbwSpots);
		moving = new MovingHead(ADDR_MOVING1, "XPos", "YPos", "Color", "Pattern", "Strobe", "Light");		
	 	//minions = new RGBFogArray(ADDR_MINIONS, ADDR_STROBE, 3);
	 	strobo = new Stroboscope(ADDR_XENON_FLASH);
		
		channelDebugControl = new ChannelDebug();
		channelTestControl = new ChannelTest();
		
		addControl(panel, settings);
		addControl(panel, soundControl);
		addControl(panel, pilot);
		addControl(panel, rgbwSpots);
		addControl(panel, rgbwauSpots);	
		//addControl(panel, minions);
		addControl(panel, moving);
		addControl(panel, lightbar);
		addControl(panel, fogger);
		addControl(panel, channelDebugControl);
		//addControl(panel, spot);
		//addControl(panel, lasers);
		addControl(panel, channelTestControl);
		
		// order matters below because it determines, which control will have it's loop method called first:
		// sound comes first (for beat detection)
		controls.add(soundControl);
		controls.add(pilot);
		controls.add(rgbwSpots);
		controls.add(rgbwauSpots);
		//controls.add(minions);
		controls.add(lightbar);
		controls.add(fogger);
		//controls.add(spot);
		controls.add(moving);
		//controls.add(moving2);
		//controls.add(lasers);
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
		
		int maxY = bounds.y + bounds.height + 800;
		size.height = maxY;
		size.width -= 100;
		panel.setPreferredSize(size);		
		panel.setSize(size);		
	}
	
	public void addEscapeListener(final JFrame frame, Settings settings) {
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
	    
	    
	    ActionListener shortcutListener = new ActionListener() {
		    boolean isBlueMode = false;
	        @Override
	        public void actionPerformed(ActionEvent e) {	        	
	        	String cmd = e.getActionCommand();
	        	System.out.println(cmd);
	        	switch (cmd) {
	        	
	        	// predefined modes
	        	case "1":
	        		soundMode();
	        		break;	        		
	        	case "2":
	        		chaseMode();
	        		break;	        		
	        	case "3":
	        		uvMode();
	        		break;	        		
	        	case "4":
	        	case "x":
	        		if (isBlueMode = !isBlueMode) {
	        			blueMode();
	        		} else {
	        			greenMode();
	        		}
	        		break;	        		
	        		
	        	// toggles
	        	case "5":
	        		toggleMovingHeads();
	        		break;	        		
	        	case "6":
	        		toggleUv();
	        		break;	        		
	        	case "7":
	        		toggleStroboscope();
	        		break;	        		
	        	case "8":
	        		toggleFogLasers();
	        		break;	   
	        		
	        	// animation	
	        	case "9":
	        		foggerShow1();
	        		break;	        		
	        	case "a":
	        		foggerShow2();
	        		break;	        		
	        	case "b":
	        		foggerShow3();
	        		break;	        		
	        	case "c":
	        		settings.toggleBlackout();
	        		break;	   
	        		
	        	// wheel 1
	        	case "d":
	        		incMusicSensitivity();
	        		break;	        		
	        	case "e":
	        		decMusicSensitivity();
	        		break;	        		
	        	case "f":
	        		toggleSoundMode();
	        		break;
	        		
	        	// wheel 2
	        	case "g":
	        		incSpeed();
	        		break;	        		
	        	case "h":
	        		decSpeed();
	        		break;	        		
	        	case "i":
	        		wheel1Press();
	        		break;	        			        	
	        	}
	        }

	        private void soundMode() {
        		rgbwSpots.setSoundMode(true);
				rgbwSpots.setChaseMode(false);
				rgbwauSpots.setLink(true);				
				lightbar.setLink(true);
				rgbwSpots.setSoundMode(true);
			}

			private void toggleUv() {
				rgbwauSpots.toggleUV(true);
			}

			private void toggleFogLasers() {
				fogger.toggleLaser();
			}

			private void toggleStroboscope() {
	        	strobo.toggle();
			}

			private void toggleMovingHeads() {
				moving.toggleMoving();
			}

			ActionThread foggerShow;

	        private void foggerShow1() {
				stopFoggerShow();				
				foggerShow = new FoggerShow1(minions);
				foggerShow.start();
			}
			
			private void foggerShow2() {
				stopFoggerShow();	
				foggerShow = new FoggerShow2(minions);
				foggerShow.start();
			}

			private void foggerShow3() {
				stopFoggerShow();				
				foggerShow = new FoggerShow3(minions);
				foggerShow.start();
			}

			private void stopFoggerShow() {
				if (null!=foggerShow) {
					foggerShow.terminate();
					foggerShow = null;
				}				
			}

			private void incMusicSensitivity() {
				soundControl.incSensitivity();				
			}

			private void decMusicSensitivity() {
				soundControl.decSensitivity();				
			}

			private void incSpeed() {
				rgbwSpots.incSpeed();
			}

			private void decSpeed() {
				rgbwSpots.decSpeed();
			}

			private void wheel1Press() {
				System.out.println("wheel1Press: NYI");
			}

			private void blueMode() {
				rgbwauSpots.setUV(false);
				fogger.setLaser(false);
				rgbwSpots.setMasterWRGB(0xff);
				rgbwSpots.setChaseMode(true);
				rgbwSpots.setRandom(false);
				rgbwauSpots.setLink(true);
				lightbar.setLink(true);
			}

			// 6
			private void uvMode() {
				fogger.setLaser(false);
				rgbwSpots.setMasterWRGB(0x00000000);
				rgbwSpots.setChaseMode(false);
				rgbwSpots.setRandom(false);
				rgbwauSpots.setLink(false);
				rgbwauSpots.setUV(true);
				lightbar.setLink(false);
			}

			private void greenMode() {
				fogger.setLaser(true);
				rgbwSpots.setMasterWRGB(0x0000ff00);
				rgbwSpots.setChaseMode(true);
				rgbwSpots.setRandom(false);
				rgbwauSpots.setLink(true);
				lightbar.setLink(true);
			}

			// 3
			private void chaseMode() {
				rgbwSpots.setSoundMode(false);
				rgbwSpots.setChaseMode(true);
				rgbwauSpots.setLink(true);
				lightbar.setLink(true);
			}
			
			private void toggleSoundMode() {
				soundControl.toggleMode();
			}
	    };

	    JRootPane root = frame.getRootPane();
	    root.registerKeyboardAction(escListener,
	            getKeyStroke(VK_ESCAPE, 0),
	            WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(quitListener,
	            getKeyStroke(VK_Q, CTRL_DOWN_MASK),
	            WHEN_IN_FOCUSED_WINDOW);
	    
	    root.registerKeyboardAction(shortcutListener, "1", getKeyStroke(VK_1, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "2", getKeyStroke(VK_2, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "3", getKeyStroke(VK_3, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "4", getKeyStroke(VK_4, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "5", getKeyStroke(VK_5, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "6", getKeyStroke(VK_6, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "7", getKeyStroke(VK_7, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "8", getKeyStroke(VK_8, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "9", getKeyStroke(VK_9, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "a", getKeyStroke(VK_A, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "b", getKeyStroke(VK_B, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "c", getKeyStroke(VK_C, 0), WHEN_IN_FOCUSED_WINDOW);
	    // wheel 1
	    root.registerKeyboardAction(shortcutListener, "d", getKeyStroke(VK_D, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "e", getKeyStroke(VK_E, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "f", getKeyStroke(VK_F, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "f", getKeyStroke(VK_X, 0), WHEN_IN_FOCUSED_WINDOW);
	    // wheel 2
	    root.registerKeyboardAction(shortcutListener, "g", getKeyStroke(VK_G, 0),        WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "h", getKeyStroke(VK_H, 0), WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "i", getKeyStroke(VK_I, 0),    WHEN_IN_FOCUSED_WINDOW);
	    root.registerKeyboardAction(shortcutListener, "i", getKeyStroke(VK_Y, 0),        WHEN_IN_FOCUSED_WINDOW);
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

	private RGBWSpotArray rgbwSpots;

	private RGBWAUSpotArray rgbwauSpots;

	private AutoPilot pilot;

	private FogMachine fogger;

	private LaserHead lasers;

	private LightBar lightbar;

	private MovingHead moving;

	private RGBFogArray minions;

	protected SoundControl soundControl;
	
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

	@Override
	public void keyPressed(KeyEvent e) {
		char c = e.getKeyChar();
		System.err.println("keyPressed: c=" + c);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		char c = e.getKeyChar();
		System.err.println("keyReleased: c=" + c);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		System.err.println("keyTyped+: c=" + c);
	}
}
