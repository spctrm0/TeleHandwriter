package grbl;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import main.Setting;
import processing.core.PApplet;

public class Grbl {
	public PApplet		p5					= null;
	public SerialComm	serialComm	= null;

	public final int	connectIntervalMsec	= 3000;
	public final int	bfrSizeMx						= 128;

	public boolean	isHome								= false;
	public long			connectTrialTimeUsec	= 0;
	public int			bfrSize								= 0;

	boolean			gate	= false;
	public int	cnt		= 0;

	public ArrayList<String>	receivedMsg	= null;
	public ArrayList<String>	grblBfr			= null;
	public ArrayList<String>	reservedMsg	= null;

	public StringBuffer prtTxtBfr = null;

	public long getWaitingTimeMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - connectTrialTimeUsec);
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

		prtTxtBfr = new StringBuffer();
	}

	public void pre() {
		a(connectIntervalMsec);
		stream();
	}

	public void a(int _tryIntervalMsec) {
		if (gate) {
			if (getWaitingTimeMsec() >= _tryIntervalMsec) {
				System.out.println("it is Time! (" + cnt + ")");
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
			else if (isEndOfStrokeCmd(cmd_) && reservedMsg.size() == 0 && bfrSize == 0 && !isHome) {
				gate = true;
				connectTrialTimeUsec = System.nanoTime();
				cnt++;
			}
		}
		else
			receivedMsg.add(_msg);
	}

	public void reserve(String strBfr) {
		reservedMsg.add(strBfr);
	}

	public boolean isEndOfStrokeCmd(String _cmd) {
		return (_cmd.contains("G4P") && _cmd.contains(String.format("%.6f", Setting.servoDelay[3])));
	}

	public boolean isHomeCmd(String _cmd) {
		return (_cmd.equals("G92X0Y0\r") || _cmd.equals("G1X0Y0\r"));
	}

	public boolean isMotionCmd(String _cmd) {
		return (_cmd.contains("G1") && (_cmd.contains("X") || _cmd.contains("Y")));
	}
}
