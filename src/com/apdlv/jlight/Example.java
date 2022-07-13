package com.apdlv.jlight;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Example {
	
	private static JTextArea text;
	private static JButton button1;
	private static JButton button2;


	private static ActionListener myListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			Object src = e.getSource();
			
			if (src == button1) {
				text.append("Hello ");	
			} else if (src == button2) {
				text.append("World\n");				
			}						
		}
		
	};

	public static void main(String[] args) {
		JFrame frame = new JFrame("Hello World!");
		
		button1 = new JButton("Hello");
		button2 = new JButton("World");
		
		button1.addActionListener(myListener);
		button2.addActionListener(myListener);
		
		text = new JTextArea();
		
		Container pane = frame.getContentPane();
		LayoutManager mgr = new BorderLayout();
		pane.setLayout(mgr);
		
		pane.add(button1, BorderLayout.NORTH);
		pane.add(button2, BorderLayout.SOUTH);
		pane.add(text, BorderLayout.CENTER);

		frame.pack();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setSize(600, 400);
		
	}	
}
