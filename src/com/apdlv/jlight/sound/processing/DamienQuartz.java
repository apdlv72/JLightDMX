package com.apdlv.jlight.sound.processing;

import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.apdlv.jlight.sound.BeatDetectorInterface;
import com.apdlv.jlight.sound.LevelMeter;

public class DamienQuartz extends Thread implements BeatDetectorInterface {

	private float volumePrescale = 1.0f;
	static int sample_rate = 44100;
	private BeatDetectorUGen detector;

	public DamienQuartz() {
		detector = new BeatDetectorUGen(sample_rate);
		detector.setSensitivity(10);		
	}

	// https://mvnrepository.com/artifact/com.jsyn/jsyn/20170815
	public static void main(String[] args) {
		DamienQuartz dq = new DamienQuartz();
		dq.addBeatListener(new BeatListener() {			
			@Override
			public void onInfo(String info, boolean peak) {
				System.err.println(info + " " + peak);				
			}
		});
		dq.start();
	}
	
    public void run() {
    	System.out.println("starting");
        AudioFormat fmt = new AudioFormat(sample_rate, 16, 1, true, false);
        final int bufferByteSize = 1024;

        TargetDataLine line;
		try {
            line = AudioSystem.getTargetDataLine(fmt);
            line.open(fmt, bufferByteSize);
        } catch(LineUnavailableException e) {
            System.err.println(e);
            return;
        }

        byte[] buf = new byte[bufferByteSize];
        double[] samples = new double[bufferByteSize / 2];

        float lastPeak = 0f;
        float avgRms = 0f;

        line.start();
        boolean wasBeat = false;
        for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {

            // convert bytes to samples here
            for(int i = 0, s = 0; i < b;) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = volumePrescale  * sample / 32768f;
            }
            
            beat = detector.detect(samples);
            
            if (null!=meter) {
            	meter.setAmplitude(detector.lastLevel);
            }
            
           // System.err.printf("beat: %s\n", beat ? "*" : "");
            if (beat != wasBeat) {
	            for (BeatListener l : listeners) {
	            	String info = String.format("V:%2.1f S:%2.1f E:%3d %s", 
	            			volumePrescale, detector.sensitivity,
	            			detector.getEnergyCursor(),
	            			beat ? "*" : " ");
	            	l.onInfo(info, beat);
	            }
            }            
            wasBeat = beat;
            
            if (interrupted) {
            	break;
            }
        }
        
        if (null!=line) {
	        line.stop();
	        line.close();
	        line = null;
        }
    }

	private LevelMeter meter;
	private boolean interrupted;
	private Set<BeatListener> listeners = new HashSet<>();
			
	private static boolean last;
	private static boolean beat;

	@Override
	public boolean consumeBeat() {
		if (!last && beat) {
			return true;
		}
		last = beat;
		return false;
	}

	@Override
	public void setVolume(double volume) {
		this.volumePrescale = (float)volume/100;
	}

	@Override
	public void setDuration(int ms) {
		detector.setSensitivity(ms/10);
	}

	@Override
	public void setThreshold(int treshold) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void addBeatListener(BeatListener l) {
		listeners.add(l);		
	}

	@Override
	public void close() {
		interrupt();		
	}

	@Override
	public void interrupt() {
		this.interrupted = true;
	}

	@Override
	public void setMeter(LevelMeter meter) {
		this.meter = meter;
	}
	
}
