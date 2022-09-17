package com.apdlv.jlight;

import com.apdlv.jlight.controls.RGBFogArray;

public class FoggerShow3 extends Thread implements ActionThread {

	private volatile boolean terminating;
	private RGBFogArray minions;

	public FoggerShow3(RGBFogArray minions) {
		this.minions = minions;
	}

	@Override
	public void terminate() {
		terminating = true;
	}

	@Override
	public void run() {
		for (int i=0; i<10 && !terminating; i++) {
			System.out.println("FoggerShow3 " + i);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
