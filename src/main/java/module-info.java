module com.pranav.temple_software {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires org.xerial.sqlitejdbc;
	requires org.apache.pdfbox;
//	requires javafx.swing;
//	opens com.pranav.temple_software.controllers to javafx.fxml;
//	opens com.pranav.temple_software.fxml to javafx.fxml; // Add this
	exports com.pranav.temple_software.controllers;
}