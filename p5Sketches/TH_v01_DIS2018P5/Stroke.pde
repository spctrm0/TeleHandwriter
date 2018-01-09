public class Stroke {
	private ArrayList<Point> points = null;

	private boolean isCompleted = false;

	public Stroke(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY, float _pressure,
			float _tiltX, float _tiltY, long _millis, int _kind) {
		points = new ArrayList<Point>();
		points
				.add(new Point(_totalPointIdx, _strokeIdx, _pointIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, _kind));
	}

	public void addPoint(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY, float _pressure,
			float _tiltX, float _tiltY, long _millis, int _kind) {
		points
				.add(new Point(_totalPointIdx, _strokeIdx, _pointIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, _kind));
		if (_kind == 2)
			isCompleted = true;
	}

	public ArrayList<Point> getPoints() {
		return points;
	}

	public int getPointsNum() {
		return points.size();
	}

	public Point getFirstPoint() {
		return points.get(0);
	}

	public Point getSecondPoint() {
		return points.get(1);
	}

	public Point getLastPoint() {
		return points.get(points.size() - 1);
	}

	public void removeFirstPoint() {
		points.remove(0);
	}

	public boolean isCompleted() {
		return isCompleted;
	}
}
