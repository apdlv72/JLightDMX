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

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class AutoPilot extends JPanel implements DmxControlInterface {
	
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
		this.thres = new JSlider(JSlider.VERTICAL, 0,  9, 0);
		this.wait  = new JSlider(JSlider.VERTICAL, 1, 10, 5);
		LabeledPanel panel1 = new LabeledPanel(new JLabel("Auto"), thres);
		LabeledPanel panel2 = new LabeledPanel(new JLabel("Wait"), wait);
		add(panel1);
		add(panel2);
		JPanel panels = new JPanel();
		panels.add(panel1);
		panels.add(panel2);
		
//		JPanel buttons = new JPanel(new GridLayout(8, 1));
//		buttons.add(master  = new JCheckBox("Master", true));
//		buttons.add(chase   = new JCheckBox("Chase", true));
////		buttons.add(sound   = new JCheckBox("Sound"));
//		buttons.add(reverse = new JCheckBox("Reverse", true));
//		buttons.add(random  = new JCheckBox("Random", true));
//		buttons.add(fade    = new JCheckBox("Fade", true));		
//		buttons.add(time = new JLabel("----------"));
		
		add(panels, BorderLayout.CENTER);
		Font font = new Font("Monospaced",Font.PLAIN,10);
		info = new JLabel("----------");
		info.setFont(font);
		add(info, BorderLayout.SOUTH);
	}

	@Override
	public Insets getInsets() {
		return new Insets(5, 2, 17, 2);
	}
		
	boolean toggle = false;
	long lastToggle = System.currentTimeMillis();
	private String effect;	
	
	@Override
	public void loop(long count, DmxPacket packet) {
		try { loop2(count, packet); }
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void loop2(long count, DmxPacket packet) {
		
		long delta = (System.currentTimeMillis()-lastToggle)/1000;
		//time.setText(String.format("%4d", delta));
		
		int thres = this.thres.getValue();
		if (thres<1) {
			info.setText("----");
			return;
		}

		int wait = this.wait.getValue();
		boolean beat = packet.isBeat();
		long now = System.currentTimeMillis();
		if (beat) {
			lastBeat = now;
		}
		long delta1 = lastBeat<0 ? 0 : now-lastBeat;		
		long delta2 = now-lastToggle;		
		int millis = wait*1000;
		//System.out.println("AutoPilot: delta1=" + delta1 + ", delta2=" + delta2 + ", millis=" + millis);
		if (delta1<millis || delta2<millis) {
			return;
		}
		
		int dice = rand.nextInt(10);
		//System.out.println("AutoPilot: wait=" + wait+ ", thres=" + thres + ", dice=" + dice);
		if (dice<thres) {
			
			int event = rand.nextInt(10);
			toggle = !toggle;
			lastToggle = now;
			effect = "?";
			switch (event) {
			case 0:
			case 1:
			case 2:
				spots.setRandomColor();
				spots.setMaster(true);
				spots.setChase(false);
				spots.setSound(true);
				spots.setRandom(false);
				spots.setFade(true);
				effect = "MusicRGB";
				break;
			case 3:
			case 4:
			case 5:
				spots.setRandomColor();
				spots.setMaster(true);
				spots.setChase(false);
				spots.setSound(true);
				spots.setRandom(true);
				spots.setFade(false);
				effect = "MusicRGBW";
				break;
			case 6:
				spots.setMaster(false);
				spots.setChase(true);
				spots.setSound(false);
				spots.setReverse(rand.nextBoolean());
				spots.setRandom(true);
				spots.setFade(false);
				effect = "RandChase";
				break;
			case 7:
				spots.setMaster(false);
				spots.setChase(true);
				spots.setSound(false);
				spots.setReverse(rand.nextBoolean());
				spots.setRandom(true);
				spots.setFade(true);
				effect = "RandFade";
				break;
			case 8:
				spots.setRandomColor();
				spots.setMaster(true);
				spots.setChase(true);
				spots.setSound(true);
				spots.setRandom(true);
				spots.setFade(false);
				effect = "MusicChase";
				break;
			case 9:
				spots.setRandomColor();
				spots.setMaster(true);
				spots.setChase(true);
				spots.setSound(true);
				spots.setRandom(true);
				spots.setFade(true);
				effect = "MusicFade";
				break;
			default:
				toggle = !toggle;
				break;
			}
			
			info.setText(String.format("%-10s %3d", effect, delta));			
			//setBackground(toggle ? Color.BLACK : Color.GRAY.darker().darker().darker());
		}		
	}

}
