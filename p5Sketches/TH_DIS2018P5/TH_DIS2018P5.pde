import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import codeanticode.tablet.Tablet;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;

import processing.serial.*;

Drawing drawing;
OscComm oscComm;
TabletInput tabletInput;

Grbl grbl;
SerialComm serialComm;

Table table;
Interpreter interpreter;

StringBuffer strBfr = null;

long homeCmdTriggeredUsec = 0;
int waitingTimeMsec = 5000;
boolean isHomeCmdExecuted = false;

//public void settings() {
// fullScreen();
// // size(500, 500);
//}

public void homeCmd() {
  strBfr.append("M3")//
    .append("S").append(servoHover)//
    .append('\r');
  grbl.reserve(strBfr.toString());
  strBfr.setLength(0);

  grbl.reserve("G92X0Y0\r");
  grbl.reserve("G90\r");
  grbl.reserve("G94\r");
  strBfr.append("G1")//
    .append("F").append(feedrateStrokeToStoke)//
    .append("X").append(String.format("%.3f", isXInverted ? -xZero : xZero))//
    .append("Y").append(String.format("%.3f", isYInverted ? -yZero : yZero))//
    .append('\r');
  grbl.reserve(strBfr.toString());
  strBfr.setLength(0);
  grbl.reserve("G93\r");
  grbl.reserve("G92X0Y0\r");
  isHomeCmdExecuted = true;
}

public void homeCmdTrigger() {
  homeCmdTriggeredUsec = System.nanoTime();
  isHomeCmdExecuted = false;
}

public long getWaitingTimeMsec() {
  return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - homeCmdTriggeredUsec);
}

public void setWritable(boolean _isWritable) {
  tabletInput.setWritable(_isWritable);
}

public void loadSetting() {
  String[] lines_ = loadStrings("Setting.txt");
  for (String line_ : lines_) {
    String[] parsed_ = line_.split("=");
    if (parsed_.length >= 2) {
      parsed_[0] = parsed_[0].trim();
      parsed_[1] = parsed_[1].trim();
      if (parsed_[0].equals("myPort"))
        myPort = Integer.parseInt(parsed_[1]);
      else if (parsed_[0].equals("targetPort"))
        targetPort = Integer.parseInt(parsed_[1]);
      else if (parsed_[0].equals("targetIp"))
        targetIp = parsed_[1].toString();
      else if (parsed_[0].equals("isXInverted"))
        isXInverted = Boolean.parseBoolean(parsed_[1]);
      else if (parsed_[0].equals("isYInverted"))
        isYInverted = Boolean.parseBoolean(parsed_[1]);
      else if (parsed_[0].equals("xZero"))
        xZero = Float.parseFloat(parsed_[1]);
      else if (parsed_[0].equals("yZero"))
        yZero = Float.parseFloat(parsed_[1]);
      else if (parsed_[0].equals("servoHover"))
        servoHover = Integer.parseInt(parsed_[1]);
      else if (parsed_[0].equals("servoZero"))
        servoZero = Integer.parseInt(parsed_[1]);
      else if (parsed_[0].equals("servoDelay[0]"))
        servoDelay[0] = Float.parseFloat(parsed_[1]);
      else if (parsed_[0].equals("servoDelay[1]"))
        servoDelay[1] = Float.parseFloat(parsed_[1]);
      else if (parsed_[0].equals("servoDelay[2]"))
        servoDelay[2] = Float.parseFloat(parsed_[1]);
      else if (parsed_[0].equals("servoDelay[3]"))
        servoDelay[3] = Float.parseFloat(parsed_[1]);
    }
  }
}

public void setup() {
  fullScreen();

  loadSetting();

  drawing = new Drawing();
  oscComm = new OscComm(this);
  tabletInput = new TabletInput(this);

  grbl = new Grbl(this);
  serialComm = new SerialComm(this);

  table = new Table();
  table.addColumn("totalPointIdx");
  table.addColumn("strokeIdx");
  table.addColumn("pointIdx");
  table.addColumn("penX");
  table.addColumn("penY");
  table.addColumn("x");
  table.addColumn("y");
  table.addColumn("f");
  table.addColumn("pressure");
  table.addColumn("tiltX");
  table.addColumn("tiltY");
  table.addColumn("millis");
  table.addColumn("kind");
  interpreter = new Interpreter(this);

  strBfr = new StringBuffer();

  oscComm.setDrawing(drawing);
  tabletInput.setOscComm(oscComm);

  grbl.setSerialComm(serialComm);
  grbl.setOscComm(oscComm);
  serialComm.setGrbl(grbl);

  interpreter.setDrawing(drawing);
  interpreter.setGrbl(grbl);
  interpreter.setTable(table);

  homeCmdTrigger();
}

public void draw() {
  if (!isHomeCmdExecuted) {
    if (getWaitingTimeMsec() >= waitingTimeMsec) {
      homeCmd();
      setWritable(true);
    }
  }

  background(serialComm.isConnected ? 0 : 255, oscComm.isConnected ? 0 : 255, (tabletInput.isWritable()) ? 0 : 255);
  noStroke();
  fill(oscComm.isTargetIdle ? 0 : 255, oscComm.isTargetIdle ? 255 : 0, 0);
  rect(0, 0, 32, 32);
  noFill();
  stroke(255);
  for (Stroke stroke_ : drawing.getStrokes()) {
    for (int j = 0; j < stroke_.getPointsNum() - 1; j++) {
      Point a_ = stroke_.getPoints().get(j);
      Point b_ = stroke_.getPoints().get(j + 1);
      line(a_.getPenX(), a_.getPenY(), b_.getPenX(), b_.getPenY());
    }
  }
  // System.out.print(grbl.bfrSize);
  // System.out.print(", ");
  // System.out.print(grbl.grblBfr.size());
  // System.out.print(", ");
  // System.out.print(grbl.reservedMsg.size());
}

public void exit() {
  saveTable(table, "tabletInputLogs\\" + timestamp() + ".csv");
  super.exit();
}

public void oscEvent(OscMessage _oscMsg) {
  oscComm.receive(_oscMsg);
}

public void serialEvent(Serial _serialEvt) {
  char replyChar_ = _serialEvt.readChar();
  serialComm.read(replyChar_);
}

public void keyPressed() {
  if (key == '~') {
    oscComm.activateAutoConnect();
  } else if (key == 'i' || key == 'I') // toggle writable
  {
    setWritable(!tabletInput.isWritable());
  } else if (key == 'h' || key == 'H') // set home
  {
    homeCmdTrigger();
  } else if (key == 'w' || key == 'W') // servo up
  {
    strBfr.append("M3")//
      .append("S").append(servoHover)//
      .append('\r');
    grbl.reserve(strBfr.toString());
    strBfr.setLength(0);
  } else if (key == 's' || key == 'S') // servo down
  {
    strBfr.append("M3")//
      .append("S").append(servoZero)//
      .append('\r');
    grbl.reserve(strBfr.toString());
    strBfr.setLength(0);
  } else if (key == 'x' || key == 'X') // servo off
  {
    strBfr.append("M3")//
      .append("S0")//
      .append('\r');
    grbl.reserve(strBfr.toString());
    strBfr.setLength(0);
  }
}

String timestamp() {
  Calendar now = Calendar.getInstance();
  return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", now);
}