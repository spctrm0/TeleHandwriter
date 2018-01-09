public class Stroke {
  private ArrayList<Point>  points = null;

  private boolean  isCompleted = false;

  public Stroke(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y, float _pressure, float _tiltX, 
    float _tiltY, long _evtTimeInMsec, int _type) {
    points = new ArrayList<Point>();
    points.add(
      new Point(_nthPoint, _nthStroke, _nthPointInStroke, _x, _y, _pressure, _tiltX, _tiltY, _evtTimeInMsec, _type));
  }

  public void addPoint(int _nthPoint, int _nthStroke, int _nthPointInStroke, float _x, float _y, float _pressure, 
    float _tiltX, float _tiltY, long _evtTimeInMsec, int _type) {
    points.add(
      new Point(_nthPoint, _nthStroke, _nthPointInStroke, _x, _y, _pressure, _tiltX, _tiltY, _evtTimeInMsec, _type));
    boolean isTail_ = _type == 2;
    if (isTail_)
      isCompleted = true;
  }

  public ArrayList<Point> getPoints() {
    return points;
  }

  public int getPointsNum() {
    return points.size();
  }

  public Point getFirstPoint() {
    if (getPointsNum() > 0)
      return points.get(0);
    return null;
  }

  public Point getSecondPoint() {
    if (getPointsNum() > 1)
      return points.get(1);
    return null;
  }

  public Point getLastPoint() {
    if (getPointsNum() > 0)
      return points.get(getPointsNum() - 1);
    return null;
  }

  public void removeFirstPoint() {
    if (getPointsNum() > 0)
      points.remove(0);
  }

  public boolean isCompleted() {
    return isCompleted;
  }
}