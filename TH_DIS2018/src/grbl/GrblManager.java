package grbl;

import main.Setting;
import oscComm.OscComm;

public class GrblManager {
	OscComm oscComm;

	boolean	isNeedToBeHomed;
	boolean	isAtHome;
	boolean	isNeedToBeBackOffed;
	boolean	isAtBackOff;
	boolean	isMoving;

	public void checkOnWriting(String _cmd) {
		if (isMoveCmd(_cmd)) {
			isMoving = true;
			oscComm.updateGrblIsMoving(isMoving);
		}
	}

	public void checkOnReading(String _cmd) {
		if (isStopCmd(_cmd)) {
			isMoving = false;
			oscComm.updateGrblIsMoving(isMoving);
		}
	}

	public boolean isMoveCmd(String _cmd) {
		boolean isPenUp_;
		String comparatorPenUp_ = "M3";
		comparatorPenUp_ += 'S' + String.format("%.9f", Setting.servoHover);
		comparatorPenUp_ += '\r';
		isPenUp_ = comparatorPenUp_.equals(_cmd);

		return isPenUp_;
	}

	public boolean isStopCmd(String _cmd) {
		boolean isDelay_;
		String comparatorDelay_ = "G4";
		comparatorDelay_ += 'P' + String.format("%.9f", Setting.servoDelay[3]);
		comparatorDelay_ += '\r';
		isDelay_ = comparatorDelay_.equals(_cmd);

		return isDelay_;
	}

	public boolean isOnPaperStopCmd() {
		return false;
	}
	
	public boolean isBackOffStopCmd() {
		return false;
	}

	public void backOff() {
		String cmd_;
		/*
		 * UNIQUE
		 */
		// Pen up
		cmd_ = "M3";
		cmd_ += 'S' + String.format("%.9f", Setting.servoHover);
		cmd_ += '\r';
		// Set feedrate mode: unit per min
		cmd_ = "G94";
		cmd_ += '\r';
		// To point backOff
		cmd_ = "G1";
		cmd_ += 'X' + String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff);
		cmd_ += 'F' + String.format("%.4f", Setting.feedrateStrokeToStoke);
		cmd_ += '\r';
		// Set feedrate mode: inverse time
		cmd_ = "G93";
		cmd_ += '\r';
		/*
		 * UNIQUE
		 */
		// Delay
		cmd_ = "G4";
		cmd_ += 'P' + String.format("%.7f", Setting.servoDelay[3]);
		cmd_ += '\r';
	}

	public void home() {
		String cmd_;
		/*
		 * UNIQUE
		 */
		// Pen up
		cmd_ = "M3";
		cmd_ += 'S' + String.format("%.9f", Setting.servoHover);
		cmd_ += '\r';
		// Home
		cmd_ = "$H";
		cmd_ += '\r';
		// To point backOff
		cmd_ = "G1";
		cmd_ += 'X' + String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff);
		cmd_ += 'F' + String.format("%.3f", Setting.feedrateStrokeToStoke);
		cmd_ += '\r';
		// Set feedrate mode: inverse time
		cmd_ = "G93";
		cmd_ += '\r';
		// Set current position as 0
		cmd_ = "G92X0Y0";
		cmd_ += '\r';
		// Set feedrate mode: unit per min
		cmd_ = "G94";
		cmd_ += '\r';
		// Pre-defined home - unique
		cmd_ = "G1";
		cmd_ += 'F' + String.format("%.3f", Setting.feedrateStrokeToStoke);
		cmd_ += 'X' + String.format("%.3f", Setting.isXInverted ? -Setting.xZero : Setting.xZero);
		cmd_ += 'Y' + String.format("%.3f", Setting.isYInverted ? -Setting.yZero : Setting.yZero);
		cmd_ += '\r';
		// Set feedrate mode: inverse time
		cmd_ = "G93";
		cmd_ += '\r';
		// Set current position as 0
		cmd_ = "G92X0Y0";
		cmd_ += '\r';
	}
}