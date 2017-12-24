public class Point {
  private int		totalPointIdx;
  private int		strokeIdx;
  private int		pointIdx;
  private float	penX;
  private float	penY;
  private float	pressure;
  private float	tiltX;
  private float	tiltY;
  private long	millis;
  private int		kind	= 1;		// 0: head, 1: body, 2: tail

  public Point(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY, float _pressure, 
    float _tiltX, float _tiltY, long _millis, int _kind) {
    set(_totalPointIdx, _strokeIdx, _pointIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, _kind);
  }

  public void set(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY, float _pressure, 
    float _tiltX, float _tiltY, long _millis, int _kind) {
    totalPointIdx = _totalPointIdx;
    strokeIdx = _strokeIdx;
    pointIdx = _pointIdx;
    penX = _penX;
    penY = _penY;
    pressure = _pressure;
    tiltX = _tiltX;
    tiltY = _tiltY;
    millis = _millis;
    kind = _kind;
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

  public float getPenX() {
    return penX;
  }

  public float getPenY() {
    return penY;
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

  public long getMillis() {
    return millis;
  }

  public int getKind() {
    return kind;
  }
}