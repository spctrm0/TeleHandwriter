package osc;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import serial.SerialCallback;

public class OscPort {

	public LinkedList<OscCallback> listeners = null;

	public void addListener(OscCallback _listener) {
		if (!listeners.contains(_listener))
			listeners.addLast(_listener);
	}

	public void removeListener(OscCallback _listener) {
		if (listeners.contains(_listener))
			listeners.remove(_listener);
	}

	public boolean containsListener(OscCallback _listener) {
		return listeners.contains(_listener);
	}

	private PApplet p5 = null;

	private OscP5				oscP5				= null;
	private String			myIp				= null;
	private int					myPort			= 0;
	private NetAddress	targetAddr	= null;
	private String			targetIp		= null;
	private int					targetPort	= 0;

	public static final String	prefixConnectionSyn			= "Syn";
	public static final String	prefixConnectionSynAck	= "SynAck";
	public static final String	prefixConnectionAck			= "Ack";
	public static final String	prefixDisconnect				= "Disconnect";

	private long		lastConnectionAttemptInUsec	= 0;
	private boolean	isConnected									= false;
	private boolean	isConnecting								= false;

	private final int connectionAttemptIntervalInMSec = 2000;

	public OscPort(PApplet _p5, int _myPort) {
		p5 = _p5;

		listeners = new LinkedList<OscCallback>();

		p5.registerMethod("dispose", this);

		setMyAddr(_myPort);
	}

	public void dispose() {
		setConnected(false, true);
	}

	public void pre() {
		if (getElapsedTimeSinceLastConnectionAttemptInMsec() >= connectionAttemptIntervalInMSec) {
			sendOscConnectionMsg(prefixConnectionSyn);
			String print_ = "<OscPort>\tSend (Syn) Msg to " + targetIp + ":" + targetPort + ".";
			System.out.println(print_);
			setLastConnectionAttemptInUsec();
		}
	}

	public OscP5 getOscP5() {
		return oscP5;
	}

	public String getMyIp() {
		return myIp;
	}

	public int getMyPort() {
		return myPort;
	}

	public void setMyAddr(int _myPort) {
		myPort = _myPort;
		disconnect();
		oscP5 = new OscP5(p5, _myPort);
		myIp = oscP5.ip();
	}

	public String getTargetIp() {
		return targetIp;
	}

	public int getTargetPort() {
		return targetPort;
	}

	public void setTargetAddr(String _targetIp, int _targetPort) {
		targetIp = _targetIp;
		targetPort = _targetPort;
		disconnect();
		targetAddr = new NetAddress(targetIp, targetPort);
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean isConnecting() {
		return isConnecting;
	}

	public void tryConnectWithAddr(String _targetIp, int _targetPort) {
		setTargetAddr(_targetIp, _targetPort);
		tryConnect();
	}

	public void tryConnect() {
		String log_ = "<OscPort>\tTry to connect with " + targetIp + ":" + targetPort + ".";
		System.out.println(log_);
		activateSendSynMsgPeriodically();
	}

	public long getElapsedTimeSinceLastConnectionAttemptInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastConnectionAttemptInUsec);
	}

	public void disconnect() {
		setConnected(false, false);
	}

	public void readAndCallback(OscMessage _oscMsg) {
		if (_oscMsg.addrPattern().equals(prefixConnectionSyn) || _oscMsg.addrPattern().equals(prefixConnectionSynAck)
				|| _oscMsg.addrPattern().equals(prefixConnectionAck) || _oscMsg.addrPattern().equals(prefixDisconnect)) {
			String receivedIp_ = _oscMsg.get(0).stringValue();
			int receivedPort_ = _oscMsg.get(1).intValue();
			if (receivedIp_.equals(targetIp) && receivedPort_ == targetPort) {
				if (_oscMsg.addrPattern().equals(prefixDisconnect))
					disconnect();
				else if (!isConnected) {
					String log_;
					if (_oscMsg.addrPattern().equals(prefixConnectionSyn)) {
						deactivateSendSynMsgPeriodically();
						log_ = "<OscPort>\tGot a (Syn) Msg from " + targetIp + ":" + targetPort + ".\n";
						log_ += "<OscPort>\tSend back (Syn + Ack) Msg to " + targetIp + ":" + targetPort + ".";
						System.out.println(log_);
						sendOscConnectionMsg(prefixConnectionSynAck);
					}
					else if (_oscMsg.addrPattern().equals(prefixConnectionSynAck)) {
						deactivateSendSynMsgPeriodically();
						log_ = "<OscPort>\tGot a (Syn + Ack) Msg from " + targetIp + ":" + targetPort + ".\n";
						log_ += "<OscPort>\tSend back (Ack) Msg to " + targetIp + ":" + targetPort + ".";
						System.out.println(log_);
						sendOscConnectionMsg(prefixConnectionAck);
						setConnected(true, false);
					}
					else if (_oscMsg.addrPattern().equals(prefixConnectionAck))
						setConnected(true, false);
				}
			}
		}
		else
			for (OscCallback listener_ : listeners)
				listener_.oscMsgCallBack(_oscMsg);
	}

	public void write(OscMessage _oscMsg) {
		oscP5.send(_oscMsg);
	}

	private void setConnected(boolean _isConnected, boolean _forced) {
		boolean isChanged_ = _isConnected != isConnected;
		isConnected = _isConnected;
		String log_;
		if (isConnected && isChanged_) {
			for (OscCallback listener_ : listeners)
				listener_.oscConnectionCallBack(this, true);
			log_ = "<OscPort>\tConnected with " + targetAddr.address() + ":" + targetAddr.port() + ".";
			System.out.println(log_);
		}
		else if (!isConnected && isConnecting) {
			deactivateSendSynMsgPeriodically();
			if (oscP5 != null) {
				oscP5.stop();
				oscP5 = null;
			}
			for (OscCallback listener_ : listeners)
				listener_.oscConnectionCallBack(this, false);
		}
		else if (!isConnected && (isChanged_ || _forced)) {
			if (oscP5 != null) {
				oscP5.stop();
				oscP5 = null;
			}
			for (OscCallback listener_ : listeners)
				listener_.oscConnectionCallBack(this, false);
			log_ = "<OscPort>\tDisconnected with " + targetAddr.address() + ":" + targetAddr.port() + ".";
			System.out.println(log_);
		}
	}

	private void setLastConnectionAttemptInUsec() {
		lastConnectionAttemptInUsec = System.nanoTime();
	}

	private void activateSendSynMsgPeriodically() {
		if (!isConnecting) {
			isConnecting = true;
			if (oscP5 == null)
				oscP5 = new OscP5(p5, myPort);
			p5.registerMethod("pre", this);
		}
	}

	private void deactivateSendSynMsgPeriodically() {
		if (isConnecting) {
			isConnecting = false;
			p5.unregisterMethod("pre", this);
		}
	}

	private void sendOscConnectionMsg(String _prefixConnection) {
		OscMessage msg_ = new OscMessage(_prefixConnection);
		msg_.add(myIp).add(myPort);
		oscP5.send(msg_, targetAddr);
	}
}
