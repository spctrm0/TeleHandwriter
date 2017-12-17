package grbl;

import drawing.Stroke;
import main.Setting;
import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;
import drawing.Drawing;
import drawing.Point;

public class Interpreter {
	public PApplet p5 = null;

	Drawing	drawing	= null;
	Grbl		grbl		= null;
	Table		table		= null;

	int interpretCnt = 0;

	StringBuffer strBfr = null;

	public void setDrawing(Drawing _drawing) {
		drawing = _drawing;
	}

	public void setGrbl(Grbl _grbl) {
		grbl = _grbl;
	}

	public void setTable(Table _table) {
		table = _table;
	}

	public Interpreter(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);

		strBfr = new StringBuffer();
	}

	public void pre() {
		interpreting();
	}

	public void interpreting() {
		while (drawing.getSize() > 0) {
			Stroke stroke_ = drawing.getFirst();
			if (stroke_.isCompleted) {
				while (stroke_.getSize() >= 2) {
					Point a_ = stroke_.getFirst();
					Point b_ = stroke_.getSecond();
					float aX_ = Setting.targetTabletWidth * ((a_.penX - Setting.targetCalibX) / Setting.targetScreentWidth);
					float aY_ = Setting.targetTabletHeight * ((a_.penY) / Setting.targetScreenHeight);
					float bX_ = Setting.targetTabletWidth * ((b_.penX - Setting.targetCalibX) / Setting.targetScreentWidth);
					float bY_ = Setting.targetTabletHeight * ((b_.penY) / Setting.targetScreenHeight);
					float f_ = Setting.feedrateStrokeToStoke;
					long duration_ = b_.millis - a_.millis;
					if (duration_ != 0)
						f_ = (float) (60000 / (double) duration_);

					if (a_.isHead) {
						interpretCnt++;
						grbl.reserve("G94\r");
						strBfr.append("G1")//
								.append("X").append(String.format("%.3f", Setting.isXInverted ? -aX_ : aX_))//
								.append("Y").append(String.format("%.3f", Setting.isYInverted ? -aY_ : aY_))//
								.append("F").append(Setting.feedrateStrokeToStoke)//
								.append('\r');
						grbl.reserve(strBfr.toString());
						strBfr.setLength(0);
						grbl.reserve("G93\r");

						strBfr.append("M3").append("S").append(Setting.servoZero).append('\r');
						grbl.reserve(strBfr.toString());
						strBfr.setLength(0);

						System.out.println("interpretH" + interpretCnt);
					}

					strBfr.append("G1")//
							.append("X").append(String.format("%.3f", Setting.isXInverted ? -bX_ : bX_))//
							.append("Y").append(String.format("%.3f", Setting.isYInverted ? -bY_ : bY_))//
							.append("F").append(String.format("%.3f", f_))//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);

					logTable(a_.strokeIdx, a_.penX, a_.penY, a_.pressure, a_.tiltX, a_.tiltY, a_.millis);
					stroke_.removeFirst();

					if (b_.isTail) {
						strBfr.append("M3").append("S").append(Setting.servoHover).append('\r');
						grbl.reserve(strBfr.toString());
						strBfr.setLength(0);

						logTable(b_.strokeIdx, b_.penX, b_.penY, b_.pressure, b_.tiltX, b_.tiltY, b_.millis);
						stroke_.removeFirst();

						drawing.removeFirst();

						System.out.println("interpretT" + interpretCnt);
						break;
					}
				}
			}
			else {
				break;
			}
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