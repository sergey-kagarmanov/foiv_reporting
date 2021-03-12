package application.view.dialogs;

import application.MainApp;
import application.models.Chain;
import application.models.Report;
import application.utils.Constants;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddEditChainDialogController {

	private Stage dialogStage;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	private Chain chain;

	@FXML
	TextField nameField;

	@FXML
	ComboBox<Report> reportChooser;

	@FXML
	ComboBox<String> directionChooser;

	public void initialize() {

	}

	public void setData(Chain chain) {
		fillChoosers();
		if (chain != null){
			this.chain = chain;
			fillData();
		}else{
			this.chain = new Chain();
		}
	}

	private void fillChoosers() {
		reportChooser.setItems(MainApp.getDb().getReports());
		directionChooser.setItems(FXCollections.observableArrayList(Constants.IN, Constants.OUT));
	}

	private void fillData() {
		nameField.setText(chain.getName());
		reportChooser.getSelectionModel().select(chain.getReport());
		directionChooser.getSelectionModel()
				.select(chain.getDirection() ? Constants.IN : Constants.OUT);
	}

	@FXML
	public void handleCancel() {
		dialogStage.close();
	}

	@FXML
	public void handleSave() {
		if (chain != null) {
			String error = "";
			if ("".equals(nameField.getText())) {
				error += " наименование последовательности,";
			}
			if (reportChooser.getValue() == null) {
				error += " отчетность,";
			}
			if (directionChooser.getValue() == null) {
				error += " направление,";
			}
			if ("".equals(error)) {
				chain.setName(nameField.getText());
				chain.setReport(reportChooser.getValue());
				chain.setDirection(directionChooser.getValue().equals(Constants.IN));
				MainApp.getDb().save(chain);
				dialogStage.close();
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Ошибка");
				alert.setTitle("Ошибка");
				alert.setContentText(
						"При сохранении произошли ошибки, не заполнены следующие поля:" + error);
				alert.showAndWait();
			}
		}
	}

}
