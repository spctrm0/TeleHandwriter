package main;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import processing.core.PApplet;
import processing.serial.Serial;

public class SerialPort {

	public interface SerialCallback {
		void serialCallBack(SerialPort _serialPort, String _msg);
	}

	private ArrayList<SerialCallback> listeners = null;

	public void addListener(SerialCallback _listener) {
		if (!listeners.contains(_listener))
			listeners.add(_listener);
	}

	public void removeListener(SerialCallback _listener) {
		if (listeners.contains(_listener))
			listeners.remove(_listener);
	}

	private PApplet	p5			= null;
	private Serial	serial	= null;
	private int			portIdx	= -1;

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
		listeners = new ArrayList<SerialCallback>();
		p5 = _p5;
		p5.registerMethod("dispose", this);
		connectionChkMsg = _connectionChkMsg;
		charToStrBfr = new StringBuilder();

		serial = null;
		portIdx = -1;

		lastConnectionAttemptInUsec = 0;
		isConnected = false;
	}

	public void dispose() {
		setConnected(false);
	}

	public Serial getSerial() {
		return serial;
	}

	public int getPortIdx() {
		return portIdx;
	}

	public void setPortIdx(int _portIdx) {
		portIdx = _portIdx;
	}

	private void setLastConnectionAttemptInUsec() {
		lastConnectionAttemptInUsec = System.nanoTime();
	}

	public long getElapsedTimeSinceLastConnectionAttemptInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastConnectionAttemptInUsec);
	}

	public boolean isConnected() {
		return isConnected;
	}

	private void setConnected(boolean _isConnected) {
		boolean printLog_ = _isConnected && isConnected;
		isConnected = _isConnected;
		if (!isConnected)
			if (serial != null) {
				serial.clear();
				serial.stop();
				serial = null;
			}
		if (printLog_) {
			String log_ = "<SerialPort>\tDisconnected with " + "[" + portIdx + "] " + Serial.list()[portIdx];
			System.out.println(log_);
		}
	}

	public String getSerialPortProperties() {
		return baudRate + "\t" + parity + "\t" + dataBits + "\t" + stopBits;
	}

	public void setSerialPortProperties(int _baudRate, char _parity, int _dataBits, float _stopBits) {
		baudRate = _baudRate;
		parity = _parity;
		dataBits = _dataBits;
		stopBits = _stopBits;
	}

	public void setDelimeter(char _delimeter) {
		delimeter = _delimeter;
	}

	public void tryConnectWithPort(int _portIdx) {
		String log_;
		setPortIdx(_portIdx);
		setConnected(false);
		try {
			String portName_ = Serial.list()[portIdx];
			serial = new Serial(p5, portName_, baudRate, parity, dataBits, stopBits);
			log_ = "<SerialPort>\tTry to connect with " + "[" + portIdx + "] " + portName_;
		}
		catch (Exception e) {
			log_ = "<SerialPort>\t" + e.toString();
		}
		System.out.println(log_);
		setLastConnectionAttemptInUsec();
	}

	public void disconnect() {
		setConnected(false);
	}

	public void concatenateCharAndCallback(char _char) {
		if (_char != delimeter)
			charToStrBfr.append(_char);
		else {
			String msg_ = charToStrBfr.toString().trim();
			if (!isConnected)
				if (msg_.equals(connectionChkMsg)) {
					setConnected(true);
					String log_ = "<SerialPort>\tConnected with " + "[" + portIdx + "] " + Serial.list()[portIdx];
					System.out.println(log_);
				}
			for (SerialCallback listener_ : listeners)
				listener_.serialCallBack(this, msg_);
			charToStrBfr.setLength(0);
		}
	}

	public void write(String _msg) {
		serial.write(_msg);
	}
}