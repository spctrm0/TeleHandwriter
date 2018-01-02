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

	private boolean	isHomeCmdExecuted					= false;
	private boolean	isNeedToReserveBackOffCmd	= false;
	private boolean	isBackOffed								= false;
	private boolean	isHomed										= false;
	private boolean	isPreDefinedHomed					= false;
	private boolean	isOnPaper									= false;
	private boolean	isIdle										= true;
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
			else if (isBackOffed && !isPreDefinedHomed) {
				if (getTimePassedSinceIsBackOffedMsec() >= _toHomeCmdPeriodMSec) {
					reserveToHomeCmd();
					isBackOffed = false;
				}
			}
			else if (isHomed) {
				reserveToPreDefinedHomeCmd();
				isHomed = false;
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
	}

	private void reserveToPreDefinedHomeCmd() {
		// Pen up
		strBfr.append("M3")//
				.append("S").append(String.format("%.3f", Setting.servoHover))//
				.append('\r');
		reserveCmd(strBfr.toString());
		strBfr.setLength(0);

		// Set current position as 0;
		reserveCmd("G92X0Y0\r");

		// Set feedrate mode: unit per min
		reserveCmd("G94\r");

		// Pre-defined home
		strBfr.append("G1")//
				.append("F").append(String.format("%.3f", Setting.feedrateStrokeToStoke))//
				.append("X").append(String.format("%.3f", Setting.isXInverted ? -Setting.xZero : Setting.xZero))//
				.append("Y").append(String.format("%.3f", Setting.isYInverted ? -Setting.yZero : Setting.yZero))//
				.append('\r');
		reserveCmd(strBfr.toString());
		strBfr.setLength(0);

		// Set feedrate mode: inverse time
		reserveCmd("G93\r");

		// Set current position as 0;
		reserveCmd("G92X0.000000Y0.000000\r");
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

		// BackOff
		strBfr.append("G1")//
				.append("X").append(String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff))//
				.append("F").append(String.format("%.3f", Setting.feedrateStrokeToStoke))//
				.append('\r');
		reserveCmd(strBfr.toString());
		strBfr.setLength(0);

		// Set feedrate mode: inverse time
		reserveCmd("G93\r");
	}

	private void writeReservedCmdAsPossible() {
		while (bfrSize <= bfrSizeMx && reservedCmd.size() > 0) {
			String cmd_ = reservedCmd.get(0).toString();
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				bfrSize += cmd_.length();
				grblBfr.add(cmd_);
				if (isIdle) {
					if (isMotionCmd(cmd_)) {
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
			isBackOffed = false;
			isHomed = false;
			isPreDefinedHomed = false;
			if (isBackOffCmd(cmd_) || isToHomeCmd(cmd_) || isToPreDefiendHomeCmd(cmd_)) {
				isOnPaper = false;
				if (isBackOffCmd(cmd_) || isToPreDefiendHomeCmd(cmd_)) {
					if (isBackOffCmd(cmd_)) { // isBackOffCmd(cmd_)
						isBackOffed = true;
						backOffTimeUsec = System.nanoTime();
					}
					else // isToPreDefiendHomeCmd(cmd_)
						isPreDefinedHomed = true;
					if (!isIdle) {
						isIdle = true;
						oscComm.updateGrblIsIdle(isIdle);
					}
				}
				else // isToHomeCmd(cmd_)
					isHomed = true;
			}
			else if (isMotionCmd(cmd_))
				isOnPaper = true;
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
		strBfr.append("G1")//
				.append("X").append(String.format("%.3f", Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff));
		String backOffCmd_ = strBfr.toString();
		strBfr.setLength(0);
		return _cmd.contains(backOffCmd_);
	}

	private boolean isToHomeCmd(String _cmd) {
		return _cmd.equals("$H\r");
	}

	private boolean isToPreDefiendHomeCmd(String _cmd) {
		return _cmd.equals("G92X0.000000Y0.000000\r");
	}

	private boolean isMotionCmd(String _cmd) {
		return ((_cmd.contains("G1") && (_cmd.contains("X") || _cmd.contains("Y"))) || _cmd.equals("$H\r"));
	}

	private boolean isStrokeEndCmd(String _cmd) {
		strBfr.append("G4")//
				.append("P").append(String.format("%.6f", Setting.servoDelay[3]))//
				.append('\r');
		String strokeEndCmd_ = strBfr.toString();
		return _cmd.equals(strokeEndCmd_);
	}

	public void activateAutoHome() {
		isHomeCmdExecuted = true;
	}
}
