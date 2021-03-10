package application.view.dialogs;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import application.MainApp;
import application.models.FileType;
import application.models.Report;
import application.utils.Constants;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class CheckScemaDialogController {

	private Stage dialogStage;

	private MainApp mainApp;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	TextField reportField;

	@FXML
	TextField schemaField;

	@FXML
	CheckBox reportFlag;

	@FXML
	TableView<Pair<File, String>> handleTable;

	@FXML
	TableColumn<Pair<File, String>, File> nameColumn;

	@FXML
	TableColumn<Pair<File, String>, String> schemaColumn;

	private ObservableList<File> files;
	ObservableList<Pair<File, String>> handlers;
	ObservableList<String> fails;

	@FXML
	private void initialize() {
		nameColumn.setCellValueFactory(
				(cellData) -> new SimpleObjectProperty<File>(cellData.getValue().getKey()));
		schemaColumn.setCellValueFactory(
				(cellData) -> new SimpleStringProperty(cellData.getValue().getValue()));
		handleTable.setRowFactory(tv->{
			TableRow<Pair<File, String>> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (! row.isEmpty() && event.getButton()==MouseButton.PRIMARY 
		             && event.getClickCount() == 2) {

		        	Pair<File, String> clickedRow = row.getItem();
					FileChooser schemaChooser = new FileChooser();
					schemaChooser.setTitle("Выберите схему проверки");
					String pathOut = "c:\\sdm\\reporting";
					schemaChooser.setInitialDirectory(new File(pathOut));
					File schema = schemaChooser.showOpenDialog(null);
					Pair<File, String> tmp = new Pair<>(clickedRow.getKey(), schema.getAbsolutePath());
					handlers.set(row.getIndex(),tmp);
		        }
		    });
		    return row ;

		});
	}

	public void setFiles(ObservableList<File> files) {
		this.files = files;
		loadData();
	}
	
	@FXML
	public void saveData() {
		mainApp.setSchemaPairs(handlers);
		dialogStage.close();
	}

	private void loadData() {
		handlers = FXCollections.observableArrayList();
		fails = FXCollections.observableArrayList();
		Report currentReport = null;
		boolean breakFlag = false;
		List<Report> reports = mainApp.getDb().getReports();
		ObservableList<File> checkList = FXCollections.observableArrayList();
		checkList.addAll(files);
		for (Report report : reports) {
			ObservableList<FileType> fTypes = mainApp.getDb().getFileTypeAsList(report.getId(),
					Constants.OUT_DB, 0);
			for (FileType fType : fTypes) {
				Pattern p = Pattern.compile(fType.getMask());
				for (File file : files) {
					if (p.matcher(file.getName()).matches()) {
						checkList.remove(file);
						if (fType.getValidationSchema() != null
								&& !"".equals(fType.getValidationSchema()))
							handlers.add(new Pair<File, String>(file, fType.getValidationSchema()));
						else
							fails.add(file.getName());

						if (currentReport == null) {
							currentReport = report;
						} else if (report.getId() != currentReport.getId()) {
							breakFlag = true;
						}
					}
				}
			}
		}

		if (breakFlag) {
			reportField.setText("Файлы соответствуют нескольким отчетам");
		} else {
			reportField.setText(currentReport.getName());
		}

		handleTable.setItems(handlers);

		if (checkList.size() > 0) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Отсутствуют схемы");
			String tmp = "";
			for (File f : checkList) {
				tmp += f.getName() + "\r\n";
			}
			alert.setContentText("Отсутствуют схемы для следующих файлов:" + tmp);
			alert.show();
		}

	}
}
