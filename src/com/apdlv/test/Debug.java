package com.apdlv.test;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class Debug extends JPanel implements DmxEffectInterface {
	
	private JLabel[] labels;

	@Override
	public Insets getInsets() {
		return new Insets(8, 20, 8, 20);
	}
	
	public Debug() {
        Font font = new Font( "Monospaced", Font.PLAIN, 8 );
		setLayout(new GridLayout(8, 1,  20,  20));
		labels = new JLabel[8];
		for (int i=0; i<labels.length; i++) {
			add(labels[i] = new JLabel("test"));
			labels[i].setFont(font);
		}
	}
	
	long lastUpdate = -1;
	long lastFrames = -1;
	private Color bg;
	
	@Override
	public void loop(long count, DmxPacket packet) {
		if (!isVisible()) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now-lastUpdate<200) {
			return;
		}		
		for (int n=0; n<8; n++) {
			int f = 64*n;
			int t = f+64;
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%3d | ", f));
			for (int i=f; i<t; i++) {
				int b = (int)(packet.data[i] & 0xff);				
				sb.append(b<1 ? "  . " : String.format("%3d ", b));
			}
			sb.append(String.format(" | %3d", t-1));
			labels[n].setText(sb.toString());
		}
		lastUpdate = now;
	}

}