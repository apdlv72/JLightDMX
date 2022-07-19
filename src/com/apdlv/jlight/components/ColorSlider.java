package com.apdlv.jlight.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

@SuppressWarnings("serial")
public
class ColorSlider extends MySlider /* implements SelfMaintainedBackground*/ {

	public ColorSlider(Color color, String name, int orient, int min, int max, int val) {
		super(name, orient, min, max, val);
		BasicSliderUI ui = new coloredThumbSliderUI(this, color);
		super.setUI(ui);
	}	

	public ColorSlider(String name, int orient, int min, int max, int val) {
		super(name, orient, min, max, val);
//		BasicSliderUI ui = new coloredThumbSliderUI(this, Color.red);
//		super.setUI(ui);
	}	
	
	class coloredThumbSliderUI extends BasicSliderUI {
	 
	    Color thumbColor;
	    coloredThumbSliderUI(JSlider s, Color tColor) {
	        super(s);
	        thumbColor=tColor;
	    }
	 
	    public void paint( Graphics g, JComponent c ) {
	        recalculateIfInsetsChanged();
	        recalculateIfOrientationChanged();
	        Rectangle clip = g.getClipBounds();
	 
	        if ( slider.getPaintTrack() && clip.intersects( trackRect ) ) {
	            paintTrack( g );
	        }
	        if ( slider.getPaintTicks() && clip.intersects( tickRect ) ) {
	            paintTicks( g );
	        }
	        if ( slider.getPaintLabels() && clip.intersects( labelRect ) ) {
	            paintLabels( g );
	        }
	        if ( slider.hasFocus() && clip.intersects( focusRect ) ) {
	            paintFocus( g );      
	        }
	        if ( clip.intersects( thumbRect ) ) {
	            Color savedColor = slider.getBackground();
	            slider.setBackground(thumbColor);
	            paintThumb( g );
	            slider.setBackground(savedColor);
	        }
	    }
	}	
}