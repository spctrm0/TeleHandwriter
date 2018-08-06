package serial;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import processing.core.PApplet;
import processing.serial.Serial;

public class SerialPort {

	public LinkedList<SerialCallback> listeners = null;

	public void addListener(SerialCallback _listener) {
		if (!listeners.contains(_listener))
			listeners.addLast(_listener);
	}

	public void removeListener(SerialCallback _listener) {
		if (listeners.contains(_listener))
			listeners.remove(_listener);
	}

	public boolean containsListener(SerialCallback _listener) {
		return listeners.contains(_listener);
	}

	private PApplet p5 = null;

	private Serial	serial		= null;
	private int			portIdx		= -1;
	private String	portName	= null;

	private String	connectionChkMsg						= null;
	private long		lastConnectionAttemptInUsec	= 0;
	private boolean	isConnected									= false;

	private int		baudRate	= 250000;
	private char	parity		= 'n';
	private int		dataBits	= 8;
	private float	stopBits	= 1.0f;

	private char					delimeter			= '\r';
	private StringBuilder	charToStrBfr	= null;

	public SerialPort(PApplet _p5, String _connectionChkMsg) {
		p5 = _p5;
		connectionChkMsg = _connectionChkMsg;

		listeners = new LinkedList<SerialCallback>();
		charToStrBfr = new StringBuilder();
	}

	public void dispose() {
		disconnect();
	}

	public Serial getSerial() {
		return serial;
	}

	public int getPortIdx() {
		return portIdx;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortIdx(int _portIdx) {
		portIdx = _portIdx;
		portName = Serial.list()[portIdx];
		disconnect();
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setSerialPortProperties(int _baudRate, char _parity, int _dataBits, float _stopBits) {
		baudRate = _baudRate;
		parity = _parity;
		dataBits = _dataBits;
		stopBits = _stopBits;
		disconnect();
		String log_ = "<" + getClass().getName() + ">\tSet serial port properties to " + baudRate + ", " + parity + ", "
				+ dataBits + ", " + stopBits + ".";
		System.out.println(log_);
	}

	public void setDelimeter(char _delimeter) {
		delimeter = _delimeter;
		String log_ = "<" + getClass().getName() + ">\tSet delimeter to \"" + delimeter + "\".";
		System.out.println(log_);
	}

	public void tryConnectWithPort(int _portIdx) {
		setPortIdx(_portIdx);
		tryConnect();
	}

	public void tryConnect() {
		String log_;
		try {
			serial = new Serial(p5, portName, baudRate, parity, dataBits, stopBits);
			log_ = "<" + getClass().getName() + ">\tTry to connect with " + "[" + portIdx + "] " + portName + ".";
		}
		catch (Exception e) {
			log_ = "<" + getClass().getName() + ">\t" + e.toString() + ".";
		}
		setLastConnectionAttemptInUsec();
		System.out.println(log_);
	}

	public void disconnect() {
		setConnected(false);
	}

	public void write(String _msg) {
		serial.write(_msg);
	}

	public void readAndCallback(char _char) {
		if (_char != delimeter)
			charToStrBfr.append(_char);
		else {
			String msg_ = charToStrBfr.toString().trim();
			if (!isConnected && msg_.equals(connectionChkMsg))
				setConnected(true);
			for (SerialCallback listener_ : listeners)
				listener_.serialMsgCallBack(msg_);
			charToStrBfr.setLength(0);
		}
	}

	public long getElapsedTimeSinceLastConnectionAttemptInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastConnectionAttemptInUsec);
	}

	private void setLastConnectionAttemptInUsec() {
		lastConnectionAttemptInUsec = System.nanoTime();
	}

	private void setConnected(boolean _isConnected) {
		boolean isChanged_ = _isConnected != isConnected;
		isConnected = _isConnected;
		String log_;
		if (isConnected && isChanged_) {
			log_ = "<" + getClass().getName() + ">\tConnected with " + "[" + portIdx + "] " + portName + ".";
			System.out.println(log_);
			for (SerialCallback listener_ : listeners)
				listener_.serialConnectionCallBack(this, true);
		}
		else if (!isConnected && isChanged_) {
			if (serial != null) {
				serial.clear();
				serial.stop();
				serial = null;
			}
			for (SerialCallback listener_ : listeners)
				listener_.serialConnectionCallBack(this, false);
			log_ = "<" + getClass().getName() + ">\tDisconnected with " + "[" + portIdx + "] " + portName + ".";
			System.out.println(log_);
		}
	}
}