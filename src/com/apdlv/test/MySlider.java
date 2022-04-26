package com.apdlv.test;

import javax.swing.JSlider;

@SuppressWarnings("serial")
class MySlider extends JSlider {
	private String name;

	public MySlider(String name, int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
		this.name = name;
		setUI(new MyUi());
	}
	
	public String getName() {
		return name;
	}
}