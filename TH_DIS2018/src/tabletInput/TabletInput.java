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

	private int			pointArryIdx						= 0;
	private float[]	pointArryX							= { 0, 0 };
	private float[]	pointArryY							= { 0, 0 };
	private float[]	pointArryPressure				= { 0, 0 };
	private float[]	pointArryTiltX					= { 0, 0 };
	private float[]	pointArryTiltY					= { 0, 0 };
	private long[]	pointArryEvtTimeInMsec	= { 0, 0 };
	private int[]		pointArryType						= { 0, 0 };

	private int	nthPoint					= 0;
	private int	nthStroke					= 0;
	private int	nthPointInStroke	= 0;

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
					int pointType_ = 1; // Body
					if (_mEvt.getAction() == MouseEvent.PRESS)
						pointType_ = 0; // Head
					else if (_mEvt.getAction() == MouseEvent.RELEASE)
						pointType_ = 2; // Tail
					pointArryX[pointArryIdx] = tablet.getPenX();
					pointArryY[pointArryIdx] = tablet.getPenY();
					pointArryPressure[pointArryIdx] = tablet.getPressure();
					pointArryTiltX[pointArryIdx] = tablet.getTiltX();
					pointArryTiltY[pointArryIdx] = tablet.getTiltY();
					pointArryEvtTimeInMsec[pointArryIdx] = _mEvt.getMillis();
					pointArryType[pointArryIdx] = pointType_;
					if (pointArryIdx == 1) { // Event triggered more then a time aeswfaw vrqwerevwqwrrvwvrqavrvrwrvvrwqrwvqeavwqarvawvq3rva3w
						if (pointArryEvtTimeInMsec[0] - pointArryEvtTimeInMsec[1] != 0) {
							if (pointArryType[0] == 0) { // MouseEvent.PRESS
								nthStroke++;
								nthPointInStroke = 0;
							}
							countPointAndSendFirstArryDataAsStylusInput();
							pollArry();
							if (pointArryType[0] == 2) { // MouseEvent.RELEASE
								countPointAndSendFirstArryDataAsStylusInput();
							}
						}
						else if (pointArryType[1] == 2) { // MouseEvent.RELEASE
							pollArry();
							countPointAndSendFirstArryDataAsStylusInput();
						}
					}
					if (pointArryType[0] == 0)
						pointArryIdx = 1;
					else if (pointArryType[0] == 2)
						pointArryIdx = 0;
				}
			}
		}
	}

	private void countPointAndSendFirstArryDataAsStylusInput() {
		nthPointInStroke++;
		nthPoint++;
		oscComm.sendStylusInputMsg(nthPoint, nthStroke, nthPointInStroke, pointArryX[0], pointArryY[0],
				pointArryPressure[0], pointArryTiltX[0], pointArryTiltY[0], pointArryEvtTimeInMsec[0], pointArryType[0]);
	}

	private void pollArry() {
		pointArryX[0] = pointArryX[1];
		pointArryY[0] = pointArryY[1];
		pointArryPressure[0] = pointArryPressure[1];
		pointArryTiltX[0] = pointArryTiltX[1];
		pointArryTiltY[0] = pointArryTiltY[1];
		pointArryEvtTimeInMsec[0] = pointArryEvtTimeInMsec[1];
		pointArryType[0] = pointArryType[1];
	}

	public boolean isWritable() {
		return isWritable;
	}

	public void setWritable(boolean _isWritable) {
		isWritable = _isWritable;
	}
}