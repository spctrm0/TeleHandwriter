public class Drawing {
  private ArrayList<Stroke> strokes;

  public Drawing() {
    strokes = new ArrayList<Stroke>();
  }

  public void addStroke(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY, float _pressure, 
    float _tiltX, float _tiltY, long _millis, int _kind) {
    strokes.add(
      new Stroke(_totalPointIdx, _strokeIdx, _pointIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, _kind));
  }

  public void addPoint(int _totalPointIdx, int _strokeIdx, int _pointIdx, float _penX, float _penY, float _pressure, 
    float _tiltX, float _tiltY, long _millis, int _kind) {
    getLastStroke().addPoint(_totalPointIdx, _strokeIdx, _pointIdx, _penX, _penY, _pressure, _tiltX, _tiltY, _millis, 
      _kind);
  }

  public ArrayList<Stroke> getStrokes() {
    return strokes;
  }

  public int getStrokesNum() {
    return strokes.size();
  }

  public Stroke getFirstStroke() {
    return strokes.get(0);
  }

  public Stroke getLastStroke() {
    return strokes.get(strokes.size() - 1);
  }

  public void removeFirstStroke() {
    strokes.remove(0);
  }
}