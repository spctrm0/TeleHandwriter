package osc;

import oscP5.OscMessage;

public interface OscCallback {

	public void oscConnectionCallBack(OscPort _oscPort, boolean _isConnected);

	public void oscMsgCallBack(OscMessage _oscMsg);
}
