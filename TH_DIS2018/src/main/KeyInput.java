package main;

import processing.core.PApplet;
import processing.event.KeyEvent;

public class KeyInput {
	// references
	public PApplet		p5				= null;

	// buffers
	public StringBuffer	charToStrBfr	= null;

	public KeyInput(PApplet _p5) {
		p5 = _p5;

		p5.registerMethod("keyEvent", this);

		charToStrBfr = new StringBuffer();
	}

	public void keyEvent(KeyEvent _kEvt) {
		if (_kEvt.getAction() == KeyEvent.PRESS) {
			if (_kEvt.getKeyCode() == 8) // backspace
			{
				if (charToStrBfr.length() > 0) {
					charToStrBfr.setLength(charToStrBfr.length() - 1);
				}
			} else if (_kEvt.getKeyCode() == 10) // enter
			{
				charToStrBfr.append('\r');
				System.out.print(charToStrBfr.toString());
				charToStrBfr.setLength(0);
			} else if (_kEvt.getKey() >= 32 && _kEvt.getKey() <= 126) {
				charToStrBfr.append(_kEvt.getKey());
			}
		}
	}
}
