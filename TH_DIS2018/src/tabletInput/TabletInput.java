package tabletInput;

import codeanticode.tablet.Tablet;
import oscComm.OscComm;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class TabletInput {
	private PApplet	p5			= null;
	private OscComm	oscComm	= null;
	private Tablet	tablet;

	private boolean isWritable = false;

	private int			arryIdx				= 0;
	private float[]	arryPenX			= { 0, 0 };
	private float[]	arryPenY			= { 0, 0 };
	private float[]	arryPressure	= { 0, 0 };
	private float[]	arryTiltX			= { 0, 0 };
	private float[]	arryTiltY			= { 0, 0 };
	private long[]	arryMillis		= { 0, 0 };
	private int[]		arryKind			= { 0, 0 };

	private int	totalPointIdx	= -1;
	private int	strokeIdx			= -1;
	private int	pointIdx			= -1;

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
					if (_mEvt.getAction() == MouseEvent.PRESS)
						kind_ = 0;
					else if (_mEvt.getAction() == MouseEvent.RELEASE)
						kind_ = 2;
					arryPenX[arryIdx] = tablet.getPenX();
					arryPenY[arryIdx] = tablet.getPenY();
					arryPressure[arryIdx] = tablet.getPressure();
					arryTiltX[arryIdx] = tablet.getTiltX();
					arryTiltY[arryIdx] = tablet.getTiltY();
					arryMillis[arryIdx] = _mEvt.getMillis();
					arryKind[arryIdx] = kind_;
					if (arryIdx == 1) {
						if (arryMillis[0] - arryMillis[1] != 0) {
							if (arryKind[0] == 0) {// MouseEvent.PRESS
								strokeIdx++;
								pointIdx = -1;
							}
							pointIdx++;
							totalPointIdx++;
							oscComm.sendTabletInputMsg(totalPointIdx, strokeIdx, pointIdx, arryPenX[0], arryPenY[0], arryPressure[0],
									arryTiltX[0], arryTiltY[0], arryMillis[0], arryKind[0]);
							arryPenX[0] = arryPenX[1];
							arryPenY[0] = arryPenY[1];
							arryPressure[0] = arryPressure[1];
							arryTiltX[0] = arryTiltX[1];
							arryTiltY[0] = arryTiltY[1];
							arryMillis[0] = arryMillis[1];
							arryKind[0] = arryKind[1];
							if (arryKind[0] == 2) { // MouseEvent.RELEASE
								pointIdx++;
								totalPointIdx++;
								oscComm.sendTabletInputMsg(totalPointIdx, strokeIdx, pointIdx, arryPenX[0], arryPenY[0],
										arryPressure[0], arryTiltX[0], arryTiltY[0], arryMillis[0], arryKind[0]);
							}
						}
						else if (arryKind[1] == 2) {
							arryPenX[0] = arryPenX[1];
							arryPenY[0] = arryPenY[1];
							arryPressure[0] = arryPressure[1];
							arryTiltX[0] = arryTiltX[1];
							arryTiltY[0] = arryTiltY[1];
							arryMillis[0] = arryMillis[1];
							arryKind[0] = arryKind[1];
							pointIdx++;
							totalPointIdx++;
							oscComm.sendTabletInputMsg(totalPointIdx, strokeIdx, pointIdx, arryPenX[0], arryPenY[0], arryPressure[0],
									arryTiltX[0], arryTiltY[0], arryMillis[0], arryKind[0]);
						}
					}
					if (arryKind[0] == 0)
						arryIdx = 1;
					else if (arryKind[0] == 2)
						arryIdx = 0;
				}
			}
		}
	}

	public void setWritable(boolean _isWritable) {
		isWritable = _isWritable;
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