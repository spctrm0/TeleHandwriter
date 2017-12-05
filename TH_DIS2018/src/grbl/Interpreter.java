package grbl;

import drawing.Stroke;
import main.Setting;
import drawing.Drawing;
import drawing.Point;

public class Interpreter {
	Drawing			drawing	= null;
	Grbl			grbl	= null;

	StringBuffer	strBfr	= null;

	public void setDrawing(Drawing _drawing) {
		drawing = _drawing;
	}

	public void setGrbl(Grbl _grbl) {
		grbl = _grbl;
	}

	public Interpreter() {
		strBfr = new StringBuffer();
	}

	public void thread() {
		interpreting();
	}

	public void interpreting() {
		while (drawing.getSize() > 0) {
			Stroke stroke_ = drawing.getFirst();
			if (stroke_.isCompleted) {
				interpret(stroke_);
				drawing.removeFirst();
			} else
				break;
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

			strBfr.append("X").append(String.format("%.3f", Setting.isXInverted ? -bX_ : bX_))//
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

			_stroke.removeFirst();
		}
		while (_stroke.getSize() > 0) {
			_stroke.removeFirst();
		}
	}
}