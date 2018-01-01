package oscComm;

import java.util.concurrent.TimeUnit;

import drawing.Drawing;
import grbl.Grbl;
import grbl.SerialComm;
import main.Setting;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import tabletInput.TabletInput;

public class OscComm {

	private PApplet	p5			= null;
	private Drawing	drawing	= null;

	private final int			connectPeriodMsec								= 1000;
	private final String	addrPtrnSyn											= "Syn";
	private final String	addrPtrnSynAck									= "SynAck";
	private final String	addrPtrnAck											= "Ack";
	private final String	addrPtrnDisconnect							= "Disconnect";
	private final String	addrPtrnTabletInputMsg					= "TabletInputMsg";
	private final String	addrPtrnIsReadyToWriteStatusMsg	= "IsReadyToWriteStatusMsg";

	private long		connectTrialTimeUsec	= 0;
	private boolean	isConnected						= false;
	private boolean	isRecievedSynMsg			= false;
	private boolean	isRecievedSynAckMsg		= false;

	private boolean	serialCommIsConnected	= false;
	private boolean	tabletInputIsWritable	= false;
	private boolean	grblIsIdle						= false;

	private boolean isTargetReadyToWrite = false;

	private OscP5				oscPort			= null;
	private NetAddress	targetAddr	= null;
	private OscMessage	msg					= null;

	public StringBuffer prtTxtBfr = null;

	public void setDrawing(Drawing _drawing) {
		drawing = _drawing;
	}

	public OscComm(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);
		p5.registerMethod("dispose", this);

		prtTxtBfr = new StringBuffer();

		msg = new OscMessage("");

