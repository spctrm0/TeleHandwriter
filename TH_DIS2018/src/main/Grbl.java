package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Grbl {
	// references
	public SerialComm	serialComm	= null;

	// settings
	public final int	bfrSizeMx	= 128;

	public int			bfrSize		= 0;

	// objects
	public List<String>	grblBuffer	= null;
	public List<String>	revervedMsg	= null;

	public Grbl() {
		grblBuffer = Collections.synchronizedList(new ArrayList<String>());
		revervedMsg = Collections.synchronizedList(new ArrayList<String>());
	}

	public void setSerialComm(SerialComm _serialComm) {
		serialComm = _serialComm;
	}

	public void read(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			bfrSize -= grblBuffer.get(0).length();
			grblBuffer.remove(0);
		}
	}

	public void a() {
		while (bfrSize <= bfrSizeMx && revervedMsg.size() > 0) {
			if (bfrSize + revervedMsg.get(0).length() <= bfrSizeMx) {
				bfrSize += revervedMsg.get(0).length();
				grblBuffer.add(revervedMsg.get(0).toString());
				serialComm.write(revervedMsg.get(0));
				revervedMsg.remove(0);
			} else {
				break;
			}
		}
	}

	public void init() {

	}

	public void reserve(String strBfr) {
		revervedMsg.add(strBfr);
	}
}
