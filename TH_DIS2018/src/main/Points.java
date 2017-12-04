package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Points {

	List<Point>	points		= null;
	boolean		isFinished	= false;

	public Points(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		points = Collections.synchronizedList(new ArrayList<Point>());
		points.add(new Point(_penX, _penY, _pressure, _tiltX, _tiltY, _millis, true, false));
	}

	public void addPoint(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		long lastMillis_ = points.get(points.size() - 1).millis;
		if (_millis - lastMillis_ == 0) {
			points.set(points.size() - 1, new Point(_penX, _penY, _pressure, _tiltX, _tiltY, _millis));
		} else if (_millis - lastMillis_ > 0) {
			points.add(new Point(_penX, _penY, _pressure, _tiltX, _tiltY, _millis));
		}
	}

	public void addLastPoint(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		points.add(new Point(_penX, _penY, _pressure, _tiltX, _tiltY, _millis, false, true));
		isFinished = true;
	}
}
