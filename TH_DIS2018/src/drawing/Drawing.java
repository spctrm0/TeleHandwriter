package drawing;

import java.util.ArrayList;

public class Drawing {
	public ArrayList<Stroke> strokes;

	public Drawing() {
		strokes = new ArrayList<Stroke>();
	}

	public void addStroke(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY,
			long _millis) {
		strokes.add(new Stroke(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis));
	}

	public void addPoint(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY,
			long _millis, boolean _isTail) {
		getLast().addPoint(_strokeIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, _isTail);
	}

	public int getSize() {
		return strokes.size();
	}

	public Stroke getFirst() {
		return strokes.get(0);
	}

	public Stroke getLast() {
		return strokes.get(strokes.size() - 1);
	}

	public void removeFirst() {
		strokes.remove(0);
	}
}