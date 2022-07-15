package com.apdlv.jlight.sound;

public interface BeatDetectorInterface {

	interface BeatListener {
		void onInfo(String info, boolean peak);
	}
	
	boolean consumeBeat();
	
	void setVolume(double volume);

	void setDuration(int ms);

	void setThreshold(int treshold);
	
	void addBeatListener(BeatListener l);

	void start();

	void close();

	void stop();

	void interrupt();
}