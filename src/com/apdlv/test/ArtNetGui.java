package com.apdlv.test;

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
import javax.swing.border.Border;

/**
 * ArtNet Tester class
 * 
 * @author: Mirco Borella
 */

public class ArtNetGui {

	private static final int ADDR_RGB_SPOTS = 0;
	private static final int ADDR_RGBW_SPOTS2 = 60-1;
	private static final int ADDR_RGBW_SPOTS3 = 70-1;
	
	private static final int ADDR_FOGGER = 128;
	private static final int ADDR_LASER = 256;
	private static final int ADDR_MOVING = 384;
	private static final int ADDR_MOVING2 = 384 + 16;

	/**
	 * Main loop
	 * 
	 * @param args Arguments
	 * @throws Exception if problems
	 */
	public static void main(String[] args) {

		DmxPacket packet = new DmxPacket();

		ArtNetLib artnet = new ArtNetLib("255.255.255.255");
		JFrame frame = new JFrame();
		LayoutManager layout = new GridLayout(4, 1);
		layout = new FlowLayout();

		frame.getContentPane().setLayout(layout);

		RGBSpotArray spots1 = new RGBSpotArray(ADDR_RGB_SPOTS, 200, 9);
		FogMachine fogger = new FogMachine(ADDR_FOGGER);
		RGBWSpot spots2 = new RGBWSpot(ADDR_RGBW_SPOTS2, "Master", "Red", "Green", "Blue", "White", "Prgrm", "Flash");
		RGBWSpot spots3 = new RGBWSpot(ADDR_RGBW_SPOTS3, "Master", "Red", "Green", "Blue", "White", "Flash", "Prgrm");
		LaserHead lasers = new LaserHead(ADDR_LASER, "Master", "Color", "Effect", "Speed", "HPos", "VPos", "???");
		MovingHead moving = new MovingHead(ADDR_MOVING, "Pattern", "Red", "Green", "Blue", "White");
		MovingHead moving2 = new MovingHead(ADDR_MOVING2, "Pattern", "Red", "Green", "Blue", "White");
		Debug debug = new Debug();
		ChannelTest channel = new ChannelTest();
		Settings settings = new Settings(frame, frame.getContentPane(), debug, channel);

		Border border = createLineBorder(WHITE);
		spots1.setBorder(border);
		fogger.setBorder(border);
		spots2.setBorder(border);
		spots3.setBorder(border);
		lasers.setBorder(border);
		moving.setBorder(border);
		moving2.setBorder(border);
		settings.setBorder(border);
		debug.setBorder(border);
		channel.setBorder(border);

		frame.add(settings);
		frame.add(spots1);
		frame.add(fogger);
		frame.add(spots2);
		frame.add(spots3);
		frame.add(lasers);

		JPanel movings = new JPanel();
		movings.setLayout(new GridLayout(1, 2));
		movings.add(moving);
		movings.add(moving2);
		frame.add(movings);

		frame.add(channel);
		frame.add(debug);

		setColors(frame.getContentPane());

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		packet = new DmxPacket();

		int fps = 40;
		int millis = (int) Math.round(1000.0/fps);
		
		long loop = 0;
		long lastSent = 0;
		String last = "";
		while (true) {
			try {
				spots1.loop(loop, packet);
				fogger.loop(loop, packet);
				spots2.loop(loop, packet);
				spots3.loop(loop, packet);
				lasers.loop(loop, packet);
				moving.loop(loop, packet);
				moving2.loop(loop, packet);
				settings.loop(loop, packet);
				channel.loop(loop, packet);
				debug.loop(loop, packet);

				loop++;

				long now = System.currentTimeMillis();
				long next = lastSent + millis; 
				long delay = Math.max(0, next - now);
				// System.out.println("delay: " + delay);
				Thread.sleep(delay);

				lastSent = System.currentTimeMillis();
				if (doSend) {
					artnet.sendArtDmxPacket(packet.data, (byte) 0, (byte) 0, (byte) 0);
				} else {
					String line = packet.toString();
					if (!line.equals(last)) {
						System.out.println(loop + ": " + line);
						last = line;
					}
				}
			} catch (Exception e) {
				System.out.println("Exception in Main: " + e);
			}
		}
	}

	static boolean doSend = false;

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
}