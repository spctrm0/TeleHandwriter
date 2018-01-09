public class Drawing {
  private ArrayList<Stroke>  strokes;

  public Drawing() {
    strokes = new ArrayList<Stroke>();
  }

  public void addStroke(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y, float _pressure, 
    float _tiltX, float _tiltY, long _evtTimeInMsec, int _type) {
    strokes.add(
      new Stroke(_nthPoint, _nthStroke, _nthPointInStroke, _x, _y, _pressure, _tiltX, _tiltY, _evtTimeInMsec, _type));
  }

  public void addPoint(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y, float _pressure, 
    float _tiltX, float _tiltY, long _evtTimeInMsec, int _type) {
    Stroke lastStroke_ = getLastStroke();
    if (lastStroke_ != null)
      lastStroke_.addPoint(_nthPoint, _nthStroke, _nthPointInStroke, _x, _y, _pressure, _tiltX, _tiltY, _evtTimeInMsec, 
        _type);
  }

  public ArrayList<Stroke> getStrokes() {
    return strokes;
  }

  public int getStrokesNum() {
    return strokes.size();
  }

  public Stroke getFirstStroke() {
    if (getStrokesNum() > 0)
      return strokes.get(0);
    return null;
  }

  public Stroke getLastStroke() {
    if (getStrokesNum() > 0)
      return strokes.get(getStrokesNum() - 1);
    return null;
  }

  public void removeFirstStroke() {
    if (getStrokesNum() > 0)
      strokes.remove(0);
  }
}