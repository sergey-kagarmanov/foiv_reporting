package application.view;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import application.MainApp;
import application.errors.ReportError;
import application.models.FileType;
import application.models.Report;
import application.models.WorkingFile;
import application.utils.AlertWindow;
import application.utils.Constants;
import application.utils.DataLoader;
import application.utils.DateUtils;
import application.utils.FileUtils;
import application.utils.ProcessExecutor;
import application.utils.print.Printing;
import javafx.application.Platform;
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
	private TableView<WorkingFile> inFileTable;
	@FXML
	private TableView<WorkingFile> outFileTable;

	@FXML
	private TableView<WorkingFile> inArchiveFileTable;
	@FXML
	private TableView<WorkingFile> outArchiveFileTable;

	@FXML
	private TableColumn<WorkingFile, String> inFileTableColumn;
	@FXML
	private TableColumn<WorkingFile, LocalDateTime> inDateTableColumn;

	@FXML
	private TableColumn<WorkingFile, String> outFileTableColumn;
	@FXML
	private TableColumn<WorkingFile, LocalDateTime> outDateTableColumn;

	@FXML
	private TableColumn<WorkingFile, String> inArchiveFileTableColumn;
	@FXML
	private TableColumn<WorkingFile, LocalDateTime> inArchiveDateTableColumn;
	@FXML
	private TableColumn<WorkingFile, Integer> inArchiveCountColumn;
	@FXML
	private TableColumn<WorkingFile, String> outArchiveFileTableColumn;
	@FXML
	private TableColumn<WorkingFile, LocalDateTime> outArchiveDateTableColumn;

	@FXML
	private ComboBox<Report> reportChooser;

	private MainApp mainApp;

	ContextMenu contextMenu;

	public EncryptOverviewController() {

	}

	@FXML
	private void initialize() {
		reportChooser.setCellFactory(c -> {
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
		});

		outArchiveDateTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getDatetimeProperty());
		outArchiveDateTableColumn.setCellFactory(c -> new TableCell<WorkingFile, LocalDateTime>() {

			@Override
			protected void updateItem(LocalDateTime date, boolean empty) {
				super.updateItem(date, empty);
				if (empty) {
					setText(null);
				} else {
					setText(DateUtils.formatGUI(date));
				}
			}

		});

		outArchiveFileTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getOriginalNameProprety());
		outFileTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getOriginalNameProprety());
		outDateTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getDatetimeProperty());
		outDateTableColumn.setCellFactory(c -> new TableCell<WorkingFile, LocalDateTime>() {
			@Override
			protected void updateItem(LocalDateTime date, boolean empty) {
				super.updateItem(date, empty);
				if (empty) {
					setText(null);
				} else {
					setText(DateUtils.formatGUI(date));
				}
			}

		});

		inArchiveDateTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getDatetimeProperty());
		inArchiveDateTableColumn.setCellFactory(c -> new TableCell<WorkingFile, LocalDateTime>() {
			@Override
			protected void updateItem(LocalDateTime date, boolean empty) {
				super.updateItem(date, empty);
				if (empty) {
					setText(null);
				} else {
					setText(DateUtils.formatGUI(date));
				}
			}

		});

		inArchiveFileTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getOriginalNameProprety());
		inFileTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getOriginalNameProprety());
		inDateTableColumn.setCellValueFactory((cellData) -> cellData.getValue().getDatetimeProperty());
		inDateTableColumn.setCellFactory(c -> new TableCell<WorkingFile, LocalDateTime>() {
			@Override
			protected void updateItem(LocalDateTime date, boolean empty) {
				super.updateItem(date, empty);
				if (empty) {
					setText(null);
				} else {
					setText(DateUtils.formatGUI(date));
				}
			}

		});

		inArchiveFileTableColumn.setCellFactory(c -> {
			TableCell cell = new TableCell<WorkingFile, String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty ? "" : item);
					setGraphic(null);

					TableRow currentRow = getTableRow();
					if (!isEmpty()) {
						Object obj = currentRow.getItem();
						if (currentRow != null && currentRow.getItem() != null) {
							String path = DateUtils.toPath(((WorkingFile) currentRow.getItem()).getDatetime());
							if (obj != null) {

							}
							if (new File(inPathArch.getText() + "\\" + path + "\\" + item).exists()) {
								currentRow.setStyle("-fx-background-color:white");
								currentRow.setStyle("-fx-color:black");
								currentRow.setStyle("-fx-border-color: transparent #1d1d1d");

							} else {
								currentRow.setStyle("-fx-background-color:red");
							}
						}
					}
				}
			};
			return cell;
		});

		outArchiveFileTableColumn.setCellFactory(c -> {
			TableCell cell = new TableCell<WorkingFile, String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty ? "" : item);
					setGraphic(null);
					TableRow currentRow = getTableRow();
					if (!isEmpty()) {
						if (currentRow != null && currentRow.getItem() != null) {
							String path = DateUtils.toPath(((WorkingFile) currentRow.getItem()).getDatetime());
							if (new File(outPathArch.getText() + "\\" + path + "\\" + item).exists()) {
								currentRow.setStyle("-fx-background-color:transparent");
								currentRow.setStyle("-fx-color:black");
								currentRow.setStyle("-fx-border-color: transparent #1d1d1d");

							} else {
								currentRow.setStyle("-fx-background-color:red");
							}
						}
					}
				}
			};
			return cell;
		});

		outArchiveFileTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> fillFilesFromArchive());

		inArchiveFileTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> fillFilesFromArchive());

		/*
		 * reportChooser.valueProperty().addListener((observable, oldValue,
		 * newValue) -> { if (reportChooser.getValue() != null) {
		 * MainApp.info(reportChooser.getValue() + " report has been choosen");
		 * calculateData(); } });
		 */
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	private final static int TIMER = 10;

	@FXML
	private void refreshData() {
		calculateData();
	}

	public void fillData() {

		reportChooser.setItems(MainApp.getDb().getReports());
		reportChooser.setValue(reportChooser.getItems().get(0));
		calculateData();
	}

	public void checkAndUpdateData() {
		try {
			if (reportChooser.getValue().getPathOut() != null) {
				ObservableList<String> outList = FileUtils.getDirContentByMask(reportChooser.getValue().getPathOut(),
						reportChooser.getValue().getPatternOut());
				if (outList != null && outFileList.getItems() != null) {
					if (!(outList.containsAll(outFileList.getItems()) && outFileList.getItems().containsAll(outList))) {
						Platform.runLater(() -> {
							outFileList.setItems(outList);
							outFileCount.setText(outList.size() + "");
						});
					}
				}
			}

			ObservableList<FileType> list = FXCollections.observableArrayList(reportChooser.getValue().getTransportInPattern());
			if (reportChooser.getValue().getTickets() != null && reportChooser.getValue().getTickets().size() > 0) {
				list.addAll(reportChooser.getValue().getTickets());
			}
			if (reportChooser.getValue().getPathIn() != null) {
				ObservableList<String> inList = FileUtils.getDirContentByMask(reportChooser.getValue().getPathIn(), list);
				if (inList != null && inFileList.getItems() != null) {
					if (!(inList.containsAll(inFileList.getItems()) && inFileList.getItems().containsAll(inList))) {
						Platform.runLater(() -> {
							inFileList.setItems(inList);
							inFileCount.setText(inList.size() + "");
						});
					}
				}
			}
		} catch (ReportError e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка выполнения");
			alert.setContentText("При загрузке данных возникла ошибка проверьте настройки фильтрации");
			alert.show();
		}

		ObservableList<WorkingFile> inFiles = MainApp.getDb().getArchiveFilesLite(reportChooser.getValue(), Constants.INPUT_INT);
		ObservableList<WorkingFile> ouFiles = MainApp.getDb().getArchiveFilesLite(reportChooser.getValue(), Constants.OUTPUT_INT);
		if (!inArchiveFileTable.getItems().containsAll(inFiles) && inFiles.containsAll(inArchiveFileTable.getItems())) {
			inArchiveFileTable.setItems(inFiles);
		}
		if (!outArchiveFileTable.getItems().containsAll(ouFiles) && ouFiles.containsAll(outArchiveFileTable.getItems())) {
			outArchiveFileTable.setItems(ouFiles);
		}

	}

	@FXML
	public void openOutPathDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = reportChooser.getValue().getPathOut();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(reportChooser.getValue(), selectedDirectory.getAbsolutePath(), false, false, false);
			reportChooser.getValue().setPathOut(selectedDirectory.getAbsolutePath());
			outPath.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void openInPathDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String pathOut = reportChooser.getValue().getPathIn();
		if (pathOut == null || !Files.exists(Paths.get(pathOut))) {
			pathOut = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(pathOut));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(reportChooser.getValue(), selectedDirectory.getAbsolutePath(), true, false, false);
			reportChooser.getValue().setPathIn(selectedDirectory.getAbsolutePath());
			inPath.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void openInputPathDialogArch() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = reportChooser.getValue().getPathIn();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(reportChooser.getValue(), selectedDirectory.getAbsolutePath(), true, true, false);
			reportChooser.getValue().setPathArchiveIn(selectedDirectory.getAbsolutePath());
			inPathArch.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void openOutputPathDialogArch() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = reportChooser.getValue().getPathArchiveOut();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(reportChooser.getValue(), selectedDirectory.getAbsolutePath(), false, true, false);
			reportChooser.getValue().setPathArchiveOut(selectedDirectory.getAbsolutePath());
			outPathArch.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void calculateData() {
		inPath.setText(reportChooser.getValue().getPathIn());
		outPath.setText(reportChooser.getValue().getPathOut());
		inPathArch.setText(reportChooser.getValue().getPathArchiveIn());
		outPathArch.setText(reportChooser.getValue().getPathArchiveOut());
		inOutputPath.setText(reportChooser.getValue().getPathOutputIn());
		outOutputPath.setText(reportChooser.getValue().getPathOutputOut());

		outArchiveFileTable.setItems(MainApp.getDb().getArchiveFilesLite(reportChooser.getValue(), Constants.OUTPUT_INT));
		inArchiveFileTable.setItems(MainApp.getDb().getArchiveFilesLite(reportChooser.getValue(), Constants.INPUT_INT));
		outArchiveFileTable.refresh();
		inArchiveFileTable.refresh();
		inFileList.refresh();
		outFileList.refresh();
	}

	public void fillFilesFromArchive() {
		if (outArchiveFileTable.getSelectionModel().getSelectedItem() != null) {
			outFileTable.setItems(DataLoader.loadChildsLite(outArchiveFileTable.getSelectionModel().getSelectedItem()));
		}
		if (inArchiveFileTable.getSelectionModel().getSelectedItem() != null) {
			inFileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			inFileTable.setItems(DataLoader.loadChildsLite(inArchiveFileTable.getSelectionModel().getSelectedItem()));
		}
	}

	@FXML
	public void recieveFiles() {
		ProcessExecutor executor = new ProcessExecutor(inFileList.getItems(), reportChooser.getValue(), inPath.getText(), inOutputPath.getText(),
				inPathArch.getText(), true);
		try {
			ObservableList<ReportError> errors = executor.startStream();

			if (errors==null || errors.size() == 0) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Информация");
				alert.setHeaderText("Операция выполнена");
				alert.setContentText("Транспортный файл обработан!");
				alert.showAndWait();
			} else {
				AlertWindow.show(errors, mainApp.logger);
			}
		} catch (ReportError e) {
			/*
			 * MainApp.error(e.getLocalizedMessage()); e.printStackTrace();
			 * Alert msg = new Alert(AlertType.ERROR);
			 * msg.setContentText(e.getMessage());
			 * msg.setTitle("Ошибка выполнения");
			 * msg.setHeaderText("Выполнение прервано"); msg.show();
			 */
			AlertWindow.show(e, MainApp.logger);
		}
		refreshData();
	}

	@FXML
	public void sendFiles() {
		ProcessExecutor executor = new ProcessExecutor(outFileList.getItems(), reportChooser.getValue(), outPath.getText(), outOutputPath.getText(),
				outPathArch.getText(), false);
		try {
			ObservableList<ReportError> errors = executor.startStream();

			if (errors==null || errors.size() == 0) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Информация");
				alert.setHeaderText("Операция выполнена");
				alert.setContentText("Транспортный файл обработан!");
				alert.showAndWait();
			} else {
				AlertWindow.show(errors, MainApp.logger);
			}
		} catch (ReportError e) {
			/*
			 * MainApp.error(e.getLocalizedMessage()); e.printStackTrace();
			 * Alert msg = new Alert(AlertType.ERROR);
			 * msg.setContentText(e.getMessage());
			 * msg.setTitle("Ошибка выполнения");
			 * msg.setHeaderText("Выполнение прервано"); msg.show();
			 */
			AlertWindow.show(e, MainApp.logger);
		}
		executor = null;
		System.gc();
		refreshData();
	}

	@FXML
	public void openOutputPathOutDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = reportChooser.getValue().getPathOutputOut();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(reportChooser.getValue(), selectedDirectory.getAbsolutePath(), false, false, true);
			reportChooser.getValue().setPathOutputOut(selectedDirectory.getAbsolutePath());
			outOutputPath.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML
	public void openOutputPathInDialog() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		String path = reportChooser.getValue().getPathOutputIn();
		if (path == null || !Files.exists(Paths.get(path))) {
			path = "c:\\";
		}
		directoryChooser.setInitialDirectory(new File(path));
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory != null) {
			MainApp.getDb().savePath(reportChooser.getValue(), selectedDirectory.getAbsolutePath(), true, false, true);
			reportChooser.getValue().setPathOutputIn(selectedDirectory.getAbsolutePath());
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
		contextMenu.show((Node) contextMenuEvent.getSource(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
	}

	@FXML
	public void onMousePressed(MouseEvent mouseEvent) {
		if (contextMenu != null) {
			contextMenu.hide();
		}
	}

	public void printSelected() {
		ObservableList<WorkingFile> items = inFileTable.getSelectionModel().getSelectedItems();
		for (WorkingFile file : items) {
			try {
				file.readData(inPathArch.getText() + "\\" + DateUtils.toPath(file.getDatetime()));
				Printing.printFormattedXML(file);
			} catch (ReportError | UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
	}
}
