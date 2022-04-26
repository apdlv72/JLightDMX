package com.apdlv.test;

@SuppressWarnings("serial")
class ColorSlider extends MySlider implements SelfMaintainedBackground {

	public ColorSlider(String name, int orient, int min, int max, int val) {
		super(name, orient, min, max, val);				
	}
}