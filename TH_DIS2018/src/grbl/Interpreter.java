package grbl;

import drawing.Stroke;
import main.Setting;
import processing.data.Table;
import processing.data.TableRow;
import drawing.Drawing;
import drawing.Point;

public class Interpreter {
	Drawing			drawing	= null;
	Grbl			grbl	= null;
	Table			table	= null;

	StringBuffer	strBfr	= null;

	public void setDrawing(Drawing _drawing) {
		drawing = _drawing;
	}

	public void setGrbl(Grbl _grbl) {
		grbl = _grbl;
	}

	public void setTable(Table _table) {
		table = _table;
	}

	public Interpreter() {
		strBfr = new StringBuffer();
	}

	public void thread() {
		interpreting();
	}

	public void interpreting() {
		if (drawing.getSize() > 0) {
			Stroke stroke_ = drawing.getFirst();
			if (stroke_.isCompleted) {
				interpret(stroke_);
				drawing.removeFirst();
			}
		}
	}

	public void interpret(Stroke _stroke) {
		while (_stroke.getSize() >= 2) {
			Point a_ = _stroke.getFirst();
			Point b_ = _stroke.getSecond();
			float aX_ = Setting.targetTabletWidth * ((a_.penX - Setting.targetCalibX) / Setting.targetScreentWidth);
			float aY_ = Setting.targetTabletHeight * ((a_.penY) / Setting.targetScreenHeight);
			float bX_ = Setting.targetTabletWidth * ((b_.penX - Setting.targetCalibX) / Setting.targetScreentWidth);
			float bY_ = Setting.targetTabletHeight * ((b_.penY) / Setting.targetScreenHeight);
			float f_ = Setting.feedRateDefault;
			long duration_ = b_.millis - a_.millis;
			if (duration_ != 0)
				f_ = (float) (60000 / (double) duration_);

			if (a_.isHead) {
				strBfr.append("G1")//
						.append("X").append(String.format("%.3f", Setting.isXInverted ? -aX_ : aX_))//
						.append("Y").append(String.format("%.3f", Setting.isYInverted ? -aY_ : aY_))//
						.append("F").append(Setting.feedRateDefault)//
						.append('\r');
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);

				strBfr.append("M3").append("S").append(Setting.servoZero).append('\r');
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);
			}

			strBfr.append("G1")//
					.append("X").append(String.format("%.3f", Setting.isXInverted ? -bX_ : bX_))//
					.append("Y").append(String.format("%.3f", Setting.isYInverted ? -bY_ : bY_))//
					.append("F").append(String.format("%.3f", f_))//
					.append('\r');
			grbl.reserve(strBfr.toString());
			strBfr.setLength(0);

			if (b_.isTail) {
				strBfr.append("M3").append("S").append(Setting.servoHover).append('\r');
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);
			}

			logTable(a_.strokeIdx, a_.penX, a_.penY, a_.pressure, a_.tiltX, a_.tiltY, a_.millis);

			_stroke.removeFirst();
		}
		while (_stroke.getSize() > 0) {
			logTable(_stroke.getFirst().strokeIdx, _stroke.getFirst().penX, _stroke.getFirst().penY,
					_stroke.getFirst().pressure, _stroke.getFirst().tiltX, _stroke.getFirst().tiltY,
					_stroke.getFirst().millis);

			_stroke.removeFirst();
		}
	}

	public void logTable(int _strokeIdx, float _penX, float _penY, float _pressure, float _tiltX, float _tiltY,
			long _millis) {
		TableRow newRow_ = table.addRow();
		newRow_.setInt("strokeIdx", _strokeIdx);
		newRow_.setFloat("penX", _penX);
		newRow_.setFloat("penY", _penY);
		newRow_.setFloat("pressure", _pressure);
		newRow_.setFloat("tiltX", _tiltX);
		newRow_.setFloat("tiltY", _tiltY);
		newRow_.setString("millis", String.valueOf(_millis));
	}
}