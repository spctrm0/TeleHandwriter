import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

OscComm oscComm;
int cnt = 0;

public void setup() {
  size(500, 500);
  oscComm = new OscComm(this, 12001);
  oscComm.addTargetAddr(oscComm.getMyIp(), 12000);
  oscComm.addTargetAddr(oscComm.getMyIp(), 12002);
  oscComm.tryConnect();
}

public void draw() {
  background(255);
  fill(0);
  text(cnt, width / 2.0f, height / 2.0f);
  text(oscComm.getTargetPort(oscComm.getMyIp(), 12000).isConnected() ? "YES" : "NO", width / 2.0f, height / 2.0f + 16);
  text(oscComm.getTargetPort(oscComm.getMyIp(), 12002).isConnected() ? "YES" : "NO", width / 2.0f, height / 2.0f + 32);
}

public void mousePressed() {
  if (mouseButton == LEFT) {
  } else if (mouseButton == RIGHT) {
  }
}

public void oscEvent(OscMessage _oscMsg) {
  oscComm.distributeToEachTargetAddr(_oscMsg);
  cnt++;
}