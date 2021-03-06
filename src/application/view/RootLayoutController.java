package application.view;

import application.MainApp;
import javafx.fxml.FXML;

public class RootLayoutController {

	private MainApp mainApp;
	
	public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
	
	@FXML
	public void handleArchiveOpen(){
		mainApp.showArchive();
	}
	
	@FXML
	public void handleAddReportOpen(){
		mainApp.showAddReportDialog();
	}
	
	@FXML
	public void handleAttributes(){
		mainApp.showAttributeDialog();
	}
	
	@FXML
	public void handleKeys(){
		mainApp.showKeyDialog();
	}
	
	@FXML
	public void handleActions(){
		mainApp.showActionDialog();
	}
	
	@FXML
	public void handleAttributeSettings(){
		mainApp.showAttributeSettingsDialog(null);
	}
	
	@FXML
	public void handleSettings(){
		mainApp.showSettingsDialog();
	}
	
	@FXML void handleSimpleActions() {
		mainApp.showSingleAction();
	}
}
