package grbl;

import java.util.ArrayList;
import main.Setting;
import processing.core.PApplet;

public class Grbl {
	public PApplet		p5					= null;
	public SerialComm	serialComm	= null;

	public final int bfrSizeMx = 128;

	public int bfrSize = 0;

	public ArrayList<String>	receivedMsg	= null;
	public ArrayList<String>	grblBfr			= null;
	public ArrayList<String>	reservedMsg	= null;

	public StringBuffer prtTxtBfr = null;

	public void setSerialComm(SerialComm _serialComm) {
		serialComm = _serialComm;
	}

	public Grbl(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);

		receivedMsg = new ArrayList<String>();
		grblBfr = new ArrayList<String>();
		reservedMsg = new ArrayList<String>();

		prtTxtBfr = new StringBuffer();
	}

	public void pre() {
		stream();
	}

	boolean servoCmdWaiting = false;

	public void stream() {
		if (servoCmdWaiting) {
			String cmd_ = "?\r";
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				bfrSize += cmd_.length();
				grblBfr.add(cmd_);
				serialComm.write(cmd_);
			}
		}
		else {
			while (bfrSize <= bfrSizeMx && reservedMsg.size() > 0) {
				String cmd_;
				cmd_ = reservedMsg.get(0).toString();
				if (bfrSize + cmd_.length() <= bfrSizeMx) {
					bfrSize += cmd_.length();
					grblBfr.add(cmd_);
					if (isServoCmd(cmd_)) {
						servoCmdWaiting = true;
					}
					serialComm.write(cmd_);
					reservedMsg.remove(0);
				}
				else
					break;
			}
		}
	}

	// cmd_ = reservedMsg.get(0).toString();
	// if (bfrSize + cmd_.length() <= bfrSizeMx) {
	// bfrSize += cmd_.length();
	// grblBfr.add(cmd_);
	// if (isMotionCmd(cmd_)) {
	// isIdle = false;
	// }
	// serialComm.write(cmd_);
	// reservedMsg.remove(0);
	// }
	// else
	// break;

	public void init() {
		reservedMsg.clear();
		grblBfr.clear();
		bfrSize = 0;
	}

	public void read(String _msg) {

//		servoCmdWaiting = false;

		if (_msg.equals("ok") || _msg.contains("error:")) {
			String cmd_ = grblBfr.get(0);
			if (isStatusReportCmd(cmd_)) {
				for (int i = 0; i < receivedMsg.size(); i++)
					System.out.println(i + ": " + receivedMsg.get(i));
			}
			receivedMsg.clear();
			bfrSize -= grblBfr.get(0).length();
			grblBfr.remove(0);
		}
		else
			receivedMsg.add(_msg);
	}

	public void reserve(String strBfr) {
		reservedMsg.add(strBfr);
	}

	public boolean isMotionCmd(String _cmd) {
		if (_cmd.length() >= 2)
			return _cmd.substring(0, 2).equals("G1");
		return false;
	}

	public boolean isServoCmd(String _cmd) {
		if (_cmd.length() >= 2)
			return _cmd.substring(0, 2).equals("M3");
		return false;
	}

	// public boolean isServoUpCmd(String _cmd) {
	// String[] split_ = _cmd.split("S");
	// String trimmed_ = split_[1].substring(0, split_[1].length() - 1);
	// int val_ = Integer.parseInt(trimmed_);
	// return val_ == Setting.servoHover;
	// }

	public boolean isStatusReportCmd(String _cmd) {
		if (_cmd.length() >= 2)
			return _cmd.equals("?\r");
		return false;
	}
}
