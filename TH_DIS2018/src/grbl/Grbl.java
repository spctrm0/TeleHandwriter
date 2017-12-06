package grbl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.Setting;
import processing.core.PApplet;

public class Grbl {
	public PApplet		p5						= null;
	public SerialComm	serialComm				= null;

	public final int	bfrSizeMx				= 128;

	public int			bfrSize					= 0;

	public boolean		isServoUp				= false;
	public final int	connectIntervalMsec		= 5000;
	public long			connectTrialTimeUsec	= 0;
	public boolean		isIdleCaptured			= false;
	public boolean		isIdle					= true;
	public boolean		isWaiting				= false;

	public List<String>	receivedMsg				= null;
	public List<String>	grblBfr					= null;
	public List<String>	reservedMsg				= null;

	public StringBuffer	prtTxtBfr				= null;

	public void setSerialComm(SerialComm _serialComm) {
		serialComm = _serialComm;
	}

	public Grbl(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);

		receivedMsg = Collections.synchronizedList(new ArrayList<String>());
		grblBfr = Collections.synchronizedList(new ArrayList<String>());
		reservedMsg = Collections.synchronizedList(new ArrayList<String>());

		prtTxtBfr = new StringBuffer();
	}

	public void pre() {
		if (!isIdle && isIdleCaptured) {
			if (isServoUp) {
				if (getWaitingTimeMsec() >= 5000) {
					isWaiting = false;
					isIdle = true;
				}
			} else {
				if (getWaitingTimeMsec() >= 5000) {
					isWaiting = false;
					isIdle = true;
				}
			}
		}
		streaming();
	}

	public long getWaitingTimeMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - connectTrialTimeUsec);
	}

	public void streaming() {
		while (bfrSize <= bfrSizeMx && reservedMsg.size() > 0) {
			if (isIdle) {
				if (bfrSize + reservedMsg.get(0).length() <= bfrSizeMx) {
					bfrSize += reservedMsg.get(0).length();
					grblBfr.add(reservedMsg.get(0).toString());
					if (isMotionCmd(reservedMsg.get(0))) {
						isIdle = false;
						isIdleCaptured = false;
					}
					serialComm.write(reservedMsg.get(0));
					reservedMsg.remove(0);
				} else
					break;
			} else {
				if (isServoCmd(reservedMsg.get(0))) {
					isWaiting = true;
					if (isIdleCaptured) {
						if (bfrSize + reservedMsg.get(0).length() <= bfrSizeMx) {
							bfrSize += reservedMsg.get(0).length();
							grblBfr.add(reservedMsg.get(0).toString());
							serialComm.write(reservedMsg.get(0));
							reservedMsg.remove(0);
						} else
							break;
					} else {
						isServoUp = isServoUp(reservedMsg.get(0));
						if (bfrSize + 2 <= bfrSizeMx) {
							bfrSize += 2;
							grblBfr.add("?\r");
							serialComm.write("?\r");
							break;
						} else
							break;
					}
				} else if (!isWaiting) {
					if (bfrSize + reservedMsg.get(0).length() <= bfrSizeMx) {
						bfrSize += reservedMsg.get(0).length();
						grblBfr.add(reservedMsg.get(0).toString());
						serialComm.write(reservedMsg.get(0));
						reservedMsg.remove(0);
					} else
						break;
				}
			}
		}
	}

	public void init() {
		reservedMsg.clear();
		grblBfr.clear();
		bfrSize = 0;
	}

	public void read(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			if (!isIdleCaptured) {
				if (isStatusReportCmd(grblBfr.get(0))) {
					for (String receivedMsg_ : receivedMsg) {
						if (receivedMsg_.contains("Idle")) {
							connectTrialTimeUsec = System.nanoTime();
							isIdleCaptured = true;
							break;
						}
					}
				}
			}
			// prtTxtBfr.append("<MSG>").append('\t').append("Received
			// msg...").append('\n');
			// for (String receivedMsg_ : receivedMsg)
			// prtTxtBfr.append('\t').append(receivedMsg_).append('\n');
			// System.out.println(prtTxtBfr);
			// prtTxtBfr.setLength(0);
			receivedMsg.clear();
			bfrSize -= grblBfr.get(0).length();
			grblBfr.remove(0);
		} else
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

	public boolean isServoUp(String _cmd) {
		String[] split_ = _cmd.split("S");
		String trimmed_ = split_[1].substring(0, split_[1].length() - 1);
		int val_ = Integer.parseInt(trimmed_);
		return val_ == Setting.servoHover;
	}

	public boolean isStatusReportCmd(String _cmd) {
		if (_cmd.length() >= 2)
			return _cmd.equals("?\r");
		return false;
	}
}
