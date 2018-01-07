package grbl;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import main.Setting;
import oscComm.OscComm;
import processing.core.PApplet;

public class GrblComm {
	private PApplet			p5					= null;
	private SerialComm	serialComm	= null;
	private OscComm			oscComm			= null;

	private final int bfrSizeMx = 128;

	private int bfrSize = 0;

	private ArrayList<String>	reservedPreDefinedCmd	= null;
	private ArrayList<String>	reservedCmd						= null;
	private ArrayList<String>	grblBfr								= null;
	private ArrayList<String>	receivedMsg						= null;

	public void setSerialComm(SerialComm _serialComm) {
		serialComm = _serialComm;
	}

	public void setOscComm(OscComm _oscComm) {
		oscComm = _oscComm;
	}

	public GrblComm(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);

		reservedPreDefinedCmd = new ArrayList<String>();
		reservedCmd = new ArrayList<String>();
		grblBfr = new ArrayList<String>();
		receivedMsg = new ArrayList<String>();

	}

	public void init() {
		reservedPreDefinedCmd.clear();
		reservedCmd.clear();
		grblBfr.clear();
		bfrSize = 0;
		isAtHome = false;
		isNeedToMoveHome = true;
		lastStopAtBackTimeInUsec = System.nanoTime();
	}

	public void pre() {
		reservePreDefinedCmd();
		writeReservedCmdAsPossible();
	}

	private void reservePreDefinedCmd() {
		if (isNeedToMoveBack && !isAtBack) {
			if (timeSinceLastStopAtPaperInMsec() >= moveToBackPeriodInMsec)
				reserveBackCmd();
		}
		else if (isNeedToMoveHome && !isAtHome) {
			if (timeSinceLastStopAtBackInMsec() >= moveToHomePeriodInMsec)
				reserveHomeCmd();
		}
	}

	private void writeReservedCmdAsPossible() {
		while (bfrSize <= bfrSizeMx && reservedPreDefinedCmd.size() > 0) {
			String cmd_ = reservedPreDefinedCmd.get(0).toString();
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				int moveType_ = moveType(cmd_);
				if (moveType_ != 0) {
					if (!isMoving) {
						isMoving = true;
						oscComm.updateGrblIsMoving(isMoving);
					}
					isNeedToMoveBack = false;
					isNeedToMoveHome = false;
					isAtBack = false;
					isAtHome = false;
					switch (moveType_) {
						case 1: // Home
							break;
						case 2: // Paper
							break;
						case 3: // Back
							break;
					}
				}
				bfrSize += cmd_.length();
				grblBfr.add(cmd_);
				serialComm.write(cmd_);
				reservedPreDefinedCmd.remove(0);
			}
			else
				break;
		}
		while (bfrSize <= bfrSizeMx && reservedCmd.size() > 0 && reservedPreDefinedCmd.size() == 0) {
			String cmd_ = reservedCmd.get(0).toString();
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				int moveType_ = moveType(cmd_);
				if (moveType_ != 0) {
					if (!isMoving) {
						isMoving = true;
						oscComm.updateGrblIsMoving(isMoving);
					}
					isNeedToMoveBack = false;
					isNeedToMoveHome = false;
					isAtBack = false;
					isAtHome = false;
					switch (moveType_) {
						case 1: // Home
							break;
						case 2: // Paper
							break;
						case 3: // Back
							break;
					}
				}
				bfrSize += cmd_.length();
				grblBfr.add(cmd_);
				serialComm.write(cmd_);
				reservedCmd.remove(0);
			}
			else
				break;
		}
	}

	public void reservePreDefinedCmd(String _cmd) {
		reservedPreDefinedCmd.add(_cmd);
	}

	public void reserveCmd(String _cmd) {
		reservedCmd.add(_cmd);
	}

	public void read(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			String cmd_ = grblBfr.get(0);
			bfrSize -= cmd_.length();
			grblBfr.remove(0);
			receivedMsg.clear();
			int stopType_ = stopType(cmd_);
			if (stopType_ != 0) {
				if (isMoving) {
					isMoving = false;
					oscComm.updateGrblIsMoving(isMoving);
				}
				switch (stopType_) {
					case 1: // Home
						isAtHome = true;
						break;
					case 2: // Paper
						if (reservedCmd.size() == 0 && bfrSize == 0)
							isNeedToMoveBack = true;
						lastStopAtPaperTimeInUsec = System.nanoTime();
						break;
					case 3: // Back
						if (reservedCmd.size() == 0 && bfrSize == 0)
							isNeedToMoveHome = true;
						isAtBack = true;
						lastStopAtBackTimeInUsec = System.nanoTime();
						break;
				}
			}
		}
		else
			receivedMsg.add(_msg);
	}

	private boolean	isNeedToMoveBack	= false;
	private boolean	isAtBack					= false;
	private boolean	isNeedToMoveHome	= false;
	private boolean	isAtHome					= false;
	private boolean	isMoving					= false;

	private final int	moveToBackPeriodInMsec		= 500;
	private long			lastStopAtPaperTimeInUsec	= 0;

	private final int	moveToHomePeriodInMsec		= 1000;
	private long			lastStopAtBackTimeInUsec	= 0;

	private long timeSinceLastStopAtPaperInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastStopAtPaperTimeInUsec);
	}

	private long timeSinceLastStopAtBackInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastStopAtBackTimeInUsec);
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

	public void reserveBackCmd() {
		String cmd_;
		/*
		 * UNIQUE
		 */
		// Pen up
		cmd_ = "M3";
		cmd_ += 'S' + String.format("%.6f", Setting.servoHover);
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// Set feedrate mode: unit per min
		cmd_ = "G94";
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// To point backOff
		cmd_ = "G1";
		cmd_ += 'X' + String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff);
		cmd_ += 'F' + String.format("%.3f", Setting.feedrateStrokeToStoke);
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// Set feedrate mode: inverse time
		cmd_ = "G93";
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		/*
		 * UNIQUE
		 */
		// Delay
		cmd_ = "G4";
		cmd_ += 'P' + String.format("%.6f", Setting.servoDelay[3]);
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
	}

	public void reserveHomeCmd() {
		String cmd_;
		/*
		 * UNIQUE
		 */
		// Pen up
		cmd_ = "M3";
		cmd_ += 'S' + String.format("%.4f", Setting.servoHover);
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// Home
		cmd_ = "$H";
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// To point backOff
		cmd_ = "G1";
		cmd_ += 'X' + String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff);
		cmd_ += 'F' + String.format("%.3f", Setting.feedrateStrokeToStoke);
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// Set feedrate mode: inverse time
		cmd_ = "G93";
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// Set current position as 0
		cmd_ = "G92X0Y0";
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// Set feedrate mode: unit per min
		cmd_ = "G94";
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// Pre-defined home - unique
		cmd_ = "G1";
		cmd_ += 'F' + String.format("%.3f", Setting.feedrateStrokeToStoke);
		cmd_ += 'X' + String.format("%.3f", Setting.isXInverted ? -Setting.xZero : Setting.xZero);
		cmd_ += 'Y' + String.format("%.3f", Setting.isYInverted ? -Setting.yZero : Setting.yZero);
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		// Set feedrate mode: inverse time
		cmd_ = "G93";
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
		/*
		 * UNIQUE
		 */
		// Set current position as 0
		cmd_ = "G92X0000Y0000";
		cmd_ += '\r';
		reservePreDefinedCmd(cmd_);
	}

	public boolean isAtBack() {
		return isAtBack;
	}

	public boolean isAtHome() {
		return isAtHome;
	}

	public boolean isMoving() {
		return isMoving;
	}
}
