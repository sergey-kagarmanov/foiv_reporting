package application.view;

import application.MainApp;
import application.models.AttributeDescr;
import application.models.FileAttribute;
import application.models.FileType;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddEditAttributeDescrController {

	private Stage dialogStage;


	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	private AttributeDescr attr;
	
	@FXML
	ComboBox<FileType> typeChooser;
	
	@FXML
	ComboBox<FileAttribute> attributeChooser;
	
	@FXML
	CheckBox inNameCheck;
	
	@FXML
	TextField locationField;
	
	@FXML
	TextField etcField;
	
	public void initialize(){
		
	}
	
	private void fillChoosers(){
		typeChooser.setItems(MainApp.getDb().getFileTypes());
		attributeChooser.setItems(MainApp.getDb().getAttributes());
	}
	
	public void setData(AttributeDescr attr){
		fillChoosers();
		this.attr = attr;
		fillData();
	}
	
	private void fillData(){
		if (attr!=null){
			if (attr.getAttr()!=null){
				attributeChooser.setValue(attr.getAttr());
			}
			if (attr.getFile()!=null){
				typeChooser.setValue(attr.getFile());
			}
			inNameCheck.setSelected(attr.getInName());
			locationField.setText(attr.getLocation());
			etcField.setText(attr.getEtc());
		}
	}
	
	@FXML
	public void handleSave(){
		String error = "";
		String warning = "";
		boolean save = true;
		if (typeChooser.getValue()==null){
			error += " тип файла,";
		}
		if (locationField.getText()==null || "".equals(locationField.getText())){
			error += " расположение,";
		}
		if (etcField.getText() == null || "".equals(etcField.getText())){
			warning += " дополнительно,";
		}
		if (attributeChooser.getValue()==null){
			error += " атрибут";
		}
		
		if (!"".equals(error)){
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка");
			alert.setContentText("Необходимо заполнить следуюшие поля:"+error.substring(0, error.length()-2));
			alert.showAndWait();
			save = false;
		}
		
		if (!"".equals(warning)){
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Предупреждение");
			alert.setHeaderText("Предупреждение");
			alert.setContentText("Необходимо заполнить следуюшие поля:"+warning.substring(0, warning.length()-2));
			save = ButtonType.OK == alert.showAndWait().get();
		}
		if (save){
			attr.setAttr(attributeChooser.getValue());
			attr.setEtc(etcField.getText());
			attr.setFile(typeChooser.getValue());
			attr.setInName(inNameCheck.isSelected());
			attr.setLocation(locationField.getText());
			MainApp.getDb().save(attr);
			dialogStage.close();
		}
		
	}
	
	@FXML
	public void handleCancel(){
		dialogStage.close();
	}
	
	public void setFileType(FileType type) {
		typeChooser.getSelectionModel().select(type);
	}
}
