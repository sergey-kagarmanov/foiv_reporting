package application.view;

import application.MainApp;
import application.models.Key;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class ChooseKeyDialogController {

	private Stage dialogStage;

	private MainApp mainApp;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	ComboBox<Key> keysChooser;
	
	ObservableList<Key> keys;
	
	public void setKeys(ObservableList<Key> keys) {
		this.keys = keys;
		keysChooser.setItems(keys);
		keysChooser.getSelectionModel().selectFirst();
	}
	
	@FXML
	private void initialize() {
		keysChooser.setItems(keys);
		
	}
	
	@FXML
	private void save() {
		mainApp.setCurrentKey(keysChooser.getValue());
		dialogStage.close();
	}
}
