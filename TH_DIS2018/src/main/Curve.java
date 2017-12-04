package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Curve {
	public List<Point>	points		= null;

	public boolean		isCompleted	= false;

	public Curve(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		points = Collections.synchronizedList(new ArrayList<Point>());
		points.add(new Point(_penX, _penY, _pressure, _tiltX, _tiltY, _millis, true, false));
	}

	public void addPoint(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis,
			boolean _isTail) {
		long lastMillis_ = points.get(points.size() - 1).millis;
		if (_isTail) {
			if (_millis - lastMillis_ == 0)
				getLastPoint().set(_penX, _penY, _pressure, _tiltX, _tiltY, _millis + 1, false, true);
			else if (_millis - lastMillis_ > 0)
				points.add(new Point(_penX, _penY, _pressure, _tiltX, _tiltY, _millis, false, true));
			isCompleted = true;
		} else {
			if (_millis - lastMillis_ == 0)
				getLastPoint().set(_penX, _penY, _pressure, _tiltX, _tiltY, _millis, false, false);
			else if (_millis - lastMillis_ > 0)
				points.add(new Point(_penX, _penY, _pressure, _tiltX, _tiltY, _millis, false, false));

		}
	}

	public Point getFirstPoint() {
		return points.get(0);
	}

	public Point getLastPoint() {
		return points.get(points.size() - 1);
	}

	public void firstOut() {
		points.remove(0);
	}
}