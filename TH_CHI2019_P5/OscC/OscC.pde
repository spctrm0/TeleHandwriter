import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscProperties;
import processing.core.PApplet;

OscComm oscComm;
int[] ports = {12002, 12000, 12001};
int cnt = 0;

public void setup() {
  size(500, 500);
  oscComm = new OscComm(this, ports[0]);
  oscComm.addOscTarget(oscComm.getIp(), ports[1]);
  oscComm.addOscTarget(oscComm.getIp(), ports[2]);
  oscComm.tryConnectAll();
}

public void draw() {
  background(255);
  fill(0);
  text(cnt, width / 2.0f, height / 2.0f);
  text(oscComm.getOscTarget(oscComm.getIp(), ports[1]).isConnected() ? "YES" : "NO", width / 2.0f, height / 2.0f + 16);
  text(oscComm.getOscTarget(oscComm.getIp(), ports[2]).isConnected() ? "YES" : "NO", width / 2.0f, height / 2.0f + 32);
}

public void mousePressed() {
  if (mouseButton == LEFT) { 
    OscMessage msg_ = new OscMessage("AA");
    msg_.add("Hello");
    oscComm.getOscTarget(oscComm.getIp(), ports[1]).write(msg_);
  } else if (mouseButton == RIGHT) {
    OscMessage msg_ = new OscMessage("AA");
    msg_.add("Hello");
    oscComm.getOscTarget(oscComm.getIp(), ports[2]).write(msg_);
  }
}

public void keyPressed() {
  if(key == 'q') {
    oscComm.disconnectAll();
  } else if(key == 'w') {
    oscComm.disconnectAll();
  } else if(key == 'a') {
    oscComm.getOscTarget(oscComm.getIp(), ports[1]).disconnect();
  } else if(key == 's') {
    oscComm.getOscTarget(oscComm.getIp(), ports[1]).tryConnect();
  } else if(key == 'z') {
    oscComm.getOscTarget(oscComm.getIp(), ports[2]).disconnect();
  } else if(key == 'x') {
    oscComm.getOscTarget(oscComm.getIp(), ports[2]).tryConnect();
  }
}

public void oscEvent(OscMessage _oscMsg) {
  oscComm.distributeToEachOscTarget(_oscMsg);
  cnt++;
}