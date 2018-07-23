package main;

public interface SerialCallback {
	public void serialConnectionCallBack(SerialPort _serialPort, boolean _isConnected);
	public void serialMsgCallBack(String _msg);
}