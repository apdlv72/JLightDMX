package com.apdlv.jlight.sound.processing;

public class BeatDetectorUGen  {
    private static final int CHUNK_SIZE = 1024;

    //private final double[] audioBuffer;
    private double[] energyBuffer;
    private double[] deltaBuffer;
    private boolean[] beatBuffer;

    //private int audioBufferCursor;
    private int energyBufferCursor;

    private long detectTimeMillis;
    private long sensitivityTimer;

    public float sensitivity;

	float lastLevel;
    
    public BeatDetectorUGen(int frameRate) {
      sensitivity = 10;
      //audioBuffer = new double[CHUNK_SIZE];
      int bufferSize = frameRate / CHUNK_SIZE;
      energyBuffer = new double[bufferSize];
      deltaBuffer = new double[bufferSize];
      beatBuffer = new boolean[bufferSize];
    }

    // This algorithm is adapted from Damien Quartz's Minim audio library
    // http://code.compartmental.net/tools/minim/
    public boolean detect(double[] samples) {
      // compute the energy level
      float level = 0;
      for (int i = 0; i < samples.length; i++) {
        level += (samples[i] * samples[i]);
      }
      level /= samples.length;
      level = (float) Math.sqrt(level);
      
      lastLevel = level;
      
      float instant = level * 100;
      // compute the average local energy
      float E = average(energyBuffer);
      // compute the variance of the energies in eBuffer
      float V = variance(energyBuffer, E);
      // compute C using a linear digression of C with V
      float C = (-0.0025714f * V) + 1.5142857f;
      // filter negative values
      float diff = Math.max(instant - C * E, 0);
      // find the average of only the positive values in dBuffer
      float dAvg = specAverage(deltaBuffer);
      // filter negative values
      float diff2 = Math.max(diff - dAvg, 0);
      // report false if it's been less than 'sensitivity'
      // milliseconds since the last true value

      boolean beatDetected = false;

      if (detectTimeMillis - sensitivityTimer < sensitivity) {
        beatDetected = false;
      }
      // if we've made it this far then we're allowed to set a new
      // value, so set it true if it deserves to be, restart the timer
      else if (diff2 > 0 && instant > 2) {
        beatDetected = true;
        sensitivityTimer = detectTimeMillis;
      }
      // OMG it wasn't true!
      else {
        beatDetected = false;
      }
      energyBuffer[energyBufferCursor] = instant;
      deltaBuffer[energyBufferCursor] = diff;
      beatBuffer[energyBufferCursor] = beatDetected;
      energyBufferCursor++;
      if (energyBufferCursor == energyBuffer.length) {
        energyBufferCursor = 0;
      }
      // advance the current time by the number of milliseconds this buffer represents
      detectTimeMillis += (long) (((float) samples.length / getFrameRate()) * 1000);

      return beatDetected;
    }

    private float getFrameRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	private float average(double[] arr) {
      float avg = 0;
      for (int i = 0; i < arr.length; i++) {
        avg += arr[i];
      }
      avg /= arr.length;
      return avg;
    }

    private float specAverage(double[] arr) {
      float avg = 0;
      float num = 0;
      for (int i = 0; i < arr.length; i++) {
        if (arr[i] > 0) {
          avg += arr[i];
          num++;
        }
      }
      if (num > 0) {
        avg /= num;
      }
      return avg;
    }

    private float variance(double[] arr, float val) {
      float v = 0;
      for (int i = 0; i < arr.length; i++) {
        v += (float) Math.pow(arr[i] - val, 2);
      }
      v /= arr.length;
      return v;
    }

    public double[] getEnergyBuffer() {
      return energyBuffer;
    }

    public double[] getDeltaBuffer() {
      return deltaBuffer;
    }

    public boolean[] getBeatBuffer() {
      return beatBuffer;
    }

    public int getEnergyCursor() {
      return energyBufferCursor;
    }

	public void setSensitivity(int i) {
		this.sensitivity = i;
	}
  }