package com.apdlv.jlight.controls;

import static java.lang.System.currentTimeMillis;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;

import com.apdlv.jlight.sound.BeatDetector;
import com.apdlv.jlight.sound.BeatDetectorInterface;
import com.apdlv.jlight.sound.BeatDetectorInterface.BeatListener;
import com.apdlv.jlight.sound.LevelMeter;

import ddf.minim.AudioBuffer;
import ddf.minim.AudioInput;
import ddf.minim.Minim;

class SoundThread extends Thread implements BeatDetectorInterface {

		/**
		 * 
		 */
		private final SoundControl soundControl;

		private BeatDetector detector;

		private AudioBuffer buffer;
		
		private volatile boolean shuttingDown;
		private volatile boolean beat;

		private AudioInput lineIn;

		private Minim minim;

		public SoundThread(SoundControl soundControl, JLabel label, LevelMeter meter) {
			this.soundControl = soundControl;
			minim = new Minim(this);
			lineIn = minim.getLineIn();	    
			buffer = lineIn.mix;			    
			detector = new BeatDetector(
					/*retentionDuration 5.0f */ 
					0.1f*this.soundControl.duration.getValue(), 
					/*threshold  1.5f */ 
					0.1f*this.soundControl.treshold.getValue());
			detector.setVolume(1.0f);
			detector.setMeter(meter);
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