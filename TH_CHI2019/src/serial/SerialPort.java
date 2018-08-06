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

		p5.registerMethod("dispose", this);
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

	public String getSerialPortProperties() {
		return baudRate + "\t" + parity + "\t" + dataBits + "\t" + stopBits;
	}

	public void setSerialPortProperties(int _baudRate, char _parity, int _dataBits, float _stopBits) {
		baudRate = _baudRate;
		parity = _parity;
		dataBits = _dataBits;
		stopBits = _stopBits;
		disconnect();
	}

	public void setDelimeter(char _delimeter) {
		delimeter = _delimeter;
	}

	public void tryConnectWithPort(int _portIdx) {
		setPortIdx(_portIdx);
		tryConnect();
	}

	public void tryConnect() {
		String log_;
		try {
			serial = new Serial(p5, portName, baudRate, parity, dataBits, stopBits);
			log_ = "<SerialPort>\tTry to connect with " + "[" + portIdx + "] " + portName + ".";
		}
		catch (Exception e) {
			log_ = "<SerialPort>\t" + e.toString() + ".";
		}
		setLastConnectionAttemptInUsec();
		System.out.println(log_);
	}

	public void disconnect() {
		setConnected(false, false);
	}

	public void readAndCallback(char _char) {
		if (_char != delimeter)
			charToStrBfr.append(_char);
		else {
			String msg_ = charToStrBfr.toString().trim();
			if (!isConnected && msg_.equals(connectionChkMsg))
				setConnected(true, false);
			for (SerialCallback listener_ : listeners)
				listener_.serialMsgCallBack(msg_);
			charToStrBfr.setLength(0);
		}
	}

	public void write(String _msg) {
		serial.write(_msg);
	}

	private void setConnected(boolean _isConnected, boolean _forced) {
		boolean isChanged_ = _isConnected != isConnected;
		isConnected = _isConnected;
		String log_;
		if (isConnected && isChanged_) {
			for (SerialCallback listener_ : listeners)
				listener_.serialConnectionCallBack(this, true);
			log_ = "<SerialPort>\tConnected with " + "[" + portIdx + "] " + portName + ".";
			System.out.println(log_);
		}
		else if (!isConnected && (isChanged_ || _forced)) {
			if (serial != null) {
				serial.clear();
				serial.stop();
				serial = null;
			}
			for (SerialCallback listener_ : listeners)
				listener_.serialConnectionCallBack(this, false);
			log_ = "<SerialPort>\tDisconnected with " + "[" + portIdx + "] " + portName + ".";
			System.out.println(log_);
		}
	}

	private void setLastConnectionAttemptInUsec() {
		lastConnectionAttemptInUsec = System.nanoTime();
	}

	public long getElapsedTimeSinceLastConnectionAttemptInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastConnectionAttemptInUsec);
	}
}