package grbl;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import main.Setting;
import processing.core.PApplet;

public class Grbl {
	public PApplet		p5					= null;
	public SerialComm	serialComm	= null;

	public final int	returnIntervalMsec	= 1000;
	public final int	bfrSizeMx						= 128;

	public boolean	isHome						= false;
	public long			strokeEndTimeUsec	= 0;
	public int			bfrSize						= 0;

	boolean			gate					= false;
	public int	strokeEndCnt	= 0;

	public ArrayList<String>	receivedMsg	= null;
	public ArrayList<String>	grblBfr			= null;
	public ArrayList<String>	reservedMsg	= null;

	public StringBuffer	strBfr		= null;
	public StringBuffer	prtTxtBfr	= null;

	public long getWaitingTimeMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - strokeEndTimeUsec);
	}

	public void setSerialComm(SerialComm _serialComm) {
		serialComm = _serialComm;
	}

	public Grbl(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);

		receivedMsg = new ArrayList<String>();
		grblBfr = new ArrayList<String>();
		reservedMsg = new ArrayList<String>();

		strBfr = new StringBuffer();
		prtTxtBfr = new StringBuffer();
	}

	public void pre() {
		returnHome(returnIntervalMsec);
		stream();
	}

	public void returnHome(int _returnIntervalMsec) {
		if (gate) {
			if (getWaitingTimeMsec() >= _returnIntervalMsec) {
				reserveGoBackHomeCmd();
				gate = false;
			}
		}
	}

	public void stream() {
		while (bfrSize <= bfrSizeMx && reservedMsg.size() > 0) {
			String cmd_;
			cmd_ = reservedMsg.get(0).toString();
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				bfrSize += cmd_.length();
				grblBfr.add(cmd_);
				serialComm.write(cmd_);
				reservedMsg.remove(0);
			}
			else
				break;
		}
	}

	public void init() {
		reservedMsg.clear();
		grblBfr.clear();
		bfrSize = 0;
	}

	public void read(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			String cmd_ = grblBfr.get(0);
			receivedMsg.clear();
			bfrSize -= cmd_.length();
			grblBfr.remove(0);
			if (isHomeCmd(cmd_))
				isHome = true;
			else if (isMotionCmd(cmd_))
				isHome = false;
			else if (isStrokeEndCmd(cmd_)) {
				strokeEndCnt++;
				if (reservedMsg.size() == 0 && bfrSize == 0 && !isHome) {
					gate = true;
					strokeEndTimeUsec = System.nanoTime();
				}
			}
		}
		else
			receivedMsg.add(_msg);
	}

	public void reserve(String strBfr) {
		reservedMsg.add(strBfr);
	}

	public boolean isStrokeEndCmd(String _cmd) {
		return (_cmd.contains("G4P") && _cmd.contains(String.format("%.6f", Setting.servoDelay[3])));
	}

	public boolean isHomeCmd(String _cmd) {
		return (_cmd.equals("G92X0Y0\r") || _cmd.equals("G1X0Y0\r"));
	}

	public boolean isMotionCmd(String _cmd) {
		return (_cmd.contains("G1") && (_cmd.contains("X") || _cmd.contains("Y")));
	}

	public void reserveGoBackHomeCmd() {
		reserve("G94\r");
		strBfr.append("G1")//
				.append("X0").append("Y0").append("F").append(Setting.feedrateStrokeToStoke)//
				.append('\r');
		reserve(strBfr.toString());
		strBfr.setLength(0);
		reserve("G93\r");
	}
}
