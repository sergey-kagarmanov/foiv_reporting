package application.view;

import java.io.File;
import java.time.LocalDateTime;

import application.MainApp;
import application.errors.ReportError;
import application.models.FileEntity;
import application.models.FileType;
import application.models.Report;
import application.models.TransportFile;
import application.utils.DateUtils;
import application.utils.FileUtils;
import application.utils.ProcessExecutor;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import javafx.util.Duration;

public class EncryptOverviewController {

	@FXML
	private TextField inPath;
	@FXML
	private TextField outPath;

	@FXML
	private TextField inPathArch;
	@FXML
	private TextField outPathArch;

	@FXML
	private TextField inOutputPath;
	@FXML
	private TextField outOutputPath;

	@FXML
	private Label inFileCount;
	@FXML
	private Label outFileCount;

	@FXML
	private ListView<String> inFileList;
	@FXML
	private ListView<String> outFileList;

	@FXML
	private TableView<FileEntity> inFileTable;
	@FXML
	private TableView<FileEntity> outFileTable;

	@FXML
	private TableView<TransportFile> inArchiveFileTable;
	@FXML
	private TableView<TransportFile> outArchiveFileTable;

	@FXML
	private TableColumn<FileEntity, String> inFileTableColumn;
	@FXML
	private TableColumn<FileEntity, LocalDateTime> inDateTableColumn;
	@FXML
	private TableColumn<FileEntity, String> outFileTableColumn;
	@FXML
	private TableColumn<FileEntity, LocalDateTime> outDateTableColumn;

	@FXML
	private TableColumn<FileEntity, String> inArchiveFileTableColumn;
	@FXML
	private TableColumn<TransportFile, LocalDateTime> inArchiveDateTableColumn;
	@FXML
	private TableColumn<FileEntity, String> outArchiveFileTableColumn;
	@FXML
	private TableColumn<TransportFile, LocalDateTime> outArchiveDateTableColumn;

	@FXML
	private ComboBox<Report> reportChooser;

	private Report report;

	private MainApp mainApp;

	public EncryptOverviewController() {

	}

	@FXML
	private void initialize() {
		reportChooser.setCellFactory(new Callback<ListView<Report>, ListCell<Report>>() {
			@Override
			public ListCell<Report> call(ListView<Report> param) {
				final ListCell<Report> cell = new ListCell<Report>() {
					{
						super.setPrefWidth(100);
					}

					@Override
					public void updateItem(Report item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							setText(item.getName());
						} else {
							setText(null);
						}
					}
				};
				return cell;
			}
		});

		outArchiveDateTableColumn
				.setCellValueFactory((cellData) -> cellData.getValue().getDatetimeProperty());
		outArchiveDateTableColumn.setCellFactory(
				new Callback<TableColumn<TransportFile, LocalDateTime>, TableCell<TransportFile, LocalDateTime>>() {

					@Override
					public TableCell<TransportFile, LocalDateTime> call(
							TableColumn<TransportFile, LocalDateTime> param) {
						return new TableCell<TransportFile, LocalDateTime>() {
							@Override
							protected void updateItem(LocalDateTime date, boolean empty) {
								super.updateItem(date, empty);
								if (empty) {
									setText(null);
								} else {
									setText(DateUtils.formatGUI(date));
								}
							}

						};
					}
				});

