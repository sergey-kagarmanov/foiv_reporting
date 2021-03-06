package application.view.dialogs;

import java.util.ArrayList;
import java.util.List;

import application.MainApp;
import application.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Pair;

public class SettingsDialogController {

	@FXML
	private TextField dateFormat;

	@FXML
	private TextField dateFormatDB;

	@FXML
	private TextField dateFormatArchive;

	@FXML
	private CheckBox autoKey;

	@FXML
	private TextField loadKey;

	@FXML
	private TextField unloadKey;

	@FXML
	private TextField verbaPath;

	@FXML
	private TextField arjPath;

	@FXML
	private TextField workDir;

	@FXML
	private TextField tmpDir;

	@FXML
	private TextField fileSize;

	@FXML
	private TextField fileCount;

	private Stage dialogStage;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void initialize() {
		autoKey.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
					Boolean newValue) {
				if (newValue){
					loadKey.setDisable(false);
					unloadKey.setDisable(false);
				}else{
					loadKey.setDisable(true);
					unloadKey.setDisable(true);
				}
				
			}
		});
	}

	public void setData(){
		MainApp.getDb().loadSettings().forEach(data -> fillData(data));
	}
	
	private void fillData(Pair<String, String> pair) {
		switch (pair.getKey()) {
		case Constants.DATE_FORMAT:
			dateFormat.setText(pair.getValue());
			break;
		case Constants.DATE_FORMAT_DB:
			dateFormatDB.setText(pair.getValue());
			break;
		case Constants.DATE_FORMAT_ARCHIVE:
			dateFormatArchive.setText(pair.getValue());
			break;
		case Constants.ARJ_PATH:
			arjPath.setText(pair.getValue());
			break;
		case Constants.AUTO_KEY:
			autoKey.setSelected("true".equals(pair.getValue()));
			loadKey.setDisable("false".equals(pair.getValue()));
			unloadKey.setDisable("false".equals(pair.getValue()));
			break;
		case Constants.FILE_COUNT:
			fileCount.setText(pair.getValue());
			break;
		case Constants.FILE_SIZE:
			fileSize.setText(pair.getValue());
			break;
		case Constants.LOAD_KEY:
			loadKey.setText(pair.getValue());
			break;
		case Constants.TMP_DIR:
			tmpDir.setText(pair.getValue());
			break;
		case Constants.UNLOAD_KEY:
			unloadKey.setText(pair.getValue());
			break;
		case Constants.VERBA_PATH:
			verbaPath.setText(pair.getValue());
			break;
		case Constants.WORK_DIR:
			workDir.setText(pair.getValue());
			break;
		}
	}
	
	@FXML
	public void handleOk(){
		String tmp = "";
		if ("".equals(dateFormat)){
			tmp += " ???????????? ????????\r\n";
		}
		if ("".equals(dateFormatDB)){
			tmp += " ???????????? ???????? ???????? ????????????\r\n";
		}
		if ("".equals(dateFormatArchive)){
			tmp += " ???????????? ???????? ?????????????????????????? ??????????\r\n";
		}
		if (autoKey.isSelected()){
			if ("".equals(loadKey)){
				tmp += " ???????????? ???????????????? ????????????\r\n";
			}
			if ("".equals(unloadKey)){
				tmp += " ???????????? ???????????????? ????????????\r\n";
			}
		}
		if ("".equals(verbaPath)){
			tmp += " ???????? ?? ??????????-OW\r\n";
		}
		if ("".equals(arjPath)){
			tmp += " ?????????????????? ARJ\r\n";
		}
		if ("".equals(workDir)){
			tmp += " ?????????????? ????????????????????\r\n";
		}
		if ("".equals(tmpDir)){
			tmp += " ?????????????????? ????????????????????\r\n";
		}
		if ("".equals(fileSize)){
			tmp += " ???????????? ??????????\r\n";
		}
		if ("".equals(fileCount)){
			tmp += " ???????????????????? ????????????\r\n";
		}
		if ("".equals(tmp)){
			saveData();
			dialogStage.close();
		}else{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("????????????");
			alert.setHeaderText("???? ?????????????????? ?????????????????? ????????");
			alert.setContentText(tmp);
			alert.showAndWait();
		}
	}
	
	@FXML
	public void handleCancel(){
		dialogStage.close();
	}
	
	private void saveData(){
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		list.add(new Pair<String, String>(Constants.DATE_FORMAT, dateFormat.getText()));
		list.add(new Pair<String, String>(Constants.DATE_FORMAT_ARCHIVE, dateFormatArchive.getText()));
		list.add(new Pair<String, String>(Constants.DATE_FORMAT_DB, dateFormatDB.getText()));
		list.add(new Pair<String, String>(Constants.AUTO_KEY, autoKey.isSelected()? "true" : "false"));
		list.add(new Pair<String, String>(Constants.LOAD_KEY, loadKey.getText()));
		list.add(new Pair<String, String>(Constants.UNLOAD_KEY, unloadKey.getText()));
		list.add(new Pair<String, String>(Constants.VERBA_PATH, verbaPath.getText()));
		list.add(new Pair<String, String>(Constants.ARJ_PATH, arjPath.getText()));
		list.add(new Pair<String, String>(Constants.WORK_DIR, workDir.getText()));
		list.add(new Pair<String, String>(Constants.TMP_DIR, tmpDir.getText()));
		list.add(new Pair<String, String>(Constants.FILE_SIZE, fileSize.getText()));
		list.add(new Pair<String, String>(Constants.FILE_COUNT, fileCount.getText()));
		MainApp.getDb().saveSettings(list);
	}
}
