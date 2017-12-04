package main;

import codeanticode.tablet.Tablet;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class TabletInput {
	// references
	public PApplet	p5					= null;
	public Tablet	tablet;
	public OscComm	oscComm				= null;

	public boolean	isWritable			= false;
	public boolean	isCalibrationMode	= false;

	public TabletInput(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("mouseEvent", this);
		tablet = new Tablet(p5);
	}

	public void setOscComm(OscComm _oscComm) {
		oscComm = _oscComm;
	}

	public void mouseEvent(MouseEvent _mEvt) {
		if (tablet.getPenKind() == Tablet.STYLUS) {
			if (isCalibrationMode) {
				if (_mEvt.getAction() == MouseEvent.PRESS) {
					Setting.myCalibX = tablet.getPenX();
					Setting.myCalibY = tablet.getPenY();
					Setting.myScreenWidth = p5.width;
					Setting.myScreenHeight = p5.height;
				} else if (_mEvt.getAction() == MouseEvent.RELEASE) {
					oscComm.sendCalibration(Setting.myCalibX, Setting.myCalibY, Setting.myTabletWidth,
							Setting.myTabletHeight, Setting.myScreenWidth, Setting.myScreenHeight);
					isCalibrationMode = false;
				}
			} else {
				if (isWritable) {
					if (_mEvt.getAction() == MouseEvent.PRESS || _mEvt.getAction() == MouseEvent.DRAG
							|| _mEvt.getAction() == MouseEvent.RELEASE) {
						oscComm.sendPenInput(_mEvt.getAction(), tablet.getPenX(), tablet.getPenY(),
								tablet.getPressure(), tablet.getTiltX(), tablet.getTiltY(), _mEvt.getMillis());
					}
				}
			}
		}
	}

	public void toggleCalibrationMode() {
		isCalibrationMode = !isCalibrationMode;
	}

	public void toggleWritable() {
		isWritable = !isWritable;
	}
}
