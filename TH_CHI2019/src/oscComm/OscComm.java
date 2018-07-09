package oscComm;

import java.util.concurrent.TimeUnit;

import drawing.Drawing;
import main.Setting;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import tabletInput.TabletInput;

public class OscComm {
	private PApplet			p5					= null;
	private TabletInput	tabletInput	= null;
	private Drawing			drawing			= null;

	private final int			connectionTryPeriodInMsec			= 1000;
	private final String	msgPrefixConnectSyn						= "Syn";
	private final String	msgPrefixConnectSynAck				= "SynAck";
	private final String	msgPrefixConnectAck						= "Ack";
	private final String	msgPrefixDisconnect						= "Disconnect";
	private final String	msgPrefixTabletInputMsg				= "TabletInputMsg";
	private final String	msgPrefixIsWritableMsg				= "IsWritableMsg";
	private final String	msgPrefixRequestForIsWritable	= "RequestForIsWritable";

	private long		lastConnectionTryTimeInUsec	= 0;
	private boolean	isConnected									= false;
	private boolean	isRecievedSynMsg						= false;
	private boolean	isRecievedSynAckMsg					= false;

	private boolean	serialCommIsConnected	= false;
	private boolean	tabletInputIsWritable	= false;
	private boolean	grblIsMoving					= false;

	private boolean isTargetWritable = false;

	private OscP5				oscP5				= null;
	private NetAddress	targetAddr	= null;

	public void setTabletInput(TabletInput _tabletInput) {
		tabletInput = _tabletInput;
	}

	public void setDrawing(Drawing _drawing) {
		drawing = _drawing;
	}

	public OscComm(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("pre", this);
		p5.registerMethod("dispose", this);

		oscP5Init();
		setTargetAddr();

		activateAutoConnect();
	}

	public void pre() {
		tryConnectPeriodically();
	}

	private void tryConnectPeriodically() {
		if (!isConnected && (!isRecievedSynMsg || !isRecievedSynAckMsg)) {
			if (timeSinceLastConnectionTryInMsec() >= connectionTryPeriodInMsec) {
				setConnect(false, Setting.targetIp, Setting.targetPort);
				sendOscConnectionMsg(msgPrefixConnectSyn);
				String print_ = "<OSC>\tSend (Syn) Msg to " + Setting.targetIp + ":" + Setting.targetPort;
				System.out.println(print_);
				lastConnectionTryTimeInUsec = System.nanoTime();
			}
		}
	}

	private long timeSinceLastConnectionTryInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastConnectionTryTimeInUsec);
	}

	private void setConnect(boolean _isConnected, String _connectedIp, int _connectedPort) {
		if (_isConnected != isConnected) {
			isConnected = _isConnected;
			String print_;
			if (isConnected) {
				print_ = "<OSC>\tConnected with " + _connectedIp + ":" + _connectedPort;
				System.out.println(print_);
			}
			else {
				print_ = "<OSC>\tDisconnected";
				System.out.println(print_);
				isRecievedSynMsg = false;
				isRecievedSynAckMsg = false;
				sendOscConnectionMsg(msgPrefixDisconnect);
				oscP5.disconnect(targetAddr);
			}
		}
	}

	public void dispose() {
		disposeOscP5();
	}

	private void disposeOscP5() {
		if (oscP5 != null) {
			setConnect(false, Setting.targetIp, Setting.targetPort);
			oscP5.stop();
			oscP5.dispose();
			oscP5 = null;
		}
	}

	private void oscP5Init() {
		disposeOscP5();
		oscP5 = new OscP5(p5, Setting.myPort);
	}

	private void setTargetAddr() {
		targetAddr = new NetAddress(Setting.targetIp, Setting.targetPort);
		String print_ = "<OSC>\tTarget address is " + Setting.targetIp + ":" + Setting.targetPort;
		System.out.println(print_);
	}

	public void sendOscConnectionMsg(String _msgPrefixConnect) {
		OscMessage msg_ = new OscMessage(_msgPrefixConnect);
		msg_.add(oscP5.ip())//
				.add(Setting.myPort);
		oscP5.send(msg_, targetAddr);
	}

	public void sendStylusInputMsg(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y,
			float _pressure, float _tiltX, float _tiltY, long _evtTimeInMsec, int _type) {
		OscMessage msg_ = new OscMessage(msgPrefixTabletInputMsg);
		msg_.add(_nthPoint);
		msg_.add(_nthStroke);
		msg_.add(_nthPointInStroke);
		msg_.add(_x);
		msg_.add(_y);
		msg_.add(_pressure);
		msg_.add(_tiltX);
		msg_.add(_tiltY);
		msg_.add(Long.toString(_evtTimeInMsec));
		msg_.add(_type);
		oscP5.send(msg_, targetAddr);
	}

	public void sendRequestForIsWritable() {
		OscMessage msg_ = new OscMessage(msgPrefixRequestForIsWritable);
		oscP5.send(msg_, targetAddr);
	}

	public void sendIsWritableMsg() {
		OscMessage msg_ = new OscMessage(msgPrefixIsWritableMsg);
		// msg_.add((serialCommIsConnected && tabletInputIsWritable &&
		// !grblIsMoving) ? 1 : 0);
		if (serialCommIsConnected && !grblIsMoving) {
			if (!tabletInput.isWritable())
				tabletInput.setWritable(true);
			msg_.add(1);
		}
		else
			msg_.add(0);
		oscP5.send(msg_, targetAddr);
	}

	public void updateSerialCommIsConnected(boolean _serialCommIsConnected) {
		serialCommIsConnected = _serialCommIsConnected;
		sendIsWritableMsg();
	}

	public void updateTabletInputIsWritable(boolean _tabletInputIsWritable) {
		tabletInputIsWritable = _tabletInputIsWritable;
		sendIsWritableMsg();
	}

	public void updateGrblIsMoving(boolean _grblIsMoving) {
		grblIsMoving = _grblIsMoving;
		sendIsWritableMsg();
	}

	public void receive(OscMessage _oscMsg) {
		if (_oscMsg.addrPattern().equals(msgPrefixConnectSyn) || _oscMsg.addrPattern().equals(msgPrefixConnectSynAck)
				|| _oscMsg.addrPattern().equals(msgPrefixConnectAck) || _oscMsg.addrPattern().equals(msgPrefixDisconnect)) {
			String receivedIp_ = _oscMsg.get(0).stringValue();
			int receivedPort_ = _oscMsg.get(1).intValue();
			if (receivedIp_.equals(Setting.targetIp) && receivedPort_ == Setting.targetPort) {
				if (!isConnected) {
					String print_;
					if (_oscMsg.addrPattern().equals(msgPrefixConnectSyn)) {
						isRecievedSynMsg = true;
						print_ = "<OSC>\tGot a (Syn) Msg from " + receivedIp_ + ":" + receivedPort_ + "\n";
						print_ += "<OSC>\tSend back (Syn + Ack) Msg to " + receivedIp_ + ":" + receivedPort_;
						System.out.println(print_);
						sendOscConnectionMsg(msgPrefixConnectSynAck);
					}
					else if (_oscMsg.addrPattern().equals(msgPrefixConnectSynAck)) {
						isRecievedSynAckMsg = true;
						print_ = "<OSC>\tGot a (Syn + Ack) Msg from " + receivedIp_ + ":" + receivedPort_ + "\n";
						print_ += "<OSC>\tSend back (Ack) Msg to " + receivedIp_ + ":" + receivedPort_;
						System.out.println(print_);
						sendOscConnectionMsg(msgPrefixConnectAck);
						setConnect(true, receivedIp_, receivedPort_);
						sendRequestForIsWritable();
					}
					else if (_oscMsg.addrPattern().equals(msgPrefixConnectAck)) {
						print_ = "<OSC>\tGot a (Ack) Msg from " + receivedIp_ + ":" + receivedPort_;
						System.out.println(print_);
						setConnect(true, receivedIp_, receivedPort_);
						sendRequestForIsWritable();
					}
				}
				else if (_oscMsg.addrPattern().equals(msgPrefixDisconnect))
					setConnect(false, receivedIp_, receivedPort_);
			}
		}
		else if (_oscMsg.addrPattern().equals(msgPrefixTabletInputMsg)) {
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
		else if (_oscMsg.addrPattern().equals(msgPrefixIsWritableMsg)) {
			isTargetWritable = _oscMsg.get(0).intValue() == 1;
		}
		else if (_oscMsg.addrPattern().equals(msgPrefixRequestForIsWritable)) {
			sendIsWritableMsg();
		}
	}

	public void activateAutoConnect() {
		setConnect(false, Setting.targetIp, Setting.targetPort);
		lastConnectionTryTimeInUsec = 0;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean isTargetWritable() {
		return isTargetWritable;
	}
}