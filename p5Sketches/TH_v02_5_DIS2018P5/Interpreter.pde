public class Interpreter {
  private PApplet p5 = null;

  private Drawing drawing = null;
  private GrblComm grbl = null;
  private Table table = null;

  public void setDrawing(Drawing _drawing) {
    drawing = _drawing;
  }

  public void setGrblComm(GrblComm _grbl) {
    grbl = _grbl;
  }

  public void setTable(Table _table) {
    table = _table;
    table.addColumn("nthPoint");
    table.addColumn("nthStroke");
    table.addColumn("nthPointInStroke");
    table.addColumn("penX");
    table.addColumn("penY");
    table.addColumn("cncX");
    table.addColumn("cncY");
    table.addColumn("feedrate");
    table.addColumn("pressure");
    table.addColumn("tiltX");
    table.addColumn("tiltY");
    table.addColumn("evtTimeInMsec");
  }

  public Interpreter(PApplet _p5) {
    p5 = _p5;
    p5.registerMethod("pre", this);
  }

  public void pre() {
    interpret();
  }

  private void interpret() {
    while (drawing.getStrokesNum() > 0) {
      Stroke stroke_ = drawing.getFirstStroke();
      while (stroke_.getPointsNum() >= 2) {
        Point a_ = stroke_.getFirstPoint();
        Point b_ = stroke_.getSecondPoint();
        float aCalibedAndConstrainedX_ = Math.min(Math.max((a_.getX() - Setting.targetCalibXInPx), 0), 
          (Setting.targetScreentWidthInPx - Setting.targetCalibXInPx));
        float aCalibedAndConstrainedY_ = Math.min(Math.max((a_.getY() - Setting.targetCalibYInPx), 0), 
          (Setting.targetScreenHeightInPx - Setting.targetCalibYInPx));
        float bCalibedAndConstrainedX_ = Math.min(Math.max((b_.getX() - Setting.targetCalibXInPx), 0), 
          (Setting.targetScreentWidthInPx - Setting.targetCalibXInPx));
        float bCalibedAndConstrainedY_ = Math.min(Math.max((b_.getY() - Setting.targetCalibYInPx), 0), 
          (Setting.targetScreenHeightInPx - Setting.targetCalibYInPx));
        float aScaledX_ = Setting.targetTabletWidthInMm * aCalibedAndConstrainedX_
          / (float) Setting.targetScreentWidthInPx;
        float aScaledY_ = Setting.targetTabletHeightInMm * aCalibedAndConstrainedY_
          / (float) Setting.targetScreenHeightInPx;
        float bScaledX_ = Setting.targetTabletWidthInMm * bCalibedAndConstrainedX_
          / (float) Setting.targetScreentWidthInPx;
        float bScaledY_ = Setting.targetTabletHeightInMm * bCalibedAndConstrainedY_
          / (float) Setting.targetScreenHeightInPx;
        long durationInMsec_ = b_.getEvtTimeInMsec() - a_.getEvtTimeInMsec();
        float feedrate_ = (float) (60000 / (double) durationInMsec_);
        String cmd_;
        if (a_.getType() == 0) { // Head
          /*
 * UNIQUE
           */
          // Pen up
          cmd_ = "M3";
          cmd_ += "S" + String.format("%05d", Setting.servoHover);
          cmd_ += "\r";
          grbl.reserveCmd(cmd_);
          // Set feedrate mode: unit per min
          grbl.reserveCmd("G94\r");
          // To point a
          cmd_ = "G1";
          cmd_ += "X" + String.format("%.3f", Setting.isXInverted ? -aScaledX_ : aScaledX_);
          cmd_ += "Y" + String.format("%.3f", Setting.isYInverted ? -aScaledY_ : aScaledY_);
          cmd_ += "F" + String.format("%.3f", Setting.feedrateStrokeToStoke);
          cmd_ += "\r";
          grbl.reserveCmd(cmd_);
          // Set feedrate mode: inverse time
          grbl.reserveCmd("G93\r");
          // Delay
          if (Setting.servoDelay0 != 0.0f) {
            cmd_ = "G4";
            cmd_ += "P" + String.format("%.3f", Setting.servoDelay0);
            cmd_ += "\r";
            grbl.reserveCmd(cmd_);
          }
          // Pen down
          cmd_ = "M3";
          cmd_ += "S" + String.format("%03d", Setting.servoZero);
          cmd_ += "\r";
          grbl.reserveCmd(cmd_);
          // Delay
          if (Setting.servoDelay1 != 0.0f) {
            cmd_ = "G4";
            cmd_ += "P" + String.format("%.3f", Setting.servoDelay1);
            cmd_ += "\r";
            grbl.reserveCmd(cmd_);
          }
        }
        // To point b
        cmd_ = "G1";
        cmd_ += "X" + String.format("%.3f", Setting.isXInverted ? -bScaledX_ : bScaledX_);
        cmd_ += "Y" + String.format("%.3f", Setting.isYInverted ? -bScaledY_ : bScaledY_);
        cmd_ += "F" + String.format("%.3f", feedrate_);
        cmd_ += "\r";
        grbl.reserveCmd(cmd_);
        // Write log of first point on table
        writeLogOnTable(a_.getNthPoint(), a_.getNthStroke(), a_.getNthPointInStoke(), a_.getX(), a_.getY(), aScaledX_, 
          aScaledY_, feedrate_, a_.getPressure(), a_.getTiltX(), a_.getTiltY(), a_.getEvtTimeInMsec());
        // Remove first point
        stroke_.removeFirstPoint();
        if (b_.getType() == 2) { // Tail
          // Delay
          if (Setting.servoDelay2 != 0.0f) {
            cmd_ = "G4";
            cmd_ += "P" + String.format("%.3f", Setting.servoDelay2);
            cmd_ += "\r";
            grbl.reserveCmd(cmd_);
          }
          // Pen up
          cmd_ = "M3";
          cmd_ += "S" + String.format("%03d", Setting.servoHover);
          cmd_ += "\r";
          grbl.reserveCmd(cmd_);
          /*
 * UNIQUE
           */
          // Delay
          if (Setting.servoDelay3 != 0.0f) {
            cmd_ = "G4";
            cmd_ += "P" + String.format("%.5f", Setting.servoDelay3);
            cmd_ += "\r";
            grbl.reserveCmd(cmd_);
          }
          // Write log of last point on table
          writeLogOnTable(b_.getNthPoint(), b_.getNthStroke(), b_.getNthPointInStoke(), b_.getX(), b_.getY(), bScaledX_, 
            bScaledY_, feedrate_, b_.getPressure(), b_.getTiltX(), b_.getTiltY(), b_.getEvtTimeInMsec());
          // Remove last point
          stroke_.removeFirstPoint();
          // Remove first stroke which has been emptied
          drawing.removeFirstStroke();
        }
      }
      if (stroke_.getPointsNum() == 1)
        break;
    }
  }

  public void writeLogOnTable(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _penX, float _penY, 
    float _cncX, float _cncY, float _feedrate, float _pressure, float _tiltX, float _tiltY, long _evtTimeInMsec) {
    TableRow newRow_ = table.addRow();
    newRow_.setInt("nthPoint", _nthPoint);
    newRow_.setInt("nthStroke", _nthStroke);
    newRow_.setInt("nthPointInStroke", _nthPointInStroke);
    newRow_.setFloat("penX", _penX);
    newRow_.setFloat("penY", _penY);
    newRow_.setFloat("cncX", _cncX);
    newRow_.setFloat("cncY", _cncY);
    newRow_.setFloat("feedrate", _feedrate);
    newRow_.setFloat("pressure", _pressure);
    newRow_.setFloat("tiltX", _tiltX);
    newRow_.setFloat("tiltY", _tiltY);
    newRow_.setString("evtTimeInMsec", String.valueOf(_evtTimeInMsec));
  }
}