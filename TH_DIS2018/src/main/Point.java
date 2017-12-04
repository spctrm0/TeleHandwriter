package main;

public class Point {
	float	penX;
	float	penY;
	float	pressure;
	float	tiltX;
	float	tiltY;
	long	millis;
	boolean	isHead	= false;
	boolean	isTail	= false;

	public Point(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		penX = _penX - Setting.targetCalibX;
		penY = _penY;
		pressure = _pressure;
		tiltX = _tiltX;
		tiltY = _tiltY;
		millis = _millis;
	}

	public Point(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis, boolean _isHead,
			boolean _isTail) {
		penX = _penX - Setting.targetCalibX;
		penY = _penY;
		pressure = _pressure;
		tiltX = _tiltX;
		tiltY = _tiltY;
		millis = _millis;
		isHead = _isHead;
		isTail = _isTail;
	}
}
