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
	
	private final int			connectionTryPeriodInMsec	= 1000;
	private long		lastStopTimeInUsec	= 0;

	public void checkOnWriting(String _cmd) {
		int moveType_ = moveType(_cmd);
		if (moveType_ != 0) {
			if (!isMoving) {
				isMoving = true;
				oscComm.updateGrblIsMoving(isMoving);
			}
			switch (moveType_) {
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
			}
		}
	}

	public void checkOnReading(String _cmd) {
		int stopType_ = stopType(_cmd);
		if (stopType_ != 0) {
			if (isMoving) {
				isMoving = false;
				oscComm.updateGrblIsMoving(isMoving);
			}
			switch (stopType_) {
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
			}
		}
	}

	public int moveType(String _cmd) {
		String penUpCmd_ = "M3S";
		if (_cmd.contains(penUpCmd_)) {
			String home_ = String.format("%.4f", Setting.servoHover);
			String paper_ = String.format("%.5f", Setting.servoHover);
			String back_ = String.format("%.6f", Setting.servoHover);
			if (_cmd.contains(back_))
				return 3;
			else if (_cmd.contains(paper_))
				return 2;
			else if (_cmd.contains(home_))
				return 1;
		}
		return 0;
	}

	public int stopType(String _cmd) {
		if (_cmd.contains("G92X0000Y0000")) {
			return 1;
		}
		else if (_cmd.contains("G4P")) {
			String paper_ = String.format("%.5f", Setting.servoDelay[3]);
			String back_ = String.format("%.6f", Setting.servoDelay[3]);
			if (_cmd.contains(back_))
				return 3;
			else if (_cmd.contains(paper_))
				return 2;
		}
		return 0;
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
		cmd_ += 'S' + String.format("%.6f", Setting.servoHover);
		cmd_ += '\r';
		// Set feedrate mode: unit per min
		cmd_ = "G94";
		cmd_ += '\r';
		// To point backOff
		cmd_ = "G1";
		cmd_ += 'X' + String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff);
		cmd_ += 'F' + String.format("%.3f", Setting.feedrateStrokeToStoke);
		cmd_ += '\r';
		// Set feedrate mode: inverse time
		cmd_ = "G93";
		cmd_ += '\r';
		/*
		 * UNIQUE
		 */
		// Delay
		cmd_ = "G4";
		cmd_ += 'P' + String.format("%.6f", Setting.servoDelay[3]);
		cmd_ += '\r';
	}

	public void home() {
		String cmd_;
		/*
		 * UNIQUE
		 */
		// Pen up
		cmd_ = "M3";
		cmd_ += 'S' + String.format("%.4f", Setting.servoHover);
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
		/*
		 * UNIQUE
		 */
		// Set current position as 0
		cmd_ = "G92X0000Y0000";
		cmd_ += '\r';
	}
}