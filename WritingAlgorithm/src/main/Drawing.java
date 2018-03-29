package main;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class Drawing {
	protected PGraphics pg;

	public Drawing(PApplet _p5) {
		pg = _p5.createGraphics(_p5.width, _p5.height);
		_p5.registerMethod("keyEvent", this);
	}

	public PGraphics getPGraphics() {
		return pg;
	}

	public abstract void drawing(float _px, float _py, float _x, float _y);

	public void keyEvent(KeyEvent _kEvt) {
		if (_kEvt.getAction() == KeyEvent.PRESS) {
			if (_kEvt.getKey() == ' ')
				pg.clear();
		}
	}
}
