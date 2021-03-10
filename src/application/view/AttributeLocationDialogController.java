package application.view;

import application.MainApp;
import application.models.AttributeDescr;
import application.models.FileAttribute;
import application.models.FileType;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AttributeLocationDialogController {

	private Stage dialogStage;

	private MainApp mainApp;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	TableView<AttributeDescr> attributeTable;

	@FXML
	TableColumn<AttributeDescr, FileAttribute> attrColumn;

	@FXML
	TableColumn<AttributeDescr, Boolean> inNameColumn;

	@FXML
	TableColumn<AttributeDescr, String> locationColumn;

	@FXML
	TableColumn<AttributeDescr, String> etcColumn;

	@FXML
	TableColumn<AttributeDescr, FileType> fileColumn;

	@FXML
	ComboBox<FileType> fileTypeCombo;

	private ObservableList<AttributeDescr> attributes;
	private FileType type;

	public void initialize() {

		fileTypeCombo.setCellFactory(new Callback<ListView<FileType>, ListCell<FileType>>() {
			@Override
			public ListCell<FileType> call(ListView<FileType> param) {
				final ListCell<FileType> cell = new ListCell<FileType>() {
					{
						super.setPrefWidth(100);
					}

					@Override
					public void updateItem(FileType item, boolean empty) {
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

		attrColumn.setCellValueFactory((cellData) -> cellData.getValue().getAttrProperty());
		inNameColumn.setCellValueFactory((cellData) -> cellData.getValue().getInNameProperty());
		locationColumn.setCellValueFactory((cellData) -> cellData.getValue().getLocationProperty());
		etcColumn.setCellValueFactory((cellData) -> cellData.getValue().getEtcProperty());
		fileColumn.setCellValueFactory((cellData) -> cellData.getValue().getFileProperty());

		attributeTable
				.setRowFactory(new Callback<TableView<AttributeDescr>, TableRow<AttributeDescr>>() {

					@Override
					public TableRow<AttributeDescr> call(TableView<AttributeDescr> param) {
						final ContextMenu contextMnu = new ContextMenu();

						final TableRow<AttributeDescr> row = new TableRow<AttributeDescr>() {
							@Override
							protected void updateItem(AttributeDescr item, boolean empty) {
								super.updateItem(item, empty);
								if (item != null)
									if (empty) {
										setContextMenu(null);
									} else {
										setContextMenu(contextMnu);
									}
							}
						};
						final MenuItem removeMenuItem = new MenuItem("Удалить");
						removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								AttributeDescr obj = row.getItem();
								mainApp.getDb().remove(obj);
								handleSelectType();
							}
						});
						contextMnu.getItems().add(removeMenuItem);
						final MenuItem editItem = new MenuItem("Изменить");
						editItem.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								AttributeDescr obj = row.getItem();
								editItem(obj);
							}
						});
						contextMnu.getItems().add(editItem);
						row.setOnMouseClicked((event) -> {
							if (event.getClickCount() == 2) {
								editItem();
							}
						});
						return row;
					}
				});
	}

	@Deprecated
	public void setData() {
		attributes = mainApp.getDb().getAttributesDescriptions();
		attributeTable.setItems(attributes);
	}

	public void setData(ObservableList<AttributeDescr> attributes) {
		attributeTable.setItems(attributes);
	}

	public void setFileType(FileType fType) {
		this.type = fType;
	}

	public void updateData() {
		if (type != null)
			attributes = mainApp.getDb().getAttributesDescriptions(type);
		else
			attributes = mainApp.getDb().getAttributesDescriptions();
	}

	@FXML
	private void editItem() {
		AttributeDescr attr = attributeTable.getSelectionModel().getSelectedItem();
		if (attr != null) {
			mainApp.showAttributeSettingsEditDialog(attr, fileTypeCombo.getSelectionModel().getSelectedItem());
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка");
			alert.setContentText("Не выбрана строчка для редактирования");
			alert.showAndWait();
		}
		handleSelectType();
	}

	private void editItem(AttributeDescr attr) {
		mainApp.showAttributeSettingsEditDialog(attr, fileTypeCombo.selectionModelProperty().getValue().getSelectedItem());
		handleSelectType();
	}

	@FXML
	private void insertItem() {
		mainApp.showAttributeSettingsEditDialog(new AttributeDescr(), fileTypeCombo.selectionModelProperty().getValue().getSelectedItem());
		handleSelectType();
	}

	@FXML
	private void remove() {
		AttributeDescr attr = attributeTable.getSelectionModel().getSelectedItem();
		if (attr != null) {
			mainApp.getDb().remove(attr);
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка");
			alert.setContentText("Не выбрана строчка для удаления");
			alert.showAndWait();
		}
		handleSelectType();

	}

	public void fillCombo(ObservableList<FileType> types, FileType type) {
		fileTypeCombo.setItems(types);
		fileTypeCombo.getSelectionModel().select(type);
		handleSelectType();
	}

	public void setSelectedType(FileType type) {
		fileTypeCombo.getSelectionModel().select(type);
		handleSelectType();
	}
	
	@FXML
	public void handleSelectType() {
		attributes = mainApp.getDb().getAttributesDescriptions(fileTypeCombo.selectionModelProperty().getValue().getSelectedItem());
		attributeTable.setItems(attributes);
		attributeTable.refresh();
	}
}
