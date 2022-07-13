package com.apdlv.jlight.components;

import javax.swing.JSlider;
import javax.swing.plaf.metal.MetalSliderUI;

public class MyUi extends MetalSliderUI {
	protected void scrollDueToClickInTrack(int direction) {
		int value = slider.getValue();

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			value = this.valueForXPosition(slider.getMousePosition().x);
		} else if (slider.getOrientation() == JSlider.VERTICAL) {
			value = this.valueForYPosition(slider.getMousePosition().y);
		}
		slider.setValue(value);
	}
}