package tabletInput;

import codeanticode.tablet.Tablet;
import oscComm.OscComm;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class TabletInput {
	private PApplet	p5			= null;
	private OscComm	oscComm	= null;
	private Tablet	tablet;

	private boolean	isWritable		= false;
	private long		lastMillis		= -1;
	private int			totalPointIdx	= -1;
	private int			strokeIdx			= -1;
	private int			pointIdx			= -1;

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
			if (isWritable) {
				if (_mEvt.getAction() == MouseEvent.PRESS || _mEvt.getAction() == MouseEvent.DRAG
						|| _mEvt.getAction() == MouseEvent.RELEASE) {
					int kind_ = 1;
					if (_mEvt.getAction() == MouseEvent.PRESS) {
						strokeIdx++;
						pointIdx = -1;
						kind_ = 0;
					}
					else if (_mEvt.getAction() == MouseEvent.RELEASE)
						kind_ = 2;
					totalPointIdx++;
					pointIdx++;
					if (_mEvt.getMillis() - lastMillis != 0 || kind_ != 1)
						oscComm.sendTabletInputMsg(totalPointIdx, strokeIdx, pointIdx, tablet.getPenX(), tablet.getPenY(),
								tablet.getPressure(), tablet.getTiltX(), tablet.getTiltY(), _mEvt.getMillis(), kind_);
				}
				lastMillis = _mEvt.getMillis();
			}
		}
	}

	public void toggleWritable() {
		isWritable = !isWritable;
	}

	public boolean isWritable() {
		return isWritable;
	}

	public int getTotalPointIdx() {
		return totalPointIdx;
	}

	public int getStrokeIdx() {
		return strokeIdx;
	}

	public int getPointIdx() {
		return pointIdx;
	}
}
