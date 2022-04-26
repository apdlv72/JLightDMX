package com.apdlv.test;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

@SuppressWarnings("serial")
class LabeledPanel extends JPanel {
	public LabeledPanel(Component label, Component content) {
		setLayout(new BorderLayout());
		add(label, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);		
	}
}