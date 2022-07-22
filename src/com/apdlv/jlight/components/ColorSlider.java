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
		BasicSliderUI ui = new ColoredThumbSliderUI(this, color);
		super.setUI(ui);
	}	

	public ColorSlider(Color color, int min, int max, int val) {
		this(color, "", JSlider.VERTICAL, min, max, val);
	}	


	public ColorSlider(String name, int orient, int min, int max, int val) {
		this(Color.BLACK, name, orient, min, max, val);
	}	
	
	class ColoredThumbSliderUI extends BasicSliderUI {
	 
	    Color thumbColor;
	    ColoredThumbSliderUI(JSlider s, Color tColor) {
	        super(s);
	        thumbColor=tColor;
	    }
	    
		protected void scrollDueToClickInTrack(int direction) {
			int value = slider.getValue();

			if (slider.getOrientation() == JSlider.HORIZONTAL) {
				value = this.valueForXPosition(slider.getMousePosition().x);
			} else if (slider.getOrientation() == JSlider.VERTICAL) {
				value = this.valueForYPosition(slider.getMousePosition().y);
			}
			slider.setValue(value);
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