package application.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xml.sax.SAXException;

import application.MainApp;
import application.models.FileTransforming;
import application.utils.Constants;
import application.utils.FileUtils;
import application.utils.Signatura;
import application.utils.XMLValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class SingleActionController {

	private Stage dialogStage;

	private MainApp mainApp;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	private ObservableList<Pair<String, String>> actions;

	@FXML
	ComboBox<Pair<String, String>> actionChooser;

	@FXML
	TextField outPath;

	@FXML
	ListView<File> files;

	private void initActions() {
		actions = FXCollections.observableArrayList();
		actions.add(new Pair<String, String>(Constants.ACTIONS[1], "Зашифровать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.ACTIONS[2], "Подписать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.ACTIONS[3], "Расшифровать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.ACTIONS[4], "Проверить и снять подпись") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.ACTIONS[6], "Заархивировать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.ACTIONS[7], "Переименовать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.ACTIONS[8], "Распаковать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.ACTIONS[9], "Скопировать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>("CHECK", "Проверить по схеме") {
			@Override
			public String toString() {
				return getValue();
			}
		});
	}

	@FXML
	private void initialize() {
		initActions();
		actionChooser.setItems(actions);
		actionChooser.setValue(new Pair<String, String>(Constants.ACTIONS[1], "Зашифровать"));
	}

	@FXML
	public void chooseFiles() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Выберите файлы для обработки");
		String pathOut = "c:\\sdm\\reporting";
		fileChooser.setInitialDirectory(new File(pathOut));
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
		files.setItems(FXCollections.observableList(selectedFiles));
	}

	@FXML
	public void chooseOutput() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String pathOut = "c:\\sdm\\reporting\\common_out";
		directoryChooser.setInitialDirectory(new File(pathOut));
		File selectedDirectory = directoryChooser.showDialog(null);
		outPath.setText(selectedDirectory.getAbsolutePath());
	}

	@FXML
	public void startAction() {
		Pair<String, String> action = actionChooser.getSelectionModel().getSelectedItem();
		if (action.getKey().equals(Constants.ACTIONS[1])) {
			mainApp.showChooseKeyDialog();
			Signatura signatura = new Signatura();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				signatura.initConfig(mainApp.getCurrentKey().getData());
				signatura.encryptFilesInPath(FileTransforming.toFileTransforming(files.getItems()),
						"");
				FileUtils.copyFiles(files.getItems(), outPath.getText());
			}
		} else if (action.getKey().equals(Constants.ACTIONS[2])) {
			mainApp.showChooseKeyDialog();
			Signatura signatura = new Signatura();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				signatura.initConfig(mainApp.getCurrentKey().getData());
				signatura.signFilesInPath(FileTransforming.toFileTransforming(files.getItems()));
				FileUtils.copyFiles(files.getItems(), outPath.getText());
			}
		} else if (action.getKey().equals(Constants.ACTIONS[3])) {
			mainApp.showChooseKeyDialog();
			Signatura signatura = new Signatura();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				signatura.initConfig(mainApp.getCurrentKey().getData());
				signatura.decryptFilesInPath(FileTransforming.toFileTransforming(files.getItems()));
				FileUtils.copyFiles(files.getItems(), outPath.getText());
			}
		} else if (action.getKey().equals(Constants.ACTIONS[4])) {
			mainApp.showChooseKeyDialog();
			Signatura signatura = new Signatura();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				signatura.initConfig(mainApp.getCurrentKey().getData());
				signatura.setVerifyParamaters();
				signatura.verifyAndUnsignFilesInPath(
						FileTransforming.toFileTransforming(files.getItems()));
				FileUtils.copyFiles(files.getItems(), outPath.getText());
			}
		} else if (action.getKey().equals(Constants.ACTIONS[6])) {
			mainApp.showChooseArchiveDialog(files.getItems());
			String archiveName = mainApp.getCurrentArchiveName();
			String command = FileUtils.exeDir + "arj.exe A -V5000k -Y -E ";
			command += outPath.getText() + "\\" + archiveName + " ";
			for (File file : files.getItems()) {
				command += file.getAbsolutePath() + " ";
			}

			Process p = null;
			Runtime r = Runtime.getRuntime();
			try {
				p = r.exec(command);
				InputStream is = p.getInputStream();
				int w = 0;
				while ((w = is.read()) != -1) {
					System.out.print((char) w);
				}
				System.out.println(p.waitFor());
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (action.getKey().equals(Constants.ACTIONS[7])) {

		} else if (action.getKey().equals(Constants.ACTIONS[8])) {
			ObservableList<File> resultFiles = FXCollections.observableArrayList();
			try {
				for (File f : files.getItems()) {
					resultFiles.addAll(FileUtils.getFrom7z(f));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileUtils.moveFiles(resultFiles, outPath.getText());
		} else if (action.getKey().equals(Constants.ACTIONS[9])) {
			FileUtils.copyFiles(files.getItems(), outPath.getText());
		} else if ("CHECK".equals(action.getKey())) {
			ObservableMap<File, String> exceptions = FXCollections.observableHashMap();
			/*FileChooser schemaChooser = new FileChooser();
			schemaChooser.setTitle("Выберите схему проверки");
			String pathOut = "c:\\sdm\\reporting";
			schemaChooser.setInitialDirectory(new File(pathOut));
			File schema = schemaChooser.showOpenDialog(null);*/
			mainApp.showChooseSchemaDialog(files.getItems());
			ObservableList<Pair<File, String>> schemaPairs = mainApp.getSchemaPairs();
			for (Pair<File, String> f : schemaPairs) {
				List<Exception> tmpList = XMLValidator.validate(f.getKey(), new File(f.getValue()));
				if (tmpList.size()>0) {
				String errors = "В файле " + f.getKey().getName();
				for(Exception ex : tmpList) {
					errors += " " + ((SAXException) ex).getMessage() + "\r\n";
				}
				exceptions.put(f.getKey(), errors);
				}
			}
			if (exceptions.size() > 0) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Результат проверки");
				String errors = "";
				for (String e : exceptions.values()) {
					errors += " " + e;
				}
				alert.setContentText(errors);
				alert.show();
			} else {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setContentText("Ошибок нет");
				alert.setTitle("Результат проверки");
				alert.show();
			}
		}
	}

	private void showAlert(String text) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setContentText(text);
		alert.setTitle("Ошибка");
		alert.show();
	}
}
