package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Drawing {
	public List<Curve> curves;

	public Drawing() {
		curves = Collections.synchronizedList(new ArrayList<Curve>());
	}

	public void addStroke(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis) {
		curves.add(new Curve(_penX, _penY, _pressure, _tiltX, _tiltY, _millis));
	}

	public void addPoint(float _penX, float _penY, float _pressure, float _tiltX, float _tiltY, long _millis,
			boolean _isTail) {
		getLastCurve().addPoint(_penX, _penY, _pressure, _tiltX, _tiltY, _millis, _isTail);
	}

	public Curve getFirstCurve() {
		return curves.get(0);
	}

	public Curve getLastCurve() {
		return curves.get(curves.size() - 1);
	}

	public void firstOut() {
		curves.remove(0);
	}
}