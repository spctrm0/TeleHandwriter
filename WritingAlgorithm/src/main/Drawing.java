package main;

import processing.core.PApplet;
import processing.core.PGraphics;

public abstract class Drawing {
	public PGraphics pg;

	public Drawing(PApplet _p5) {
		_p5.createGraphics(_p5.width, _p5.height);
	}

	public PGraphics getPGraphics() {
		return pg;
	}

	public void clear() {
		pg.clear();
	}

	public abstract void drawing(float _px, float _py, float _x, float _y);
}
