module com.pranav.temple_software {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires org.apache.pdfbox;
	requires com.h2database;
	requires spring.context;
	requires jdk.compiler;
	exports com.pranav.temple_software.controllers;
	exports com.pranav.temple_software.listeners;
	exports com.pranav.temple_software.models;
	exports com.pranav.temple_software.repositories;
	exports com.pranav.temple_software.services;
	exports com.pranav.temple_software.utils;
	exports com.pranav.temple_software.controllers.menuControllers.SevaManager;
	exports com.pranav.temple_software.controllers.menuControllers.History;
	opens com.pranav.temple_software.controllers.menuControllers.History to javafx.fxml;
}