package com.apdlv.jlight.controls;

import static java.lang.System.currentTimeMillis;
import static javax.swing.SwingConstants.VERTICAL;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.dmx.DmxPacket;
import com.apdlv.jlight.sound.BeatDetectorInterface;
import com.apdlv.jlight.sound.BeatDetectorInterface.BeatListener;
import com.apdlv.jlight.sound.LevelMeter;
import com.apdlv.jlight.sound.Recorder;
import com.apdlv.jlight.sound.ThresholdDetector;
import com.apdlv.jlight.sound.processing.DamienQuartz;


@SuppressWarnings("serial")
public class SoundControl extends JPanel implements ChangeListener, DmxControlInterface, BeatListener, ActionListener {

	private static final String OFF = "Off";
	private static final String D_QUARTZ = "DQuartz";
	private static final String PEAK_AVG = "PeakAvg";
	private static final String BEAT_DETECT = "BeatDetect";
	private static final String V_THRESH = "VThresh";
	private JSlider volume;
	JSlider treshold;
	JSlider duration;
	private JLabel infoText;
	private BeatDetectorInterface thread;
	
	@SuppressWarnings("unused")
	private void test() {
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
	
	String options[] = { PEAK_AVG, V_THRESH, BEAT_DETECT, D_QUARTZ, OFF };
	private JComboBox<String> algo;
	private LevelMeter meter;
	private String mode;

	public SoundControl() {
	    
	    setLayout(new BorderLayout());
	    
	    JPanel panel = new JPanel();
	    panel.setLayout(new FlowLayout());	    
	    
	    algo = new JComboBox<>(options);
	    algo.addActionListener(this);
	    
		volume   = new MySlider("Vol", VERTICAL, 0, 150, 100);
		duration = new MySlider("Dur", VERTICAL, 0, 100, 50);
		treshold = new MySlider("Tre", VERTICAL, 0, 100, 15);
		meter = new LevelMeter();
		
		volume.addChangeListener(this);
		duration.addChangeListener(this);
		treshold.addChangeListener(this);
		
		panel.add(algo);
		JLabel vol = new JLabel("Vol");
		panel.add(new LabeledPanel(vol, volume));
		JLabel dur = new JLabel("Dur");
		panel.add(new LabeledPanel(dur, duration));
		JLabel trs = new JLabel("Thres");
		panel.add(new LabeledPanel(trs, treshold));
		panel.add(meter);
		
		infoText = new JLabel("info");
		Font font = new Font("Monospaced",Font.PLAIN,10);
		infoText.setFont(font);
		
		add(panel, BorderLayout.CENTER);
		add(infoText, BorderLayout.SOUTH);

	}

	@Override
	public Insets getInsets() {
		return new Insets(5, 5, 17, 5);
	}
	
	@Override
	public void loop(long count, DmxPacket packet) {
		
		if (null==thread) {
			
			boolean old = false;
			if (old) {			
				setMode(SoundControl.BEAT_DETECT);
			} 
			else {
				setMode(PEAK_AVG);
			}
			thread.addBeatListener(this); 
		}
		
		boolean beat = thread.consumeBeat();
		if (beat) {
			//System.out.println("loop: beat -> 511");
			packet.data[511] = (byte)0xff;
		} else {
			packet.data[511] = 0;
		}
		packet.setBeat(beat);
	}

	private void startVolumeThreshold() {
		stopAll();
		ThresholdDetector td = new ThresholdDetector(meter);
		thread = td;
		thread.addBeatListener(this);
		thread.start();
	}

	private void startBeatDetect() {
		stopAll();
		SoundThread st = new SoundThread(this, infoText, meter);
		thread = st;
		thread.addBeatListener(this);
		thread.start();
	}
	
	private void startPeakAverage() {
		stopAll();
		Recorder recorder = new Recorder(meter);
		recorder.addBeatListener(this);
		thread = recorder;		
		thread.start();
	}
	
	private void startDQuartz() {
		stopAll();
		DamienQuartz dq  = new DamienQuartz();
		dq.setMeter(meter);
		dq.addBeatListener(this);
		thread = dq;		
		thread.start();
	}

	private void stopAll() {
		if (null!=thread) {
			thread.close();
			thread.stop();
		}
		meter.setAmplitude(0);
		meter.setPeak(0);
		meter.setBeat(false);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if (src == volume) {
			thread.setVolume(volume.getValue());
		} else if (src == duration) {
			thread.setDuration(duration.getValue());
		} else if (src == treshold) {			
			thread.setThreshold(treshold.getValue());
		} 
	}

	private long now() {
		return currentTimeMillis();
	}

	@Override
	public void onInfo(String info, boolean peak) {
		infoText.setText(info);
		meter.setBeat(peak);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (algo==src) {
			String mode = (String) algo.getSelectedItem();
			setMode(mode);
		}
	}

	private void setMode(String mode) {
		this.mode = mode;
		switch (mode) {
		case V_THRESH:
			startVolumeThreshold();
			break;
		case BEAT_DETECT:
			startBeatDetect();
			break;
		case PEAK_AVG:
			startPeakAverage();
			break;
		case D_QUARTZ:
			startDQuartz();
			break;
		case OFF:
			stopAll();
			break;
		}
		algo.setSelectedItem(mode);
	}
	
	public void toggleMode() {
		switch (mode) {
		case V_THRESH:
			setMode(BEAT_DETECT);
			break;
		case SoundControl.BEAT_DETECT:
			setMode(PEAK_AVG);
			break;
		case PEAK_AVG:
			setMode(D_QUARTZ);
			break;
		case D_QUARTZ:
			setMode(OFF);
			break;
		case OFF:		
			setMode(SoundControl.V_THRESH);
			break;
		}
	}

	public void incSensitivity() {
		if (null==thread) {
			setMode(SoundControl.V_THRESH);
		}
		thread.incSensitivity();
	}

	public void decSensitivity() {
		if (null==thread) {
			setMode(SoundControl.V_THRESH);
		}
		thread.decSensitivity();
	}
}
