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
				float aCalibX_ = Math.min(Math.max((a_.getX() - Setting.targetCalibX), 0),
						(Setting.targetScreentWidth - Setting.targetCalibX));
				float aCalibY_ = Math.min(Math.max((a_.getY() - Setting.targetCalibY), 0),
						(Setting.targetScreenHeight - Setting.targetCalibY));
				float bCalibX_ = Math.min(Math.max((b_.getX() - Setting.targetCalibX), 0),
						(Setting.targetScreentWidth - Setting.targetCalibX));
				float bCalibY_ = Math.min(Math.max((b_.getY() - Setting.targetCalibY), 0),
						(Setting.targetScreenHeight - Setting.targetCalibY));
				float aX_ = Setting.targetTabletWidth * aCalibX_ / (float) Setting.targetScreentWidth;
				float aY_ = Setting.targetTabletHeight * aCalibY_ / (float) Setting.targetScreenHeight;
				float bX_ = Setting.targetTabletWidth * bCalibX_ / (float) Setting.targetScreentWidth;
				float bY_ = Setting.targetTabletHeight * bCalibY_ / (float) Setting.targetScreenHeight;
				long duration_ = b_.getEvtTimeInMsec() - a_.getEvtTimeInMsec();
				float f_ = (float) (60000 / (double) duration_);
				// Head
				if (a_.getType() == 0) {
					grbl.reserve("G94\r");
					strBfr.append("G1")//
							.append("X").append(String.format("%.3f", Setting.isXInverted ? -aX_ : aX_))//
							.append("Y").append(String.format("%.3f", Setting.isYInverted ? -aY_ : aY_))//
							.append("F").append(Setting.feedrateStrokeToStoke)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);
					grbl.reserve("G93\r");

					if (Setting.servoDelay[0] != 0.0f) {
						strBfr.append("G4")//
								.append("P").append(String.format("%.3f", Setting.servoDelay[0]))//
								.append('\r');
						grbl.reserve(strBfr.toString());
						strBfr.setLength(0);
					}

					strBfr.append("M3")//
							.append("S").append(Setting.servoZero)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);

					if (Setting.servoDelay[1] != 0.0f) {
						strBfr.append("G4")//
								.append("P").append(String.format("%.3f", Setting.servoDelay[1]))//
								.append('\r');
						grbl.reserve(strBfr.toString());
						strBfr.setLength(0);
					}
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
				logTable(a_.getNthPoint(), a_.getNthStroke(), a_.getNthPointInStoke(), a_.getX(), a_.getY(), aX_, aY_, f_,
						a_.getPressure(), a_.getTiltX(), a_.getTiltY(), a_.getEvtTimeInMsec());
				// remove first point
				stroke_.removeFirstPoint();

				// Tail
				if (b_.getType() == 2) {
					if (Setting.servoDelay[2] != 0.0f) {
						strBfr.append("G4")//
								.append("P").append(String.format("%.3f", Setting.servoDelay[2]))//
								.append('\r');
						grbl.reserve(strBfr.toString());
						strBfr.setLength(0);
					}

					strBfr.append("M3")//
							.append("S").append(Setting.servoHover)//
							.append('\r');
					grbl.reserve(strBfr.toString());
					strBfr.setLength(0);

					if (Setting.servoDelay[3] != 0.0f) {
						strBfr.append("G4")//
								.append("P").append(String.format("%.6f", Setting.servoDelay[3]))//
								.append('\r');
						grbl.reserve(strBfr.toString());
						strBfr.setLength(0);
					}

					// logging second (last) point on table
					logTable(b_.getNthPoint(), b_.getNthStroke(), b_.getNthPointInStoke(), b_.getX(), b_.getY(), bX_, bY_, f_,
							b_.getPressure(), b_.getTiltX(), b_.getTiltY(), b_.getEvtTimeInMsec());
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

	public void logTable(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY, float _x, float _y,
			float _f, float _pressure, float _tiltX, float _tiltY, long _millis) {
		TableRow newRow_ = table.addRow();
		newRow_.setInt("totalPointIdx", _totalPointIdx);
		newRow_.setInt("strokeIdx", _strokeIdx);
		newRow_.setInt("pointIdx", _pointIdx);
		newRow_.setFloat("penX", _penX);
		newRow_.setFloat("penY", _penY);
		newRow_.setFloat("x", _x);
		newRow_.setFloat("y", _y);
		newRow_.setFloat("f", _f);
		newRow_.setFloat("pressure", _pressure);
		newRow_.setFloat("tiltX", _tiltX);
		newRow_.setFloat("tiltY", _tiltY);
		newRow_.setString("millis", String.valueOf(_millis));
	}
}