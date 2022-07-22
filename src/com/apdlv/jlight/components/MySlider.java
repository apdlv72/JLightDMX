package com.apdlv.jlight.components;

import javax.swing.JSlider;

@SuppressWarnings("serial")
public
class MySlider extends JSlider {
	private String name;

	public MySlider(String name, int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
		this.name = name;
		setUI(new MyUi());
	}
	
	public MySlider(int min, int max, int value) {
		this("", JSlider.VERTICAL, min, max, value);
		this.name = name;
		setUI(new MyUi());
	}
	
	public String getName() {
		return name;
	}
}