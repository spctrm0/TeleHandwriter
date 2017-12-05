package drawing;

public class Point {
	public int		strokeIdx;
	public float	penX;
	public float	penY;
	public float	pressure;
	public float	tiltX;
	public float	tiltY;
	public long		millis;
	public boolean	isHead	= false;
	public boolean	isTail	= false;

	public Point(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		set(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis);
	}

	public Point(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis,
			boolean _isHead, boolean _isTail) {
		set(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, _isHead, _isTail);
	}

	public void set(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY,
			long _millis) {
		strokeIdx = _strokeIdx;
		penX = _penX;
		penY = _penY;
		pressure = _pressure;
		tiltX = _tiltX;
		tiltY = _tiltY;
		millis = _millis;
	}

	public void set(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis,
			boolean _isHead, boolean _isTail) {
		strokeIdx = _strokeIdx;
		penX = _penX;
		penY = _penY;
		pressure = _pressure;
		tiltX = _tiltX;
		tiltY = _tiltY;
		millis = _millis;
		isHead = _isHead;
		isTail = _isTail;
	}
}