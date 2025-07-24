module com.pranav.temple_software {
	// JavaFX Modules
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;

	// Standard Java Modules
	requires java.sql;
	requires java.desktop;
	requires java.logging; // Replaced SLF4J with the standard logger

	// External Library Modules
	// requires org.slf4j; // This line has been removed
	requires com.h2database;
	requires org.xerial.sqlitejdbc;

	// Export packages for FXML access
	exports com.pranav.temple_software;
	exports com.pranav.temple_software.controllers;
	exports com.pranav.temple_software.listeners;
	exports com.pranav.temple_software.models;
	exports com.pranav.temple_software.repositories;
	exports com.pranav.temple_software.services;
	exports com.pranav.temple_software.utils;
	exports com.pranav.temple_software.controllers.menuControllers;
	exports com.pranav.temple_software.controllers.menuControllers.SevaManager;
	exports com.pranav.temple_software.controllers.menuControllers.History;
	exports com.pranav.temple_software.controllers.menuControllers.DonationManager;
	exports com.pranav.temple_software.controllers.menuControllers.OtherSevaManager;

	// Open packages for reflection access by JavaFX
	opens com.pranav.temple_software.controllers to javafx.fxml;
	opens com.pranav.temple_software.controllers.menuControllers to javafx.fxml;
	opens com.pranav.temple_software.controllers.menuControllers.History to javafx.fxml;
	opens com.pranav.temple_software.controllers.menuControllers.SevaManager to javafx.fxml;
	opens com.pranav.temple_software.controllers.menuControllers.DonationManager to javafx.fxml;
	opens com.pranav.temple_software.controllers.menuControllers.OtherSevaManager to javafx.fxml;
}
