package grbl;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import main.Setting;
import oscComm.OscComm;
import processing.core.PApplet;

public class Grbl {
	private PApplet			p5					= null;
	private SerialComm	serialComm	= null;
	private OscComm			oscComm			= null;

	private final int	backOffCmdPeriodMsec	= 500;
	private final int	toHomeCmdPeriodMSec		= 5000;
	private final int	bfrSizeMx							= 128;

	public boolean	isHomeCmdExecuted					= false;
	public boolean	isNeedToReserveBackOffCmd	= false;
	public boolean	isNeedToReserveToHomeCmd	= false;
	public boolean	isBackOffed								= false;
	public boolean	isPreDefinedHomed					= false;
	public boolean	isOnPaper									= false;
	public boolean	isIdle										= true;
	private long		strokeEndTimeUsec					= 0;
	private long		backOffTimeUsec						= 0;
	private int			bfrSize										= 0;
	private int			strokeEndCnt							= 0;

	private ArrayList<String>	receivedMsg	= null;
	private ArrayList<String>	grblBfr			= null;
	private ArrayList<String>	reservedCmd	= null;

	private StringBuffer	strBfr		= null;
	private StringBuffer	prtTxtBfr	= null;

	public void setSerialComm(SerialComm _serialComm) {
		serialComm = _serialComm;
	}

	public void setOscComm(OscComm _oscComm) {
		oscComm = _oscComm;
	}

	public Grbl(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);

		receivedMsg = new ArrayList<String>();
		grblBfr = new ArrayList<String>();
		reservedCmd = new ArrayList<String>();

