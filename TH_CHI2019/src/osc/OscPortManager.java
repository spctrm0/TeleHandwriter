package osc;

import java.util.Iterator;
import java.util.LinkedList;

import main.G;
import oscP5.OscMessage;
import processing.core.PApplet;
import processing.serial.Serial;
import serial.SerialPort;

public class OscPortManager implements OscCallback {
	private PApplet p5 = null;

	private LinkedList<OscComm>	oscPorts			= null;
	private LinkedList<OscComm>	tempOscPorts	= null;

	private final int targetOscPortsNum = 1;

	public OscPortManager(PApplet _p5) {
		p5 = _p5;

		oscPorts = new LinkedList<OscComm>();
		tempOscPorts = new LinkedList<OscComm>();

		p5.registerMethod("dispose", this);
	}

	public void dispose() {
		disconnectAll();
	}

	public void disconnectAll() {
		Iterator<OscComm> tempDescendingIter_ = tempOscPorts.descendingIterator();
		while (tempDescendingIter_.hasNext()) {
			OscComm tempOscPort_ = tempDescendingIter_.next();
			if (tempOscPort_.containsListener(this))
				tempOscPort_.removeListener(this);
			tempOscPort_.disconnect();
			tempOscPorts.remove(tempOscPort_);
		}
		Iterator<OscComm> descendingIter_ = oscPorts.descendingIterator();
		while (descendingIter_.hasNext())
			removeAndDisconnectSerialPort(descendingIter_.next());
		String log_ = "<OscPortManager>\tDisconnect all.";
		System.out.println(log_);
	}

	private void removeAndDisconnectSerialPort(OscComm _oscPort) {
		if (_oscPort.containsListener(this))
			_oscPort.removeListener(this);
		_oscPort.disconnect();
		oscPorts.remove(_oscPort);
		String log_ = "<OscPortManager>\t" + _oscPort.getMyIp() + ":" + _oscPort.getMyPort() + " is removed, size = "
				+ oscPorts.size() + ".";
		System.out.println(log_);
	}

	public LinkedList<OscComm> getOscPorts() {
		return oscPorts;
	}

	public LinkedList<OscComm> getTempOscPorts() {
		return tempOscPorts;
	}

	@Override
	public void oscConnectionCallBack(OscComm _oscPort, boolean _isConnected) {
		if (_isConnected)
			addConnectedSerialPort(_oscPort);
		else
			removeAndDisconnectSerialPort(_oscPort);
	}

	private void addConnectedSerialPort(OscComm _oscPort) {
		String log_;
		if (oscPorts.size() < targetOscPortsNum && _oscPort.isConnected() && !oscPorts.contains(_oscPort)) {
			if (_oscPort.containsListener(this))
				_oscPort.removeListener(this);
			oscPorts.add(_oscPort);
			tempOscPorts.remove(_oscPort);
			log_ = "<SerialPortManager>\t" + _oscPort.getMyIp() + ":" + _oscPort.getMyPort() + " is added, idx = "
					+ (oscPorts.size() - 1) + ".";
			System.out.println(log_);
		}
		else {
			log_ = "<SerialPortManager>\tFailed to add " + _oscPort.getMyIp() + ":" + _oscPort.getMyPort() + ".";
			System.out.println(log_);
		}
	}

	@Override
	public void oscMsgCallBack(OscMessage _oscMsg) {
		// TODO Auto-generated method stub

	}
}
