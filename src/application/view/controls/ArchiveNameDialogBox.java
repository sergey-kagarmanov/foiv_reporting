package application.view.controls;

import java.io.IOException;

import application.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ArchiveNameDialogBox extends DialogBox {

	public static String display(String data) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainApp.class.getResource("/view/controls/ArchiveNameDialog.fxml"));
		AnchorPane pane = null;
		try {
			pane = (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Stage stage = getStage("Имя архива", pane);
		ArchiveNameController controller = loader.getController();
		controller.setData(data);
		controller.setDialogStage(stage);
		stage.showAndWait();
		return (String) stage.getUserData();
	}
}