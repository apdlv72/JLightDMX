package com.apdlv.jlight.controls;

import static java.lang.System.currentTimeMillis;
import static javax.swing.SwingConstants.VERTICAL;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.dmx.DmxPacket;
import com.apdlv.jlight.sound.BeatDetectorInterface;
import com.apdlv.jlight.sound.BeatDetectorInterface.BeatListener;
import com.apdlv.jlight.sound.LevelControl;
import com.apdlv.jlight.sound.LevelMeter;
import com.apdlv.jlight.sound.Recorder;
import com.apdlv.jlight.sound.ThresholdDetector;


@SuppressWarnings("serial")
public class SoundControl extends JPanel implements ChangeListener, DmxControlInterface, BeatListener, ActionListener {

	private JSlider volume;
	JSlider treshold;
	JSlider duration;
	private JLabel infoText;
	private BeatDetectorInterface thread;
	
	@SuppressWarnings("unused")
	private void test() {
		long count = 0;
		DmxPacket packet = new DmxPacket();
		for (;;) {
			loop(count++, packet);
			boolean beat = packet.isBeat();
			if (beat) {
	    		System.out.println("go [" + now() + "]: beat");
	    		try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
			}			
		}
	}
	
	String options[] = { "Vol Tresh", "Beat Detect", "Peak Avg", "Off" };
	private JComboBox algo;
	private LevelMeter meter;

	public SoundControl() {
	    
	    setLayout(new BorderLayout());
	    
	    JPanel panel = new JPanel();
	    panel.setLayout(new FlowLayout());	    
	    
	    algo = new JComboBox<>(options);
	    algo.addActionListener(this);
	    
		volume   = new MySlider("Vol", VERTICAL, 0, 150, 100);
		duration = new MySlider("Dur", VERTICAL, 0, 100, 50);
		treshold = new MySlider("Tre", VERTICAL, 0, 100, 15);
		meter = new LevelMeter();
		
		volume.addChangeListener(this);
		duration.addChangeListener(this);
		treshold.addChangeListener(this);
		
		panel.add(algo);
		JLabel vol = new JLabel("Vol");
		panel.add(new LabeledPanel(vol, volume));
		JLabel dur = new JLabel("Dur");
		panel.add(new LabeledPanel(dur, duration));
		JLabel trs = new JLabel("Tres");
		panel.add(new LabeledPanel(trs, treshold));
		panel.add(meter);
		
		infoText = new JLabel("info");
		Font font = new Font("Monospaced",Font.PLAIN,10);
		infoText.setFont(font);
		
		add(panel, BorderLayout.CENTER);
		add(infoText, BorderLayout.SOUTH);

	}

	@Override
	public Insets getInsets() {
		return new Insets(5, 5, 17, 5);
	}
	
	@Override
	public void loop(long count, DmxPacket packet) {
		
		if (null==thread) {
			
			boolean old = false;
			if (old) {			
			    startBeatDetect();
			} 
			else {
				startVolumeThreshold();
			}
			thread.addBeatListener(this); 
		    thread.start();
		}
		
		boolean beat = thread.consumeBeat();
		if (beat) {
			//System.out.println("loop: beat -> 511");
			packet.data[511] = (byte)0xff;
		} else {
			packet.data[511] = 0;
		}
		packet.setBeat(beat);
	}

	private void startVolumeThreshold() {
		stopAll();
		ThresholdDetector td = new ThresholdDetector(meter);
		thread = td;
		thread.addBeatListener(this);
		thread.start();
	}

	private void startBeatDetect() {
		stopAll();
		SoundThread st = new SoundThread(this, infoText, meter);
		thread = st;
		thread.addBeatListener(this);
		thread.start();
	}
	
	private void startPeakAverage() {
		stopAll();
		Recorder recorder = new Recorder(meter);
		recorder.addBeatListener(this);
		thread = recorder;		
		thread.start();
	}

	private void stopAll() {
		if (null!=thread) {
			thread.close();
			thread.stop();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if (src == volume) {
			thread.setVolume(volume.getValue());
		} else if (src == duration) {
			float v = 0.1f * duration.getValue();
			thread.setDuration(duration.getValue());
		} else if (src == treshold) {			
			thread.setThreshold(treshold.getValue());
		} 
	}

	private long now() {
		return currentTimeMillis();
	}

	@Override
	public void onInfo(String info, boolean peak) {
		infoText.setText(info);
		meter.setBeat(peak);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (algo==src) {
			String name = (String) algo.getSelectedItem();
			System.err.println("Algo: " + name);
			switch (name) {
			case "Vol Tresh":
				startVolumeThreshold();
				break;
			case "Beat Detect":
				startBeatDetect();
				break;
			case "Peak Avg":
				startPeakAverage();
				break;
			case "Off":
				stopAll();
				break;
			}
		}
	}
}
