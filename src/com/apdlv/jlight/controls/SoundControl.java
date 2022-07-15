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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.dmx.DmxPacket;
import com.apdlv.jlight.sound.BeatDetector;
import com.apdlv.jlight.sound.BeatDetectorInterface;
import com.apdlv.jlight.sound.BeatDetectorInterface.BeatListener;
import com.apdlv.jlight.sound.ThresholdDetector;

import ddf.minim.AudioBuffer;
import ddf.minim.AudioInput;
import ddf.minim.Minim;


@SuppressWarnings("serial")
public class SoundControl extends JPanel implements ChangeListener, DmxControlInterface, BeatListener, ActionListener {

	class SoundThread extends Thread implements BeatDetectorInterface {

		private BeatDetector detector;

		private AudioBuffer buffer;
		
		private volatile boolean shuttingDown;
		private volatile boolean beat;

		private AudioInput lineIn;

		private Minim minim;

		public SoundThread(JLabel label) {
			minim = new Minim(this);
			lineIn = minim.getLineIn();	    
			buffer = lineIn.mix;			    
			detector = new BeatDetector(
					/*retentionDuration 5.0f */ 
					0.1f*duration.getValue(), 
					/*threshold  1.5f */ 
					0.1f*treshold.getValue());
			detector.setVolume(1.0f);
		}
		
		@Override
		public void close() {
			if (null!=lineIn) {
				lineIn.close();
			}
			shuttingDown = true;
		}
		
		@Override
		public void interrupt() {
			shuttingDown = true;
			super.interrupt();
		}
		
		@Override
		public void run() {
			
		    boolean toggle = false;
			boolean wasBeat = false;
		    while (!shuttingDown) {
			    long t0 = currentTimeMillis();
		    	//boolean isBeat = detector.processBuffer(buffer);
		    	
			    StringBuilder info = new StringBuilder();
		    	boolean isBeat = detector.processBufferAvg(buffer, info);
		    	for (BeatListener l : listeners) {
		    		l.onInfo(info.toString(), isBeat);
		    	}
		    	
		    	long t1 = currentTimeMillis();
		    	long delta = t1-t0;
		    	if (delta>10) {
		    		System.err.println("SoundThread: processBuffer: " + delta + " ms");
		    	}
		    	
		    	if (isBeat && !wasBeat) {
		    		//System.out.println("" + t0 + " " + isBeat);
		    		toggle = !toggle;
//		    		label.setForeground(toggle ? Color.RED : Color.GRAY);
//		    		label.setEnabled(toggle);
//		    		label.repaint();
		    		//System.err.println("BEAT");
		    		this.beat = true;
		    	} 
		    	wasBeat = isBeat;
		    	
//		    	try {
//					Thread.sleep(1);
//				} catch (InterruptedException e) {
//					System.err.println(e);
//				}
		    }
		    System.out.println("" + this + ": terminating");
		}

		@Override
		public boolean consumeBeat() {
			boolean b = beat;
			beat = false;
			return b;
		}

		@Override
		public void setVolume(double volume) {
			float v = (float) (0.01f * volume);
			System.out.println("SoundThread: setVolume=" + volume + " -> " + v);
			detector.setVolume(v);		
		}	

		@Override
		public void setDuration(int duration) {
			float v = 0.1f * duration;
			System.out.println("SoundThread: setDuration=" + duration + " -> " + v);
			detector.setDuration(v);
		}

		@Override
		public void setThreshold(int treshold) {
			float v = (float) (0.1f * treshold);
			System.out.println("SoundThread: setThreshold=" + treshold + " -> " + v);
			detector.setThreshold(v);
		}

		@Override
		public void addBeatListener(BeatListener l) {
			listeners.add(l);
		}
		
		Set<BeatListener> listeners = new HashSet<BeatListener>();
	}

	private JCheckBox old;
	private JSlider volume;
	private JSlider treshold;
	private JSlider duration;
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

	public SoundControl() {
	    
	    setLayout(new BorderLayout());
	    
	    JPanel panel = new JPanel();
	    panel.setLayout(new FlowLayout());	    
	    
	    old = new JCheckBox("old");
	    old.addActionListener(this);
		volume   = new MySlider("Vol", VERTICAL, 0, 100, 100);
		duration = new MySlider("Dur", VERTICAL, 0, 100, 50);
		treshold = new MySlider("Tre", VERTICAL, 0, 100, 15);
		
		volume.addChangeListener(this);
		duration.addChangeListener(this);
		treshold.addChangeListener(this);
		
		panel.add(old);
		JLabel vol = new JLabel("Vol");
		panel.add(new LabeledPanel(vol, volume));
		JLabel dur = new JLabel("Dur");
		panel.add(new LabeledPanel(dur, duration));
		JLabel trs = new JLabel("Tres");
		panel.add(new LabeledPanel(trs, treshold));
		
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
			    startOldImplem();
			} 
			else {
				startNewImplem();
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

	private void startNewImplem() {
		if (null!=thread) {
			thread.close();
			thread.stop();
		}
		thread = new ThresholdDetector();
		thread.addBeatListener(this);
		thread.start();
	}

	private void startOldImplem() {
		if (null!=thread) {
			thread.close();
			thread.interrupt();
			thread.stop();
		}

		thread = new SoundThread(infoText);
		thread.addBeatListener(this);
		thread.start();
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

	boolean toggle = false;
	
	@Override
	public void onInfo(String info, boolean peak) {
		//System.out.println("onInfo: " + currentTimeMillis() + " " + info);
		infoText.setText(info);
		if (peak) {
			toggle = !toggle;
			//System.out.println("toggle: " + toggle);
		} 		
		if (toggle) {
			this.setBackground(peak ? Color.RED : Color.BLACK);
		};						
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src==old) {		
			if (old.isSelected()) {
				startOldImplem();
			} else {
				startNewImplem();
			}
		}			
	}
}
