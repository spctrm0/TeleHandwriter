package main;

import codeanticode.tablet.Tablet;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class TabletInput {
	// references
	public PApplet	p5			= null;
	public Tablet	tablet;

	public boolean	isWritable	= false;

	public TabletInput(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("mouseEvent", this);
		tablet = new Tablet(p5);
	}

	public void mouseEvent(MouseEvent _mEvt) {
		if (tablet.getPenKind() == Tablet.STYLUS) {
			if (_mEvt.getAction() == MouseEvent.PRESS || _mEvt.getAction() == MouseEvent.DRAG
					|| _mEvt.getAction() == MouseEvent.RELEASE)
				send(_mEvt.getAction(), tablet.getPenX(), tablet.getPenY(), tablet.getTiltX(), tablet.getTiltY(),
						_mEvt.getMillis());
		}
	}

	private void send(int _action, float _penX, float _penY, float _tiltX, float _tiltY, long _millis) {
		System.out.println(_action + ", " + _penX + ", " + _penY + ", " + _tiltX + ", " + _tiltY + ", " + _millis);
	}
}
