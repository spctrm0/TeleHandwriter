package cnc;

import java.util.concurrent.TimeUnit;

import main.G;

public class Plotter {

	// -1:unknown, 0:home, 1:back, 2:paper
	private static int location = -1;
	// -1:stop, 0:home, 1:back, 2:paper
	private static int movingToward = -1;

	private static long	lastStopAtPaperInUsec	= 0;
	private static long	lastStopAtBackInUsec	= 0;

	public static void init() {
		location = -1;
		movingToward = -1;
		setLastStopAtPaperInUsec();
		setLastStopAtBackInUsec();
	}

	public static int getLocation() {
		return location;
	}

	public static void setLocation(int _location) {
		location = _location;
		String log_;
		log_ = "<Plotter>\tPlotter`s Location is ";
		switch (movingToward) {
			case -1:
				log_ += "unknown.";
				break;
			case 0:
				log_ += "home.";
				break;
			case 1:
				setLastStopAtBackInUsec();
				log_ += "back.";
				break;
			case 2:
				setLastStopAtPaperInUsec();
				log_ += "paper.";
				break;
		}
		System.out.println(log_);
	}

	public static int getMovingToward() {
		return movingToward;
	}

	public static void setmovingToward(int _movingToward) {
		movingToward = _movingToward;
		String log_;
		if (movingToward == -1) {
			log_ = "<Plotter>\tPlotter is stopped.";
		}
		else {
			log_ = "<Plotter>\tPlotter is moving toward ";
			switch (movingToward) {
				case 0:
					log_ += "home.";
					break;
				case 1:
					log_ += "back.";
					break;
				case 2:
					log_ += "paper.";
					break;
			}
		}
		System.out.println(log_);
	}

	// -1:irrelevant, 0:home, 1:back, 2:paper
	public static int movingToward(String _cmd) {
		String penUpCmd_ = "M3S";
		if (_cmd.contains(penUpCmd_)) {
			String home_ = String.format("%04d", G.servoHover);
			String back_ = String.format("%06d", G.servoHover);
			String paper_ = String.format("%05d", G.servoHover);
			if (_cmd.contains(paper_))
				return 2;
			else if (_cmd.contains(back_))
				return 1;
			else if (_cmd.contains(home_))
				return 0;
		}
		return -1;
	}

	// -1:irrelevant, 0:home, 1:back, 2:paper
	public static int stopAt(String _cmd) {
		if (_cmd.contains("G92X0000Y0000"))
			return 0;
		else if (_cmd.contains("G4P")) {
			String back_ = String.format("%.6f", G.servoDelay3);
			String paper_ = String.format("%.5f", G.servoDelay3);
			if (_cmd.contains(paper_))
				return 2;
			else if (_cmd.contains(back_))
				return 1;
		}
		return -1;
	}

	private static void setLastStopAtPaperInUsec() {
		lastStopAtPaperInUsec = System.nanoTime();
	}

	private static void setLastStopAtBackInUsec() {
		lastStopAtBackInUsec = System.nanoTime();
	}

	public static long getElapsedTimeSinceLastStopAtPaperInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastStopAtPaperInUsec);
	}

	public static long getElapsedTimeSinceLastStopAtBackInMsec() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastStopAtBackInUsec);
	}
}