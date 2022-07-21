package com.apdlv.jlight.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Random;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class AutoPilot extends JPanel implements DmxControlInterface, ChangeListener {
	
	private RGBWSpotArray spots;
	private Random rand;
	private JSlider thres;
	private JSlider wait;
	private long lastBeat = -1;
	private JLabel info;
	private JLabel time;
	private JCheckBox master;
	private JCheckBox chase;
	private JCheckBox sound;
	private JCheckBox reverse;
	private JCheckBox random;
	private JCheckBox fade;

	public AutoPilot(RGBWSpotArray spots) {
		super(new BorderLayout());
		this.spots = spots;
		this.rand = new Random();
		this.thres = new JSlider(JSlider.VERTICAL, 0,  100, 0);
		//this.wait  = new JSlider(JSlider.VERTICAL, 1, 10, 5);
		LabeledPanel panel1 = new LabeledPanel(new JLabel("Auto"), thres);
		thres.addChangeListener(this);
		//LabeledPanel panel2 = new LabeledPanel(new JLabel("Wait"), wait);
		add(panel1);
		//add(panel2);
//		JPanel panels = new JPanel();
//		panels.add(panel1);
		//panels.add(panel2);
		
//		JPanel buttons = new JPanel(new GridLayout(8, 1));
//		buttons.add(master  = new JCheckBox("Master", true));
//		buttons.add(chase   = new JCheckBox("Chase", true));
////		buttons.add(sound   = new JCheckBox("Sound"));
//		buttons.add(reverse = new JCheckBox("Reverse", true));
//		buttons.add(random  = new JCheckBox("Random", true));
//		buttons.add(fade    = new JCheckBox("Fade", true));		
//		buttons.add(time = new JLabel("----------"));
		
		add(panel1, BorderLayout.CENTER);
		Font font = new Font("Monospaced",Font.PLAIN,10);
		info = new JLabel("----------");
		info.setFont(font);
		info.setText(String.format("%-10s %3d", "?", 0));			
		add(info, BorderLayout.SOUTH);
	}

	@Override
	public Insets getInsets() {
		return new Insets(8, 4, 24, 4);
	}
		
	boolean toggle = false;
	long lastToggle = System.currentTimeMillis();
	private String effect = "-";	
	
	int countdown = -1;
	private int lastEvent;
	
	public void loop(long count, DmxPacket packet) {
		
		int thres = this.thres.getValue();
		if (thres<1) {
			info.setText(String.format("%-10s %3d", "Off", 0));			
			return;
		}		
		info.setText(String.format("%-10s %3d", effect, countdown));			
		if (packet.isBeat()) {
			countdown--;
		}
		if (countdown>0) {
			return;
		}
		Random r = new Random();

		System.err.println("countdown");
		int event = rand.nextInt(8);
		while (event==this.lastEvent) {
			event = rand.nextInt(8);
		}		
		this.lastEvent = event;
		
		toggle = !toggle;
		effect = "?";
		switch (event) {
		case 0:
		case 1:
			spots.setRandomColor();
			spots.setMaster(true);
			spots.setChase(false);
			spots.setSound(true);
			spots.setRandom(false);
			spots.setFade(true);
			effect = "MusicRGB";
			countdown = 100 * (thres + r.nextInt(1+thres/2));
			break;
		case 2:
		case 3:
			spots.setRandomColor();
			spots.setMaster(true);
			spots.setChase(false);
			spots.setSound(true);
			spots.setRandom(true);
			spots.setFade(false);
			effect = "MusicRGBW";
			countdown = 100 * (thres + r.nextInt(1+thres/2));
			break;
		case 4:
			spots.setMaster(false);
			spots.setChase(true);
			spots.setSound(false);
			spots.setReverse(rand.nextBoolean());
			spots.setRandom(true);
			spots.setFade(false);
			effect = "RandChase";
			countdown = 100 * (thres/4 + r.nextInt(1+thres/8));
			break;
		case 5:
			spots.setMaster(false);
			spots.setChase(true);
			spots.setSound(false);
			spots.setReverse(rand.nextBoolean());
			spots.setRandom(true);
			spots.setFade(true);
			effect = "RandFade";
			countdown = 100 * (thres/4 + r.nextInt(1+thres/8));
			break;
		case 6:
			spots.setRandomColor();
			spots.setMaster(true);
			spots.setChase(true);
			spots.setSound(true);
			spots.setRandom(true);
			spots.setFade(false);
			effect = "MusicChase";
			countdown = 100 * (thres/4 + r.nextInt(1+thres/8));
			break;
		case 7:
			spots.setRandomColor();
			spots.setMaster(true);
			spots.setChase(true);
			spots.setSound(true);
			spots.setRandom(true);
			spots.setFade(true);
			effect = "MusicFade";
			countdown = 100 * (thres/4 + r.nextInt(1+thres/8));
			break;
		default:
			countdown = 100 * (thres/4 + r.nextInt(1+thres/8));
			break;
		}		
//		System.err.println("effect: " + effect);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		int value = thres.getValue();
		this.countdown = 100*value;
	}

}
