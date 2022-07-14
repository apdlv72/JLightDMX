package com.apdlv.jlight;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static javax.swing.BorderFactory.createLineBorder;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import com.apdlv.jlight.components.SelfMaintainedBackground;
import com.apdlv.jlight.components.SelfMaintainedForeground;
import com.apdlv.jlight.controls.ChannelDebug;
import com.apdlv.jlight.controls.ChannelTest;
import com.apdlv.jlight.controls.DmxControlInterface;
import com.apdlv.jlight.controls.FogMachine;
import com.apdlv.jlight.controls.LaserHead;
import com.apdlv.jlight.controls.MovingHead;
import com.apdlv.jlight.controls.RGBWSpotArray;
import com.apdlv.jlight.controls.Settings;
import com.apdlv.jlight.controls.SoundControl;
import com.apdlv.jlight.dmx.ArtNetLib;
import com.apdlv.jlight.dmx.DmxPacket;

/**
 * ArtNet Tester class
 * 
 * @author: Mirco Borella
 */

public class JLightDMX {

	//private static final int ADDR_RGB_SPOTS = 0;
	private static final int ADDR_RGBW_SPOTS = 64-1; // ch 64 -> index 63
	
	private static final int ADDR_FOGGER = 128; // channel 129
	private static final int ADDR_LASER = 255;
	private static final int ADDR_MOVING1 = 300-1; // channels 300 - 311
	private static final int ADDR_MOVING2 = 320-1; // channel 320 - 331

	/**
	 * Main loop
	 * 
	 * @param args Arguments
	 * @throws Exception if problems
	 */
	public static void main(String[] args) {

		String name = UIManager.getCrossPlatformLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(name);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			System.err.println("Failed to set " + name);
		}
		
		DmxPacket packet = new DmxPacket();
		ArtNetLib artnet = new ArtNetLib("255.255.255.255");
		JFrame frame = new JFrame();
		LayoutManager layout = new GridLayout(4, 1);
		layout = new FlowLayout();

		frame.getContentPane().setLayout(layout);
		
		SoundControl sound = new SoundControl();
		RGBWSpotArray rgbwSpots = new RGBWSpotArray(ADDR_RGBW_SPOTS, 200, 4);
		FogMachine fogger = new FogMachine(ADDR_FOGGER);
		// RGBWSpot spot = new RGBWSpot(ADDR_RGBW_SPOTS2, "Master", "Red", "Green", "Blue", "White", "Prgrm", "Flash");
		LaserHead lasers = new LaserHead(ADDR_LASER, 
				"Laser"  , // 50 values/step 
				"Pattern" , // 10 values/step 
				"X-Swap", 
				"Y-Pos", 
				"Speed" // Inverse - 255 is slowest 
				); 
		MovingHead moving1  = new MovingHead(ADDR_MOVING1, "XPos", "YPos", "Speed", "Color", "Pattern", "Strobe", "Light", "Progr");
		MovingHead moving2  = null; // new MovingHead(ADDR_MOVING1, "XPos", "YPos", "Speed", "Color", "Pattern", "Strobe", "Light", "Progr");
		ChannelDebug debug  = new ChannelDebug();
		ChannelTest channel = new ChannelTest();
		Settings settings   = new Settings(frame, frame.getContentPane(), debug, channel);

		Border border = createLineBorder(WHITE);
		setBorder(sound,border);
		setBorder(rgbwSpots,border);
		setBorder(fogger,border);
		//setBorder(spot, border);
		setBorder(lasers, border);
		setBorder(moving1, border);
		setBorder(moving2, border);
		setBorder(settings, border);
		setBorder(debug, border);
		setBorder(channel, border);

		add(frame, settings);
		add(frame, sound);
		add(frame, rgbwSpots);
		add(frame, fogger);
		add(frame, lasers);
		//add(frame, spot);
		add(frame, channel);
		add(frame, debug);
		add(frame, moving1);
		add(frame, moving2);

		setColors(frame.getContentPane());

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		packet = new DmxPacket();

		int fps = 40;
		int millis = (int) Math.round(1000.0/fps);
		
		long loopCount = 0;
		long lastSent = 0;
		String last = "";
		while (true) {
			try {
				packet.setLoopCount(loopCount);
				loop(sound, packet);
				loop(rgbwSpots, packet);
				loop(fogger, packet);
				//loop(spot, packet);
				//loop(moving1, packet);
				//loop(moving2, packet);
				loop(lasers, packet);
				loop(settings, packet);
				loop(channel, packet);
				loop(debug, packet);

				loopCount++;

				long now = System.currentTimeMillis();
				long next = lastSent + millis; 
				long delay = Math.max(0, next - now);
				//System.out.println("delay: " + delay);
				Thread.sleep(delay);

				if (doSend) {
					artnet.sendArtDmxPacket(packet.data, (byte) 0, (byte) 0, (byte) 0);
				} else {
					String line = packet.toString();
					if (!line.equals(last)) {
						System.out.println(loopCount + ": " + line);
						last = line;
					}
				}
				lastSent = System.currentTimeMillis();
			} catch (Exception e) {
				System.out.println("Exception in Main: " + e);
			}
		}
	}

	private static void loop(DmxControlInterface control, DmxPacket packet) {
		if (null!=control) {
			long loopCount = packet.getLoopCount();					
			control.loop(loopCount, packet);
		}
	}

	private static void add(JFrame frame, JPanel p) {
		if (null!=p) {
			frame.add(p);
		}
	}

	private static void setBorder(JPanel p, Border border) {
		if (null!=p) {
			p.setBorder(border);
		}
	}

	private static boolean doSend = false;

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

	public static void setSending(boolean doSend) {
		JLightDMX.doSend = doSend;
	}
}