package com.pranav.temple_software;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.utils.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Launcher extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		new DatabaseManager();
		FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource(
				"/fxml/MainViewKannada.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		MainController controller = fxmlLoader.getController(); // Get controller instance
		controller.setMainStage(stage);
		stage.setTitle("Temple Software");
		stage.setScene(scene);
//		stage.setMinHeight(350);
//		stage.setMinWidth(450);
		stage.setMaximized(true);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.show();
	}
}