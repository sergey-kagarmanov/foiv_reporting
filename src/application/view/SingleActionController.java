package application.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import application.MainApp;
import application.models.ErrorFile;
import application.models.FileTransforming;
import application.models.Key;
import application.utils.AlertWindow;
import application.utils.Constants;
import application.utils.DateUtils;
import application.utils.FileUtils;
import application.utils.skzi.DecryptorHandler;
import application.utils.skzi.EncryptorHandler;
import application.utils.skzi.HashHandler;
import application.utils.skzi.SignHandler;
import application.utils.skzi.SignaturaHandler;
import application.utils.skzi.SignaturaTheadingExecutor;
import application.utils.skzi.UnsignHandler;
import application.utils.xml.XMLValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
		String pathOut = "c:\\";
		fileChooser.setInitialDirectory(new File(pathOut));
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
		files.setItems(FXCollections.observableList(selectedFiles));
	}

	@FXML
	public void chooseOutput() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String pathOut = files.getItems().size() > 0 ? files.getItems().get(0).getParent()
				: "c:\\";
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
		ObservableList<ErrorFile> errorFiles = null;
		if (action.getKey().equals(Constants.ENCRYPT)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(FileTransforming.toFileTransforming(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) {
						return new EncryptorHandler(key);
					}
					
				};
				try {
					errorFiles = executor.execute(mainApp.getCurrentKey(), outPath.getText());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				AlertWindow.show(Constants.ENCRYPT_RUS, errorFiles);
			}
		} else if (action.getKey().equals(Constants.SIGN)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(FileTransforming.toFileTransforming(files.getItems())) {
					
					@Override
					public SignaturaHandler getHandler(Key key) {
						return new SignHandler(key);
					}
				};
				try {
					errorFiles = executor.execute(mainApp.getCurrentKey(), outPath.getText());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				AlertWindow.show(Constants.SIGN_RUS, errorFiles);
			}
		} else if (action.getKey().equals(Constants.DECRYPT)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(FileTransforming.toFileTransforming(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) {
						return new DecryptorHandler(key);
					}
				};
				try {
					errorFiles = executor.execute(mainApp.getCurrentKey(), outPath.getText());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				AlertWindow.show(Constants.DECRYPT_RUS, errorFiles);
			}
		} else if (action.getKey().equals(Constants.UNSIGN)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			}else {
				SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(FileTransforming.toFileTransforming(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) {
						return new UnsignHandler(key);
					}
				};
				try {
					errorFiles = executor.execute(mainApp.getCurrentKey(), outPath.getText());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				AlertWindow.show(Constants.UNSIGN_RUS, errorFiles);
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
				
				AlertWindow.show(Constants.PACK_RUS, errorFiles);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (action.getKey().equals(Constants.RENAME)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("Ключ не выбран");
			} else {
				SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(FileTransforming.toFileTransforming(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) {
						return new HashHandler(key);
					}
				};
				try {
					errorFiles = executor.execute(mainApp.getCurrentKey(), outPath.getText());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				AlertWindow.show(Constants.DECRYPT_RUS, errorFiles);
			}

		} else if (action.getKey().equals(Constants.UNPACK)) {
			ObservableList<File> resultFiles = FXCollections.observableArrayList();
			try {
				ObservableList<File> tmp = copyFiles();
				for (File f : tmp) {
					resultFiles.addAll(FileUtils.getFrom7z(f));
				}
				if (resultFiles.size() > 0) {
					AlertWindow.show(Constants.UNPACK_RUS, errorFiles);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			FileUtils.moveFiles(resultFiles, outPath.getText());
		} else if (action.getKey().equals(Constants.COPY)) {
			FileUtils.copyFiles(files.getItems(), outPath.getText());
			AlertWindow.show(Constants.COPY_RUS, errorFiles);
		} else if ("CHECK".equals(action.getKey())) {
			mainApp.showChooseSchemaDialog(files.getItems());
			ObservableList<Pair<File, String>> schemaPairs = mainApp.getSchemaPairs();
			//StringBuilder errors = new StringBuilder();
			List<String> testList = new ArrayList<>();
			for (Pair<File, String> f : schemaPairs) {
				List<Exception> tmpList = XMLValidator.validate(f.getKey(), new File(f.getValue()));
				if (tmpList.size() > 0) {
						//errors.append("В файле " + f.getKey().getName());
					for (Exception ex : tmpList) {
						//errors .append(" " + ((SAXException) ex).getMessage() + System.lineSeparator());
						testList.add("Файл - "+ f.getKey().getName()+" " + ((SAXException) ex).getMessage());
					}
					//exceptions.put(f.getKey(), errors.toString());
				}
			}
			AlertWindow.show(Constants.COPY_RUS, errorFiles);


		}
	}

	private void showAlert(String text) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setContentText(text);
		alert.setTitle("Ошибка");
		alert.show();
	}
}
