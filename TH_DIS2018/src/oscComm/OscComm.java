package oscComm;

import java.util.concurrent.TimeUnit;

import drawing.Drawing;
import main.Setting;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

public class OscComm {
	private PApplet	p5			= null;
	private Drawing	drawing	= null;

	private final int			connectPeriodMsec			= 1000;
	private final String	addrPtrnSyn						= "Syn";
	private final String	addrPtrnSynAck				= "SynAck";
	private final String	addrPtrnAck						= "Ack";
	private final String	addrPtrnDisconnect		= "Disconnect";
	private final String	addrPtrnTabletInput		= "TabletInput";
	private final String	addrPtrnStatusReport	= "StatusReport";

	private long		connectTrialTimeUsec	= 0;
	private boolean	isConnected						= false;
	private boolean	isRecievedSynMsg			= false;
	private boolean	isRecievedSynAckMsg		= false;

	private boolean isTargetIdle = true;

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

	public void tryConnectPeriodically(int _periodMsec) {
		if (!isConnected && (!isRecievedSynMsg || !isRecievedSynAckMsg)) {
			if (getTimePassedMsec() >= _periodMsec) {
				setConnect(false, Setting.targetIp, Setting.targetPort);
				sendConnectionMsg(addrPtrnSyn);
				prtTxtBfr.append("<OSC>").append('\t').append("Send (Syn) Msg to ").append(Setting.targetIp).append(":")
						.append(Setting.targetPort);
				System.out.println(prtTxtBfr);
				prtTxtBfr.setLength(0);
				connectTrialTimeUsec = System.nanoTime();
			}
		}
	}

	public long getTimePassedMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - connectTrialTimeUsec);
	}

	public void setConnect(boolean _isConnected, String _receivedIp, int _receivedPort) {
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
				sendConnectionMsg(addrPtrnDisconnect);
			}
		}
	}

	public void dispose() {
		if (oscPort != null)
			closePort();
	}

	public void closePort() {
		setConnect(false, Setting.targetIp, Setting.targetPort);
		oscPort.stop();
		oscPort = null;
	}

	public void openPort() {
		if (oscPort != null)
			oscPort.stop();
		oscPort = new OscP5(p5, Setting.myPort);
	}

	public void setTargetAddr() {
		targetAddr = new NetAddress(Setting.targetIp, Setting.targetPort);
		prtTxtBfr.append("<OSC>").append('\t').append("Target address...").append('\n');
		prtTxtBfr.append('\t').append(Setting.targetIp).append(":").append(Setting.targetPort);
		System.out.println(prtTxtBfr);
		prtTxtBfr.setLength(0);
	}

	public void sendConnectionMsg(String _addrPattern) {
		msg.clear();
		msg.setAddrPattern(_addrPattern);
		msg.add(oscPort.ip())//
				.add(Setting.myPort);
		oscPort.send(msg, targetAddr);
	}

	public void sendStylusInputMsg(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY,
			float _pressure, float _tiltX, float _tiltY, long _millis, int _kind) {
		msg.clear();
		msg.setAddrPattern(addrPtrnTabletInput);
		msg.add(_totalPointIdx);
		msg.add(_strokeIdx);
		msg.add(_pointIdx);
		msg.add(_penX);
		msg.add(_penY);
		msg.add(_pressure);
		msg.add(_tiltX);
		msg.add(_tiltY);
		msg.add(Long.toString(_millis));
		msg.add(_kind);
		oscPort.send(msg, targetAddr);
	}

	public void sendStatusMsg(boolean _isIdle) {
		msg.clear();
		msg.setAddrPattern(addrPtrnStatusReport);
		msg.add(_isIdle ? 1 : 0);
		oscPort.send(msg, targetAddr);
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
						sendConnectionMsg(addrPtrnSynAck);
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
						sendConnectionMsg(addrPtrnAck);
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
		else if (_oscMsg.addrPattern().equals(addrPtrnTabletInput)) {
			int totalPointIdx_ = _oscMsg.get(0).intValue();
			int strokeIdx_ = _oscMsg.get(1).intValue();
			int pointIdx_ = _oscMsg.get(2).intValue();
			float penX_ = _oscMsg.get(3).floatValue();
			float penY_ = _oscMsg.get(4).floatValue();
			float pressure_ = _oscMsg.get(5).floatValue();
			float tiltX_ = _oscMsg.get(6).floatValue();
			float tiltY_ = _oscMsg.get(7).floatValue();
			long millis_ = Long.parseLong(_oscMsg.get(8).stringValue());
			int kind_ = _oscMsg.get(9).intValue();
			if (kind_ == 0)
				drawing.addStroke(totalPointIdx_, strokeIdx_, pointIdx_, penX_, penY_, pressure_, tiltX_, tiltY_, millis_,
						kind_);
			else
				drawing.addPoint(totalPointIdx_, strokeIdx_, pointIdx_, penX_, penY_, pressure_, tiltX_, tiltY_, millis_,
						kind_);
		}
		else if (_oscMsg.addrPattern().equals(addrPtrnStatusReport)) {
			isTargetIdle = _oscMsg.get(0).intValue() == 1;
		}
	}

	public void activateAutoConnect() {
		setConnect(false, Setting.targetIp, Setting.targetPort);
		tryConnectPeriodically(connectPeriodMsec);
	}
}