		strBfr = new StringBuffer();
		prtTxtBfr = new StringBuffer();
	}

	public void init() {
		reservedCmd.clear();
		grblBfr.clear();
		bfrSize = 0;
	}

	public void pre() {
		writePreDefinedCmdPeriodically(backOffCmdPeriodMsec, toHomeCmdPeriodMSec);
		writeReservedCmdAsPossible();
	}

	private void writePreDefinedCmdPeriodically(int _backOffCmdPeriodMsec, int _toHomeCmdPeriodMSec) {
		if (serialComm.isConnected() && !isHomeCmdExecuted) {
			reserveToHomeCmd();
			isHomeCmdExecuted = true;
		}
		else {
			if (isNeedToReserveBackOffCmd && !isBackOffed) {
				if (getTimePassedSinceStrokeEndMsec() >= _backOffCmdPeriodMsec) {
					reserveBackOffCmd();
					isNeedToReserveBackOffCmd = false;
				}
			}
			else {
				// System.out.println("isBackOffed = " + isBackOffed);
				// System.out.println("isPreDefinedHomed = " + isPreDefinedHomed);
				if (isBackOffed && isNeedToReserveToHomeCmd && !isPreDefinedHomed) {
					if (getTimePassedSinceIsBackOffedMsec() >= _toHomeCmdPeriodMSec) {
						reserveToHomeCmd();
						isNeedToReserveToHomeCmd = false;
					}
				}
			}
		}
	}

	private long getTimePassedSinceStrokeEndMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - strokeEndTimeUsec);
	}

	private long getTimePassedSinceIsBackOffedMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - backOffTimeUsec);
	}

	private void reserveToHomeCmd() {
		// Pen up
		strBfr.append("M3")//
				.append("S").append(String.format("%.3f", Setting.servoHover))//
				.append('\r');
		reserveCmd(strBfr.toString());
		strBfr.setLength(0);

		// Home
		reserveCmd("$H\r");

		// Set current position as 0
		reserveCmd("G92X0Y0\r");

		// Set feedrate mode: unit per min
		reserveCmd("G94\r");

		// Pre-defined home - unique
		strBfr.append("G1")//
				.append("F").append(String.format("%.4f", Setting.feedrateStrokeToStoke))//
				.append("X").append(String.format("%.3f", Setting.isXInverted ? -Setting.xZero : Setting.xZero))//
				.append("Y").append(String.format("%.3f", Setting.isYInverted ? -Setting.yZero : Setting.yZero))//
				.append('\r');
		reserveCmd(strBfr.toString());
		strBfr.setLength(0);

		// Set feedrate mode: inverse time
		reserveCmd("G93\r");

		// Set current position as 0 - unique
		reserveCmd("G92X0.0000Y0.0000\r");
	}

	private void reserveBackOffCmd() {
		// Pen up
		strBfr.append("M3")//
				.append("S").append(String.format("%.3f", Setting.servoHover))//
				.append('\r');
		reserveCmd(strBfr.toString());
		strBfr.setLength(0);

		// Set feedrate mode: unit per min
		reserveCmd("G94\r");

		// BackOff - unique
		strBfr.append("G1")//
				.append("X").append(String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff))//
				.append("F").append(String.format("%.4f", Setting.feedrateStrokeToStoke))//
				.append('\r');
		reserveCmd(strBfr.toString());
		strBfr.setLength(0);

		// Set feedrate mode: inverse time
		reserveCmd("G93\r");

		// Delay - unique
		strBfr.append("G4")//
				.append(' ')//
				.append("P").append("0.001")//
				.append('\r');
		reserveCmd(strBfr.toString());
		strBfr.setLength(0);
	}

	private void writeReservedCmdAsPossible() {
		while (bfrSize <= bfrSizeMx && reservedCmd.size() > 0) {
			String cmd_ = reservedCmd.get(0).toString();
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				bfrSize += cmd_.length();
				grblBfr.add(cmd_);
				if (isMotionCmd(cmd_)) {
					if (isBackOffMotionCmd(cmd_))
						isPreDefinedHomed = false;
					else if (isToHomeMotionCmd(cmd_))
						isBackOffed = false;
					else {
						isBackOffed = false;
						isPreDefinedHomed = false;
					}
					if (isIdle) {
						isIdle = false;
						oscComm.updateGrblIsIdle(isIdle);
					}
				}
				serialComm.write(cmd_);
				reservedCmd.remove(0);
			}
			else
				break;
		}
	}

	public void reserveCmd(String _cmd) {
		reservedCmd.add(_cmd);
	}

	public void read(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			String cmd_ = grblBfr.get(0);
			bfrSize -= cmd_.length();
			grblBfr.remove(0);
			System.out.println(cmd_ + "(" + isBackOffCmd(cmd_) + ")");
			if (isBackOffCmd(cmd_) || isToPreDefiendHomeCmd(cmd_)) {
				isOnPaper = false;
				if (isBackOffCmd(cmd_)) { // isBackOffCmd(cmd_)
					isBackOffed = true;
					isPreDefinedHomed = false;
					backOffTimeUsec = System.nanoTime();
				}
				else { // isToPreDefiendHomeCmd(cmd_)
					isBackOffed = false;
					isPreDefinedHomed = true;
				}
				if (!isIdle) {
					isIdle = true;
					oscComm.updateGrblIsIdle(isIdle);
				}
			}
			else if (isOnPaperMotionCmd(cmd_)) {
				isOnPaper = true;
				isBackOffed = false;
				isPreDefinedHomed = false;
			}
			else if (isStrokeEndCmd(cmd_)) {
				strokeEndCnt++;
				if (reservedCmd.size() == 0 && bfrSize == 0 && isOnPaper) {
					isNeedToReserveBackOffCmd = true;
					strokeEndTimeUsec = System.nanoTime();
					if (!isIdle) {
						isIdle = true;
						oscComm.updateGrblIsIdle(isIdle);
					}
				}
			}
			receivedMsg.clear();
		}
		else
			receivedMsg.add(_msg);
	}

	private boolean isBackOffCmd(String _cmd) {
		prtTxtBfr.append("G4")//
				.append(' ')//
				.append("P").append("0.001")//
				.append('\r');
		String backOffCmd_ = prtTxtBfr.toString();
		prtTxtBfr.setLength(0);
		// return _cmd.contains("G4P0.0010");
		return _cmd.equals(backOffCmd_);
	}

	private boolean isToPreDefiendHomeCmd(String _cmd) {
		return _cmd.equals("G92X0.0000Y0.0000\r");
	}

	private boolean isMotionCmd(String _cmd) {
		boolean isMotionCmd_ = _cmd.contains("G1") && (_cmd.contains("X") || _cmd.contains("Y"));
		boolean isHomeCmd_ = _cmd.equals("$H\r");
		return isMotionCmd_ || isHomeCmd_;
	}

	private boolean isOnPaperMotionCmd(String _cmd) {
		boolean isMotionCmd_ = _cmd.contains("G1") && (_cmd.contains("X") || _cmd.contains("Y"));
		boolean isNotHomeCmd_ = !_cmd.equals("$H\r");
		boolean isNotToHomeMotionCmd_ = !isToHomeMotionCmd(_cmd);
		boolean isbackOffMotionCmd_ = !isBackOffMotionCmd(_cmd);
		return isMotionCmd_ && isNotHomeCmd_ && isNotToHomeMotionCmd_ && isbackOffMotionCmd_;
	}

	private boolean isToHomeMotionCmd(String _cmd) {
		prtTxtBfr.append("G1")//
				.append("F").append(String.format("%.4f", Setting.feedrateStrokeToStoke))//
				.append("X").append(String.format("%.3f", Setting.isXInverted ? -Setting.xZero : Setting.xZero))//
				.append("Y").append(String.format("%.3f", Setting.isYInverted ? -Setting.yZero : Setting.yZero))//
				.append('\r');
		String toHomeMotionCmd_ = prtTxtBfr.toString();
		prtTxtBfr.setLength(0);
		return _cmd.equals(toHomeMotionCmd_);
	}

	private boolean isBackOffMotionCmd(String _cmd) {
		prtTxtBfr.append("G1")//
				.append("X").append(String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff))//
				.append("F").append(String.format("%.4f", Setting.feedrateStrokeToStoke))//
				.append('\r');
		String backOffMotionCmd_ = prtTxtBfr.toString();
		prtTxtBfr.setLength(0);
		return _cmd.equals(backOffMotionCmd_);
	}

	private boolean isStrokeEndCmd(String _cmd) {
		prtTxtBfr.append("G4")//
				.append("P").append(String.format("%.6f", Setting.servoDelay[3]))//
				.append('\r');
		String strokeEndCmd_ = prtTxtBfr.toString();
		return _cmd.equals(strokeEndCmd_);
	}

	public void activateAutoHome() {
		isHomeCmdExecuted = false;
	}
}
