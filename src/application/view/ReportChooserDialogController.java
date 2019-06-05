package application.view;

import application.MainApp;
import application.models.Report;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class ReportChooserDialogController {

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
	
	void initialize(){
		
	}
	
	public void setData(ObservableList<Report> data){
		reportChooser.setItems(data);
	}
	
	@FXML
	void handleCancel(){
		dialogStage.close();
	}
	
	@FXML
	void handleOk(){
		mainApp.setCurrentReport(reportChooser.getValue());
		dialogStage.close();
	}
	
}
