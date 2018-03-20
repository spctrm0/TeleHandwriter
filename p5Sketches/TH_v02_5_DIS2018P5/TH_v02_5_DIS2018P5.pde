//setting.txt
//myCalibXInPx = 0
//myCalibYInPx = 0
//targetCalibXInPx = 0
//targetCalibYInPx = 0
//servoHover = 223
//servoZero = 127
//servoDelay0 = .025
//servoDelay1 = .150
//servoDelay2 = .025
//servoDelay3 = .150
//feedrateStrokeToStoke = 15000
//isXInverted = false
//isYInverted = true
//xZero = 43.5
//yZero = 26
//xBack = -20
//myPort = 8765
//targetPort = 4321
//targetIp = 192.168.0.4

import processing.serial.*;

import codeanticode.tablet.*;

import netP5.*;
import oscP5.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

TabletInput tabletInput;

SerialComm serialComm;
GrblComm grblComm;

OscComm oscComm;

Drawing drawing;

Table table;
Interpreter interpreter;

public void loadSetting() {
  String print_ = "<SYS>\tLoad setting...\n";
  try {
    ArrayList<Field> fields_ = new ArrayList<Field>(Arrays.asList(Setting.class.getDeclaredFields()));
    String[] lines_ = loadStrings("Setting.txt");
    for (String line_ : lines_) {
      String[] parsed_ = line_.split("=");
      if (parsed_.length >= 2) {
        parsed_[0] = parsed_[0].trim();
        parsed_[1] = parsed_[1].trim();
        for (int i = fields_.size() - 1; i >= 0; i--) {
          Field field_ = fields_.get(i);
          String fieldName_ = field_.getName();
          String fieldType_ = field_.getType().getTypeName();
          if (parsed_[0].equals(fieldName_)) {
            try {
              if (fieldType_.equals("boolean"))
                field_.set(null, Boolean.parseBoolean(parsed_[1]));
              else if (fieldType_.equals("int"))
                field_.set(null, Integer.parseInt(parsed_[1]));
              else if (fieldType_.equals("float"))
                field_.set(null, Float.parseFloat(parsed_[1]));
              else if (fieldType_.equals("java.lang.String"))
                field_.set(null, parsed_[1]);
              print_ += "\t:" + fieldName_ + " = " + Setting.class.getDeclaredField(fieldName_).get(null) + "\n";
              fields_.remove(i);
            }
            catch (Exception e) {
              print_ += "\t" + e.toString() + "\n";
            }
            break;
          }
        }
      }
    }
    System.out.print(print_);
  }
  catch (NullPointerException e) {
    print_ += "\t" + e.toString();
    System.out.println(print_);
  }
}

public void setup() {
  fullScreen();
  //size(800, 800);

  loadSetting();

  tabletInput = new TabletInput(this);

  serialComm = new SerialComm(this);
  grblComm = new GrblComm(this);

  oscComm = new OscComm(this);

  drawing = new Drawing();

  table = new Table();
  interpreter = new Interpreter(this);

  tabletInput.setOscComm(oscComm);

  serialComm.setGrblComm(grblComm);
  serialComm.setOscComm(oscComm);
  grblComm.setSerialComm(serialComm);
  grblComm.setOscComm(oscComm);

  oscComm.setTabletInput(tabletInput);
  oscComm.setDrawing(drawing);

  interpreter.setDrawing(drawing);
  interpreter.setGrblComm(grblComm);
  interpreter.setTable(table);
}

public void draw() {
  background(serialComm.isConnected() ? 0 : 255, oscComm.isConnected() ? 0 : 255, 
    (tabletInput.isWritable()) ? 0 : 255);

  noStroke();
  fill(oscComm.isTargetWritable() ? 0 : 255, oscComm.isTargetWritable() ? 255 : 0, 0);
  rect(0, 0, 32, 32);

  noFill();
  stroke(255);
  for (Stroke stroke_ : drawing.getStrokes()) {
    for (int j = 0; j < stroke_.getPointsNum() - 1; j++) {
      Point a_ = stroke_.getPoints().get(j);
      Point b_ = stroke_.getPoints().get(j + 1);
      line(a_.getX(), a_.getY(), b_.getX(), b_.getY());
    }
  }

  noStroke();
  fill(255);
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
  String cmd_;
  if (key == ']') {
    System.out.println("brfSize = " + grblComm.getBfrSize());
    System.out.println("grblBfrCmdNum = " + grblComm.getGrblBfrCmdNum());
    System.out.println("reservedPreDefinedCmdNum = " + grblComm.getReservedPreDefinedCmdNum());
    System.out.println("reservedCmdNum = " + grblComm.getReservedCmdNum());
    System.out.println("atBack = " + grblComm.isAtBack());
    System.out.println("atHome = " + grblComm.isAtHome());
    System.out.println("moving = " + grblComm.isMoving());
  } else if (key == '~') {
    oscComm.activateAutoConnect();
  } else if (key == 'i' || key == 'I') // toggle writable
  {
    tabletInput.setWritable(!tabletInput.isWritable());
  } else if (key == 'h' || key == 'H') // set home
  {
    grblComm.activateAutoHome();
  } else if (key == 'w' || key == 'W') // servo up
  {
    cmd_ = "M3";
    cmd_ += "S" + String.format("%03d", Setting.servoHover);
    cmd_ += "\r";
    grblComm.reserveCmd(cmd_);
  } else if (key == 's' || key == 'S') // servo down
  {
    cmd_ = "M3";
    cmd_ += "S" + String.format("%03d", Setting.servoZero);
    cmd_ += "\r";
    grblComm.reserveCmd(cmd_);
  } else if (key == 'x' || key == 'X') // servo off
  {
    cmd_ = "M3";
    cmd_ += "S" + String.format("%03d", 0);
    cmd_ += "\r";
    grblComm.reserveCmd(cmd_);
  }
}

String timestamp() {
  Calendar now = Calendar.getInstance();
  return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", now);
}