		openPort();
		setTargetAddr();
	}

	public void pre() {
		tryConnectPeriodically(connectPeriodMsec);
	}

	public void dispose() {
		if (oscPort != null)
			closePort();
	}

	private void tryConnectPeriodically(int _periodMsec) {
		if (!isConnected && (!isRecievedSynMsg || !isRecievedSynAckMsg)) {
			if (getTimePassedMsec() >= _periodMsec) {
				setConnect(false, Setting.targetIp, Setting.targetPort);
				sendOscConnectionMsg(addrPtrnSyn);
				prtTxtBfr.append("<OSC>").append('\t').append("Send (Syn) Msg to ").append(Setting.targetIp).append(":")
						.append(Setting.targetPort);
				System.out.println(prtTxtBfr);
				prtTxtBfr.setLength(0);
				connectTrialTimeUsec = System.nanoTime();
			}
		}
	}

	private long getTimePassedMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - connectTrialTimeUsec);
	}

	private void setConnect(boolean _isConnected, String _receivedIp, int _receivedPort) {
		if (_isConnected != isConnected) {
			isConnected = _isConnected;
			if (isConnected) {
				isConnected = true;
				prtTxtBfr.append("<OSC>").append('\t').append("Connected with ")//
						.append(_receivedIp)//
						.append(":")//
						.append(_receivedPort);
				System.out.println(prtTxtBfr);
				prtTxtBfr.setLength(0);
			}
			else {
				prtTxtBfr.append("<OSC>").append('\t').append("Disconnected");
				System.out.println(prtTxtBfr);
				prtTxtBfr.setLength(0);
				isRecievedSynMsg = false;
				isRecievedSynAckMsg = false;
				sendOscConnectionMsg(addrPtrnDisconnect);
			}
		}
	}

	private void closePort() {
		setConnect(false, Setting.targetIp, Setting.targetPort);
		oscPort.stop();
		oscPort = null;
	}

	private void openPort() {
		if (oscPort != null)
			oscPort.stop();
		oscPort = new OscP5(p5, Setting.myPort);
	}

	private void setTargetAddr() {
		targetAddr = new NetAddress(Setting.targetIp, Setting.targetPort);
		prtTxtBfr.append("<OSC>").append('\t').append("Target address...").append('\n');
		prtTxtBfr.append('\t').append(Setting.targetIp).append(":").append(Setting.targetPort);
		System.out.println(prtTxtBfr);
		prtTxtBfr.setLength(0);
	}

	public void sendOscConnectionMsg(String _addrPattern) {
		msg.clear();
		msg.setAddrPattern(_addrPattern);
		msg.add(oscPort.ip())//
				.add(Setting.myPort);
		oscPort.send(msg, targetAddr);
	}

	public void sendStylusInputMsg(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y,
			float _pressure, float _tiltX, float _tiltY, long _evtTimeInMsec, int _type) {
		msg.clear();
		msg.setAddrPattern(addrPtrnTabletInputMsg);
		msg.add(_nthPoint);
		msg.add(_nthStroke);
		msg.add(_nthPointInStroke);
		msg.add(_x);
		msg.add(_y);
		msg.add(_pressure);
		msg.add(_tiltX);
		msg.add(_tiltY);
		msg.add(Long.toString(_evtTimeInMsec));
		msg.add(_type);
		oscPort.send(msg, targetAddr);
	}

	public void sendReadyToWriteStatusMsg() {
		msg.clear();
		msg.setAddrPattern(addrPtrnIsReadyToWriteStatusMsg);
		msg.add((serialCommIsConnected && tabletInputIsWritable && grblIsIdle) ? 1 : 0);
		oscPort.send(msg, targetAddr);
	}

	public void updateSerialCommIsConnected(boolean _serialCommIsConnected) {
		serialCommIsConnected = _serialCommIsConnected;
		sendReadyToWriteStatusMsg();
	}

	public void updateTabletInputIsWritable(boolean _tabletInputIsWritable) {
		tabletInputIsWritable = _tabletInputIsWritable;
		sendReadyToWriteStatusMsg();
	}

	public void updateGrblIsIdle(boolean _grblIsIdle) {
		grblIsIdle = _grblIsIdle;
		sendReadyToWriteStatusMsg();
	}

	public void receive(OscMessage _oscMsg) {
		if (_oscMsg.addrPattern().equals(addrPtrnSyn) || _oscMsg.addrPattern().equals(addrPtrnSynAck)
				|| _oscMsg.addrPattern().equals(addrPtrnAck) || _oscMsg.addrPattern().equals(addrPtrnDisconnect)) {
			String receivedIp_ = _oscMsg.get(0).stringValue();
			int receivedPort_ = _oscMsg.get(1).intValue();
			if (receivedIp_.equals(Setting.targetIp) && receivedPort_ == Setting.targetPort) {
				if (!isConnected) {
					if (_oscMsg.addrPattern().equals(addrPtrnSyn)) {
						isRecievedSynMsg = true;
						prtTxtBfr.append("<OSC>").append('\t').append("Got a (Syn) Msg from ")//
								.append(receivedIp_)//
								.append(":")//
								.append(receivedPort_)//
								.append('\n');
						prtTxtBfr.append("<OSC>").append('\t').append("Send back (Syn + Ack) Msg to ")//
								.append(receivedIp_)//
								.append(":")//
								.append(receivedPort_);
						System.out.println(prtTxtBfr);
						prtTxtBfr.setLength(0);
						sendOscConnectionMsg(addrPtrnSynAck);
					}
					else if (_oscMsg.addrPattern().equals(addrPtrnSynAck)) {
						isRecievedSynAckMsg = true;
						prtTxtBfr.append("<OSC>").append('\t').append("Got a (Syn + Ack) Msg from ")//
								.append(receivedIp_)//
								.append(":")//
								.append(receivedPort_)//
								.append('\n');
						prtTxtBfr.append("<OSC>").append('\t').append("Send back (Ack) Msg to ")//
								.append(receivedIp_)//
								.append(":")//
								.append(receivedPort_);
						System.out.println(prtTxtBfr);
						prtTxtBfr.setLength(0);
						sendOscConnectionMsg(addrPtrnAck);
						setConnect(true, receivedIp_, receivedPort_);
					}
					else if (_oscMsg.addrPattern().equals(addrPtrnAck)) {
						prtTxtBfr.append("<OSC>").append('\t').append("Got a (Ack) Msg from ")//
								.append(receivedIp_)//
								.append(":")//
								.append(receivedPort_);
						System.out.println(prtTxtBfr);
						prtTxtBfr.setLength(0);
						setConnect(true, receivedIp_, receivedPort_);
					}
				}
				else if (_oscMsg.addrPattern().equals(addrPtrnDisconnect))
					setConnect(false, receivedIp_, receivedPort_);
			}
		}
		else if (_oscMsg.addrPattern().equals(addrPtrnTabletInputMsg)) {
			int nthPoint_ = _oscMsg.get(0).intValue();
			int nthStroke_ = _oscMsg.get(1).intValue();
			int nthPointInStroke_ = _oscMsg.get(2).intValue();
			float x_ = _oscMsg.get(3).floatValue();
			float y_ = _oscMsg.get(4).floatValue();
			float pressure_ = _oscMsg.get(5).floatValue();
			float tiltX_ = _oscMsg.get(6).floatValue();
			float tiltY_ = _oscMsg.get(7).floatValue();
			long evtTimeInMsec_ = Long.parseLong(_oscMsg.get(8).stringValue());
			int type_ = _oscMsg.get(9).intValue();
			if (type_ == 0)
				drawing.addStroke(nthPoint_, nthStroke_, nthPointInStroke_, x_, y_, pressure_, tiltX_, tiltY_, evtTimeInMsec_,
						type_);
			else
				drawing.addPoint(nthPoint_, nthStroke_, nthPointInStroke_, x_, y_, pressure_, tiltX_, tiltY_, evtTimeInMsec_,
						type_);
		}
		else if (_oscMsg.addrPattern().equals(addrPtrnIsReadyToWriteStatusMsg)) {
			isTargetReadyToWrite = _oscMsg.get(0).intValue() == 1;
		}
	}

	public void activateAutoConnect() {
		setConnect(false, Setting.targetIp, Setting.targetPort);
		tryConnectPeriodically(connectPeriodMsec);
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean isTargetReadyToWrite() {
		return isTargetReadyToWrite;
	}
}