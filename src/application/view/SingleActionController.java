package application.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import application.MainApp;
import application.errors.ReportError;
import application.models.ErrorFile;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.AlertWindow;
import application.utils.Constants;
import application.utils.DateUtils;
import application.utils.FileUtils;
import application.utils.skzi.SignaturaHandler;
import application.utils.skzi.SignaturaTheadingExecutor;
import application.utils.skzi.signatura5.HashHandler;
import application.utils.skzi.signatura6.DecryptorHandler;
import application.utils.skzi.signatura6.EncryptorHandler;
import application.utils.skzi.signatura6.LocalSignatura;
import application.utils.skzi.signatura6.SignHandler;
import application.utils.skzi.signatura6.UnsignHandler;
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
	private Logger logger = Logger.getLogger(SingleActionController.class.getName());

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
		actions.add(new Pair<String, String>(Constants.ENCRYPT, "??????????????????????") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.SIGN, "??????????????????") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.DECRYPT, "????????????????????????") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.UNSIGN, "?????????????????? ?? ?????????? ??????????????") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.PACK, "????????????????????????????") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.RENAME, "??????????????????????????") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.UNPACK, "??????????????????????") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>(Constants.COPY, "??????????????????????") {
			@Override
			public String toString() {
				return getValue();
			}
		});
		actions.add(new Pair<String, String>("CHECK", "?????????????????? ???? ??????????") {
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
		actionChooser.setValue(new Pair<String, String>(Constants.ENCRYPT, "??????????????????????"));
	}

	@FXML
	public void chooseFiles() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("???????????????? ?????????? ?????? ??????????????????");
		String pathOut = "c:\\";
		fileChooser.setInitialDirectory(new File(pathOut));
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
		files.setItems(FXCollections.observableList(selectedFiles));
	}

	@FXML
	public void chooseOutput() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String pathOut = files.getItems().size() > 0 ? files.getItems().get(0).getParent() : "c:\\";
		directoryChooser.setInitialDirectory(new File(pathOut));
		File selectedDirectory = directoryChooser.showDialog(null);
		outPath.setText(selectedDirectory.getAbsolutePath());
	}

	private ObservableList<File> copyFiles() {
		File archive = new File(
				"arch\\" + actionChooser.getSelectionModel().getSelectedItem().getKey() + "\\" + DateUtils.toFolderString(LocalDate.now()));
		archive.mkdirs();
		FileUtils.copyFiles(files.getItems(), archive.getAbsolutePath());
		FileUtils.clearTmp();
		return FileUtils.copyFilesReturn(files.getItems(), FileUtils.tmpDir);
	}

	@FXML
	public void startAction() {
		Pair<String, String> action = actionChooser.getSelectionModel().getSelectedItem();
		ObservableList<ErrorFile> errorFiles = null;
		ObservableList<WorkingFile> wFiles = null;
		if (action.getKey().equals(Constants.ENCRYPT)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("???????? ???? ????????????");
			} else {

				// LocalSignatura.initSignatura(mainApp.getCurrentKey().getData());

				try (SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(WorkingFile.toWorking(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) throws ReportError {
						return new EncryptorHandler(key);
					}

				}) {
					wFiles = executor.execute(mainApp.getCurrentKey());
					wFiles.forEach(file -> {
						FileUtils.saveWorkingFile(file, outPath.getText());
						logger.info("File encrypted "+file.getName());
					});
					AlertWindow.show(Constants.ENCRYPT_RUS, errorFiles);
				} catch (ReportError e) {
					AlertWindow.show(e, logger);
				} catch (Exception e1) {
					AlertWindow.show(new ReportError(ReportError.RUNTIME_ERROR, e1.getLocalizedMessage(), null, e1), logger);
				}
			}
		} else if (action.getKey().equals(Constants.SIGN)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("???????? ???? ????????????");
			} else {

				try (SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(WorkingFile.toWorking(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) throws ReportError {
						return new SignHandler(key);
					}
				}) {

					wFiles = executor.execute(mainApp.getCurrentKey());
					wFiles.forEach(file -> {
						FileUtils.saveWorkingFile(file, outPath.getText());
						logger.info("File signed "+file.getName());
					});
					AlertWindow.show(Constants.SIGN_RUS, errorFiles);
				} catch (Exception e) {
					e.printStackTrace();
					AlertWindow.show(new ReportError(ReportError.RUNTIME_ERROR, e.getLocalizedMessage(), null , e), logger);
				}

			}
		} else if (action.getKey().equals(Constants.DECRYPT)) {
			mainApp.showChooseKeyDialog();
			if (mainApp.getCurrentKey() == null) {
				showAlert("???????? ???? ????????????");
			} else {
				try (SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(WorkingFile.toWorking(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) throws ReportError {
						return new DecryptorHandler(key);
					}
				}) {
					wFiles = executor.execute(mainApp.getCurrentKey());
					wFiles.forEach(file -> {
						FileUtils.saveWorkingFile(file, outPath.getText());
						logger.info("File decrypted "+file.getName());
					});
					AlertWindow.show(Constants.DECRYPT_RUS, errorFiles);
				} catch (ReportError e) {
					AlertWindow.show(e, logger);
				} catch (Exception e1) {
					e1.printStackTrace();
					AlertWindow.show(new ReportError(ReportError.RUNTIME_ERROR, e1.getLocalizedMessage(), null, e1), logger);
				}
			}
		} else if (action.getKey().equals(Constants.UNSIGN)) {
			mainApp.showChooseKeyDialog();
			// LocalSignatura.initSignatura(mainApp.getCurrentKey().getData());
			if (mainApp.getCurrentKey() == null) {
				showAlert("???????? ???? ????????????");
			} else {
				try (SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(WorkingFile.toWorking(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) throws ReportError {
						return new UnsignHandler(key);
					}
				}) {
					wFiles = executor.execute(mainApp.getCurrentKey());
					wFiles.forEach(file -> {
						FileUtils.saveWorkingFile(file, outPath.getText());
						logger.info("File unsigned "+file.getName());
					});
					AlertWindow.show(Constants.UNSIGN_RUS, errorFiles);

				} catch (ReportError e) {
					AlertWindow.show(e, logger);
				} catch (Exception e1) {
					e1.printStackTrace();
					AlertWindow.show(new ReportError(ReportError.RUNTIME_ERROR, e1.getLocalizedMessage(), null, e1), logger);
				}
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
				showAlert("???????? ???? ????????????");
			} else {
				SignaturaTheadingExecutor executor = new SignaturaTheadingExecutor(WorkingFile.toWorking(files.getItems())) {

					@Override
					public SignaturaHandler getHandler(Key key) throws ReportError {
						return new HashHandler(key);
					}
				};
				try {
					wFiles = executor.execute(mainApp.getCurrentKey());
					wFiles.forEach(file -> {
						FileUtils.saveWorkingFile(file, outPath.getText());
					});
				} catch (ReportError e) {
					AlertWindow.show(e, logger);
				}
				AlertWindow.show(Constants.DECRYPT_RUS, errorFiles);
				LocalSignatura.uninitilize();
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
		} else if (Constants.CHECK.equals(action.getKey())) {
			mainApp.showChooseSchemaDialog(files.getItems());
			ObservableList<Pair<File, String>> schemaPairs = mainApp.getSchemaPairs();
			// StringBuilder errors = new StringBuilder();
			List<String> testList = new ArrayList<>();
			errorFiles = FXCollections.observableArrayList();
			for (Pair<File, String> f : schemaPairs) {
				List<Exception> tmpList = XMLValidator.validate(f.getKey(), new File(f.getValue()));
				if (tmpList.size() > 0) {
					// errors.append("?? ?????????? " + f.getKey().getName());
					for (Exception ex : tmpList) {
						// errors .append(" " + ((SAXException) ex).getMessage()
						// + System.lineSeparator());
						if (ex instanceof SAXParseException) {
							testList.add("???????? - " + f.getKey().getName() + " " + ((SAXParseException) ex).getMessage());
							errorFiles.add(new ErrorFile(f.getKey().getName() + " ???????????? - " +((SAXParseException) ex).getLineNumber()+ ", ?????????????? - "+((SAXParseException) ex).getColumnNumber(), 0, ((SAXException) ex).getMessage()));
						} else {
							testList.add("???????? - " + f.getKey().getName() + " " + ex.getMessage());
							errorFiles.add(new ErrorFile(f.getKey().getName(), 0, "?????????????????? ???? ?????????????????????????? XML-??????????????"));

						}
					}
					// exceptions.put(f.getKey(), errors.toString());
				}
			}
			AlertWindow.show(Constants.CHECK_RUS, errorFiles);
		}
	}

	private void showAlert(String text) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setContentText(text);
		alert.setTitle("????????????");
		alert.show();
	}
}