		outArchiveFileTableColumn
				.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());
		outFileTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());
		outDateTableColumn
				.setCellValueFactory((cellData) -> cellData.getValue().getDatetimeProperty());
		outDateTableColumn.setCellFactory(
				new Callback<TableColumn<FileEntity, LocalDateTime>, TableCell<FileEntity, LocalDateTime>>() {

					@Override
					public TableCell<FileEntity, LocalDateTime> call(
							TableColumn<FileEntity, LocalDateTime> param) {
						return new TableCell<FileEntity, LocalDateTime>() {
							@Override
							protected void updateItem(LocalDateTime date, boolean empty) {
								super.updateItem(date, empty);
								if (empty) {
									setText(null);
								} else {
									setText(DateUtils.formatGUI(date));
								}
							}

						};
					}
				});

		inArchiveDateTableColumn
				.setCellValueFactory((cellData) -> cellData.getValue().getDatetimeProperty());
		inArchiveDateTableColumn.setCellFactory(
				new Callback<TableColumn<TransportFile, LocalDateTime>, TableCell<TransportFile, LocalDateTime>>() {

					@Override
					public TableCell<TransportFile, LocalDateTime> call(
							TableColumn<TransportFile, LocalDateTime> param) {
						return new TableCell<TransportFile, LocalDateTime>() {
							@Override
							protected void updateItem(LocalDateTime date, boolean empty) {
								super.updateItem(date, empty);
								if (empty) {
									setText(null);
								} else {
									setText(DateUtils.formatGUI(date));
								}
							}

						};
					}
				});

		inArchiveFileTableColumn
				.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());
		inFileTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());
		inDateTableColumn
				.setCellValueFactory((cellData) -> cellData.getValue().getDatetimeProperty());
		inDateTableColumn.setCellFactory(
				new Callback<TableColumn<FileEntity, LocalDateTime>, TableCell<FileEntity, LocalDateTime>>() {

					@Override
					public TableCell<FileEntity, LocalDateTime> call(
							TableColumn<FileEntity, LocalDateTime> param) {
						return new TableCell<FileEntity, LocalDateTime>() {
							@Override
							protected void updateItem(LocalDateTime date, boolean empty) {
								super.updateItem(date, empty);
								if (empty) {
									setText(null);
								} else {
									setText(DateUtils.formatGUI(date));
								}
							}

						};
					}
				});

		inArchiveFileTableColumn.setCellFactory(
				new Callback<TableColumn<FileEntity, String>, TableCell<FileEntity, String>>() {
					public TableCell<FileEntity, String> call(
							javafx.scene.control.TableColumn<FileEntity, String> param) {
						TableCell cell = new TableCell<FileEntity, String>() {
							@Override
							public void updateItem(String item, boolean empty) {
								super.updateItem(item, empty);
								setText(empty ? "" : item);
								setGraphic(null);

								TableRow currentRow = getTableRow();
								if (!isEmpty()) {

									if (new File(inPathArch.getText() + "/" + item).exists()) {
										currentRow.setStyle("-fx-background-color:white");
										currentRow.setStyle("-fx-color:black");
										currentRow
												.setStyle("-fx-border-color: transparent #1d1d1d");

									} else {
										currentRow.setStyle("-fx-background-color:red");
									}
								}
							}
						};
						return cell;
					};
				});

		outArchiveFileTableColumn.setCellFactory(
				new Callback<TableColumn<FileEntity, String>, TableCell<FileEntity, String>>() {
					public TableCell<FileEntity, String> call(
							javafx.scene.control.TableColumn<FileEntity, String> param) {
						TableCell cell = new TableCell<FileEntity, String>() {
							@Override
							public void updateItem(String item, boolean empty) {
								super.updateItem(item, empty);
								setText(empty ? "" : item);
								setGraphic(null);
								TableRow currentRow = getTableRow();
								if (!isEmpty()) {

									if (new File(outPathArch.getText() + "\\" + item).exists()) {
										currentRow.setStyle("-fx-background-color:transparent");
										currentRow.setStyle("-fx-color:black");
										currentRow
												.setStyle("-fx-border-color: transparent #1d1d1d");

									} else {
										currentRow.setStyle("-fx-background-color:red");
									}
								}
							}
						};
						return cell;
					};
				});

		outArchiveFileTable.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> fillFilesFromArchive());

		inArchiveFileTable.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> fillFilesFromArchive());

		reportChooser.valueProperty().addListener(new ChangeListener<Report>() {
			@Override
			public void changed(ObservableValue<? extends Report> observable, Report oldValue,
					Report newValue) {
				if (reportChooser.getValue() != null) {
					report = FileUtils.testReport(reportChooser.getValue());
					calculateData();
				}
			}
		});
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		fillData();

	}

	private int timer = 5000;

	private void fillData() {

		reportChooser.setItems(mainApp.getDb().getReports());
		reportChooser.setValue(reportChooser.getItems().get(0));
		outArchiveFileTable
				.setItems(mainApp.getDb().getArchiveFiles(reportChooser.getValue(), false));
		inArchiveFileTable
				.setItems(mainApp.getDb().getArchiveFiles(reportChooser.getValue(), true));

		calculateData();
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.millis(timer), ae -> calculateData()));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	@FXML
	public void openOutPathDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathOut();
		if (path == null) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			mainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), false, false,
					false);
			report.setPathOut(selectedDirectory.getAbsolutePath());
			outPath.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void openInPathDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String pathOut = report.getPathIn();
		if (pathOut == null) {
			pathOut = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(pathOut));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			mainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), true, false,
					false);
			report.setPathIn(selectedDirectory.getAbsolutePath());
			inPath.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void openInputPathDialogArch() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathIn();
		if (path == null) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			mainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), true, true,
					false);
			report.setPathArchiveIn(selectedDirectory.getAbsolutePath());
			inPathArch.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void openOutputPathDialogArch() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathArchiveOut();
		if (path == null) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			mainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), false, true,
					false);
			report.setPathArchiveOut(selectedDirectory.getAbsolutePath());
			outPathArch.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void calculateData() {
		// path = mainApp.getDb().getPathForReport(reportChooser.getValue());
		report = mainApp.getDb().getReportById(reportChooser.getValue().getId());
		inPath.setText(report.getPathIn());
		outPath.setText(report.getPathOut());
		inPathArch.setText(report.getPathArchiveIn());
		outPathArch.setText(report.getPathArchiveOut());
		inOutputPath.setText(report.getPathOutputIn());
		outOutputPath.setText(report.getPathOutputOut());
		outArchiveFileTable
				.setItems(mainApp.getDb().getArchiveFiles(reportChooser.getValue(), false));
		inArchiveFileTable
				.setItems(mainApp.getDb().getArchiveFiles(reportChooser.getValue(), true));
		try {
			outFileList.setItems(FileUtils.getDirContentByMask(report.getPathOut(),
					reportChooser.getValue().getPatternOut()));
			ObservableList<FileType> list = FXCollections
					.observableArrayList(reportChooser.getValue().getTransportInPattern());
			if (report.getTickets() != null && report.getTickets().size() > 0) {
				list.addAll(report.getTickets());
			}
			inFileList.setItems(FileUtils.getDirContentByMask(report.getPathIn(), list));

			inFileCount
					.setText(FileUtils.getDirContentByMask(report.getPathIn(), list).size() + "");
			outFileCount.setText(FileUtils.getDirContentByMask(report.getPathOut(),
					reportChooser.getValue().getPatternOut()).size() + "");
			if (timer > 5000) {// if error appears timer to check will be 50000
								// or greater
				timer = timer / 10;
			}
		} catch (ReportError e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка выполнения");
			alert.setContentText(
					"При загрузке данных возникла ошибка проверьте настройки фильтрации");
			alert.show();
			timer = timer * 10;
		}
	}

	public void fillFilesFromArchive() {
		if (outArchiveFileTable.getSelectionModel().getSelectedItem() != null)
			outFileTable.setItems(FXCollections.observableArrayList(outArchiveFileTable
					.getSelectionModel().getSelectedItem().getListFiles().values()));
		if (inArchiveFileTable.getSelectionModel().getSelectedItem() != null)
			inFileTable.setItems(FXCollections.observableArrayList(inArchiveFileTable
					.getSelectionModel().getSelectedItem().getListFiles().values()));
	}

	@FXML
	public void recieveFiles() {
		ProcessExecutor executor = new ProcessExecutor(inFileList.getItems(),
				reportChooser.getValue(), mainApp.getDb(), inPath.getText(), inOutputPath.getText(),
				inPathArch.getText(), true);
		try {
			executor.start();

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Информация");
			alert.setHeaderText("Операция выполнена");
			alert.setContentText("Транспортный файл обработан!");
			alert.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}
		fillData();
	}

	@FXML
	public void sendFiles() {
		ProcessExecutor executor = new ProcessExecutor(outFileList.getItems(),
				reportChooser.getValue(), mainApp.getDb(), outPath.getText(),
				outOutputPath.getText(), outPathArch.getText(), false);
		try {
			executor.start();

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Информация");
			alert.setHeaderText("Операция выполнена");
			alert.setContentText("Транспортный файл сформирован!");
			alert.showAndWait();
		} catch (ReportError e) {
			e.printStackTrace();
		}
		executor = null;
		System.gc();
		fillData();
	}

	@FXML
	public void openOutputPathOutDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathOutputOut();
		if (path == null) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			mainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), false, false,
					true);
			report.setPathOutputOut(selectedDirectory.getAbsolutePath());
			outOutputPath.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void openOutputPathInDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathOutputIn();
		if (path == null) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			mainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), true, false,
					true);
			report.setPathOutputIn(selectedDirectory.getAbsolutePath());
			inOutputPath.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void openArchive() {
		mainApp.showArchive();
	}
}
