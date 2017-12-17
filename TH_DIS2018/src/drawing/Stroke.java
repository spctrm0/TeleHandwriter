package drawing;

import java.util.ArrayList;

public class Stroke {
	public ArrayList<Point>	points		= null;

	public boolean		isCompleted	= false;

	public Stroke(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		points = new ArrayList<Point>();
		points.add(new Point(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, true, false));
	}

	public void addPoint(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY,
			long _millis, boolean _isTail) {
		long lastMillis_ = points.get(points.size() - 1).millis;
		if (_isTail) {
			if (_millis - lastMillis_ == 0)
				getLast().set(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis + 1, false, true);
			else if (_millis - lastMillis_ > 0)
				points.add(new Point(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, false, true));
			isCompleted = true;
		} else {
			if (_millis - lastMillis_ == 0)
				getLast().set(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, false, false);
			else if (_millis - lastMillis_ > 0)
				points.add(new Point(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, false, false));
		}
	}
	
	public int getSize() {
		return points.size();
	}

	public Point getFirst() {
		return points.get(0);
	}

	public Point getSecond() {
		return points.get(1);
	}

	public Point getLast() {
		return points.get(points.size() - 1);
	}

	public void removeFirst() {
		points.remove(0);
	}
}