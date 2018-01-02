package grbl;

import drawing.Stroke;
import main.Setting;
import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;
import drawing.Drawing;
import drawing.Point;

public class Interpreter {
	private PApplet p5 = null;

	private Drawing	drawing	= null;
	private Grbl		grbl		= null;
	private Table		table		= null;

	private StringBuffer strBfr = null;

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

	private void interpret() {
		while (drawing.getStrokesNum() > 0) {
			Stroke stroke_ = drawing.getFirstStroke();
			while (stroke_.getPointsNum() >= 2) {
				Point a_ = stroke_.getFirstPoint();
				Point b_ = stroke_.getSecondPoint();
				float aCalibedNConstrainedX_ = Math.min(Math.max((a_.getX() - Setting.targetCalibXInPx), 0),
						(Setting.targetScreentWidthInPx - Setting.targetCalibXInPx));
				float aCalibedNConstrainedY_ = Math.min(Math.max((a_.getY() - Setting.targetCalibYInPx), 0),
						(Setting.targetScreenHeightInPx - Setting.targetCalibYInPx));
				float bCalibedNConstrainedX_ = Math.min(Math.max((b_.getX() - Setting.targetCalibXInPx), 0),
						(Setting.targetScreentWidthInPx - Setting.targetCalibXInPx));
				float bCalibedNConstrainedY_ = Math.min(Math.max((b_.getY() - Setting.targetCalibYInPx), 0),
						(Setting.targetScreenHeightInPx - Setting.targetCalibYInPx));
				float aScaledX_ = Setting.targetTabletWidthInMm * aCalibedNConstrainedX_
						/ (float) Setting.targetScreentWidthInPx;
				float aScaledY_ = Setting.targetTabletHeightInMm * aCalibedNConstrainedY_
						/ (float) Setting.targetScreenHeightInPx;
				float bScaledX_ = Setting.targetTabletWidthInMm * bCalibedNConstrainedX_
						/ (float) Setting.targetScreentWidthInPx;
				float bScaledY_ = Setting.targetTabletHeightInMm * bCalibedNConstrainedY_
						/ (float) Setting.targetScreenHeightInPx;
				long duration_ = b_.getEvtTimeInMsec() - a_.getEvtTimeInMsec();
				float feedrate_ = (float) (60000 / (double) duration_);

				if (a_.getType() == 0) { // Head
					// Pen up
					strBfr.append("M3")//
							.append("S").append(String.format("%.3f", Setting.servoHover))//
							.append('\r');
					grbl.reserveCmd(strBfr.toString());
					strBfr.setLength(0);

					// Set feedrate mode: unit per min
					grbl.reserveCmd("G94\r");

					// To point a
					strBfr.append("G1")//
							.append("X").append(String.format("%.3f", Setting.isXInverted ? -aScaledX_ : aScaledX_))//
							.append("Y").append(String.format("%.3f", Setting.isYInverted ? -aScaledY_ : aScaledY_))//
							.append("F").append(Setting.feedrateStrokeToStoke)//
							.append('\r');
					grbl.reserveCmd(strBfr.toString());
					strBfr.setLength(0);

					// Set feedrate mode: inverse time
					grbl.reserveCmd("G93\r");

					// Delay
					if (Setting.servoDelay[0] != 0.0f) {
						strBfr.append("G4")//
								.append("P").append(String.format("%.3f", Setting.servoDelay[0]))//
								.append('\r');
						grbl.reserveCmd(strBfr.toString());
						strBfr.setLength(0);
					}

					// Pen down
					strBfr.append("M3")//
							.append("S").append(Setting.servoZero)//
							.append('\r');
					grbl.reserveCmd(strBfr.toString());
					strBfr.setLength(0);

					// Delay
					if (Setting.servoDelay[1] != 0.0f) {
						strBfr.append("G4")//
								.append("P").append(String.format("%.3f", Setting.servoDelay[1]))//
								.append('\r');
						grbl.reserveCmd(strBfr.toString());
						strBfr.setLength(0);
					}
				}

				// To point b
				strBfr.append("G1")//
						.append("X").append(String.format("%.3f", Setting.isXInverted ? -bScaledX_ : bScaledX_))//
						.append("Y").append(String.format("%.3f", Setting.isYInverted ? -bScaledY_ : bScaledY_))//
						.append("F").append(String.format("%.3f", feedrate_))//
						.append('\r');
				grbl.reserveCmd(strBfr.toString());
				strBfr.setLength(0);

				// logging first point on table
				logTable(a_.getNthPoint(), a_.getNthStroke(), a_.getNthPointInStoke(), a_.getX(), a_.getY(), aScaledX_,
						aScaledY_, feedrate_, a_.getPressure(), a_.getTiltX(), a_.getTiltY(), a_.getEvtTimeInMsec());
				// remove first point
				stroke_.removeFirstPoint();

				if (b_.getType() == 2) { // Tail
					// Delay
					if (Setting.servoDelay[2] != 0.0f) {
						strBfr.append("G4")//
								.append("P").append(String.format("%.3f", Setting.servoDelay[2]))//
								.append('\r');
						grbl.reserveCmd(strBfr.toString());
						strBfr.setLength(0);
					}

					// Pen up
					strBfr.append("M3")//
							.append("S").append(Setting.servoHover)//
							.append('\r');
					grbl.reserveCmd(strBfr.toString());
					strBfr.setLength(0);

					// Delay: end of stroke
					if (Setting.servoDelay[3] != 0.0f) {
						strBfr.append("G4")//
								.append("P").append(String.format("%.6f", Setting.servoDelay[3]))//
								.append('\r');
						grbl.reserveCmd(strBfr.toString());
						strBfr.setLength(0);
					}

					// logging second (last) point on table
					logTable(b_.getNthPoint(), b_.getNthStroke(), b_.getNthPointInStoke(), b_.getX(), b_.getY(), bScaledX_,
							bScaledY_, feedrate_, b_.getPressure(), b_.getTiltX(), b_.getTiltY(), b_.getEvtTimeInMsec());
					// remove second (last) point
					stroke_.removeFirstPoint();
					// remove first stroke which is empty
					drawing.removeFirstStroke();
				}
			}
			if (stroke_.getPointsNum() == 1)
				break;
		}
	}

	public void logTable(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _penX, float _penY, float _cncX,
			float _cncY, float _feedrate, float _pressure, float _tiltX, float _tiltY, long _evtTimeInMsec) {
		TableRow newRow_ = table.addRow();
		newRow_.setInt("nthPoint", _nthPoint);
		newRow_.setInt("nthStroke", _nthStroke);
		newRow_.setInt("nthPointInStroke", _nthPointInStroke);
		newRow_.setFloat("penX", _penX);
		newRow_.setFloat("penY", _penY);
		newRow_.setFloat("cncX", _cncX);
		newRow_.setFloat("cncY", _cncY);
		newRow_.setFloat("feedrate", _feedrate);
		newRow_.setFloat("pressure", _pressure);
		newRow_.setFloat("tiltX", _tiltX);
		newRow_.setFloat("tiltY", _tiltY);
		newRow_.setString("evtTimeInMsec", String.valueOf(_evtTimeInMsec));
	}
}