package com.pranav.temple_software;

/**
 * A non-modular main class that acts as a bridge to launch the
 * modular JavaFX application from a fat JAR.
 * This class should NOT be declared in the module-info.java file.
 */
public class Main {
	public static void main(String[] args) {
		// Call the main method of your actual JavaFX Application class
		Launcher.main(args);
	}
}