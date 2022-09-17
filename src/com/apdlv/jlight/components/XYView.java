package com.apdlv.jlight.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class XYView extends JPanel implements MouseListener, MouseMotionListener {

	private static final int X_MAX = 65535;
	private static final int Y_MAX = 65535;
	
	public static void main2(String[] args) {
		JFrame frame = new JFrame("XYTest");
				
		XYView view = new XYView(X_MAX, Y_MAX);
		
		frame.add(view);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		for (long loop=0;;loop++) {			
			view.animateTwoWheels(loop);
			sleep(1);
			view.animateInterference(loop);
			sleep(1);
		}
	}
	
	int stepX = 200;
	int stepY = 300;

	public void animateBounce(long loop) {
		
		XYView view = this;
		int[] xy = view.getXY();		
		int x = xy[0];
		int y = xy[1];
		
		x += view.stepX;
		y += view.stepY;
		if (x>X_MAX) {
			x=X_MAX;
			view.stepX = -view.stepX;			
		} else if (x<0) {
			x = 0;
			view.stepX = -view.stepX;			
		}
		
		if (y>Y_MAX) {
			y=Y_MAX;
			view.stepY = -view.stepY;			
		} else if (y<0) {
			y = 0;
			view.stepY = -view.stepY;			
		}
		
		view.setXY(x, y);
	}

	public void animateInterference(long loop) {
		XYView view = this;
		int R = X_MAX/2;
		int degOuter = (int) ((loop/2)%360);
		int degInner = (int) ((loop/1)%360);
		
		double angOuter = deg2rad(degOuter);
		double angInner = deg2rad(degInner);
		
		int x = round(R + 0.9*R*Math.cos(angOuter));
		int y = round(R + 0.9*R*Math.sin(angInner));
		
		view.setXY(x, y);
	}

	public void animateTwoWheels(long loop) {
		XYView view = this;
		int R = X_MAX/2;
		int radOuter = 30;
		int radInner = 95-radOuter;
		
		long speed = loop/4;
		int degOuter = (int) ((speed/2)%360);
		int degInner = (int) -((3*speed)%360);
		
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
		addMouseMotionListener(this);
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
		g.fillRect(0, 0, d, d);
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
		Point p = e.getPoint();				
		setPosition(p);
	}

	private void setPosition(Point p) {
		double x = p.getX();
		double y = p.getY();		
		

		Dimension s = getSize();
		double w = s.getWidth();
		double h = s.getHeight();
		double dia = Math.min(w, h);
		
		this.rx = clamp(Math.round(maxX*x/dia), 0.0, maxX); 
		this.ry = clamp(Math.round(maxY*y/dia), 0.0, maxY);
		repaint();
		
		//System.out.println("x: " + x + ", y: " + y + ", rx: " + rx + ", ry: " + ry);
	}
	
	public void setXY(int x, int y) {
		this.rx = clamp(x, 0, maxX);
		this.ry = clamp(y, 0, maxY);
		repaint();
	}

	public int[] getXY() {
		return new int [] { round(this.rx), round(this.ry) };
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

	@Override
	public void mouseDragged(MouseEvent e) {
		//System.out.println("mouseDragged: " + e);
		Point p = e.getPoint();				
		setPosition(p);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		//System.out.println("mouseMoved: " + e);
	}

	public void setIdlePosition() {
		int x = maxX/2;
		int y = maxY/2;
		if (x!=rx || y!=ry) {
			setXY(x, y);
		}
	}
}
