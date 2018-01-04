package grbl;

import java.util.ArrayList;
import processing.core.PApplet;

public class GrblComm {
	private PApplet			p5					= null;
	private SerialComm	serialComm	= null;
	private GrblManager	grblManager;

	private final int bfrSizeMx = 128;

	private int bfrSize = 0;

	private ArrayList<String>	receivedMsg	= null;
	private ArrayList<String>	grblBfr			= null;
	private ArrayList<String>	reservedCmd	= null;

	public void setSerialComm(SerialComm _serialComm) {
		serialComm = _serialComm;
	}

	public GrblComm(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);

		receivedMsg = new ArrayList<String>();
		grblBfr = new ArrayList<String>();
		reservedCmd = new ArrayList<String>();
	}

	public void init() {
		reservedCmd.clear();
		grblBfr.clear();
		bfrSize = 0;
	}

	public void pre() {
		writeReservedCmdAsPossible();
	}

	private void writeReservedCmdAsPossible() {
		while (bfrSize <= bfrSizeMx && reservedCmd.size() > 0) {
			String cmd_ = reservedCmd.get(0).toString();
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				grblManager.checkOnWriting(cmd_);
				bfrSize += cmd_.length();
				grblBfr.add(cmd_);
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
			grblManager.checkOnReading(cmd_);
			bfrSize -= cmd_.length();
			grblBfr.remove(0);
			receivedMsg.clear();
		}
		else
			receivedMsg.add(_msg);
	}
}
