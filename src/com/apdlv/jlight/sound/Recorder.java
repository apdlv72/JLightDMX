package com.apdlv.jlight.sound;

import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.SwingUtilities;

public class Recorder extends Thread implements BeatDetectorInterface {
    LevelMeter meter;
	
    private volatile boolean interrupted = false;
    private volatile int retention = 8;
	private volatile float threshold = 1.5f;

    public Recorder(LevelMeter meter2) {
    	this.meter = meter2;
	}

	@Override
    public void run() {
        AudioFormat fmt = new AudioFormat(8000f, 16, 1, true, false);
        final int bufferByteSize = 1024;

        try {
            line = AudioSystem.getTargetDataLine(fmt);
            line.open(fmt, bufferByteSize);
        } catch(LineUnavailableException e) {
            System.err.println(e);
            return;
        }

        byte[] buf = new byte[bufferByteSize];
        float[] samples = new float[bufferByteSize / 2];

        float lastPeak = 0f;
        float avgRms = 0f;

        line.start();
        for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {

            // convert bytes to samples here
            for(int i = 0, s = 0; i < b;) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = volumePrescale * sample / 32768f;
            }

            float rms = 0f;
            float peak = 0f;
            for(float sample : samples) {

                float abs = Math.abs(sample);
                if(abs > peak) {
                    peak = abs;
                }

                rms += sample * sample;
            }

            rms = (float)Math.sqrt(rms / samples.length);
            avgRms = ((retention-1)*avgRms+rms)/retention;

            if (lastPeak > peak) {
                peak = lastPeak * 0.875f;
            }

            lastPeak = peak;

            setMeterOnEDT(avgRms, rms, peak);
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

	protected void setRetention(int value) {
		this.retention = value;		
		System.err.println("setRetention " + value + " -> " + retention);
	}

	public void setThreshold(int value) {
		this.threshold = 0.1f*value; 
		meter.setThreshold(this.threshold);
		System.err.println("setThreshold " + value + " -> " + threshold);
	}    	
    
    long numCalls = 0;
    long firstCall =-1;
    boolean lastComp = false;
    volatile boolean wasOverThreshold = false;
    
    void setMeterOnEDT(final float avgRms, final float rms, final float peak) {
    	
    	float t = threshold*avgRms;
    	
    	isOverThreshold = rms > t;
    	boolean isPeakOverT = peak > t;
    	
        boolean beatStart = isOverThreshold && !wasOverThreshold;
        boolean beatStop = !isOverThreshold && wasOverThreshold;

        SwingUtilities.invokeLater(new Runnable() {            	
            @Override
            public void run() {
            	wasOverThreshold = isOverThreshold;
            	if (null!=meter) {
	                meter.setAverage(avgRms);
	                meter.setAmplitude(rms);
	                meter.setPeak(peak);                    
	                meter.setBeat(beatStart);
            	}
                
                //if (beatStart || beatStop) 
                {
	                for (BeatListener l : listeners) {
	                	String info = String.format("R:%4.2f A:%4.2f T:%2.1f T:%4.2f %s %s %s %s %s", 
	                			rms, avgRms,
	                			threshold, threshold*avgRms,
	                			wasOverThreshold ? "*" : " ", 
	                			isOverThreshold ? "*" : " ", 
	                			beatStart ? "*" : " ",
	                    		beatStop ? "*" : " ",
	                    		isPeakOverT ? "*" : " " 
	                			);
	                	l.onInfo(info, isOverThreshold);                	
	                }
                }
            }
        });
    }

	public void interrupt() {
		// TODO Auto-generated method stub
		
	}

	public void addBeatListener(BeatListener l) {
		System.err.println("Recorder: adding listener " + l);
		listeners.add(l);
	}
	
	Set<BeatListener> listeners = new HashSet<>();

	private TargetDataLine line;

	private boolean isOverThreshold;

	private float volumePrescale = 1.0f;

	public void close() {
		if (null!=line) {
			line.close();
		}
	}

	@Override
	public boolean consumeBeat() {
		return isOverThreshold;
	}

	@Override
	public void setVolume(double volume) {
		this.volumePrescale = 0.01f * (float)volume;
		System.err.println("setVolume NYI " + volume + " -> " + volumePrescale);
	}

	@Override
	public void setDuration(int ms) {
		this.retention = ms;
		System.err.println("setDuration NYI " + ms + " -> " + retention);
	}

	@Override
	public void setMeter(LevelMeter meter) {
		this.meter = meter;
	}

}