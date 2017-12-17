package tabletInput;

import codeanticode.tablet.Tablet;
import main.Setting;
import oscComm.OscComm;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class TabletInput {
	public PApplet	p5			= null;
	public OscComm	oscComm	= null;
	public Tablet		tablet;

	public boolean	modeWritable		= false;
	public boolean	modeCalibration	= false;

	public int strokeIdx = -1;

	public void setOscComm(OscComm _oscComm) {
		oscComm = _oscComm;
	}

	public TabletInput(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("mouseEvent", this);
		tablet = new Tablet(p5);
	}

	public void mouseEvent(MouseEvent _mEvt) {
		if (tablet.getPenKind() == Tablet.STYLUS) {
			if (modeCalibration) {
				if (_mEvt.getAction() == MouseEvent.PRESS)
					oscComm.sendCalibrationMsg(tablet.getPenX(), tablet.getPenY(), Setting.myTabletWidth, Setting.myTabletHeight,
							Setting.myScreenWidth, Setting.myScreenHeight);
				else if (_mEvt.getAction() == MouseEvent.RELEASE)
					toggleCalibration();
			}
			else {
				if (modeWritable) {
					if (_mEvt.getAction() == MouseEvent.PRESS || _mEvt.getAction() == MouseEvent.DRAG
							|| _mEvt.getAction() == MouseEvent.RELEASE) {
						boolean isHead_ = false;
						boolean isTail_ = false;
						if (_mEvt.getAction() == MouseEvent.PRESS) {
							strokeIdx++;
							isHead_ = true;
							System.out.println("tabletH" + strokeIdx);
						}
						else if (_mEvt.getAction() == MouseEvent.RELEASE) {
							isTail_ = true;
							System.out.println("tabletT" + strokeIdx);
						}
						oscComm.sendTabletInputMsg(strokeIdx, tablet.getPenX(), tablet.getPenY(), tablet.getPressure(),
								tablet.getTiltX(), tablet.getTiltY(), _mEvt.getMillis(), isHead_ ? 1 : 0, isTail_ ? 1 : 0);
					}
				}
			}
		}
	}

	public boolean toggleWritable() {
		modeWritable = !modeWritable;
		return modeWritable;
	}

	public boolean toggleCalibration() {
		modeCalibration = !modeCalibration;
		return modeCalibration;
	}
}
