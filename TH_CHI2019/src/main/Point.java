package main;

public class Point {
	
	public float	x;
	public float	y;
	public float	pressure;
	public float	tiltX;
	public float	tiltY;
	public long		evtTimeInMsec;
	public int		type;

	public Point(float _x, float _y, float _pressure, float _tiltX, float _tiltY, long _evtTimeInMsec, int _type) {
		x = _x;
		y = _y;
		pressure = _pressure;
		tiltX = _tiltX;
		tiltY = _tiltY;
		evtTimeInMsec = _evtTimeInMsec;
		type = _type;
	}
}
