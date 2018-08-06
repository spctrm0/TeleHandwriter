package cnc;

import java.util.LinkedList;

import processing.core.PApplet;
import serial.SerialCallback;
import serial.SerialPort;

public class GrblComm implements SerialCallback {
	
	private PApplet			p5					= null;
	private SerialPort	serialPort	= null;
	private boolean			isConnected	= false;
	// Check buffer size at
	// https://github.com/gnea/grbl/wiki/Grbl-v1.1-Interface#streaming-a-g-code-program-to-grbl
	private final int						bfrSizeMx							= 128;
	private int									bfrSize								= 0;
	private LinkedList<String>	preDefinedCmdsRepo		= null;
	private LinkedList<String>	drawingCmdsRepo				= null;
	private LinkedList<String>	cmdsInBfr							= null;
	private LinkedList<Integer>	cmdsInBfrMovingToward	= null;
	private LinkedList<String>	feedback							= null;
	private final int						backTime							= 2000;
	private final int						homeTime							= 8000;

	public GrblComm(PApplet _p5) {
		p5 = _p5;
		preDefinedCmdsRepo = new LinkedList<String>();
		drawingCmdsRepo = new LinkedList<String>();
		cmdsInBfr = new LinkedList<String>();
		cmdsInBfrMovingToward = new LinkedList<Integer>();
		feedback = new LinkedList<String>();
		p5.registerMethod("pre", this);
	}

	public void pre() {
		reservePreDefinedCmdToRepo();
		writeCmdsToGrblBfr();
	}

	private void reservePreDefinedCmdToRepo() {
		boolean isStopAtPaper_ = Plotter.getMovingToward() == -1 && Plotter.getLocation() == 2;
		// boolean isBfrEmpty_ = bfrSize == 0 && cmdsInBfr.isEmpty() &&
		// cmdsInBfrMovingToward.isEmpty();
		boolean isNothingToDraw_ = drawingCmdsRepo.isEmpty();
		boolean isTimeToBack_ = Plotter.getElapsedTimeSinceLastStopAtPaperInMsec() > backTime;
		if (isStopAtPaper_ && isNothingToDraw_ && isTimeToBack_) {
		}
		else {
			boolean isStopAtBack_ = Plotter.getMovingToward() == -1 && Plotter.getLocation() == 1;
			boolean isTimeToHome_ = Plotter.getElapsedTimeSinceLastStopAtBackInMsec() > homeTime;
			if (isStopAtBack_ && isNothingToDraw_ && isTimeToHome_) {
			}
		}
	}

	private void writeCmdsToGrblBfr() {
		while (bfrSize <= bfrSizeMx && !preDefinedCmdsRepo.isEmpty()) {
			String cmd_ = preDefinedCmdsRepo.getFirst();
			preDefinedCmdsRepo.removeFirst();
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				bfrSize += cmd_.length();
				cmdsInBfr.add(cmd_);
				int movingToward_ = Plotter.movingToward(cmd_);
				if (cmdsInBfr.size() == 1 && movingToward_ != -1) {
					Plotter.setmovingToward(movingToward_);
					Plotter.setLocation(movingToward_);
				}
				else if (cmdsInBfr.size() > 1)
					cmdsInBfrMovingToward.add(movingToward_);
				serialPort.write(cmd_);
			}
			else
				break;
		}
		while (bfrSize <= bfrSizeMx && !drawingCmdsRepo.isEmpty() && preDefinedCmdsRepo.isEmpty()) {
			String cmd_ = drawingCmdsRepo.getFirst();
			drawingCmdsRepo.removeFirst();
			if (bfrSize + cmd_.length() <= bfrSizeMx) {
				bfrSize += cmd_.length();
				cmdsInBfr.add(cmd_);
				int movingToward_ = Plotter.movingToward(cmd_);
				if (cmdsInBfr.size() == 1 && movingToward_ != -1) {
					Plotter.setmovingToward(movingToward_);
					Plotter.setLocation(movingToward_);
				}
				else if (cmdsInBfr.size() > 1)
					cmdsInBfrMovingToward.add(movingToward_);
				serialPort.write(cmd_);
			}
			else
				break;
		}
	}

	@Override
	public void serialConnectionCallBack(SerialPort _serialPort, boolean _isConnected) {
		setConnected(_serialPort, _isConnected);
	}

	public void setConnected(SerialPort _serialPort, boolean _isConnected) {
		boolean isChanged_ = _isConnected != isConnected;
		_isConnected = isConnected;
		if (isConnected && isChanged_) {
			String log_ = "<GrblComm>\tConnected.";
			System.out.println(log_);
			serialPort = _serialPort;
			Plotter.init();
			p5.registerMethod("pre", this);
		}
		else if (!isConnected) {
			if (isChanged_) {
				p5.unregisterMethod("pre", this);
				serialPort = null;
				String log_ = "<GrblComm>\tDisconnected.";
				System.out.println(log_);
			}
		}
	}

	@Override
	public void serialMsgCallBack(String _msg) {
		manageGrblFeedback(_msg);
	}

	public void manageGrblFeedback(String _msg) {
		if (_msg.equals("ok") || _msg.contains("error:")) {
			if (!cmdsInBfrMovingToward.isEmpty()) {
				int movingToward_ = cmdsInBfrMovingToward.getFirst();
				cmdsInBfrMovingToward.removeFirst();
				if (movingToward_ != -1) {
					Plotter.setmovingToward(movingToward_);
					Plotter.setLocation(movingToward_);
				}
			}
			String cmdInBfr_ = cmdsInBfr.getFirst();
			bfrSize -= cmdInBfr_.length();
			cmdsInBfr.removeFirst();
			feedback.clear();
			int stopAt_ = Plotter.stopAt(cmdInBfr_);
			if (stopAt_ != -1) {
				Plotter.setmovingToward(-1);
				Plotter.setLocation(stopAt_);
			}
		}
		feedback.add(_msg);
	}
}