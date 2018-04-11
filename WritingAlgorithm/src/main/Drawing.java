package main;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;

public abstract class Drawing {
	protected PGraphics pg;

	public Drawing(PApplet _p5, int w, int h) {
		pg = _p5.createGraphics(w, h);
//		pg.beginDraw();
//		pg.background(255);
//		pg.endDraw();
	}

	public PGraphics getPGraphics() {
		return pg;
	}

	public abstract void drawing(float _px, float _py, float _x, float _y);
}
