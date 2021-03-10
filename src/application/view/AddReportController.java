package application.view;

import java.io.File;

import application.MainApp;
import application.errors.ReportError;
import application.models.FileType;
import application.models.Report;
import application.utils.Constants;
import application.utils.xml.XMLCreator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AddReportController {

	@FXML
	private TableView<FileType> maskIn;

	@FXML
	private TableColumn<FileType, String> inNameColumn;

	@FXML
	private TableColumn<FileType, String> inMaskColumn;

	@FXML
	private TableColumn<FileType, String> inSchema;

	@FXML
	private TableView<FileType> maskOut;

	@FXML
	private TableColumn<FileType, String> outNameColumn;

	@FXML
	private TableColumn<FileType, String> outMaskColumn;

	@FXML
	private TableColumn<FileType, String> outSchema;

	@FXML
	private TextField maskTransportIn;
	@FXML
	private TextField nameTransportIn;
	@FXML
	private TextField schemaFieldIn;

	@FXML
	private TextField schemaFieldOut;

	@FXML
	private TextField nameField;

	@FXML
	private TextField maskTransportOut;

	@FXML
	private TextField nameTransportOut;

	@FXML
	private TextField nameInChooser;

	@FXML
	private TextField maskInValue;

	@FXML
	private TextField nameOutChooser;

	@FXML
	private TextField maskOutValue;

	@FXML
	private ComboBox<Report> reportChooser;

	@FXML
	private TableView<FileType> ticketTable;

	@FXML
	private TableColumn<FileType, String> ticketNameColumn;

	@FXML
	private TableColumn<FileType, String> ticketMaskColumn;

	@FXML
	private TableColumn<FileType, String> ticketTypeColumn;

	private MainApp mainApp;

	private Report report;

	public AddReportController() {
	}

	/**
	 * List of 2 items: first - decrypt, second - encrypt
	 * 
	 * @param settings
	 */
	public void setData(Report report) {
		this.report = report;
		fillData();
	}

	public void initialize() {
		inNameColumn.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());
		inMaskColumn.setCellValueFactory((cellData) -> cellData.getValue().getMaskProperty());
		inSchema.setCellValueFactory(
				(cellData) -> cellData.getValue().getValidationSchemaProperty());
		outNameColumn.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());
		outMaskColumn.setCellValueFactory((cellData) -> cellData.getValue().getMaskProperty());
		outSchema.setCellValueFactory(
				(cellData) -> cellData.getValue().getValidationSchemaProperty());

		maskIn.setRowFactory(new Callback<TableView<FileType>, TableRow<FileType>>() {

			@Override
			public TableRow<FileType> call(TableView<FileType> param) {
				final TableRow<FileType> row = new TableRow<>();
				final ContextMenu contextMenu = new ContextMenu();
				final MenuItem removeMenuItem = new MenuItem("Удалить");
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						mainApp.getDb().remove(row.getItem());
						report.getPatternIn().remove(row.getItem());
						maskIn.getItems().remove(row.getItem());
					}
				});
				contextMenu.getItems().add(removeMenuItem);
				final MenuItem editItem = new MenuItem("Изменить");
				editItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						FileType obj = row.getItem();
						editTicket(obj);
					}
				});
				contextMenu.getItems().add(editItem);

				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty().bind(Bindings.when(row.emptyProperty())
						.then((ContextMenu) null).otherwise(contextMenu));
				row.setOnMouseClicked((event) -> {
					if (event.getClickCount() == 2) {
						FileType obj = ((TableRow<FileType>) event.getSource()).getItem();
						editTicket(obj);
					}
				});

				return row;
			}
		});
		inNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		inNameColumn
				.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<FileType, String>>() {

					@Override
					public void handle(CellEditEvent<FileType, String> event) {
						event.getTableView().getItems().get(event.getTablePosition().getRow())
								.setName(event.getNewValue());

					}
				});
		inMaskColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		inMaskColumn
				.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<FileType, String>>() {

					@Override
					public void handle(CellEditEvent<FileType, String> event) {
						event.getTableView().getItems().get(event.getTablePosition().getRow())
								.setMask(event.getNewValue());

					}
				});

		maskOut.setRowFactory(new Callback<TableView<FileType>, TableRow<FileType>>() {

			@Override
			public TableRow<FileType> call(TableView<FileType> param) {
				final TableRow<FileType> row = new TableRow<>();
				final ContextMenu contextMenu = new ContextMenu();
				final MenuItem removeMenuItem = new MenuItem("Удалить");
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						maskOut.getItems().remove(row.getItem());
					}
				});
				contextMenu.getItems().add(removeMenuItem);
				final MenuItem editItem = new MenuItem("Изменить");
				editItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						FileType obj = row.getItem();
						editTicket(obj);
					}
				});
				contextMenu.getItems().add(editItem);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty().bind(Bindings.when(row.emptyProperty())
						.then((ContextMenu) null).otherwise(contextMenu));
				row.setOnMouseClicked((event) -> {
					if (event.getClickCount() == 2) {
						FileType obj = ((TableRow<FileType>) event.getSource()).getItem();
						editTicket(obj);
					}
				});

				return row;
			}
		});

		outNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		outNameColumn
				.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<FileType, String>>() {

					@Override
					public void handle(CellEditEvent<FileType, String> event) {
						event.getTableView().getItems().get(event.getTablePosition().getRow())
								.setName(event.getNewValue());

					}
				});

		outMaskColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		outMaskColumn
				.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<FileType, String>>() {

					@Override
					public void handle(CellEditEvent<FileType, String> event) {
						event.getTableView().getItems().get(event.getTablePosition().getRow())
								.setMask(event.getNewValue());

					}
				});

		reportChooser.valueProperty().addListener(new ChangeListener<Report>() {
			@Override
			public void changed(ObservableValue<? extends Report> observable, Report oldValue,
					Report newValue) {
				report = reportChooser.getValue();
				if (report != null && report.getId() == 0) {
					report.setPatternIn(FXCollections.observableArrayList());
					report.setPatternOut(FXCollections.observableArrayList());
					// report.setName("");
				}
				fillData();
			}
		});

		ticketNameColumn.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());
		ticketMaskColumn.setCellValueFactory((cellData) -> cellData.getValue().getMaskProperty());
		ticketTypeColumn.setCellValueFactory((cellData) -> {
			Integer tmp = cellData.getValue().getFileType();
			if (tmp != null && tmp < Constants.FILETYPE.length)
				return new SimpleStringProperty(Constants.FILETYPE[tmp]);
			else
				return new SimpleStringProperty("");
		});

		ticketTable.setRowFactory(new Callback<TableView<FileType>, TableRow<FileType>>() {

			@Override
			public TableRow<FileType> call(TableView<FileType> param) {
				final ContextMenu contextMnu = new ContextMenu();
				TableRow<FileType> row = new TableRow<FileType>() {
					@Override
					protected void updateItem(FileType item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							if (empty) {
								setContextMenu(null);
							} else {
								setContextMenu(contextMnu);
							}

						}
					}
				};

				final MenuItem removeMenuItem = new MenuItem("Удалить");
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						Object obj = row.getItem();
						mainApp.getDb().remove((FileType) obj);
						fillData();
					}
				});
				contextMnu.getItems().add(removeMenuItem);
				final MenuItem editItem = new MenuItem("Изменить");
				editItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						FileType obj = row.getItem();
						editTicket(obj);
					}
				});
				contextMnu.getItems().add(editItem);
				row.setOnMouseClicked((event) -> {
					if (event.getClickCount() == 2) {
						FileType obj = ((TableRow<FileType>) event.getSource()).getItem();
						editTicket(obj);
					}
				});

				return row;
			}
		});
	}

	private void editTicket(FileType type) {
		mainApp.showFileTypeDialog(type, report.getId());
		fillData();
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		fillChoosers();
	}

	@FXML
	public void addTicket() {
		editTicket(new FileType() {
			{
				setTicket(true);
			}
		});
		fillData();
	}

	private void fillChoosers() {
		reportChooser.setItems(mainApp.getDb().getReports());
		reportChooser.getItems().add(new Report() {
			{
				setName("Добавить");
				setId(0);
			}
		});
		if (report == null || report.getId() == 0) {
			reportChooser.getSelectionModel().select(new Report() {
				{
					setId(0);
				}
			});
		} else {
			reportChooser.getSelectionModel().select(report);
		}

	}

	@FXML
	public void saveAction() {
		try {
			String warning = "";
			boolean save = true;
			if ("".equals(maskTransportIn.getText())) {
				warning += " маска входящего транспортного файла,";
			}
			if ("".equals(maskTransportOut.getText())) {
				warning += " маска исходящего транспортного файла,";
			}
			if (!"".equals(warning)) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Предупреждение");
				alert.setHeaderText("Не заполнены поля");
				alert.setContentText(
						"Не заполнены следующие поля:" + warning.substring(0, warning.length() - 1)
								+ ". Если хотите продолжить нажмите \"Ок\"");
				save = ButtonType.OK == alert.showAndWait().get();
			}
			if (save) {// if transport doesn't fill create them with empty data
				if (report.getTransportInPattern() == null) {
					report.setTransportInPattern(new FileType(0, "", "", "", true, true, false, 0, null));
				}

				report.getTransportInPattern().setName(nameTransportIn.getText());
				report.getTransportInPattern().setMask(maskTransportIn.getText());
				if ("".equals(maskTransportIn.getText())) {
					report.setTransportInPattern(null);
				}

				if (report.getTransportOutPattern() == null) {
					report.setTransportOutPattern(
							new FileType(0, "", "", "", false, true, false, 0, null));
				}
				report.getTransportOutPattern().setName(nameTransportOut.getText());
				report.getTransportOutPattern().setMask(maskTransportOut.getText());
				if ("".equals(maskTransportOut.getText())) {
					report.setTransportOutPattern(null);
				}
				if (report.getId() == 0) {
					report.setName(nameField.getText());
				}

				report.setId(mainApp.getDb().saveReport(report));
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Сохранение");
				alert.setContentText("Сохранение прошло успешно!");
				alert.setHeaderText("Успешно!");
				alert.showAndWait();

				fillChoosers();

			}
		} catch (ReportError e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void deleteData() {
		mainApp.getDb().deleteReport(report);
	}
	
	public void fillData() {
		if (report != null) {

			if (report.getId() != 0) {
				report = mainApp.getDb().getReportById(report.getId());
				nameField.setText(report.getName());
			} else
				nameField.setText("");
			if (report.getTransportInPattern() != null) {
				maskTransportIn.setText(report.getTransportInPattern().getMask());
				nameTransportIn.setText(report.getTransportInPattern().getName());
			} else {
				maskTransportIn.setText("");
				nameTransportIn.setText("");
			}

			if (report.getTransportOutPattern() != null) {
				maskTransportOut.setText(report.getTransportOutPattern().getMask());
				nameTransportOut.setText(report.getTransportOutPattern().getName());
			} else {
				maskTransportOut.setText("");
				nameTransportOut.setText("");
			}

			if (report.getPatternIn() != null) {
				maskIn.setItems(report.getPatternIn());
			} else {
				maskIn.setItems(FXCollections.observableArrayList());
			}

			if (report.getPatternOut() != null) {
				maskOut.setItems(report.getPatternOut());
			} else {
				maskOut.setItems(FXCollections.observableArrayList());
			}

			if (report.getTickets() == null) {
				report.setTickets(FXCollections.observableArrayList());
			}
			ticketTable.setItems(report.getTickets());
			ticketTable.refresh();
		}
	}

	@FXML
	public void addInMask() {
		/*
		 * if (maskInValue.getText() != null &&
		 * !"".equals(maskInValue.getText())) { if (report.getPatternIn() !=
		 * null) { report.getPatternIn().add(new FileType(0,
		 * nameInChooser.getText(), maskInValue.getText(),
		 * schemaFieldIn.getText(), true, false, false, 0)); } else {
		 * report.setPatternIn(FXCollections.observableArrayList( new
		 * FileType(0, nameInChooser.getText(), maskInValue.getText(),
		 * schemaFieldIn.getText(), true, false, false, 0))); }
		 * 
		 * maskInValue.setText(""); nameInChooser.setText("");
		 * schemaFieldIn.setText(""); } else { Alert alert = new
		 * Alert(AlertType.ERROR); alert.setTitle("Ошибка");
		 * alert.setContentText("Маска для добавления не введена");
		 * alert.showAndWait(); }
		 */
		editTicket(new FileType() {
			{
				setDirection(true);
				setTransport(false);
				setTicket(false);
			}
		});
		fillData();
		maskIn.refresh();
	}

	@FXML
	public void addOutMask() {
		/*
		 * if (maskOutValue.getText() != null &&
		 * !"".equals(maskOutValue.getText())) { if (report.getPatternOut() !=
		 * null) { report.getPatternOut().add(new FileType(0,
		 * nameOutChooser.getText(), maskOutValue.getText(),
		 * schemaFieldOut.getText(), false, false, false, 0)); } else {
		 * report.setPatternOut(FXCollections.observableArrayList( new
		 * FileType(0, nameOutChooser.getText(), maskOutValue.getText(),
		 * schemaFieldOut.getText(), false, false, false, 0))); }
		 * maskOutValue.setText(""); nameOutChooser.setText("");
		 * schemaFieldOut.setText("");
		 * 
		 * } else { Alert alert = new Alert(AlertType.ERROR);
		 * alert.setTitle("Ошибка");
		 * alert.setContentText("Маска для добавления не введена");
		 * alert.showAndWait(); }
		 */
		editTicket(new FileType() {
			{
				setTicket(false);
				setTransport(false);
				setDirection(false);
			}
		});
		fillData();
		maskOut.refresh();

	}

	private Stage dialogStage;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	@FXML
	public void openDecryptPathInDialog() {
		FileChooser chooser = new FileChooser();
		/*
		 * directoryChooser.setInitialDirectory( new
		 * File(mainApp.getDb().getPathForReport(reportChooser.getValue())[1]));
		 */
		File file = chooser.showOpenDialog(null);

		if (file != null) {
			schemaFieldIn.setText(file.getName());
		}

	}

	@FXML
	public void openDecryptPathOutDialog() {
		FileChooser chooser = new FileChooser();
		/*
		 * directoryChooser.setInitialDirectory( new
		 * File(mainApp.getDb().getPathForReport(reportChooser.getValue())[1]));
		 */
		File file = chooser.showOpenDialog(null);

		if (file != null) {
			schemaFieldOut.setText(file.getName());
		}

	}
	
	@FXML
	public void export(){
		if (report!=null){
			DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = 
                    directoryChooser.showDialog(dialogStage);
             
            if(selectedDirectory == null){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setContentText("Директория не выбрана");
                alert.showAndWait();
            }else{
    			String text = XMLCreator.create(reportChooser.getValue(), mainApp.getDb(), selectedDirectory);
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setContentText("Экспорт выполнен в файл - "+text);
    			alert.showAndWait();
               
            }
		}
	}

	@FXML
	public void importXML(){
		FileChooser fileChooser = new FileChooser();
		File file = fileChooser.showOpenDialog(dialogStage);
		XMLCreator.load(file, mainApp.getDb());
	}
}
