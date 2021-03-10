package application.view.dialogs;

import application.MainApp;
import application.models.Action;
import application.models.Chain;
import application.models.Key;
import application.models.ProcessStep;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddeditActionDialogController {

	private Stage dialogStage;

	private MainApp mainApp;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	private ProcessStep step;

	@FXML
	TextField position;

	@FXML
	ComboBox<Action> typeChooser;

	@FXML
	ComboBox<Key> keyChooser;

	@FXML
	TextField dataField;

	private Chain chain;

	public void initialize() {

	}

	private void fillChoosers() {
		typeChooser.setItems(mainApp.getDb().getActions());
		keyChooser.setItems(mainApp.getDb().getKeys());
	}

	public void setData(ProcessStep step, Chain chain) {
		fillChoosers();
		if (step != null) {
			this.step = step;
			position.setText(step.getPosition().toString());
			typeChooser.getSelectionModel().select(step.getAction());			
			if(keyChooser.getItems().contains(step.getKey())) {
				keyChooser.getSelectionModel().select(step.getKey());
			}
			dataField.setText(step.getData());
		}else{
			this.step = new ProcessStep();
		}
		this.chain = chain;
	}

	@FXML
	public void saveData() {
		String error = "";
		String warning = "";
		boolean save = true;
		boolean posType = false;
		if ("".equals(position.getText())) {
			error += " позиция,";
		} else {
			try {
				Integer.parseInt(position.getText());
			} catch (Exception e) {
				posType = true;
			}
		}
		if (typeChooser.getValue() == null) {
			error += " тип операции,";
		}
		if (keyChooser.getValue() == null) {
			warning += " ключ,";
		}
		if ("".equals(dataField)) {
			warning += " данные,";
		}

		if (!"".equals(error)) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Ошибка");
			alert.setTitle("Ошибка");
			alert.setContentText("Не заполнены следующие поля:"
					+ error.substring(0, error.length() - 1) + "." + (posType
							? " Неверное значение в поле \"Позиция\" - должно быть число." : ""));
			alert.showAndWait();
			save = false;
		} else if (!"".equals(warning)) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Предупреждение");
			alert.setHeaderText("Подтвердите сохраниние");
			alert.setContentText("Вы оставили поля:" + warning.substring(0, warning.length() - 1)
					+ " пустыми. Вы точно хотите сохранить настройки этапа?");
			save = alert.showAndWait().get() == ButtonType.OK;
		}

		if (save) {
			step.setAction(typeChooser.getValue());
			step.setPosition(Integer.parseInt(position.getText()));
			step.setKey(keyChooser.getValue());
			step.setData(dataField.getText());
			mainApp.getDb().save(step, chain);
			dialogStage.close();
		}
	}
	
	
	@FXML
	public void cancel(){
		dialogStage.close();
	}
}
