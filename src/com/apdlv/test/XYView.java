package com.apdlv.test;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class XYView extends JPanel implements MouseListener {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("XYTest");
		
		
		XYView view = new XYView(65535, 65535);
		
		frame.add(view);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		for (long loop=0;;loop++) {			
			animateTwoWheels(view, loop);
			sleep(1);
			animateInterference(view, loop);
			sleep(1);
		}
	}

	public static void animateInterference(XYView view, long loop) {
		int R = 65535/2;
		int degOuter = (int) ((loop/2)%360);
		int degInner = (int) ((loop/1)%360);
		
		double angOuter = deg2rad(degOuter);
		double angInner = deg2rad(degInner);
		
		int x = round(R + 0.9*R*Math.cos(angOuter));
		int y = round(R + 0.9*R*Math.sin(angInner));
		
		view.setXY(x, y);
	}

	public static void animateTwoWheels(XYView view, long loop) {
		int R = 65535/2;
		int radOuter = 40;
		int radInner = 90-radOuter;
		
		int degOuter = (int) ((loop/2)%360);
		int degInner = (int) ((loop/1)%360);
		
		double angOuter = deg2rad(degOuter);
		double angInner = deg2rad(degInner);
		
		double yOuter = Math.sin(angOuter)*radOuter;
		double xOuter = Math.cos(angOuter)*radOuter;
		
		double yInner = Math.sin(angInner)*radInner;
		double xInner = Math.cos(angInner)*radInner;
		
		int x = round(R + R*(xOuter+xInner)/100);
		int y = round(R + R*(yOuter+yInner)/100);
		
		view.setXY(x, y);
	}

    private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	static double deg2rad(double deg) {
        return Math.PI*deg/180;
    }

	private int maxX;
	private int maxY;
	private double rx;
	private double ry;
	
	public XYView(int maxX, int maxY) {
		super();
		this.maxX = maxX;
		this.maxY = maxY;
		this.rx = maxX/2;
		this.ry = maxY/2;
		addMouseListener(this);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200,200);
	}
	
	@Override
	public void paint(Graphics g) {
		Dimension s = getSize();
		int w = (int)s.getWidth();
		int h = (int)s.getHeight();
		int d = (int)Math.min(w, h);
		
		g.setColor(getBackground());
		g.fillRoundRect(0, 0, d, d, 30, 30);
		g.setColor(getForeground());
		g.drawRect(0, 0, d-1, d-1);
		
		int mx = round(d*rx/maxX);
		int my = round(d*ry/maxY);
		
		g.fillOval(mx-5, my-5, 10, 10);
	}

	private static int round(double d) {
		return (int) Math.round(d);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Dimension s = getSize();
		double w = s.getWidth();
		double h = s.getHeight();
		double dia = Math.min(w, h);
		
		Point p = e.getPoint();
		double x = p.getX();
		double y = p.getY();		
		
		this.rx = clamp(Math.round(maxX*x/dia), 0.0, maxX); 
		this.ry = clamp(Math.round(maxY*y/dia), 0.0, maxY);
		repaint();
		
		System.out.println("x: " + x + ", y: " + y + ", rx: " + rx + ", ry: " + ry);
	}
	
	public void setXY(int x, int y) {
		this.rx = clamp(x, 0, maxX);
		this.ry = clamp(y, 0, maxY);
		repaint();
	}

	private double clamp(long v, double mn, double mx) {
		return v<mn ? mn : v>mx ? mx : v;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
