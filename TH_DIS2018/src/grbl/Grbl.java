package grbl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Grbl {
	public SerialComm	serialComm	= null;

	public final int	bfrSizeMx	= 128;

	public int			bfrSize		= 0;

	public boolean		isIdle		= true;

	public List<String>	receivedMsg	= null;
	public List<String>	grblBfr		= null;
	public List<String>	revervedMsg	= null;

	public StringBuffer	prtTxtBfr	= null;

	public void setSerialComm(SerialComm _serialComm) {
		serialComm = _serialComm;
	}

	public Grbl() {
		receivedMsg = Collections.synchronizedList(new ArrayList<String>());
		grblBfr = Collections.synchronizedList(new ArrayList<String>());
		revervedMsg = Collections.synchronizedList(new ArrayList<String>());

		prtTxtBfr = new StringBuffer();
	}

	public void thread() {
		streaming();
	}

	public void streaming() {
		if (bfrSize <= bfrSizeMx && revervedMsg.size() > 0) {
			if (isIdle) {
				if (bfrSize + revervedMsg.get(0).length() <= bfrSizeMx) {
					bfrSize += revervedMsg.get(0).length();
					grblBfr.add(revervedMsg.get(0).toString());
					if (isMotionCmd(revervedMsg.get(0)))
						isIdle = false;
					serialComm.write(revervedMsg.get(0));
					revervedMsg.remove(0);
				}
			} else {
				if (isServoCmd(revervedMsg.get(0))) {
					if (bfrSize + 2 <= bfrSizeMx) {
						bfrSize += 2;
						grblBfr.add("?\r");
						serialComm.write("?\r");
					}
				} else {
					if (bfrSize + revervedMsg.get(0).length() <= bfrSizeMx) {
						bfrSize += revervedMsg.get(0).length();
						grblBfr.add(revervedMsg.get(0).toString());
						serialComm.write(revervedMsg.get(0));
						revervedMsg.remove(0);
					}
				}
			}
		}
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

	public boolean isStatusReportCmd(String _cmd) {
		if (_cmd.length() >= 2)
			return _cmd.equals("?\r");
		return false;
	}

	public void init() {
		revervedMsg.clear();
		grblBfr.clear();
		bfrSize = 0;
	}

	public void read(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			if (isStatusReportCmd(grblBfr.get(0))) {
				for (String receivedMsg_ : receivedMsg) {
					if (receivedMsg_.contains("Idle")) {
						isIdle = true;
						break;
					}
				}
			}
//			prtTxtBfr.append("<MSG>").append('\t').append("Received msg...").append('\n');
//			for (String receivedMsg_ : receivedMsg)
//				prtTxtBfr.append('\t').append(receivedMsg_).append('\n');
//			System.out.println(prtTxtBfr);
//			prtTxtBfr.setLength(0);
//			receivedMsg.clear();
			bfrSize -= grblBfr.get(0).length();
			grblBfr.remove(0);
		} else
			receivedMsg.add(_msg);
	}

	public void reserve(String strBfr) {
		revervedMsg.add(strBfr);
	}
}
