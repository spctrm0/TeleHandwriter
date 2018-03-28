package main;

import java.util.ArrayList;

import codeanticode.tablet.Tablet;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class TabletInput extends Drawing {
	private PApplet	p5	= null;
	private Tablet	tablet;

	private boolean isWritable = false;

	private int			arryIdx							= 0;
	private float[]	pointX							= { 0, 0 };
	private float[]	pointY							= { 0, 0 };
	private float[]	pointPressure				= { 0, 0 };
	private float[]	pointTiltX					= { 0, 0 };
	private float[]	pointTiltY					= { 0, 0 };
	private long[]	pointEvtTimeInMsec	= { 0, 0 };
	private int[]		pointType						= { 0, 0 };
	private int			nthPoint						= 0;
	private int			nthStroke						= 0;
	private int			nthPointInStroke		= 0;

	public interface ReceivingPoint {
		public void receivePoint(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y, float _pressure,
				float _tiltX, float _tiltY, long _evtTimeInMsec, int _type);
	}

	private ArrayList<ReceivingPoint>	receivingPointObjs;
	private int												receivingPointObjIdx	= 0;

	public ArrayList<ReceivingPoint> getReceivingPointObjs() {
		return receivingPointObjs;
	}

	public ReceivingPoint getReceivingPointObj() {
		return receivingPointObjs.get(receivingPointObjIdx);
	}

	public void addReceivingPointObj(ReceivingPoint _receivingPointObj) {
		if (!receivingPointObjs.contains(_receivingPointObj))
			receivingPointObjs.add(_receivingPointObj);
	}

	public int getReceivingPointObjIdx() {
		return receivingPointObjIdx;
	}

	public void setReceivingPointObjIdx(int _receivingPointObjIdx) {
		receivingPointObjIdx = _receivingPointObjIdx;
		receivingPointObjIdx = receivingPointObjIdx < 0 ? 0
				: receivingPointObjIdx >= receivingPointObjs.size() ? receivingPointObjs.size() - 1 : receivingPointObjIdx;
	}

	public TabletInput(PApplet _p5) {
		super(_p5);
		p5 = _p5;
		p5.registerMethod("mouseEvent", this);
		tablet = new Tablet(p5);
		receivingPointObjs = new ArrayList<ReceivingPoint>();
	}

	public void mouseEvent(MouseEvent _mEvt) {
		if (tablet.getPenKind() == Tablet.STYLUS) {
			if (isWritable) {
				if (_mEvt.getAction() == MouseEvent.PRESS || _mEvt.getAction() == MouseEvent.DRAG
						|| _mEvt.getAction() == MouseEvent.RELEASE) {
					int pointType_ = 1; // Body
					switch (_mEvt.getAction()) {
						case MouseEvent.PRESS:
							pointType_ = 0;
							break;
						case MouseEvent.RELEASE:
							pointType_ = 1;
							break;
					}
					pointX[arryIdx] = tablet.getPenX();
					pointY[arryIdx] = tablet.getPenY();
					pointPressure[arryIdx] = tablet.getPressure();
					pointTiltX[arryIdx] = tablet.getTiltX();
					pointTiltY[arryIdx] = tablet.getTiltY();
					pointEvtTimeInMsec[arryIdx] = _mEvt.getMillis();
					pointType[arryIdx] = pointType_;
					if (arryIdx == 1) {
						if (pointEvtTimeInMsec[0] - pointEvtTimeInMsec[1] != 0) {
							if (pointType[0] == 0) { // MouseEvent.PRESS
								nthStroke++;
								nthPointInStroke = 0;
							}
							countPoint();
							SendPoint();
							shiftArry();
							if (pointType[0] == 2) { // MouseEvent.RELEASE
								countPoint();
								SendPoint();
							}
						}
						else if (pointType[1] == 2) { // MouseEvent.RELEASE
							shiftArry();
							countPoint();
							SendPoint();
						}
					}
					if (pointType[0] == 0)
						arryIdx = 1;
					else if (pointType[0] == 2)
						arryIdx = 0;
				}
			}
		}
	}

	private void countPoint() {
		nthPointInStroke++;
		nthPoint++;
	}

	private void SendPoint() {
		getReceivingPointObj().receivePoint(nthPoint, nthStroke, nthPointInStroke, pointX[0], pointY[0], pointPressure[0],
				pointTiltX[0], pointTiltY[0], pointEvtTimeInMsec[0], pointType[0]);
	}

	private void shiftArry() {
		pointX[0] = pointX[1];
		pointY[0] = pointY[1];
		pointPressure[0] = pointPressure[1];
		pointTiltX[0] = pointTiltX[1];
		pointTiltY[0] = pointTiltY[1];
		pointEvtTimeInMsec[0] = pointEvtTimeInMsec[1];
		pointType[0] = pointType[1];
	}

	public boolean isWritable() {
		return isWritable;
	}

	public void setWritable(boolean _isWritable) {
		isWritable = _isWritable;
	}

	@Override
	public void drawing(float _px, float _py, float _x, float _y) {
		pg.beginDraw();
		pg.noStroke();
		pg.fill(255, 0, 0);
		pg.ellipse(_px, _py, 3, 3);
		pg.ellipse(_x, _y, 3, 3);
		pg.noFill();
		pg.stroke(255, 0, 0);
		pg.line(_px, _py, _x, _y);
		pg.endDraw();
	}
}