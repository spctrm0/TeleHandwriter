package cnc;

import main.G;

public class Cmd {

	public void a() {

	}

	// M3S000000
	// G4P000000
	public String cmdBack() {
		String cmd_ = "";
		// Pen up
		cmd_ += "M3";
		cmd_ += "S" + String.format("%06d", G.servoHover);
		cmd_ += "\r";
		cmd_ += "\n";
		// Set feedrate mode: unit per min
		cmd_ += "G94";
		cmd_ += "\r";
		cmd_ += "\n";
		// Back
		cmd_ += "G1";
		cmd_ += "X" + String.format("%.3f", G.isXInverted ? -G.xBack : G.xBack);
		cmd_ += "F" + String.format("%.3f", G.feedrateStrokeToStoke);
		cmd_ += "\r";
		cmd_ += "\n";
		// Set feedrate mode: inverse time
		cmd_ += "G93";
		cmd_ += "\r";
		cmd_ += "\n";
		// Delay
		cmd_ += "G4";
		cmd_ += "P" + String.format("%.6f", G.servoDelay3);
		cmd_ += "\r";
		cmd_ += "\n";
		return cmd_;
	}

	// $X
	// G92X000Y000
	public String cmdHome() {
		String cmd_ = "";
		// Lock off
		cmd_ += "$X";
		cmd_ += "\r";
		cmd_ += "\n";
		// Pen up
		cmd_ += "M3";
		cmd_ += "S" + String.format("%04d", G.servoHover);
		cmd_ += "\r";
		cmd_ += "\n";
		// Plotter home
		cmd_ += "$H";
		cmd_ += "\r";
		cmd_ += "\n";
		// Set current position as 0
		cmd_ += "G92X0Y0";
		cmd_ += "\r";
		cmd_ += "\n";
		// Set feedrate mode: unit per min
		cmd_ += "G94";
		cmd_ += "\r";
		cmd_ += "\n";
		// TeleHandwriter home
		cmd_ += "G1";
		cmd_ += "F" + String.format("%.3f", G.feedrateStrokeToStoke);
		cmd_ += "X" + String.format("%.3f", G.isXInverted ? -G.xZero : G.xZero);
		cmd_ += "Y" + String.format("%.3f", G.isYInverted ? -G.yZero : G.yZero);
		cmd_ += "\r";
		cmd_ += "\n";
		// Set feedrate mode: inverse time
		cmd_ += "G93";
		cmd_ += "\r";
		cmd_ += "\n";
		// Set current position as 0
		cmd_ += "G92X0000Y0000";
		cmd_ += "\r";
		cmd_ += "\n";
		return cmd_;
	}
}
