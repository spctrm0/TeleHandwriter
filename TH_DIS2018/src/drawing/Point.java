package drawing;

public class Point {
	private int		nthPoint;
	private int		nthStroke;
	private int		nthPointInStroke;
	private float	x;
	private float	y;
	private float	pressure;
	private float	tiltX;
	private float	tiltY;
	private long	evtTimeInMsec;
	private int		type	= 1;				// 0: Head, 1: Body, 2: Tail

	public Point(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y, float _pressure, float _tiltX,
			float _tiltY, long _evtTimeInMsec, int _type) {
		nthPoint = _nthPoint;
		nthStroke = _nthStroke;
		nthPointInStroke = _nthPointInStroke;
		x = _x;
		y = _y;
		pressure = _pressure;
		tiltX = _tiltX;
		tiltY = _tiltY;
		evtTimeInMsec = _evtTimeInMsec;
		type = _type;
	}

	public int getNthPoint() {
		return nthPoint;
	}

	public int getNthStroke() {
		return nthStroke;
	}

	public int getNthPointInStoke() {
		return nthPointInStroke;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getPressure() {
		return pressure;
	}

	public float getTiltX() {
		return tiltX;
	}

	public float getTiltY() {
		return tiltY;
	}

	public long getEvtTimeInMsec() {
		return evtTimeInMsec;
	}

	public int getType() {
		return type;
	}
}