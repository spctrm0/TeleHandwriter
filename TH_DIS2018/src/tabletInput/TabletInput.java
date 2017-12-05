package tabletInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import codeanticode.tablet.Tablet;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class TabletInput {
	public PApplet					p5				= null;
	public Tablet					tablet;

	public boolean					modeWritable	= false;
	public boolean					modeCalibration	= false;

	public List<TabletInputData>	tabletInputData;
	public int						strokeIdx		= -1;

	public TabletInput(PApplet _p5) {
		p5 = _p5;
		p5.registerMethod("mouseEvent", this);
		tablet = new Tablet(p5);
		tabletInputData = Collections.synchronizedList(new ArrayList<TabletInputData>());
	}

	public void mouseEvent(MouseEvent _mEvt) {
		if (tablet.getPenKind() == Tablet.STYLUS) {
			if (modeCalibration) {
				if (_mEvt.getAction() == MouseEvent.PRESS) {
					tabletInputData.add(new TabletInputData(-1, tablet.getPenX(), tablet.getPenY(),
							tablet.getPressure(), tablet.getTiltX(), tablet.getTiltY(), _mEvt.getMillis()));
				} else if (_mEvt.getAction() == MouseEvent.RELEASE)
					toggleCalibration();
			} else {
				if (modeWritable) {
					if (_mEvt.getAction() == MouseEvent.PRESS || _mEvt.getAction() == MouseEvent.DRAG
							|| _mEvt.getAction() == MouseEvent.RELEASE) {
						boolean isHead_ = false;
						boolean isTail_ = false;
						if (_mEvt.getAction() == MouseEvent.PRESS) {
							strokeIdx++;
							isHead_ = true;
						} else if (_mEvt.getAction() == MouseEvent.RELEASE)
							isTail_ = true;
						tabletInputData.add(
								new TabletInputData(strokeIdx, tablet.getPenX(), tablet.getPenY(), tablet.getPressure(),
										tablet.getTiltX(), tablet.getTiltY(), _mEvt.getMillis(), isHead_, isTail_));
					}
				}
			}
		}
	}

	public boolean toggleWritable() {
		modeWritable = !modeWritable;
		return modeWritable;
	}

	public boolean toggleCalibration() {
		modeCalibration = !modeCalibration;
		return modeCalibration;
	}

	public boolean isDataEmpty() {
		return tabletInputData.size() == 0;
	}

	public TabletInputData getData() {
		if (!isDataEmpty())
			return tabletInputData.get(0);
		return null;
	}

	public void removeData() {
		tabletInputData.remove(0);
	}
}
