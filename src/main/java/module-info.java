module com.pranav.temple_software {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires java.sql;
	exports com.pranav.temple_software.controllers;
	exports com.pranav.temple_software.listeners;
	exports com.pranav.temple_software.models;
	exports com.pranav.temple_software.repositories;
	exports com.pranav.temple_software.services;
	exports com.pranav.temple_software.utils;
	exports com.pranav.temple_software.controllers.menuControllers.SevaManager;
	exports com.pranav.temple_software.controllers.menuControllers.History;
	opens com.pranav.temple_software.controllers.menuControllers.History to javafx.fxml;
	exports com.pranav.temple_software to javafx.graphics;
	opens com.pranav.temple_software.controllers;
	opens com.pranav.temple_software.controllers.menuControllers;
	exports com.pranav.temple_software.controllers.menuControllers.DonationManager;
	exports com.pranav.temple_software.controllers.menuControllers.OtherSevaManager;
	opens com.pranav.temple_software.controllers.menuControllers.SevaManager;
	opens com.pranav.temple_software.controllers.menuControllers.DonationManager;
	opens com.pranav.temple_software.controllers.menuControllers.OtherSevaManager;
}