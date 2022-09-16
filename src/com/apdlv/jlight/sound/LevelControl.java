package com.apdlv.jlight.sound;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apdlv.jlight.components.LabeledPanel;
import com.apdlv.jlight.components.MySlider;
import com.apdlv.jlight.controls.DmxControlInterface;
import com.apdlv.jlight.dmx.DmxPacket;

@SuppressWarnings("serial")
public class LevelControl extends JPanel implements BeatDetectorInterface, DmxControlInterface {

	public static void main(String[] args) {
        LevelControl control = new LevelControl();
        control.addBeatListener(new BeatListener() {
			
			@Override
			public void onInfo(String info, boolean peak) {
				System.out.println(info);
			}
		});
        
        SwingUtilities.invokeLater(new Runnable() {
			@Override
            public void run() {
				new Thread(control.recorder).start();

                JFrame frame = new JFrame("Meter");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(control);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);                                
            }
        });
    }	

	private LevelMeter meter;
	private MySlider thres;
	private MySlider reten;
	Recorder recorder;

	public LevelControl() {
		super(new BorderLayout());
        setBorder(new EmptyBorder(25, 50, 25, 50));
        meter = new LevelMeter();
        add(meter, BorderLayout.CENTER);			
        JPanel sliders = new JPanel();        LabeledPanel panel1 = new LabeledPanel(new JLabel("Thres"), thres = new MySlider("Thres", JSlider.VERTICAL, 1, 20, 10));
        LabeledPanel panel2 = new LabeledPanel(new JLabel("Reten"), reten = new MySlider("Reten", JSlider.VERTICAL, 1, 20, 15));
		sliders.add(panel1);
		sliders.add(panel2);
        add(sliders, BorderLayout.WEST);
        
        recorder = new Recorder(meter);
        
        thres.addChangeListener(new ChangeListener() {					
			@Override
			public void stateChanged(ChangeEvent e) {
				recorder.setThreshold(thres.getValue());						
			}
		});
        reten.addChangeListener(new ChangeListener() {				
			@Override
			public void stateChanged(ChangeEvent e) {
				recorder.setRetention(reten.getValue());
			}
		});                
	}

	@Override
	public boolean consumeBeat() {
		return meter.isOverThreshold();
	}

	@Override
	public void setVolume(double volume) {
	}

	@Override
	public void setDuration(int ms) {
		recorder.setRetention(ms);
	}

	@Override
	public void setThreshold(int treshold) {
		recorder.setThreshold(treshold);
	}

	@Override
	public void addBeatListener(BeatListener l) {
		recorder.addBeatListener(l);
	}

	@Override
	public void start() {
		if (null!=recorder) {
			recorder.interrupt();
			recorder.close();
			recorder = null;			
		}
		recorder = new Recorder(meter);
		new Thread(recorder).start();
	}

	@Override
	public void close() {
		recorder.interrupt();
	}

	@Override
	public void stop() {
		recorder.interrupt();
	}

	@Override
	public void interrupt() {
		recorder.interrupt();
	}

	@Override
	public void loop(long count, DmxPacket packet) {
		packet.setBeat(meter.isOverThreshold());
	}
	
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