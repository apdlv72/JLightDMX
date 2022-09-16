package com.apdlv.jlight.sound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.RuntimeErrorException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

// https://proteo.me.uk/2009/10/sound-level-monitoring-in-java/
public class ThresholdDetector extends Thread implements BeatDetectorInterface {

	public static void main(String[] args) throws LineUnavailableException, InterruptedException {
		LevelMeter meter = new LevelMeter(); 
		ThresholdDetector detector = new ThresholdDetector(meter);
		detector.run();
	}

	private TargetDataLine targetDataLine;

	
	// 8 kHz with 2 bytes produces 16kB/s
	private volatile int duration = 125; // ms
	private volatile int bufferSize;
	private byte [] buffer;
	
	private volatile boolean peak;

	private volatile double volumePrescale = 1.0;
	private volatile double volumeThreshold = 0.5; 
	
	private volatile boolean shuttingDown = false;


	private LevelMeter meter;


	public ThresholdDetector(LevelMeter meter) {
		this.meter = meter;
		try {
			AudioFormat audioFormat = getAudioFormat();
			targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
			targetDataLine.open();
			targetDataLine.start();
		} catch (LineUnavailableException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public final void close() {
		shuttingDown = true;
		targetDataLine.stop();		
		targetDataLine.close();
	}
	
	@Override
	public void run() {

		bufferSize = duration * 16000 / 1000;
		buffer = new byte[bufferSize];
		
		int lastDuration = -1;
		double lastVolume = 0;
		String oldInfo = null;
		while (!shuttingDown) {
			
			if (lastDuration!=duration) {
				bufferSize = duration * 16000 / 1000;
				System.out.println("buffer size changed " + bufferSize);
				buffer = new byte[bufferSize];
			}
			lastDuration = duration;
			
			int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
			short max = 0;
			if (bytesRead >=0) {
				max = (short) (buffer[0] + (buffer[1] << 8));
				for (int p=2;p<bytesRead-1;p+=2) {
					short thisValue = (short) (buffer[p] + (buffer[p+1] << 8));
					if (thisValue>max) max=thisValue;
				}
			}		  
			
			double currVolume =  (volumePrescale * max)/32768;
			//System.out.println("currVolume " + currVolume  + ", lastVolume " + lastVolume + ", volumeThreshold " + volumeThreshold);
			meter.setAmplitude((float)currVolume);
			
			if (currVolume > volumeThreshold) {
//				if (lastVolume<=volumeThreshold) 
				{
					this.peak = true;
					//System.out.println("" + System.currentTimeMillis() + ": "+ currVolume + " > " + volumeThreshold + " -> beat");
				}
			}
			
			String info = String.format("P:%3.2f D:%3.2fs T:%3.2f V:%3.2f %s", 
					volumePrescale, 0.001*duration, volumeThreshold, currVolume, peak ? "*" : " ");
			if (!info.equals(oldInfo)) {
				//System.out.println(info);
				for (BeatListener l : listeners) {
					//System.err.println("Calling listsner " + l);
					l.onInfo(info, peak);
				}
			}			

			oldInfo = info;				
			lastVolume = currVolume;
		}
	}

	private AudioFormat getAudioFormat() {
		float sampleRate = 8000.0F;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	@Override
	public boolean consumeBeat() {
		boolean p = peak;
		peak = false;
		return p;
	}

	@Override
	public void setVolume(double volume) {
		System.out.println("ThresholdDetector.setVolume " + volume);
		this.volumePrescale = 0.01*volume;		
	}

	@Override
	public void setDuration(int value) {		
		int ms = 10 * value; // 0 - 1000 ms		
		if (ms<10) {
			ms = 10;
		}
		if (ms>5000) {
			ms = 5000;
		}
		//System.out.println("ThresholdDetector.setDuration " + value + ", " + ms + " ms");
		this.duration = ms;		
	}

	@Override
	public void setThreshold(int value) {
		this.volumeThreshold = 0.01 * value;
		//System.out.println("ThresholdDetector.setThreshold " + value + ", " + volumePrescale);
	}

	@Override
	public void addBeatListener(BeatListener l) {
		listeners.add(l);
	}
	
	Set<BeatListener> listeners = new HashSet<BeatListener>();

	@Override
	public void setMeter(LevelMeter meter) {
		this.meter = meter;
	}

	@Override
	public void incSensitivity() {
		System.err.println("LevelControl.incSensitivity NYI");
	}

	@Override
	public void decSensitivity() {
		System.err.println("LevelControl.decSensitivity NYI");
	}	
}
