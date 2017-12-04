package main;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import processing.core.PApplet;

public class Grbl {
	// references
	public PApplet							p5			= null;

	// settings
	public final int						bfrSizeMx	= 128;

	public int								bfrSize		= 0;

	// objects
	public ConcurrentLinkedQueue<String>	grblBuffer	= null;
	public ConcurrentLinkedQueue<String>	msgQueue	= null;

	public void read(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			bfrSize -= grblBuffer.poll().length();
		}
	}

	public void write(String _msg) {

	}

	public void init() {
		// TODO Auto-generated method stub

	}
}
