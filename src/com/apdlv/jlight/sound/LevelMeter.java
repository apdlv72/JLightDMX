package com.apdlv.jlight.sound;

import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static java.awt.Color.YELLOW;
import static java.lang.System.currentTimeMillis;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LevelMeter extends JPanel implements MouseListener {

	public LevelMeter() {
        setPreferredSize(new Dimension(30, 100));
		addMouseListener(this);
	}

    private int meterWidth = 30;

    private float amp = 0f;
    private float peak = 0f;
    private float threshold = 0.5f;

	private float avgRms;

	private boolean overThreshold;

    public void setAmplitude(float amp) {
        this.amp = Math.abs(amp);
        repaint();
    }

    public void setPeak(float peak) {
        this.peak = Math.abs(peak);
        repaint();
    }

    public void setMeterWidth(int meterWidth) {
        this.meterWidth = meterWidth;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = Math.min(meterWidth, getWidth());
        int h = getHeight();
        int x = getWidth() / 2 - w / 2;
        int y = 0;

        g.setColor(overThreshold ? RED :  LIGHT_GRAY);
        g.fillRect(x, y, w, h);

        g.setColor(BLACK);
        g.drawRect(x, y, w - 1, h - 1);

        int a = Math.round(amp * (h - 2));
        g.setColor(GREEN.darker().darker());
        g.fillRect(x + 1, y + h - 1 - a, w - 2, a);

        int p = Math.round(peak * (h - 2));
        g.setColor(BLUE);
        g.drawLine(x + 1, y + h - 1 - p, x + w - 1, y + h - 1 - p);

        int r = Math.round(avgRms * (h - 2));
        g.setColor(YELLOW);
        g.fillRect(x + 1, y + h - 1 - r, w-2, 3);

        float t = avgRms * threshold;
        int q = Math.round(t * (h - 2));
        g.setColor(RED);
        g.fillRect(x + 1, y + h - 1 - q, w-2, 3);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension min = super.getMinimumSize();
        if(min.width < meterWidth)
            min.width = meterWidth;
        if(min.height < meterWidth)
            min.height = meterWidth;
        return min;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        pref.width = meterWidth;
        return pref;
    }

    @Override
    public void setPreferredSize(Dimension pref) {
        super.setPreferredSize(pref);
        setMeterWidth(pref.width);
    }

    @Override
	public void mouseClicked(MouseEvent e) {
		Point p = e.getPoint();
		int h = getHeight();
		int y = (int) p.getY();
		float perc = 1.0f*(h-y)/h;
		//System.out.println("mouseClicked " + y + " of " + h + " -> " + perc);
		threshold = perc;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	public void setBeat(boolean beat) {
		this.overThreshold = beat;
	}

	protected void setAverage(float avgRms) {
		this.avgRms = avgRms;		
	}

	public boolean isOverThreshold() {
		return overThreshold;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}