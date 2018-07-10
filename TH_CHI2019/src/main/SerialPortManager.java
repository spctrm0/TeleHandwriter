package main;

import java.util.ArrayList;

import main.SerialPort.SerialCallback;
import processing.core.PApplet;
import processing.serial.Serial;

public class SerialPortManager implements SerialCallback {
	private PApplet p5 = null;

	private ArrayList<SerialPort>	serialPorts			= null;
	private SerialPort						tempSerialPort	= null;

	private final int	targetSerialPortsNum						= 2;
	private final int	connectionAttemptIntervalInMSec	= 2000;

	public SerialPortManager(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);
		p5.registerMethod("dispose", this);

		serialPorts = new ArrayList<SerialPort>();

		printSerialList();
	}

	public void pre() {
		tryConnectPeriodically();
		if (serialPorts.size() >= targetSerialPortsNum)
			p5.unregisterMethod("pre", this);
	}

	private void tryConnectPeriodically() {
		if (serialPorts.size() == 0) {
			if (tempSerialPort == null) {
				tempSerialPort = new SerialPort(p5, G.grblConnectionChkMsg);
				tempSerialPort.addListener(this);
			}
			else if (!tempSerialPort.isConnected())
				if (tempSerialPort.getElapsedTimeSinceLastConnectionAttemptInMsec() >= connectionAttemptIntervalInMSec)
					tempSerialPort.tryConnectWithPort((tempSerialPort.getPortIdx() + 1) % Serial.list().length);
		}
		else if (serialPorts.size() == 1) {
			if (tempSerialPort == null) {
				tempSerialPort = new SerialPort(p5, G.arduinoConnectionChkMsg);
				tempSerialPort.addListener(this);
			}
			else if (!tempSerialPort.isConnected())
				if (tempSerialPort.getElapsedTimeSinceLastConnectionAttemptInMsec() >= connectionAttemptIntervalInMSec) {
					int portIdx_ = (tempSerialPort.getPortIdx() + 1) % Serial.list().length;
					if (portIdx_ != serialPorts.get(0).getPortIdx())
						tempSerialPort.tryConnectWithPort(portIdx_);
					else
						tempSerialPort.tryConnectWithPort((portIdx_ + 1) % Serial.list().length);
				}
		}
	}

	public void dispose() {
		serialPorts = null;
		tempSerialPort = null;
	}

	public ArrayList<SerialPort> getSerialPorts() {
		return serialPorts;
	}

	public SerialPort getTempSerialPort() {
		return tempSerialPort;
	}

	private void printSerialList() {
		String log_ = "<SerialPortManager>\tPortList...\n";
		for (int i = 0; i < Serial.list().length; i++)
			log_ += "\t:[" + i + "] " + Serial.list()[i] + "\n";
		System.out.print(log_);
	}

	@Override
	public void serialCallBack(SerialPort _serialPort, String _msg) {
		if (serialPorts.size() < targetSerialPortsNum)
			if (_serialPort.isConnected())
				if (!serialPorts.contains(_serialPort)) {
					_serialPort.removeListener(this);
					serialPorts.add(_serialPort);
					tempSerialPort = null;
				}
	}
}
