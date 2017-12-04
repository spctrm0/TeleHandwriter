package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Strokes {
	List<Points> strokes;

	public Strokes() {
		strokes = Collections.synchronizedList(new ArrayList<Points>());
	}

	public void addStroke(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		strokes.add(new Points(_penX, _penY, _pressure, _tiltX, _tiltY, _millis));
	}

	public void addPoint(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		strokes.get(strokes.size() - 1).addPoint(_penX, _penY, _pressure, _tiltX, _tiltY, _millis);
	}

	public void addLastPoint(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		strokes.get(strokes.size() - 1).addLastPoint(_penX, _penY, _pressure, _tiltX, _tiltY, _millis);
	}
}