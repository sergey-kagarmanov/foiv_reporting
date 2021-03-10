package application.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.xml.sax.SAXException;

import application.MainApp;
import application.models.ErrorFile;
import application.models.FileTransforming;
import application.utils.Constants;
import application.utils.DateUtils;
import application.utils.FileUtils;
import application.utils.Signatura;
import application.utils.SignaturaService;
import application.utils.SignaturaServiceAbstract;
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
		actions.add(new Pair<String, String>(Constants.ENCRYPT, "Зашифровать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.SIGN, "Подписать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.DECRYPT, "Расшифровать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.UNSIGN, "Проверить и снять подпись") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.PACK, "Заархивировать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.RENAME, "Переименовать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.UNPACK, "Распаковать") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.COPY, "Скопировать") {
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
		actionChooser.setValue(new Pair<String, String>(Constants.ENCRYPT, "Зашифровать"));
	}

	@FXML
	public void chooseFiles() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Выберите файлы для обработки");
		String pathOut = System.getProperty("user.dir");
		fileChooser.setInitialDirectory(new File(pathOut));
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
		files.setItems(FXCollections.observableList(selectedFiles));
	}

	@FXML
	public void chooseOutput() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String pathOut = files.getItems().size() > 0 ? files.getItems().get(0).getParent()
				: System.getProperty("user.dir");
		directoryChooser.setInitialDirectory(new File(pathOut));
		File selectedDirectory = directoryChooser.showDialog(null);
		outPath.setText(selectedDirectory.getAbsolutePath());
	}

	private ObservableList<File> copyFiles() {
		File archive = new File(
				"arch\\" + actionChooser.getSelectionModel().getSelectedItem().getKey()
						+"\\"+ DateUtils.toFolderString(LocalDate.now()));
		archive.mkdirs();
		FileUtils.copyFiles(files.getItems(), archive.getAbsolutePath());
		FileUtils.clearTmp();
		return FileUtils.copyFilesReturn(files.getItems(), FileUtils.tmpDir);
	}

	@FXML
	public void startAction() {
		Pair<String, String> action = actionChooser.getSelectionModel().getSelectedItem();
		if (action.getKey().equals(Constants.ENCRYPT)) {
			mainApp.showChooseKeyDialog();
			Signatura signatura = new Signatura();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				ObservableList<File> tmp = copyFiles();
				signatura.initConfig(mainApp.getCurrentKey().getData());
				ObservableList<ErrorFile> errorFiles = null;
				try {
					SignaturaServiceAbstract service = new SignaturaServiceAbstract() {
						
					
						@Override
						public int action(String source, String target) {
							return signatura.encrypt(source, target);
						}
					};
					errorFiles = service.execute(FileTransforming.toFileTransforming(tmp));
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (errorFiles.size() == 0) {
					FileUtils.copyFiles(tmp, outPath.getText());
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Зашифрование");
					alert.setContentText("Обработка выполнена успешно");
					alert.show();
				}
			}
		} else if (action.getKey().equals(Constants.SIGN)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				ObservableList<File> tmp = copyFiles();
				SignaturaService service = new SignaturaService(mainApp.getCurrentKey());
				ObservableList<ErrorFile> errorFiles = service
						.sign(FileTransforming.toFileTransforming(tmp));
				
				if (errorFiles.size() == 0) {
					FileUtils.copyFiles(tmp, outPath.getText());
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Подпись");
					alert.setContentText("Обработка выполнена успешно");
					alert.show();
				}
			}
		} else if (action.getKey().equals(Constants.DECRYPT)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				SignaturaService service = new SignaturaService(mainApp.getCurrentKey());
				ObservableList<File> tmp = copyFiles();
				
				ObservableList<ErrorFile> errorFiles = service.decrypt(FileTransforming.toFileTransforming(tmp));
				if (errorFiles.size() == 0) {
					FileUtils.copyFiles(tmp, outPath.getText());
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Расшифрование");
					alert.setContentText("Обработка выполнена успешно");
					alert.show();
				}
				service.unload();
			}
		} else if (action.getKey().equals(Constants.UNSIGN)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				SignaturaService service = new SignaturaService(mainApp.getCurrentKey());
				ObservableList<File> tmp = copyFiles();
				ObservableList<ErrorFile> errorFiles = service.unsign(
						FileTransforming.toFileTransforming(tmp));
				if (errorFiles.size() == 0) {
					FileUtils.copyFiles(tmp, outPath.getText());
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Поверка и снятие подписи");
					alert.setContentText("Обработка выполнена успешно");
					alert.show();
				}
				service.unload();
			}
		} else if (action.getKey().equals(Constants.PACK)) {
			ObservableList<File> tmp = copyFiles();
			mainApp.showChooseArchiveDialog(files.getItems());
			String archiveName = mainApp.getCurrentArchiveName();
			String command = FileUtils.exeDir + "arj.exe A -V5000k -Y -E ";
			command += outPath.getText() + "\\" + archiveName + " ";
			for (File file : tmp) {
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
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Архивация");
				alert.setContentText("Обработка выполнена успешно");
				alert.show();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (action.getKey().equals(Constants.RENAME)) {

		} else if (action.getKey().equals(Constants.UNPACK)) {
			ObservableList<File> resultFiles = FXCollections.observableArrayList();
			try {
				ObservableList<File> tmp = copyFiles();
				for (File f : tmp) {
					resultFiles.addAll(FileUtils.getFrom7z(f));
				}
				if (resultFiles.size() > 0) {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Разархивирование");
					alert.setContentText("Обработка выполнена успешно");
					alert.show();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			FileUtils.moveFiles(resultFiles, outPath.getText());
		} else if (action.getKey().equals(Constants.COPY)) {
			FileUtils.copyFiles(files.getItems(), outPath.getText());
		} else if ("CHECK".equals(action.getKey())) {
			ObservableMap<File, String> exceptions = FXCollections.observableHashMap();
			/*
			 * FileChooser schemaChooser = new FileChooser();
			 * schemaChooser.setTitle("Выберите схему проверки"); String pathOut
			 * = "c:\\sdm\\reporting"; schemaChooser.setInitialDirectory(new
			 * File(pathOut)); File schema = schemaChooser.showOpenDialog(null);
			 */
			mainApp.showChooseSchemaDialog(files.getItems());
			ObservableList<Pair<File, String>> schemaPairs = mainApp.getSchemaPairs();
			for (Pair<File, String> f : schemaPairs) {
				List<Exception> tmpList = XMLValidator.validate(f.getKey(), new File(f.getValue()));
				if (tmpList.size() > 0) {
					String errors = "В файле " + f.getKey().getName();
					for (Exception ex : tmpList) {
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
