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
        
        if(a_.getType() != 2){
          b_.setTiltX(a_.getTiltX());
          b_.setTiltY(a_.getTiltY());
        }
        
        float aCalibedAndConstrainedX_ = Math.min(Math.max((a_.getX() - Setting.targetCalibXInPx), 0), 
          (Setting.targetScreentWidthInPx - Setting.targetCalibXInPx));
        float aCalibedAndConstrainedY_ = Math.min(Math.max((a_.getY() - Setting.targetCalibYInPx), 0), 
          (Setting.targetScreenHeightInPx - Setting.targetCalibYInPx));
        float bCalibedAndConstrainedX_ = Math.min(Math.max((b_.getX() - Setting.targetCalibXInPx), 0), 
          (Setting.targetScreentWidthInPx - Setting.targetCalibXInPx));
        float bCalibedAndConstrainedY_ = Math.min(Math.max((b_.getY() - Setting.targetCalibYInPx), 0), 
          (Setting.targetScreenHeightInPx - Setting.targetCalibYInPx));
          
        float _aAlpha = map(a_.getTiltX(), -1, 1, -PI/3, PI/3);
        float aAlpha = Math.min(Math.max(-PI/9,_aAlpha),PI/9);
        float _aBeta = map(a_.getTiltY(), -1, 1, PI/3, -PI/3);
        float aBeta = Math.min(Math.max(-PI/9,_aBeta),PI/9);
        float _bAlpha = map(b_.getTiltX(), -1, 1, -PI/3, PI/3);
        float bAlpha = Math.min(Math.max(-PI/9,_bAlpha),PI/9);
        float _bBeta = map(b_.getTiltY(), -1, 1, PI/3, -PI/3);
        float bBeta = Math.min(Math.max(-PI/9,_bBeta),PI/9);
        int _aTiltX = int(map(a_.getTiltY(), -1, 1, 30, 150));
        int aTiltX = Math.min(Math.max(70,_aTiltX),110);
        int _aTiltY = int(map(a_.getTiltX(), -1, 1, 30, 150));
        int aTiltY = Math.min(Math.max(70,_aTiltY),110);
        //int _bTiltX = int(map(b_.getTiltX(), -1, 1, 150, 30));
        //int bTiltX = Math.min(Math.max(70,_bTiltX),110);
        //int _bTiltY = int(map(b_.getTiltY(), -1, 1, 30, 150));
        //int bTiltY = Math.min(Math.max(70,_bTiltY),110);
        float aReviseX = Setting.d2-Setting.d2*cos(aAlpha)-Setting.h*sin(aAlpha)*cos(aBeta);
        float aReviseY = Setting.h*sin(aBeta);
        float aReviseZ = Setting.h*cos(aAlpha)*cos(aBeta)-Setting.d2*sin(aAlpha)-Setting.h;
        float bReviseX = Setting.d2-Setting.d2*cos(bAlpha)-Setting.h*sin(bAlpha)*cos(bBeta);
        float bReviseY = Setting.h*sin(bBeta);  
        float bReviseZ = Setting.h*cos(bAlpha)*cos(bBeta)-Setting.d2*sin(bAlpha)-Setting.h;
        
        /*
        float aReviseAndScaledX_ = Setting.targetScreentWidthInPx * aReviseX / (float) Setting.targetTabletWidthInMm;
        float aReviseAndScaledY_ = Setting.targetScreenHeightInPx * aReviseY / (float) Setting.targetTabletHeightInMm;
        float bReviseAndScaledX_ = Setting.targetScreentWidthInPx * bReviseX / (float) Setting.targetTabletWidthInMm;
        float bReviseAndScaledY_ = Setting.targetScreenHeightInPx * bReviseY / (float) Setting.targetTabletHeightInMm;
        */
        
        float aScaledX_ = (Setting.targetTabletWidthInMm * aCalibedAndConstrainedX_
          / (float) Setting.targetScreentWidthInPx) - aReviseX;
        float aScaledY_ = (Setting.targetTabletHeightInMm * aCalibedAndConstrainedY_
          / (float) Setting.targetScreenHeightInPx) - aReviseY;
        float bScaledX_ = (Setting.targetTabletWidthInMm * bCalibedAndConstrainedX_
          / (float) Setting.targetScreentWidthInPx) - bReviseX;
        float bScaledY_ = (Setting.targetTabletHeightInMm * bCalibedAndConstrainedY_
          / (float) Setting.targetScreenHeightInPx) - bReviseY;
        long durationInMsec_ = b_.getEvtTimeInMsec() - a_.getEvtTimeInMsec();
        float feedrate_ = (float) (18000 / (double) durationInMsec_);

        float aGamma = acos(1-(4-aReviseZ)/15);
        float bGamma = acos(1-(4-bReviseZ)/15);
        int aReviseServoZero = int(Setting.servoHover-255*aGamma/PI) ;
        int bReviseServoZero = int(Setting.servoHover-255*bGamma/PI) ;
        
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
          
          cmd_ = "G94";
          cmd_ += "\0 ;<";
          cmd_ += String.format("%03d", aTiltX);
          cmd_ += ",";
          cmd_ += String.format("%03d", aTiltY);
          cmd_ += ">";
          cmd_ += "\r";
          // Set feedrate mode: unit per min
          grbl.reserveCmd(cmd_);
          
          cmd_ = "G4";
          cmd_ += "P" + String.format("%.3f", Setting.servoDelay10);
          cmd_ += "\r";
          grbl.reserveCmd(cmd_);
          
          // To point a
          cmd_ = "G1";
          cmd_ += "X" + String.format("%.3f", Setting.isXInverted ? -aScaledX_: aScaledX_);
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
          cmd_ += "S" + String.format("%03d", aReviseServoZero); 
          //cmd_ += "\0 ;" + String.format("%.3f", aReviseZ) +  ", " + String.format("%.3f", aGamma) + ", " + String.format("%03d", aReviseServoZero) + ", " + String.format("%05d", aReviseServoZero);
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
        
        
        //cmd_ = "G4";
        //cmd_ += "P" + String.format("%.3f", 0.001f);
        //cmd_ += "\r";
        
            
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