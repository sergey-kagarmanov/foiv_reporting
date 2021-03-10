package application.view;

import application.MainApp;
import application.models.FileAttribute;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AttributeEditController {

	private Stage dialogStage;

	private MainApp mainApp;
	private FileAttribute attribute;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	@FXML
	TextField nameField;

	@FXML
	TableView<FileAttribute> attributeView;

	@FXML
	TableColumn<FileAttribute, String> nameColumn;

	@FXML
	Button button;

	public void initialize() {
		nameColumn.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());

		attributeView
				.setRowFactory(new Callback<TableView<FileAttribute>, TableRow<FileAttribute>>() {

					@Override
					public TableRow<FileAttribute> call(TableView<FileAttribute> param) {
						final TableRow<FileAttribute> row = new TableRow<>();
						final ContextMenu contextMenu = new ContextMenu();

						final MenuItem removeMenuItem = new MenuItem("Удалить");
						removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								attributeView.getItems().remove(row.getItem());
								mainApp.getDb().delete(row.getItem());
							}
						});
						contextMenu.getItems().add(removeMenuItem);
						// Set context menu on row, but use a binding to make it
						// only show for non-empty rows:
						row.contextMenuProperty().bind(Bindings.when(row.emptyProperty())
								.then((ContextMenu) null).otherwise(contextMenu));
						return row;
					}
				});

		attributeView.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldSelection, newSelection) -> {
					if (newSelection != null) {
						nameField.setText(newSelection.getName());
						attribute = newSelection;
						button.setText("Изменить");
					} else {
						button.setText("Добавить");
					}
				});

	}

	public void setData() {
		attributeView.setItems(mainApp.getDb().getAttributes());
	}

	@FXML
	public void addItem() {
		if (nameField.getText() != null && !"".equals(nameField.getText().trim())) {
			if (attributeView.getSelectionModel().getSelectedIndex() != -1) {
				attributeView.getItems().get(attributeView.getSelectionModel().getSelectedIndex())
						.setName(nameField.getText());
				mainApp.getDb().saveAttribute(attributeView.getItems()
						.get(attributeView.getSelectionModel().getSelectedIndex()));
			}else{
				mainApp.getDb().saveAttribute(new FileAttribute(0, nameField.getText(), null));
			}
			setData();
		} else {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Поле ввода не заполнено");
			alert.setHeaderText("Ошибка");
			alert.showAndWait();

		}
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

	}

}
