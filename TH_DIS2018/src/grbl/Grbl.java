package grbl;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import main.Setting;
import oscComm.OscComm;
import processing.core.PApplet;

public class Grbl {
  public PApplet		p5					= null;
  public SerialComm	serialComm	= null;
  public OscComm		oscComm			= null;

  public final int	backOffIntervalMsec	= 500;
  public final int	bfrSizeMx						= 128;

  public boolean	isBusy						= false;
  public boolean	isOnPaper					= false;
  public long			strokeEndTimeUsec	= 0;
  public int			bfrSize						= 0;

  public boolean	backOffGate		= false;
  public int			strokeEndCnt	= 0;

  public ArrayList<String>	receivedMsg	= null;
  public ArrayList<String>	grblBfr			= null;
  public ArrayList<String>	reservedMsg	= null;

  public StringBuffer	strBfr		= null;
  public StringBuffer	prtTxtBfr	= null;

  public void setSerialComm(SerialComm _serialComm) {
    serialComm = _serialComm;
  }

  public void setOscComm(OscComm _oscComm) {
    oscComm = _oscComm;
  }

  public Grbl(PApplet _p5) {
    p5 = _p5;
    p5.registerMethod("pre", this);

    receivedMsg = new ArrayList<String>();
    grblBfr = new ArrayList<String>();
    reservedMsg = new ArrayList<String>();

    strBfr = new StringBuffer();
    prtTxtBfr = new StringBuffer();
  }

  public void pre() {
    backOff(backOffIntervalMsec);
    stream();
  }

  public void backOff(int _backOffIntervalMsec) {
    if (backOffGate) {
      if (getWaitingTimeMsec() >= _backOffIntervalMsec) {
        reserveBackOffCmd();
        backOffGate = false;
      }
    }
  }

  public long getWaitingTimeMsec() {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - strokeEndTimeUsec);
  }

  public void stream() {
    while (bfrSize <= bfrSizeMx && reservedMsg.size() > 0) {
      String cmd_;
      cmd_ = reservedMsg.get(0).toString();
      if (bfrSize + cmd_.length() <= bfrSizeMx) {
        bfrSize += cmd_.length();
        grblBfr.add(cmd_);
        if (!isBusy) {
          if (isMotionCmd(cmd_)) {
            isBusy = true;
            oscComm.sendGrblStatusMsg(!isBusy);
          }
        }
        serialComm.write(cmd_);
        reservedMsg.remove(0);
      } else
        break;
    }
  }

  public void init() {
    reservedMsg.clear();
    grblBfr.clear();
    bfrSize = 0;
  }

  public void read(String _msg) {
    if (_msg.equals("ok") || _msg.contains("error:")) {
      String cmd_ = grblBfr.get(0);
      receivedMsg.clear();
      bfrSize -= cmd_.length();
      grblBfr.remove(0);
      if (isHomeCmd(cmd_) || isBackOffCmd(cmd_)) {
        isOnPaper = false;
        if (isBusy) {
          isBusy = false;
          oscComm.sendGrblStatusMsg(!isBusy);
        }
      } else if (isMotionCmd(cmd_))
        isOnPaper = true;
      else if (isStrokeEndCmd(cmd_)) {
        strokeEndCnt++;
        if (reservedMsg.size() == 0 && bfrSize == 0 && isOnPaper) {
          backOffGate = true;
          strokeEndTimeUsec = System.nanoTime();
          if (isBusy) {
            isBusy = false;
            oscComm.sendGrblStatusMsg(!isBusy);
          }
        }
      }
    } else
      receivedMsg.add(_msg);
  }

  public void reserve(String strBfr) {
    reservedMsg.add(strBfr);
  }

  public boolean isHomeCmd(String _cmd) {
    return (_cmd.equals("G92X0Y0\r") || _cmd.contains("G1X0Y0"));
  }

  public boolean isBackOffCmd(String _cmd) {
    strBfr.append("G1")//
      .append("X").append(Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff);
    String backOffCmd_ = strBfr.toString();
    strBfr.setLength(0);
    return _cmd.contains(backOffCmd_);
  }

  public boolean isMotionCmd(String _cmd) {
    return (_cmd.contains("G1") && (_cmd.contains("X") || _cmd.contains("Y")));
  }

  public boolean isStrokeEndCmd(String _cmd) {
    return (_cmd.contains("G4P") && _cmd.contains(String.format("%.6f", Setting.servoDelay[3])));
  }

  public void reserveBackOffCmd() {
    reserve("G94\r");
    strBfr.append("G1")//
      .append("X").append(Setting.isXInverted ? -Setting.xBackOff : Setting.xBackOff)//
      .append("F").append(Setting.feedrateStrokeToStoke)//
      .append('\r');
    reserve(strBfr.toString());
    strBfr.setLength(0);
    reserve("G93\r");
  }
}
