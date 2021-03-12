package application.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
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
import application.utils.print.Printing;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
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
	private TableColumn<FileEntity, Integer> inArchiveCountColumn;
	@FXML
	private TableColumn<FileEntity, String> outArchiveFileTableColumn;
	@FXML
	private TableColumn<TransportFile, LocalDateTime> outArchiveDateTableColumn;

	@FXML
	private ComboBox<Report> reportChooser;

	private Report report;

	private MainApp mainApp;

	ContextMenu contextMenu;
	
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
									Object obj = currentRow.getItem();
									if (currentRow != null && currentRow.getItem() != null) {
										String path = DateUtils.toPath(
												((FileEntity) currentRow.getItem()).getDatetime());
										if (obj != null) {

										}
										if (new File(
												inPathArch.getText() + "\\" + path + "\\" + item)
														.exists()) {
											currentRow.setStyle("-fx-background-color:white");
											currentRow.setStyle("-fx-color:black");
											currentRow.setStyle(
													"-fx-border-color: transparent #1d1d1d");

										} else {
											currentRow.setStyle("-fx-background-color:red");
										}
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
									if (currentRow != null && currentRow.getItem() != null) {
										String path = DateUtils.toPath(
												((FileEntity) currentRow.getItem()).getDatetime());
										if (new File(
												outPathArch.getText() + "\\" + path + "\\" + item)
														.exists()) {
											currentRow.setStyle("-fx-background-color:transparent");
											currentRow.setStyle("-fx-color:black");
											currentRow.setStyle(
													"-fx-border-color: transparent #1d1d1d");

										} else {
											currentRow.setStyle("-fx-background-color:red");
										}
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
					MainApp.info(reportChooser.getValue() + " report has been choosen");
					report = FileUtils.testReport(reportChooser.getValue());
					calculateData();
				}
			}
		});
		MainApp.info("Load main view EncriptOverviewController");
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		fillData();

	}

	private int timer = 60000;
	
	private final static int TIMER = 5000;

	@FXML
	private void refreshData() {
		calculateData();
	}
	
	private void fillData() {

		reportChooser.setItems(MainApp.getDb().getReports());
		reportChooser.setValue(reportChooser.getItems().get(0));
		outArchiveFileTable
				.setItems(MainApp.getDb().getArchiveFiles(reportChooser.getValue(), false));
		inArchiveFileTable
				.setItems(MainApp.getDb().getArchiveFiles(reportChooser.getValue(), true));

		calculateData();
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.millis(TIMER), ae -> calculateData()));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	private void fillData(String form) {
		Report tmp = null;
		reportChooser.setItems(MainApp.getDb().getReports());
		for (Report current : reportChooser.getItems()) {
			if (current.getName().equals(form)) {
				tmp = current;
			}
		}
		reportChooser.setValue(tmp);
		outArchiveFileTable
				.setItems(MainApp.getDb().getArchiveFiles(reportChooser.getValue(), false));
		inArchiveFileTable
				.setItems(MainApp.getDb().getArchiveFiles(reportChooser.getValue(), true));

		calculateData();
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.millis(TIMER), ae -> calculateData()));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	@FXML
	public void openOutPathDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathOut();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), false, false,
					false);
			report.setPathOut(selectedDirectory.getAbsolutePath());
			outPath.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void openInPathDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String pathOut = report.getPathIn();
		if (pathOut == null || !Files.exists(Paths.get(pathOut))) {
			pathOut = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(pathOut));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), true, false,
					false);
			report.setPathIn(selectedDirectory.getAbsolutePath());
			inPath.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void openInputPathDialogArch() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathIn();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), true, true,
					false);
			report.setPathArchiveIn(selectedDirectory.getAbsolutePath());
			inPathArch.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void openOutputPathDialogArch() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathArchiveOut();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), false, true,
					false);
			report.setPathArchiveOut(selectedDirectory.getAbsolutePath());
			outPathArch.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void calculateData() {
		// path = mainApp.getDb().getPathForReport(reportChooser.getValue());
		/**
		 * save selection
		 */
		FileEntity inFile = inFileTable.getSelectionModel().getSelectedItem();
		FileEntity outFile = outFileTable.getSelectionModel().getSelectedItem();
		TransportFile inArchiveFile = inArchiveFileTable.getSelectionModel().getSelectedItem();
		TransportFile outArchiveFile = outArchiveFileTable.getSelectionModel().getSelectedItem();
		
		report = MainApp.getDb().getReportById(reportChooser.getValue().getId());
		inPath.setText(report.getPathIn());
		outPath.setText(report.getPathOut());
		inPathArch.setText(report.getPathArchiveIn());
		outPathArch.setText(report.getPathArchiveOut());
		inOutputPath.setText(report.getPathOutputIn());
		outOutputPath.setText(report.getPathOutputOut());
		/*int inIndex = inArchiveFileTable.getSelectionModel().getSelectedIndex();
		int outIndex = outArchiveFileTable.getSelectionModel().getSelectedIndex();*/
		outArchiveFileTable.setItems(FXCollections.observableArrayList());
		inArchiveFileTable.setItems(FXCollections.observableArrayList());
		outArchiveFileTable.refresh();
		inArchiveFileTable.refresh();
		outArchiveFileTable
				.setItems(MainApp.getDb().getArchiveFilesPerDay(reportChooser.getValue(), false, LocalDate.now()));
		inArchiveFileTable
				.setItems(MainApp.getDb().getArchiveFilesPerDay(reportChooser.getValue(), true, LocalDate.now()));
		/*outArchiveFileTable.getSelectionModel().select(outIndex);
		inArchiveFileTable.getSelectionModel().select(inIndex);*/
		try {
			if (report.getPathOut() != null) {
				outFileList.setItems(FileUtils.getDirContentByMask(report.getPathOut(),
						reportChooser.getValue().getPatternOut()));
				outFileCount
						.setText(
								FileUtils
										.getDirContentByMask(report.getPathOut(),
												reportChooser.getValue().getPatternOut())
										.size() + "");
			}
			ObservableList<FileType> list = FXCollections
					.observableArrayList(reportChooser.getValue().getTransportInPattern());
			if (report.getTickets() != null && report.getTickets().size() > 0) {
				list.addAll(report.getTickets());
			}
			if (report.getPathIn() != null) {
				inFileList.setItems(FileUtils.getDirContentByMask(report.getPathIn(), list));

				inFileCount.setText(
						FileUtils.getDirContentByMask(report.getPathIn(), list).size() + "");
			}
			/*if (!"0".equals(inFileCount.getText()) || !"0".equals(outFileCount.getText()))
				// MainApp.info("Data for "+reportChooser.getValue().getName()+"
				// was loaded. Count in files is "+inFileCount.getText()+".
				// Count out files is "+ outFileCount.getText());
				if (timer > 600000) {// if error appears timer to check will be
									// 50000
									// or greater
					timer = timer / 10;
				}*/
			outFileTable.getSelectionModel().select(outFile);
			inFileTable.getSelectionModel().select(inFile);
			outArchiveFileTable.getSelectionModel().select(outArchiveFile);
			inArchiveFileTable.getSelectionModel().select(inArchiveFile);
		} catch (ReportError e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка выполнения");
			alert.setContentText(
					"При загрузке данных возникла ошибка проверьте настройки фильтрации");
			alert.show();
			timer = timer * 10;
		}finally {
			System.gc();
		}
	}

	public void fillFilesFromArchive() {
		if (outArchiveFileTable.getSelectionModel().getSelectedItem() != null)
			outFileTable.setItems(FXCollections.observableArrayList(outArchiveFileTable
					.getSelectionModel().getSelectedItem().getListFiles().values()));
		if (inArchiveFileTable.getSelectionModel().getSelectedItem() != null) {
			inFileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			inFileTable.setItems(FXCollections.observableArrayList(inArchiveFileTable
					.getSelectionModel().getSelectedItem().getListFiles().values()));
		}
	}

	@FXML
	public void recieveFiles() {
		ProcessExecutor executor = new ProcessExecutor(inFileList.getItems(),
				reportChooser.getValue(), inPath.getText(), inOutputPath.getText(),
				inPathArch.getText(), true);
		try {
			executor.start();

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Информация");
			alert.setHeaderText("Операция выполнена");
			alert.setContentText("Транспортный файл обработан!");
			alert.showAndWait();
		} catch (Exception e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
			Alert msg = new Alert(AlertType.ERROR);
			msg.setContentText(e.getMessage());
			msg.setTitle("Ошибка выполнения");
			msg.setHeaderText("Выполнение прервано");
			msg.show();
		}
		fillData(reportChooser.getSelectionModel().selectedItemProperty().getValue().getName());
	}

	@FXML
	public void sendFiles() {
		ProcessExecutor executor = new ProcessExecutor(outFileList.getItems(),
				reportChooser.getValue(), outPath.getText(),
				outOutputPath.getText(), outPathArch.getText(), false);
		try {
			executor.start();

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Информация");
			alert.setHeaderText("Операция выполнена");
			alert.setContentText("Транспортный файл сформирован!");
			alert.showAndWait();
		} catch (ReportError e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
			Alert msg = new Alert(AlertType.ERROR);
			msg.setContentText(e.getMessage());
			msg.setTitle("Ошибка выполнения");
			msg.setHeaderText("Выполнение прервано");
			msg.show();
		}
		executor = null;
		System.gc();
		fillData(reportChooser.getSelectionModel().selectedItemProperty().getValue().getName());
	}

	@FXML
	public void openOutputPathOutDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathOutputOut();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), false, false,
					true);
			report.setPathOutputOut(selectedDirectory.getAbsolutePath());
			outOutputPath.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void openOutputPathInDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = report.getPathOutputIn();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(report, selectedDirectory.getAbsolutePath(), true, false,
					true);
			report.setPathOutputIn(selectedDirectory.getAbsolutePath());
			inOutputPath.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void openArchive() {
		mainApp.showArchive();
	}

	@FXML
	public void contextMenu(ContextMenuEvent contextMenuEvent) {
		contextMenu = new ContextMenu();
		MenuItem printItem = new MenuItem("Печатать выбранные");
		printItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				printSelected();
			}
		});

		contextMenu.getItems().add(printItem);
		contextMenu.setX(10.0);
		contextMenu.setY(10.0);
		contextMenu.show((Node) contextMenuEvent.getSource(), contextMenuEvent.getScreenX(),
				contextMenuEvent.getScreenY());
	}
	
	@FXML
	public void onMousePressed(MouseEvent mouseEvent) {
		if (contextMenu!=null) {
			contextMenu.hide();
		}
	}
	
	public void printSelected() {
		ObservableList<FileEntity> items = inFileTable.getSelectionModel().getSelectedItems();
		for (FileEntity file : items) {
			String file_name = file.getReport().getPathArchiveIn() + "\\"
					+ DateUtils.toPath(file.getDatetime()) + "\\" + file.getName();
			try {
				byte[] file_content = Files.readAllBytes(Paths.get(file_name));
				Printing.printFormattedXML(file.getName(), file_content);
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
}
