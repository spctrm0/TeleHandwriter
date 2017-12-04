package main;

public class Interpreter {
	Strokes			strokes	= null;
	Grbl			grbl	= null;
	StringBuffer	strBfr	= null;

	public Interpreter() {
		strBfr = new StringBuffer();
	}

	public void setStrokes(Strokes _strokes) {
		strokes = _strokes;
	}

	public void setGrbl(Grbl _grbl) {
		grbl = _grbl;
	}

	public void pre() {
		for (int i = 0; i < strokes.strokes.size(); i++) {
			Points points_ = strokes.strokes.get(i);
			if (points_.isFinished) {
				interpret(points_);
				strokes.strokes.remove(i);
				i--;
			} else {
				break;
			}
		}
	}

	public void interpret(Points _points) {
		for (int i = 0; i < _points.points.size() - 1; i++) {
			Point a_ = _points.points.get(i);
			Point b_ = _points.points.get(i + 1);
			// float aX_ = Setting.targetTabletWidth * ((a_.penX -
			// Setting.targetCalibX) / Setting.targetScreentWidth);
			float aX_ = Setting.targetTabletWidth * ((a_.penX) / Setting.targetScreentWidth);
			float aY_ = Setting.targetTabletHeight * ((a_.penY) / Setting.targetScreenHeight);
			// float bX_ = Setting.targetTabletWidth * ((b_.penX -
			// Setting.targetCalibX) / Setting.targetScreentWidth);
			float bX_ = Setting.targetTabletWidth * ((b_.penX) / Setting.targetScreentWidth);
			float bY_ = Setting.targetTabletHeight * ((b_.penY) / Setting.targetScreenHeight);
			float f_ = Setting.feedRateDefault;
			long duration_ = b_.millis - a_.millis;
			if (duration_ != 0) {
				f_ = (float) (60000 / (double) duration_);
			}
			if (a_.isHead) {
				strBfr.append("G90").append("G93").append('\r');
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);

				strBfr.append("G1").append("F").append(Setting.feedRateDefault).append('\r');
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);

				strBfr.append("X").append(String.format("%.3f", Setting.isXInverted ? -aX_ : aX_)).append("Y")
						.append(String.format("%.3f", Setting.isYInverted ? -aY_ : aY_)).append('\r');
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);

				strBfr.append("M3").append("S").append(Setting.servoZero);
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);
			} else {
				strBfr.append("X").append(String.format("%.3f", Setting.isXInverted ? -bX_ : bX_)).append("Y")
						.append(String.format("%.3f", Setting.isYInverted ? -bY_ : bY_)).append("F")
						.append(String.format("%.3f", f_)).append('\r');
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);
				if (b_.isTail) {
					strBfr.append("M3").append("S").append(Setting.servoHover);
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);
				}
			}
			_points.points.remove(i);
			i--;
		}
	}
}
