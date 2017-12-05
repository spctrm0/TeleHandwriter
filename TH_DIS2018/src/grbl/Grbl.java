package grbl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Grbl {
	public SerialComm	serialComm	= null;

	public final int	bfrSizeMx	= 128;

	public int			bfrSize		= 0;

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
		while (bfrSize <= bfrSizeMx && revervedMsg.size() > 0) {
			if (bfrSize + revervedMsg.get(0).length() <= bfrSizeMx) {
				bfrSize += revervedMsg.get(0).length();
				grblBfr.add(revervedMsg.get(0).toString());
				serialComm.write(revervedMsg.get(0));
				revervedMsg.remove(0);
			} else {
				break;
			}
		}
	}

	public void init() {
		revervedMsg.clear();
		grblBfr.clear();
		bfrSize = 0;
	}

	public void read(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			bfrSize -= grblBfr.get(0).length();
			grblBfr.remove(0);
			prtTxtBfr.append("<MSG>").append('\t').append("Received msg...").append('\n');
			for (int i = 0; i < receivedMsg.size(); i++) {
				String receivedMsg_ = receivedMsg.get(i);
				prtTxtBfr.append('\t').append(receivedMsg_);
			}
			System.out.println(prtTxtBfr);
			prtTxtBfr.setLength(0);
			receivedMsg.clear();
		} else {
			receivedMsg.add(_msg);
		}
	}

	public void reserve(String strBfr) {
		revervedMsg.add(strBfr);
	}
}
