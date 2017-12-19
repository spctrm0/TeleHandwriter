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
		interpret();
	}

	public void interpret() {
		while (drawing.getStrokesNum() > 0) {
			Stroke stroke_ = drawing.getFirstStroke();
			while (stroke_.getPointsNum() >= 2) {
				Point a_ = stroke_.getFirstPoint();
				Point b_ = stroke_.getSecondPoint();
				float aX_ = Setting.targetTabletWidth * ((a_.getPenX() - Setting.targetCalibX) / Setting.targetScreentWidth);
				float aY_ = Setting.targetTabletHeight * ((a_.getPenY()) / Setting.targetScreenHeight);
				float bX_ = Setting.targetTabletWidth * ((b_.getPenX() - Setting.targetCalibX) / Setting.targetScreentWidth);
				float bY_ = Setting.targetTabletHeight * ((b_.getPenY()) / Setting.targetScreenHeight);
				long duration_ = b_.getMillis() - a_.getMillis();
				float f_ = (float) (60000 / (double) duration_);
				// Head
				if (a_.getKind() == 0) {
					grbl.reserve("G94\r");
					strBfr.append("G1")//
							.append("X").append(String.format("%.3f", Setting.isXInverted ? -aX_ : aX_))//
							.append("Y").append(String.format("%.3f", Setting.isYInverted ? -aY_ : aY_))//
							.append("F").append(Setting.feedrateStrokeToStoke)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);
					grbl.reserve("G93\r");

					strBfr.append("G4")//
							.append("P")//
							.append(Setting.servoDelayPreDown)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);

					strBfr.append("M3")//
							.append("S")//
							.append(Setting.servoZero)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);

					strBfr.append("G4")//
							.append("P")//
							.append(Setting.servoDelayPostDown)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);
				}

				// For all
				strBfr.append("G1")//
						.append("X").append(String.format("%.3f", Setting.isXInverted ? -bX_ : bX_))//
						.append("Y").append(String.format("%.3f", Setting.isYInverted ? -bY_ : bY_))//
						.append("F").append(String.format("%.3f", f_))//
						.append('\r');
				grbl.reserve(strBfr.toString());
				strBfr.setLength(0);

				// logging first point on table
				logTable(a_.getStrokeIdx(), a_.getPenX(), a_.getPenY(), a_.getPressure(), a_.getTiltX(), a_.getTiltY(),
						a_.getMillis());
				// remove first point
				stroke_.removeFirstPoint();

				// Tail
				if (b_.getKind() == 2) {
					strBfr.append("G4")//
							.append("P")//
							.append(Setting.servoDelayPreUp)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);

					strBfr.append("M3")//
							.append("S")//
							.append(Setting.servoHover)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);

					strBfr.append("G4")//
							.append("P")//
							.append(Setting.servoDelayPostUp)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);

					// logging second (last) point on table
					logTable(b_.getStrokeIdx(), b_.getPenX(), b_.getPenY(), b_.getPressure(), b_.getTiltX(), b_.getTiltY(),
							b_.getMillis());
					// remove second (last) point
					stroke_.removeFirstPoint();
					// remove first stroke which just has been empty
					drawing.removeFirstStroke();
				}
			}
			if (stroke_.getPointsNum() == 1)
				break;
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