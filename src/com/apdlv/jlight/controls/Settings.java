package com.apdlv.jlight.controls;

import static java.awt.Color.BLACK;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.RED;
import static java.awt.Color.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apdlv.jlight.JLightDMX;
import com.apdlv.jlight.components.SelfMaintainedBackground;
import com.apdlv.jlight.components.SelfMaintainedForeground;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class Settings extends JPanel implements DmxControlInterface, ActionListener {
	
	private JButton dark;
	private JButton light;
	private Container container;
	private JCheckBox black;
	private JLabel info;
	private JCheckBox full;
	private JFrame frame;
	private JCheckBox debug;
	private ChannelDebug debugPanel;
	private JCheckBox send;
	long lastUpdate = -1;
	long lastFrames = -1;
	private Color bg;
	private GraphicsDevice device;
	private boolean isFull;
	private ChannelTest channelTest;
	

	@Override
	public Insets getInsets() {
		return new Insets(8, 20, 8, 20);
	}
	
	public Settings(JFrame frame, Container container, ChannelDebug debugPanel, ChannelTest channelTest) {
		this.frame = frame;
		this.container = container; 
		this.debugPanel = debugPanel;
		this.channelTest = channelTest;
		
		device = GraphicsEnvironment
		        .getLocalGraphicsEnvironment().getScreenDevices()[0];
		
		setLayout(new GridLayout(7, 1,  10,  10));
		(light = new JButton("Light")).addActionListener(this);
		(dark = new JButton("Dark")).addActionListener(this);
		(black = new JCheckBox("Blackout")).addActionListener(this);
		(full = new JCheckBox("Fullscreen")).addActionListener(this);
		(debug = new JCheckBox("Debug")).addActionListener(this);
		(send = new JCheckBox("Send")).addActionListener(this);
		debug.setSelected(true);
		
		add(light);
		add(dark);		
		add(black);		
		add(full);
		add(debug);
		add(send);
		add(info = new JLabel("?"));		
		
		
		Enumeration<NetworkInterface> ifs;
		try {
			ifs = NetworkInterface.getNetworkInterfaces();
			while (ifs.hasMoreElements() ) {
				NetworkInterface i = ifs.nextElement();
				if (i.isLoopback() || !i.isUp()) {
					continue;
				}
				List<InterfaceAddress> list = i.getInterfaceAddresses();
				for (InterfaceAddress a : list) {
					InetAddress addr = a.getAddress();
					if (addr instanceof Inet4Address) {
						send.setText(addr.toString().replace("/", ""));
						break;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		super.setBackground(YELLOW);							
	}
	
	@Override
	public void loop(long count, DmxPacket packet) {
		
		if (count>10) {
			count = count;
		}
		
		if (black.isSelected()) {
			for (int i=0; i<packet.data.length; i++) {
				packet.data[i] = 0;
			}
		}
		
		long now = System.currentTimeMillis();
		if (now-lastUpdate>1000) {
			long deltaFrames = count-lastFrames;
			long deltaTime = now-lastUpdate;
			double fps = (1000.0*deltaFrames)/deltaTime;
			String text = String.format("%7d frames, %2.1f f/s", count-1, fps);
			//System.out.println(text);
			info.setText(text);
			lastUpdate  = now;
			lastFrames = count;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (dark==s) {
			setColors(container, WHITE, BLACK);
		} else if (light==s) {
			setColors(container, BLACK, LIGHT_GRAY);
			
		} else if (black==s) {
			boolean selected = black.isSelected();
			Color c = selected ? RED : bg;
			super.setBackground(c);					
		} else if (full==s) {
			if (isFull) {
				device.setFullScreenWindow(null);
				isFull = false;				
			} else {
				device.setFullScreenWindow(frame);
				isFull = true;
			}
		} else if (debug==s) {
			debugPanel.setVisible(debug.isSelected());
			channelTest.setVisible(debug.isSelected());
		} else if (send==s) {
			boolean selected = send.isSelected();
			info.setVisible(selected);
			JLightDMX.setSending(selected);			
			Color c = selected ? bg : YELLOW;
			super.setBackground(c);					
		}
	}
	
	@Override
	public void setBackground(Color c) {
		this.bg = c;		
		if (null!=black && black.isSelected()) c = RED;
		if (null!=send && !send.isSelected()) c = YELLOW;
		super.setBackground(c);
	}
	
	private static void setColors(Component c, Color fg, Color bg) {
		if (c instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox)c;
		}		
		if (!(c instanceof SelfMaintainedBackground)) {
			c.setBackground(bg);
		}
		if (!(c instanceof SelfMaintainedForeground)) {
			c.setForeground(fg);				
		}
		if (c instanceof JPanel) {
			JPanel p = (JPanel)c;
			Component[] all = p.getComponents();
			for (Component x : all) {
				setColors(x, fg, bg);
			}
		}
	}

}
