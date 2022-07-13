package com.apdlv.jlight.controls;

import static java.lang.System.currentTimeMillis;
import static javax.swing.SwingConstants.VERTICAL;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.JButton;
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

import ddf.minim.AudioBuffer;
import ddf.minim.AudioInput;
import ddf.minim.Minim;

class SoundThread extends Thread {

	private BeatDetector detector;
	private JLabel label;
	private AudioBuffer buffer;
	
	private volatile boolean shuttingDown;
	private volatile boolean beat;

	public SoundThread(BeatDetector detector, AudioBuffer buffer, JLabel button) {
		this.detector = detector;
		this.buffer = buffer;
		this.label = button;
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
	    	label.setText(info.toString());
	    	
	    	long t1 = currentTimeMillis();
	    	long delta = t1-t0;
	    	if (delta>10) {
	    		System.err.println("SoundThread: processBuffer: " + delta + " ms");
	    	}
	    	
	    	if (isBeat && !wasBeat) {
	    		//System.out.println("" + t0 + " " + isBeat);
	    		toggle = !toggle;
	    		label.setForeground(toggle ? Color.RED : Color.GRAY);
	    		label.setEnabled(toggle);
	    		label.repaint();
	    		//System.err.println("BEAT");
	    		this.beat = true;
	    	} 
	    	wasBeat = isBeat;
	    	
//	    	try {
//				Thread.sleep(1);
//			} catch (InterruptedException e) {
//				System.err.println(e);
//			}
	    }
	    System.out.println("" + this + ": terminating");
	}

	public boolean resetBeat() {
		boolean b = beat;
		beat = false;
		return b;
	}	
}

@SuppressWarnings("serial")
public class SoundControl extends JPanel implements ChangeListener, DmxControlInterface {

	private BeatDetector detector;
	private JSlider duration;
	private JSlider threshold;
	private JLabel button;
	private AudioBuffer buffer;
	private SoundThread thread;
	
	public static void main(String[] args) {
		SoundControl test = new SoundControl();
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.add(test);		
		frame.pack();
		frame.setVisible(true);
		frame.setSize(400, 400);

		test.go();
	}

	private void go() {
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
	    
		duration  = new MySlider("Dur", VERTICAL, 0, 100, 50);
		threshold = new MySlider("Vol", VERTICAL, 0, 100, 15);
	    detector = new BeatDetector(
	    		/*retentionDuration 5.0f */ 
	    		0.1f*duration.getValue(), 
	    		/*threshold  1.5f */ 
	    		0.1f*threshold.getValue());
		
		duration.addChangeListener(this);
		threshold.addChangeListener(this);
		
		JButton vol = new JButton("Vol");
		panel.add(new LabeledPanel(vol, threshold));
		JButton dur = new JButton("Dur");
		dur.setEnabled(false);
		panel.add(new LabeledPanel(dur, duration));
		
		button = new JLabel("info");
		Font font = new Font("Monospaced",Font.PLAIN,10);
		button.setFont(font);
		
		add(panel, BorderLayout.CENTER);
		add(button, BorderLayout.SOUTH);

	    Minim minim = new Minim(this);
	    AudioInput lineIn = minim.getLineIn();	    
	    buffer = lineIn.mix;
	    
	}

	@Override
	public void loop(long count, DmxPacket packet) {
		if (null==thread) {
		    thread = new SoundThread(detector, buffer, button);
		    thread.start();
		}
		boolean beat = thread.resetBeat();
		if (beat) {
			//System.out.println("loop: beat -> 511");
			packet.data[511] = (byte)0xff;
		} else {
			packet.data[511] = 0;
		}
		packet.setBeat(beat);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if (src == duration) {
			float v = 0.1f * duration.getValue();
			System.out.println("SoundControl: duration=" + v);
			detector.setDuration(v);
		} else if (src == threshold) {
			float v = 0.1f * threshold.getValue();
			System.out.println("SoundControl: threshold=" + v);
			detector.setThreshold(v);
		}
		
	}

	private long now() {
		return currentTimeMillis();
	}
}
