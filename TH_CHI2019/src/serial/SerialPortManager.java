package serial;

import java.util.Iterator;
import java.util.LinkedList;

import main.G;
import processing.core.PApplet;
import processing.serial.Serial;

// Code below is required to work.
// public void serialEvent(Serial _serial) {
// if (<SerialPortManager>.getTempSerialPort() != null)
// <SerialPortManager>.getTempSerialPort().readAndCallback(_serial.readChar());
// else
// for (SerialPort serialPort_ : <SerialPortManager>.getSerialPorts())
// if (_serial == serialPort_.getSerial())
// serialPort_.readAndCallback(_serial.readChar());
// }

public class SerialPortManager implements SerialCallback {

	private PApplet p5 = null;

	private LinkedList<SerialPort>	serialPorts			= null;
	private SerialPort							tempSerialPort	= null;

	private boolean isAutoConnectActive = false;

	public static final int	targetSerialPortsNum						= 2;
	public static final int	connectionAttemptIntervalInMSec	= 2000;

	public SerialPortManager(PApplet _p5) {
		p5 = _p5;

		serialPorts = new LinkedList<SerialPort>();

		p5.registerMethod("dispose", this);

		printSerialList();
		activateAutoConnect();
	}

	public void dispose() {
		disconnectAll();
	}

	public void disconnectAll() {
		if (tempSerialPort != null) {
			if (tempSerialPort.containsListener(this))
				tempSerialPort.removeListener(this);
			tempSerialPort.disconnect();
			tempSerialPort = null;
		}
		Iterator<SerialPort> descendingIter_ = serialPorts.descendingIterator();
		while (descendingIter_.hasNext())
			removeAndDisconnectSerialPort(descendingIter_.next());
		String log_ = "<" + getClass().getName() + ">\tDisconnect all.";
		System.out.println(log_);
	}

	public void activateAutoConnect() {
		boolean isChanged_ = !isAutoConnectActive;
		isAutoConnectActive = true;
		if (isChanged_) {
			disconnectAll();
			p5.registerMethod("pre", this);
			String log_ = "<SerialPortManager>\tAutoConnect is activated.";
			System.out.println(log_);
		}
	}

	public void deactivateAutoConnect() {
		boolean isChanged_ = isAutoConnectActive;
		isAutoConnectActive = false;
		if (isChanged_) {
			p5.unregisterMethod("pre", this);
			String log_ = "<SerialPortManager>\tAutoConnect is deactivated.";
			System.out.println(log_);
		}
	}

	public void pre() {
		tryConnectPeriodically();
		if (serialPorts.size() >= targetSerialPortsNum)
			deactivateAutoConnect();
	}

	public LinkedList<SerialPort> getSerialPorts() {
		return serialPorts;
	}

	public SerialPort getTempSerialPort() {
		return tempSerialPort;
	}

	public boolean isAutoConnectActived() {
		return isAutoConnectActive;
	}

	@Override
	public void serialConnectionCallBack(SerialPort _serialPort, boolean _isConnected) {
		if (_isConnected)
			addConnectedSerialPort(_serialPort);
		else
			removeAndDisconnectSerialPort(_serialPort);
	}

	@Override
	public void serialMsgCallBack(String _msg) {
		// TODO Auto-generated method stub

	}

	private void addConnectedSerialPort(SerialPort _serialPort) {
		String log_;
		if (serialPorts.size() < targetSerialPortsNum && _serialPort.isConnected() && !serialPorts.contains(_serialPort)) {
			if (_serialPort.containsListener(this))
				_serialPort.removeListener(this);
			serialPorts.add(_serialPort);
			tempSerialPort = null;
			log_ = "<SerialPortManager>\t" + "[" + _serialPort.getPortIdx() + "] " + _serialPort.getPortName()
					+ " is added, idx = " + (serialPorts.size() - 1) + ".";
			System.out.println(log_);
		}
		else {
			log_ = "<SerialPortManager>\tFailed to add [" + _serialPort.getPortIdx() + "] " + _serialPort.getPortName() + ".";
			System.out.println(log_);
		}
	}

	private void removeAndDisconnectSerialPort(SerialPort _serialPort) {
		if (_serialPort.containsListener(this))
			_serialPort.removeListener(this);
		_serialPort.disconnect();
		serialPorts.remove(_serialPort);
		String log_ = "<" + getClass().getName() + ">\t" + "[" + _serialPort.getPortIdx() + "] " + _serialPort.getPortName()
				+ " is removed, size = " + serialPorts.size() + ".";
		System.out.println(log_);
	}

	private void printSerialList() {
		String log_ = "<SerialPortManager>\tPortList...\n";
		for (int i = 0; i < Serial.list().length; i++)
			log_ += "\t:[" + i + "] " + Serial.list()[i] + ".\n";
		System.out.print(log_);
	}

	private void tryConnectPeriodically() {
		if (serialPorts.isEmpty()) {
			if (tempSerialPort == null) {
				tempSerialPort = new SerialPort(p5, G.grblConnectionChkMsg);
				tempSerialPort.addListener(this);
			}
			else if (!tempSerialPort.isConnected()
					&& tempSerialPort.getElapsedTimeSinceLastConnectionAttemptInMsec() >= connectionAttemptIntervalInMSec)
				tempSerialPort.tryConnectWithPort((tempSerialPort.getPortIdx() + 1) % Serial.list().length);
		}
		else if (serialPorts.size() == 1) {
			if (tempSerialPort == null) {
				tempSerialPort = new SerialPort(p5, G.arduinoConnectionChkMsg);
				tempSerialPort.addListener(this);
			}
			else if (!tempSerialPort.isConnected()
					&& tempSerialPort.getElapsedTimeSinceLastConnectionAttemptInMsec() >= connectionAttemptIntervalInMSec) {
				int portIdx_ = (tempSerialPort.getPortIdx() + 1) % Serial.list().length;
				tempSerialPort.tryConnectWithPort(
						portIdx_ != serialPorts.get(0).getPortIdx() ? portIdx_ : (portIdx_ + 1) % Serial.list().length);
			}
		}
	}
}
