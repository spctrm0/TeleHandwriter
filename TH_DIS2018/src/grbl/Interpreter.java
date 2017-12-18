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
//		boolean isDone_ = false;
//
//		while (!isDone_) {
//			Stroke stroke_ = drawing.getFirstStroke();
//			if (stroke_.getPointsNum() >= 2) {
//				while (stroke_.getPointsNum() >= 2) {
//					Point a_ = stroke_.getFirstPoint();
//					Point b_ = stroke_.getSecondPoint();
//					float aX_ = Setting.targetTabletWidth * ((a_.getPenX() - Setting.targetCalibX) / Setting.targetScreentWidth);
//					float aY_ = Setting.targetTabletHeight * ((a_.getPenY()) / Setting.targetScreenHeight);
//					float bX_ = Setting.targetTabletWidth * ((b_.getPenX() - Setting.targetCalibX) / Setting.targetScreentWidth);
//					float bY_ = Setting.targetTabletHeight * ((b_.getPenY()) / Setting.targetScreenHeight);
//					float f_ = Setting.feedrateStrokeToStoke;
//					long duration_ = b_.getMillis() - a_.getMillis();
//					if (duration_ != 0)
//						f_ = (float) (60000 / (double) duration_);
//				}
//			}
//			else
//				isDone_ = true;
//		}

		while (drawing.getStrokesNum() > 0) {
			Stroke stroke_ = drawing.getFirstStroke();
			if (stroke_.isCompleted()) {
				while (stroke_.getPointsNum() >= 2) {
					Point a_ = stroke_.getFirstPoint();
					Point b_ = stroke_.getSecondPoint();
					float aX_ = Setting.targetTabletWidth * ((a_.getPenX() - Setting.targetCalibX) / Setting.targetScreentWidth);
					float aY_ = Setting.targetTabletHeight * ((a_.getPenY()) / Setting.targetScreenHeight);
					float bX_ = Setting.targetTabletWidth * ((b_.getPenX() - Setting.targetCalibX) / Setting.targetScreentWidth);
					float bY_ = Setting.targetTabletHeight * ((b_.getPenY()) / Setting.targetScreenHeight);
					float f_ = Setting.feedrateStrokeToStoke;
					long duration_ = b_.getMillis() - a_.getMillis();
					if (duration_ != 0)
						f_ = (float) (60000 / (double) duration_);

					if (a_.getKind() == 0) {
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

					logTable(a_.getStrokeIdx(), a_.getPenX(), a_.getPenY(), a_.getPressure(), a_.getTiltX(), a_.getTiltY(),
							a_.getMillis());
					stroke_.removeFirstPoint();

					if (b_.getKind() == 2) {
						strBfr.append("M3").append("S").append(Setting.servoHover).append('\r');
						grbl.reserve(strBfr.toString());
						strBfr.setLength(0);

						logTable(b_.getStrokeIdx(), b_.getPenX(), b_.getPenY(), b_.getPressure(), b_.getTiltX(), b_.getTiltY(),
								b_.getMillis());
						stroke_.removeFirstPoint();

						drawing.removeFirstStroke();

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