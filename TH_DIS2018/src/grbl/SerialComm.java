package grbl;

import java.util.concurrent.TimeUnit;

import main.Setting;
import processing.core.PApplet;
import processing.serial.Serial;

public class SerialComm {
	public PApplet		p5						= null;
	public Grbl			grbl					= null;

	public final int	connectIntervalMsec		= 3000;
	public final int	baudRate				= 250000;
	public final char	parity					= 'n';
	public final int	dataBits				= 8;
	public final float	stopBits				= 1.0f;
	public final char	delimeter				= '\r';

	public long			connectTrialTimeUsec	= 0;
	public int			portIdx					= 0;
	public boolean		isConnected				= false;

	public Serial		srlPort					= null;

	public StringBuffer	charToStrBfr			= null;
	public StringBuffer	prtTxtBfr				= null;

	public void setGrbl(Grbl _grbl) {
		grbl = _grbl;
	}

	public SerialComm(PApplet _p5) {
		p5 = _p5;

		p5.registerMethod("pre", this);
		p5.registerMethod("dispose", this);

		charToStrBfr = new StringBuffer();
		prtTxtBfr = new StringBuffer();

		printSerialList();
	}

	public void pre() {
		if (!isConnected)
			connecting(connectIntervalMsec);
	}

	public void connecting(int _tryIntervalMsec) {
		if (getWaitingTimeMsec() >= _tryIntervalMsec) {
			if (Serial.list().length > 0) {
				portIdx++;
				if (portIdx >= Serial.list().length)
					portIdx = 0;
				connect(portIdx);
			}
		}
	}

	public long getWaitingTimeMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - connectTrialTimeUsec);
	}

	public void connect(int _portIdx) {
		if (Serial.list().length > 0 && _portIdx < Serial.list().length) {
			String portName_ = Serial.list()[_portIdx];
			if (srlPort != null)
				srlPort.stop();
			srlPort = new Serial(p5, portName_, baudRate, parity, dataBits, stopBits);
			connectTrialTimeUsec = System.nanoTime();
			prtTxtBfr.append("<SRL>").append('\t').append("Try to connect with ").append(portName_);
			System.out.println(prtTxtBfr.toString());
			prtTxtBfr.setLength(0);
		}
	}

	public void dispose() {
		if (srlPort != null)
			disconnect();
	}

	public void disconnect() {
		srlPort.stop();
		srlPort = null;
		boolean wasConnected_ = isConnected;
		isConnected = false;
		if (wasConnected_ != isConnected) {
			prtTxtBfr.append("<SRL>").append('\t').append("Disconnected");
			System.out.println(prtTxtBfr.toString());
			prtTxtBfr.setLength(0);
		}
	}

	public void printSerialList() {
		prtTxtBfr.append("<SRL>").append('\t').append("PortList...").append('\n');
		for (int i = 0; i < Serial.list().length; i++) {
			String portName_ = Serial.list()[i];
			prtTxtBfr.append('\t').append("[").append(i).append("] ").append(portName_).append('\n');
		}
		System.out.print(prtTxtBfr.toString());
		prtTxtBfr.setLength(0);
	}

	public void read(char _char) {
		if (_char != delimeter)
			charToStrBfr.append(_char);
		else {
			String msg_ = charToStrBfr.toString().trim();
			if (!msg_.isEmpty()) {
				if (!isConnected) {
					if (msg_.equals(Setting.connectionChkTxt)) {
						boolean wasConnected_ = isConnected;
						isConnected = true;
						if (wasConnected_ != isConnected) {
							prtTxtBfr.append("<SRL>").append('\t').append("Connected with ")
									.append(srlPort.port.getPortName()).append('\n');
							prtTxtBfr.append("<GRBL>").append('\t').append(msg_);
							System.out.println(prtTxtBfr.toString());
							prtTxtBfr.setLength(0);
						}
						grbl.init();
					}
				} else
					grbl.read(msg_);
			}
			charToStrBfr.setLength(0);
		}
	}

	public void write(String _msg) {
		srlPort.write(_msg);
	}
}
