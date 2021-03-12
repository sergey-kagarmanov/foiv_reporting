package application.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import application.MainApp;
import application.models.Key;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class KeyViewController {

	private Stage dialogStage;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	@FXML
	TextField nameField;

	@FXML
	TextField dataField;

	@FXML
	TableView<Key> keyTable;

	@FXML
	TableColumn<Key, String> nameColumn;

	@FXML
	TableColumn<Key, String> dataColumn;

	@FXML
	Button editButton;

	private Key key;

	public void initialize() {
		nameColumn.setCellValueFactory((cellData) -> cellData.getValue().getNameProperty());
		dataColumn.setCellValueFactory((cellData) -> cellData.getValue().getDataProperty());

		keyTable.setRowFactory(new Callback<TableView<Key>, TableRow<Key>>() {

			@Override
			public TableRow<Key> call(TableView<Key> param) {
				final TableRow<Key> row = new TableRow<>();
				final ContextMenu contextMenu = new ContextMenu();

				final MenuItem removeMenuItem = new MenuItem("Удалить");
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						keyTable.getItems().remove(row.getItem());
						MainApp.getDb().delete(row.getItem());
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

		keyTable.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldSelection, newSelection) -> {
					if (newSelection != null) {
						nameField.setText(newSelection.getName());
						dataField.setText(newSelection.getData());
						key = newSelection;
						editButton.setDisable(false);
					} else {
						editButton.setDisable(true);
					}
				});

	}
	
	public void setData(){
		keyTable.setItems(MainApp.getDb().getKeys());
	}
	
	@FXML
	public void addKey(){
		List<String> errors = new ArrayList<String>();
		if ("".equals(nameField.getText())){
			errors.add("Не заполнено поле имя");
		}
		
		if ("".equals(dataField.getText())){
			errors.add("Не заполнено поле путь");
		}
		
		if (errors.size() == 0){
			MainApp.getDb().addKey(nameField.getText(), dataField.getText());
			setData();
		}else{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка");
			String text = "";
			for(String error : errors){
				text += "\r\n"+error;
			}
			alert.setContentText(text);
		}
	}
	
	@FXML
	public void editKey(){
		List<String> errors = new ArrayList<String>();
		if ("".equals(nameField.getText())){
			errors.add("Не заполнено поле имя");
		}
		
		if ("".equals(dataField.getText())){
			errors.add("Не заполнено поле путь");
		}
		
		if (errors.size() == 0){
			key.setName(nameField.getText());
			key.setData(dataField.getText());
			MainApp.getDb().editKey(key);
			setData();
		}else{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка");
			String text = "";
			for(String error : errors){
				text += "\r\n"+error;
			}
			alert.setContentText(text);
		}
	}
	
	@FXML
	public void openPathDialog() {
		FileChooser chooser = new FileChooser();
		File file = chooser.showOpenDialog(null);

		if (file != null) {
			dataField.setText(file.getAbsolutePath());
		}

	}

}
