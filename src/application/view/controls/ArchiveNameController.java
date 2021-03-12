package application.view.controls;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ArchiveNameController {
	private Stage dialogStage;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	@FXML
	TextField archiveField;

	@FXML
	private void initialize() {
	}

	public void setData(String data) {
		archiveField.setText(data);
	}
	
	@FXML
	public void saveName() {
		dialogStage.setUserData(archiveField.getText());
		dialogStage.close();
	}

}
