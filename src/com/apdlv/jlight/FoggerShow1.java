package com.apdlv.jlight;

import java.util.Random;

import com.apdlv.jlight.controls.RGBFogArray;
import com.apdlv.jlight.controls.RGBFogSliders;

public class FoggerShow1 extends Thread implements ActionThread {

	private volatile boolean terminating;
	private RGBFogArray minions;
	private int[] rainbow;

	public FoggerShow1(RGBFogArray minions) {
		this.minions = minions;
		rainbow = computeRainbow(100);
	}

	@Override
	public void terminate() {
		terminating = true;
	}

	@Override
	public void run() {
		
		for (int i=0; i<90 && !terminating; i++) {
			int seq = i/10; // 0, ...,9
			int idx = (seq%3); // 0, 1, 2, 0, 1, 2, 0, 1, 2
			RGBFogSliders m = minions.get(idx);
			int rgb = getColor(i);
			m.setFog(true);
			m.setRGB(rgb);			
			System.out.println("FoggerShow1 " + i + " " + rgb);			
			boolean proceed = delay(50);
			m.setFog(false);
			m.setRGB(0);
			if (!proceed) {
				return;
			}
		}
	}

	private int getColor(int i) {
		return rainbow[i%rainbow.length];
	}

	private boolean delay(long ms) {
		try {
			for (int i=0; i<ms; i++) {
				Thread.sleep(1);
			}
			if (terminating) {
				return false;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

    int[] computeRainbow(int count) {
    	int rainbow[] = new int[count];
        double a = 360.0 / count;
        double ar = 0;
        double ag = 120;
        double ab = 240;
        for (int i=0; i<count; i++) {
            double rr = deg2rad(i*a + ar);
            double rg = deg2rad(i*a + ag);
            double rb = deg2rad(i*a + ab);
            double sr = Math.sin(rr);
            double sg = Math.sin(rg);
            double sb = Math.sin(rb);
            int r = clamp255D(127.5 + 127.5*sr);
            int g = clamp255D(127.5 + 127.5*sg);
            int b = clamp255D(127.5 + 127.5*sb);
            rainbow[i] = (((r & 0xff))<<16) | (((g & 0xff))<<8) | (((b & 0xff)));
        }
        return rainbow;
    }
    
    double deg2rad(double deg) {
        return Math.PI*deg/180;
    }
    
    int clamp255D(double v) {
        int i = (int) Math.round(v);
        return i<0 ? 0 : i>255 ? 255 : i;
    }
    
}
