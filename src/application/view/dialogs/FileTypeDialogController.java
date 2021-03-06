package application.view.dialogs;

import java.io.File;

import application.MainApp;
import application.models.FileType;
import application.models.Report;
import application.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileTypeDialogController {

	private Stage dialogStage;

	private MainApp mainApp;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	ComboBox<Report> reportChooser;

	@FXML
	ComboBox<String> directionChooser;

	@FXML
	ComboBox<String> fileTypeChooser;

	@FXML
	TextField nameField;

	@FXML
	TextField maskField;

	@FXML
	TextField validSchemaField;

	@FXML
	CheckBox ticketCheck;

	@FXML
	CheckBox transportCheck;

	private FileType type;

	private Integer reportId;


	public void initialize() {

		ticketCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
					Boolean newValue) {
				if (newValue){
					transportCheck.setSelected(false);
					directionChooser.getSelectionModel().select(Constants.IN);
					transportCheck.setDisable(true);
					directionChooser.setDisable(true);
				}else{
					transportCheck.setDisable(false);
					directionChooser.setDisable(false);
				}
			}
		});
	}

	public void setData(FileType type, Integer reportId) {
		if (type != null) {
			this.type = type;
		} else {
			this.type = new FileType();
		}
		this.reportId = reportId;
		fillChoosers();
		fillData();
	}

	private void fillChoosers() {
		reportChooser.setItems(MainApp.getDb().getReports());
		directionChooser.setItems(FXCollections.observableArrayList(Constants.IN, Constants.OUT));
		fileTypeChooser.setItems(FXCollections.observableArrayList(Constants.FILETYPE));
		directionChooser.getSelectionModel().select(0);
		fileTypeChooser.getSelectionModel().select(0);
	}

	private void fillData() {
		reportChooser.getSelectionModel().select(MainApp.getDb().getReportById(reportId));
		directionChooser.getSelectionModel().select(type.getDirection() ? Constants.IN : Constants.OUT);
		fileTypeChooser.getSelectionModel().select(Constants.FILETYPE[type.getFileType()]);
		nameField.setText(type.getName());
		maskField.setText(type.getMask());
		ticketCheck.setSelected(type.getTicket());
		transportCheck.setSelected(type.getTransport());
		validSchemaField.setText(type.getValidationSchema());
	}

	@FXML
	public void handleSave(){
		String error = "";
		String warning = "";
		
		if ("".equals(nameField.getText())){
			warning += " ?????? ???????? ??????????,";
		}
		if ("".equals(maskField.getText())){
			error += " ??????????,";
		}
		if ("".equals(validSchemaField.getText())){
			warning += " ?????????? ??????????????????,";
		}
		if ("".equals(error)){
			boolean save = false;
			if ("".equals(warning)){
				save = true;
			}else{
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("????????????????????????????");
				alert.setHeaderText("???????????? ????????");
				alert.setContentText("?????? ???????????????????? ???????????????????? ???????????????????????? ????????:"+warning.substring(0, warning.length()-1)+". ???? ?????????? ???????????? ???????????????????");
				save = alert.showAndWait().get()==ButtonType.OK;
			}
			
			if (save){
				type.setDirection(Constants.IN.equals(directionChooser.getValue()));
				type.setFileType(Constants.getFileType(fileTypeChooser.getValue()));
				type.setMask(maskField.getText());
				type.setName(nameField.getText());
				type.setTicket(ticketCheck.isSelected());
				type.setTransport(transportCheck.isSelected());
				type.setValidationSchema(validSchemaField.getText());
				if (type.getId()==0){
					MainApp.getDb().insertFileType(type, reportId);
				}else{
					MainApp.getDb().updateFileType(type, reportId);
				}
				dialogStage.close();
			}
			
		}else{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("????????????");
			alert.setHeaderText("????????????");
			alert.setContentText("???? ?????????????????? ????????:"+error.substring(0, error.length()));
			alert.showAndWait();
		}
	}

	@FXML
	public void handleCancel() {
		dialogStage.close();
	}
	
	@FXML
	public void openvSchemaDialog() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File((validSchemaField.getText()==null || "".equals(validSchemaField.getText()))? "/" : validSchemaField.getText().substring(0, validSchemaField.getText().lastIndexOf("\\"))));
		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			validSchemaField.setText(selectedFile.getAbsolutePath());
		}

	}
	
	@FXML
	public void handleAttributesFile() {
		mainApp.showAttributeSettingsDialog(type);
	}

}
