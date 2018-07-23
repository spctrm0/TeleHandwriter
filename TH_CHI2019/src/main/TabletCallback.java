package main;

public interface TabletCallback {
	public void tabletInputCallBack(float _x, float _y, float _pressure, float _tiltX, float _tiltY, long _evtTimeInMsec, int _type);
}