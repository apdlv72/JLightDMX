package com.apdlv.jlight.sound;

import java.util.List;

import ddf.minim.AudioBuffer;

// http://marcusrossel.com/2019-11-23/beat-detector
public final class BeatDetector {

    private TimedQueue neighborLoudnesses;
    private volatile float significanceThreshold;
    
    // Additions for visualization.
    float currentLoudness;
    float averageLoudness;
    float beatLoudness;
    
    public BeatDetector(float retentionDuration, float threshold) {
        neighborLoudnesses = new TimedQueue(retentionDuration);
        significanceThreshold = threshold;
    }
    
    public boolean processBufferAvg(AudioBuffer buffer, StringBuilder sb) {
        currentLoudness = buffer.level();
        
        averageLoudness = average(neighborLoudnesses);
        beatLoudness = significanceThreshold * averageLoudness;

        neighborLoudnesses.add(currentLoudness);

        boolean rtv = currentLoudness >= beatLoudness;

        String s = String.format("cu:%3.1f av:%3.1f be:%3.1f %d", 
        		currentLoudness, averageLoudness, beatLoudness, rtv ? 1 : 0);
        if (s.length()>27) {
        	//System.err.println(s.length());
        	s = s.substring(0, 27);
        }
        sb.append(s);
        
        return rtv;
    }

    private float average(TimedQueue queue) {
        List<Float> values = queue.getValues();
        float sum = 0;

        for (float value : values) {
            sum += value;
        }

        return sum / values.size();
    }

	public void setDuration(float v) {
		neighborLoudnesses.setDuration(v);
	}

	public void setThreshold(float v) {
        significanceThreshold = v;		
	}
}
