package tablet;

import java.util.LinkedList;

import codeanticode.tablet.Tablet;

import processing.core.PApplet;
import processing.event.MouseEvent;

public class TabletInput {

	/*
	 * TODO
	 * log function
	 */

	public LinkedList<TabletCallback> listeners = null;

	public void addListener(TabletCallback _listener) {
		if (!listeners.contains(_listener))
			listeners.addLast(_listener);
	}

	public void removeListener(TabletCallback _listener) {
		if (listeners.contains(_listener))
			listeners.remove(_listener);
	}

	public boolean containsListener(TabletCallback _listener) {
		return listeners.contains(_listener);
	}

	private PApplet p5 = null;

	private Tablet tablet = null;

	private int			bufferIdx			= 0;
	private float[]	x							= { 0, 0 };
	private float[]	y							= { 0, 0 };
	private float[]	pressure			= { 0, 0 };
	private float[]	tiltX					= { 0, 0 };
	private float[]	tiltY					= { 0, 0 };
	private long[]	evtTimeInMsec	= { 0, 0 };
	private int[]		type					= { 0, 0 };

	private LinkedList<Point> points = null;

	private int	numPoints					= 0;
	private int	numStrokes				= 0;
	private int	numPointsInStroke	= 0;

	private boolean	isActive		= false;
	private boolean	isRealtime	= false;

	public TabletInput(PApplet _p5) {
		p5 = _p5;

		tablet = new Tablet(p5);
		points = new LinkedList<Point>();

		activateTablet();
	}

	public void activateTablet() {
		boolean isChanged_ = !isActive;
		isActive = true;
		if (isChanged_) {
			init();
			p5.registerMethod("mouseEvent", this);
		}
	}

	public void deactivateTablet() {
		boolean isChanged_ = isActive;
		isActive = false;
		if (isChanged_)
			p5.unregisterMethod("mouseEvent", this);
	}

	public void mouseEvent(MouseEvent _mEvt) {
		if (tablet.getPenKind() == Tablet.STYLUS) {
			if (_mEvt.getAction() == MouseEvent.PRESS || _mEvt.getAction() == MouseEvent.DRAG
					|| _mEvt.getAction() == MouseEvent.RELEASE) {
				// Body
				int pointType_ = 1;
				// Head
				if (_mEvt.getAction() == MouseEvent.PRESS)
					pointType_ = 0;
				// Tail
				else if (_mEvt.getAction() == MouseEvent.RELEASE)
					pointType_ = 2;

				x[bufferIdx] = tablet.getPenX();
				y[bufferIdx] = tablet.getPenY();
				pressure[bufferIdx] = tablet.getPressure();
				tiltX[bufferIdx] = tablet.getTiltX();
				tiltY[bufferIdx] = tablet.getTiltY();
				evtTimeInMsec[bufferIdx] = _mEvt.getMillis();
				type[bufferIdx] = pointType_;

				if (bufferIdx == 1) {
					if (evtTimeInMsec[0] - evtTimeInMsec[1] != 0) {
						// MouseEvent.PRESS
						if (type[0] == 0) {
							numStrokes++;
							numPointsInStroke = 0;
						}
						countPoint();
						if (isRealtime)
							callback(x[0], y[0], pressure[0], tiltX[0], tiltY[0], evtTimeInMsec[0], type[0]);
						else
							points.add(new Point(x[0], y[0], pressure[0], tiltX[0], tiltY[0], evtTimeInMsec[0], type[0]));
						shiftArry();
						// MouseEvent.RELEASE
						if (type[0] == 2) {
							countPoint();
							if (isRealtime)
								callback(x[0], y[0], pressure[0], tiltX[0], tiltY[0], evtTimeInMsec[0], type[0]);
							else {
								points.add(new Point(x[0], y[0], pressure[0], tiltX[0], tiltY[0], evtTimeInMsec[0], type[0]));
								callbackAll();
							}
						}
					}
					// MouseEvent.RELEASE
					else if (type[1] == 2) {
						shiftArry();
						countPoint();
						if (isRealtime)
							callback(x[0], y[0], pressure[0], tiltX[0], tiltY[0], evtTimeInMsec[0], type[0]);
						else {
							points.add(new Point(x[0], y[0], pressure[0], tiltX[0], tiltY[0], evtTimeInMsec[0], type[0]));
							callbackAll();
						}
					}
				}
				if (type[0] == 0)
					bufferIdx = 1;
				else if (type[0] == 2)
					bufferIdx = 0;
			}
		}
	}

	private void init() {
		bufferIdx = 0;
		x[1] = 0;
		y[1] = 0;
		pressure[1] = 0;
		tiltX[1] = 0;
		tiltY[1] = 0;
		evtTimeInMsec[1] = 0;
		type[1] = 0;

		shiftArry();

		numPoints = 0;
		numStrokes = 0;
		numPointsInStroke = 0;

		points.clear();
	}

	private void shiftArry() {
		x[0] = x[1];
		y[0] = y[1];
		pressure[0] = pressure[1];
		tiltX[0] = tiltX[1];
		tiltY[0] = tiltY[1];
		evtTimeInMsec[0] = evtTimeInMsec[1];
		type[0] = type[1];
	}

	private void countPoint() {
		numPointsInStroke++;
		numPoints++;
	}

	private void callback(float _x, float _y, float _pressure, float _tiltX, float _tiltY, long _evtTimeInMsec,
			int _type) {
		for (TabletCallback listener_ : listeners)
			listener_.tabletInputCallBack(_x, _y, _pressure, _tiltX, _tiltY, _evtTimeInMsec, _type);
	}

	private void callbackAll() {
		while (!points.isEmpty()) {
			Point point_ = points.poll();
			float x_ = point_.x;
			float y_ = point_.y;
			float pressure_ = point_.pressure;
			float tiltX_ = point_.tiltX;
			float tiltY_ = point_.tiltY;
			long evtTimeInMsec_ = point_.evtTimeInMsec;
			int type_ = point_.type;
			callback(x_, y_, pressure_, tiltX_, tiltY_, evtTimeInMsec_, type_);
		}
	}
}