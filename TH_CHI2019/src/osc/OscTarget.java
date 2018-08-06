package osc;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import netP5.NetAddress;
import oscP5.OscMessage;
import processing.core.PApplet;

public class OscTarget {

	private LinkedList<OscCallback> listeners = null;

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

	private PApplet	p5			= null;
	private OscComm	oscComm	= null;

	private NetAddress	addr	= null;
	private String			ip		= null;
	private int					port	= 0;

	public static final String	prefixConnectionSyn			= "Syn";
	public static final String	prefixConnectionSynAck	= "SynAck";
	public static final String	prefixConnectionAck			= "Ack";
	public static final String	prefixDisconnect				= "Disconnect";

	private long		lastConnectionAttemptInUsec	= 0;
	private boolean	isConnected									= false;
	private boolean	isConnecting								= false;

	public static final int connectionAttemptIntervalInMSec = 2000;

	public OscTarget(PApplet _p5, OscComm _oscComm, String _ip, int _port) {
		p5 = _p5;
		oscComm = _oscComm;

		listeners = new LinkedList<OscCallback>();

		p5.registerMethod("dispose", this);

		ip = _ip;
		port = _port;
		addr = new NetAddress(ip, port);
	}

	public void pre() {
		if (getElapsedTimeSinceLastConnectionAttemptInMsec() >= connectionAttemptIntervalInMSec) {
			sendOscConnectionMsg(prefixConnectionSyn);
			String log_ = "<" + getClass().getName() + ">\tSend \"Syn\" msg to " + ip + ":" + port + ".";
			System.out.println(log_);
			setLastConnectionAttemptInUsec();
		}
	}

	public void dispose() {
		disconnect();
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public void setAddr(String _ip, int _port) {
		ip = _ip;
		port = _port;
		disconnect();
		addr = new NetAddress(ip, port);
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean isConnecting() {
		return isConnecting;
	}

	public void tryConnect() {
		disconnect();
		activateSendSynMsgPeriodically();
		String log_ = "<" + getClass().getName() + ">\tTry to connect with " + addr.address() + ":" + addr.port() + ".";
		System.out.println(log_);
	}

	public void disconnect() {
		if (isConnected)
			sendOscConnectionMsg(prefixDisconnect);
		setConnected(false);
	}

	public void write(OscMessage _oscMsg) {
		_oscMsg.add(oscComm.getIp()).add(oscComm.getPort());
		oscComm.getOscP5().send(_oscMsg, addr);
	}

	protected void readAndCallback(OscMessage _oscMsg) {
		if (_oscMsg.addrPattern().equals(prefixConnectionSyn) || _oscMsg.addrPattern().equals(prefixConnectionSynAck)
				|| _oscMsg.addrPattern().equals(prefixConnectionAck) || _oscMsg.addrPattern().equals(prefixDisconnect)) {
			String log_;
			if (_oscMsg.addrPattern().equals(prefixDisconnect)) {
				log_ = "<" + getClass().getName() + ">\tGot a disconnect msg from " + ip + ":" + port + ".";
				System.out.println(log_);
				setConnected(false);
			}
			else if (!isConnected) {
				if (_oscMsg.addrPattern().equals(prefixConnectionSyn)) {
					log_ = "<" + getClass().getName() + ">\tGot a \"Syn\" msg from " + ip + ":" + port + ".";
					System.out.println(log_);
					deactivateSendSynMsgPeriodically();
					sendOscConnectionMsg(prefixConnectionSynAck);
					log_ = "<" + getClass().getName() + ">\tSend back \"Syn+Ack\" msg to " + ip + ":" + port + ".";
					System.out.println(log_);
				}
				else if (_oscMsg.addrPattern().equals(prefixConnectionSynAck)) {
					log_ = "<" + getClass().getName() + ">\tGot a \"Syn+Ack\" msg from " + ip + ":" + port + ".";
					System.out.println(log_);
					deactivateSendSynMsgPeriodically();
					sendOscConnectionMsg(prefixConnectionAck);
					log_ = "<" + getClass().getName() + ">\tSend back \"Ack\" msg to " + ip + ":" + port + ".";
					System.out.println(log_);
					setConnected(true);
				}
				else if (!isConnected && _oscMsg.addrPattern().equals(prefixConnectionAck)) {
					log_ = "<" + getClass().getName() + ">\tGot a \"Ack\" msg from " + ip + ":" + port + ".";
					System.out.println(log_);
					setConnected(true);
				}
			}
		}
		else
			for (OscCallback listener_ : listeners)
				listener_.oscMsgCallBack(_oscMsg);
	}

	private long getElapsedTimeSinceLastConnectionAttemptInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastConnectionAttemptInUsec);
	}

	private void sendOscConnectionMsg(String _prefixConnection) {
		OscMessage msg_ = new OscMessage(_prefixConnection);
		write(msg_);
	}

	private void setLastConnectionAttemptInUsec() {
		lastConnectionAttemptInUsec = System.nanoTime();
	}

	private void activateSendSynMsgPeriodically() {
		if (!isConnecting) {
			isConnecting = true;
			p5.registerMethod("pre", this);
		}
	}

	private void deactivateSendSynMsgPeriodically() {
		if (isConnecting) {
			isConnecting = false;
			p5.unregisterMethod("pre", this);
		}
	}

	private void setConnected(boolean _isConnected) {
		boolean isChanged_ = _isConnected != isConnected;
		isConnected = _isConnected;
		String log_;
		if (isConnected && isChanged_) {
			log_ = "<" + getClass().getName() + ">\tConnected with " + addr.address() + ":" + addr.port() + ".";
			System.out.println(log_);
			for (OscCallback listener_ : listeners)
				listener_.oscConnectionCallBack(this, true);
		}
		else if (!isConnected) {
			if (isConnecting)
				deactivateSendSynMsgPeriodically();
			else if (isChanged_) {
				for (OscCallback listener_ : listeners)
					listener_.oscConnectionCallBack(this, false);
				log_ = "<" + getClass().getName() + ">\tDisconnected with " + addr.address() + ":" + addr.port() + ".";
				System.out.println(log_);
			}
		}
	}
}
