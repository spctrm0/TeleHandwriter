package tabletInput;

public class TabletInputData {
	public int		strokeIdx;
	public float	penX;
	public float	penY;
	public float	pressure;
	public float	tiltX;
	public float	tiltY;
	public long		millis;
	public boolean	isHead	= false;
	public boolean	isTail	= false;

	public TabletInputData(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY,
			long _millis) {
		strokeIdx = _strokeIdx;
		penX = _penX;
		penY = _penY;
		pressure = _pressure;
		tiltX = _tiltX;
		tiltY = _tiltY;
		millis = _millis;
	}
	
	public TabletInputData(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY,
			long _millis, boolean _isHead, boolean _isTail) {
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
