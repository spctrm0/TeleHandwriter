public class Interpreter {
  public PApplet p5 = null;

  Drawing	drawing	= null;
  Grbl		grbl		= null;
  Table		table		= null;

  StringBuffer strBfr = null;

  public void setDrawing(Drawing _drawing) {
    drawing = _drawing;
  }

  public void setGrbl(Grbl _grbl) {
    grbl = _grbl;
  }

  public void setTable(Table _table) {
    table = _table;
  }

  public Interpreter(PApplet _p5) {
    p5 = _p5;
    p5.registerMethod("pre", this);

    strBfr = new StringBuffer();
  }

  public void pre() {
    interpret();
  }

  public void interpret() {
    while (drawing.getStrokesNum() > 0) {
      Stroke stroke_ = drawing.getFirstStroke();
      while (stroke_.getPointsNum() >= 2) {
        Point a_ = stroke_.getFirstPoint();
        Point b_ = stroke_.getSecondPoint();
        float aX_ = targetTabletWidth * ((a_.getPenX() - targetCalibX) / targetScreentWidth);
        float aY_ = targetTabletHeight * ((a_.getPenY()) / targetScreenHeight);
        float bX_ = targetTabletWidth * ((b_.getPenX() - targetCalibX) / targetScreentWidth);
        float bY_ = targetTabletHeight * ((b_.getPenY()) / targetScreenHeight);
        long duration_ = b_.getMillis() - a_.getMillis();
        float f_ = (float) (60000 / (double) duration_);
        // Head
        if (a_.getKind() == 0) {
          grbl.reserve("G94\r");
          strBfr.append("G1")//
            .append("X").append(String.format("%.3f", isXInverted ? -aX_ : aX_))//
            .append("Y").append(String.format("%.3f", isYInverted ? -aY_ : aY_))//
            .append("F").append(feedrateStrokeToStoke)//
            .append('\r');
          grbl.reserve(strBfr.toString());
          strBfr.setLength(0);
          grbl.reserve("G93\r");

          if (servoDelay[0] != 0.0f) {
            strBfr.append("G4")//
              .append("P").append(String.format("%.3f", servoDelay[0]))//
              .append('\r');
            grbl.reserve(strBfr.toString());
            strBfr.setLength(0);
          }

          strBfr.append("M3")//
            .append("S").append(servoZero)//
            .append('\r');
          grbl.reserve(strBfr.toString());
          strBfr.setLength(0);

          if (servoDelay[1] != 0.0f) {
            strBfr.append("G4")//
              .append("P").append(String.format("%.3f", servoDelay[1]))//
              .append('\r');
            grbl.reserve(strBfr.toString());
            strBfr.setLength(0);
          }
        }

        // For all
        strBfr.append("G1")//
          .append("X").append(String.format("%.3f", isXInverted ? -bX_ : bX_))//
          .append("Y").append(String.format("%.3f", isYInverted ? -bY_ : bY_))//
          .append("F").append(String.format("%.3f", f_))//
          .append('\r');
        grbl.reserve(strBfr.toString());
        strBfr.setLength(0);

        // logging first point on table
        logTable(a_.getTotalPointIdx(), a_.getStrokeIdx(), a_.getPointIdx(), a_.getPenX(), a_.getPenY(), aX_, aY_, f_, a_.getPressure(), a_.getTiltX(), a_.getTiltY(), 
          a_.getMillis());
        // remove first point
        stroke_.removeFirstPoint();

        // Tail
        if (b_.getKind() == 2) {
          if (servoDelay[2] != 0.0f) {
            strBfr.append("G4")//
              .append("P").append(String.format("%.3f", servoDelay[2]))//
              .append('\r');
            grbl.reserve(strBfr.toString());
            strBfr.setLength(0);
          }

          strBfr.append("M3")//
            .append("S").append(servoHover)//
            .append('\r');
          grbl.reserve(strBfr.toString());
          strBfr.setLength(0);

          if (servoDelay[3] != 0.0f) {
            strBfr.append("G4")//
              .append("P").append(String.format("%.6f", servoDelay[3]))//
              .append('\r');
            grbl.reserve(strBfr.toString());
            strBfr.setLength(0);
          }

          // logging second (last) point on table
          logTable(b_.getTotalPointIdx(), b_.getStrokeIdx(), b_.getPointIdx(), b_.getPenX(), b_.getPenY(), bX_, bY_, f_, b_.getPressure(), b_.getTiltX(), b_.getTiltY(), 
            b_.getMillis());
          // remove second (last) point
          stroke_.removeFirstPoint();
          // remove first stroke which just has been empty
          drawing.removeFirstStroke();
        }
      }
      if (stroke_.getPointsNum() == 1)
        break;
    }
  }

  public void logTable(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY, float _x, float _y, float _f, float _pressure, float _tiltX, float _tiltY, 
    long _millis) {
    TableRow newRow_ = table.addRow();
    newRow_.setInt("totalPointIdx", _totalPointIdx);
    newRow_.setInt("strokeIdx", _strokeIdx);
    newRow_.setInt("pointIdx", _pointIdx);
    newRow_.setFloat("penX", _penX);
    newRow_.setFloat("penY", _penY);
    newRow_.setFloat("x", _x);
    newRow_.setFloat("y", _y);
    newRow_.setFloat("f", _f);
    newRow_.setFloat("pressure", _pressure);
    newRow_.setFloat("tiltX", _tiltX);
    newRow_.setFloat("tiltY", _tiltY);
    newRow_.setString("millis", String.valueOf(_millis));
  }
}