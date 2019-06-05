package application.view;

import java.util.Collections;

import application.MainApp;
import application.models.Chain;
import application.models.FileAttribute;
import application.models.FileType;
import application.models.ProcessStep;
import application.models.Report;
import application.utils.Constants;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ActionViewController {

	private Stage dialogStage;

	private MainApp mainApp;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	TreeTableView chainsTable;

	@FXML
	TreeTableColumn nameColumn;

	@FXML
	TreeTableColumn reportColumn;

	@FXML
	TreeTableColumn actionTypeColumn;

	@FXML
	TreeTableColumn keyColumn;

	@FXML
	TreeTableColumn dataColumn;

	@FXML
	TreeTableColumn directionColumn;

	@FXML
	ComboBox<Report> reportChooser;

	@FXML
	ComboBox<String> directionChooser;

	private ObservableList<Chain> chains;

	public void initialize() {
		nameColumn.setCellValueFactory(new Callback() {

			@Override
			public Object call(Object obj) {
				final Object dataObj = ((TreeTableColumn.CellDataFeatures) obj).getValue()
						.getValue();
				if (dataObj instanceof Chain) {
					return ((Chain) dataObj).getNameProperty();
				} else {
					return ((ProcessStep) dataObj).getPositionProperty();
				}
			}
		});
		reportColumn.setCellValueFactory(new Callback() {

			@Override
			public Object call(Object obj) {
				final Object dataObj = ((TreeTableColumn.CellDataFeatures) obj).getValue()
						.getValue();
				if (dataObj instanceof Chain) {
					return ((Chain) dataObj).getReport().getNameProperty();
				} else {
					return new ReadOnlyStringWrapper("");
				}
			}
		});

		actionTypeColumn.setCellValueFactory((obj) -> {
			final Object dataObj = ((TreeTableColumn.CellDataFeatures) obj).getValue().getValue();
			if (dataObj instanceof ProcessStep) {
				return new ReadOnlyStringWrapper(((ProcessStep) dataObj).getAction().getName());
			} else {
				return new ReadOnlyStringWrapper("");
			}
		});

		keyColumn.setCellValueFactory((obj) -> {
			final Object dataObj = ((TreeTableColumn.CellDataFeatures) obj).getValue().getValue();
			if (dataObj instanceof ProcessStep) {
				return ((ProcessStep) dataObj).getKey().getNameProperty();
			} else {
				return new ReadOnlyStringWrapper("");
			}
		});

		dataColumn.setCellValueFactory((obj) -> {
			final Object dataObj = ((TreeTableColumn.CellDataFeatures) obj).getValue().getValue();
			if (dataObj instanceof ProcessStep) {
				return ((ProcessStep) dataObj).getDataProperty();
			} else {
				return new ReadOnlyStringWrapper("");
			}
		});

		directionColumn.setCellValueFactory((obj) -> {
			final Object dataObj = ((TreeTableColumn.CellDataFeatures) obj).getValue().getValue();
			if (dataObj instanceof Chain) {
				return new ReadOnlyStringWrapper(
						Constants.direct(((Chain) dataObj).getDirection()));
			} else {
				return new ReadOnlyStringWrapper("");
			}
		});

		chainsTable.setRowFactory(new Callback() {

			@Override
			public TreeTableRow call(Object param) {
				final ContextMenu contextMnu = new ContextMenu();

				final TreeTableRow row = new TreeTableRow() {
					@Override
					protected void updateItem(Object item, boolean empty) {
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
						Object obj = row.getItem();
						if (obj instanceof Chain) {
							mainApp.getDb().remove((Chain) obj);
						} else {
							mainApp.getDb().remove((ProcessStep) obj);
						}
						setData();
					}
				});
				contextMnu.getItems().add(removeMenuItem);
				final MenuItem editItem = new MenuItem("Изменить");
				editItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						Object obj = row.getItem();
						if (obj instanceof ProcessStep)
							editItem((ProcessStep) obj);
						else
							editChain((Chain) obj);
					}
				});
				contextMnu.getItems().add(editItem);
				row.setOnMouseClicked((event) -> {
					if (event.getClickCount() == 2) {
						handleEdit();
					}
				});
				// Set context menu on row, but use a binding to make it
				// only show for non-empty rows:
				/*
				 * row.contextMenuProperty().bind(Bindings.when(row.
				 * emptyProperty()) .then((ContextMenu)
				 * null).otherwise(contextMnu));
				 */
				return row;
			}
		});

	}

	@FXML
	public void handleEdit() {
		Object obj = chainsTable.getSelectionModel().getSelectedItem();
		if (obj instanceof TreeItem) {
			Object tmpObj = ((TreeItem) obj).getValue();
			if (tmpObj instanceof ProcessStep)
				editItem((ProcessStep) tmpObj);
			else
				editChain((Chain) tmpObj);
		}
	}

	@FXML
	public void handleAdd() {
		if (chainsTable.getSelectionModel().getSelectedItem() != null) {
			Object obj = chainsTable.getSelectionModel().getSelectedItem();
			Chain selectedChain = null;
			if (obj instanceof TreeItem) {
				TreeItem tmpObject = (TreeItem) obj;
				if (tmpObject.getValue() instanceof Chain) {
					selectedChain = (Chain) tmpObject.getValue();
				} else {
					selectedChain = mainApp.getDb()
							.getChainByItemId(((ProcessStep) tmpObject.getValue()).getId());
				}
			}
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setHeaderText("Подтверждение");
			alert.setTitle("Подтверждение");
			alert.setContentText("Добавить действие в " + selectedChain.getName() + "?");
			if (alert.showAndWait().get() == ButtonType.OK) {
				addProcessStep(selectedChain);
			} else {
				Alert a2 = new Alert(AlertType.CONFIRMATION);
				a2.setHeaderText("Подтверждение");
				a2.setTitle("Подтверждение");
				a2.setContentText("Добавить новую последовательность?");
				if (a2.showAndWait().get() == ButtonType.OK) {
					addChain();
				}
			}
		} else {
			addChain();
		}
		setData();
	}

	private void addProcessStep(Chain chain) {
		mainApp.showProcessStepDialog(null, chain);
	}

	private void addChain() {
		mainApp.showChainDialog(null);
	}

	private void editItem(ProcessStep item) {
		mainApp.showProcessStepDialog(item, mainApp.getDb().getChainByItemId(item.getId()));
	}

	private void editChain(Chain chain) {
		mainApp.showChainDialog(chain);
	}

	@FXML
	public void handleReport() {
		chains = mainApp.getDb().getChains(reportChooser.getValue(), directionChooser.getValue());
		createTree();
	}

	public void setData() {
		fillCombo();
		this.chains = mainApp.getDb().getChains(reportChooser.getValue(), directionChooser.getValue());
		createTree();
	}

	private void fillCombo() {
		reportChooser.setItems(mainApp.getDb().getReports());
		reportChooser.getItems().add(new Report() {
			{
				setName(Constants.ALL);
			}
		});
		reportChooser.getSelectionModel().selectLast();
		directionChooser.setItems(
				FXCollections.observableArrayList(Constants.IN, Constants.OUT, Constants.ALL));
		directionChooser.getSelectionModel().select(Constants.ALL);
	}

	private void createTree() {
		final TreeItem dummyRoot = new TreeItem();
		//chainsTable = new TreeTableView();
		chainsTable.setRoot(dummyRoot);
		chainsTable.setShowRoot(false);
		chains.forEach((chain) -> {
			TreeItem item = new TreeItem(chain);
			dummyRoot.getChildren().add(item);
			ObservableList<ProcessStep> list = chain.getSteps();
			/*if (list != null) {
				Collections.reverse(list);
			}*/
			list.forEach((step) -> {
				item.getChildren().add(new TreeItem(step));
			});
		});
	}

	@FXML
	public void handleDelete(){
		Object obj = chainsTable.getSelectionModel().getSelectedItem();
		if (obj!=null){
			Object tmp = (TreeItem)obj;
			if (tmp instanceof Chain){
				mainApp.getDb().remove((Chain) tmp);
			}else{
				mainApp.getDb().remove((ProcessStep)tmp);
			}
		}else{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Ошибка");
			alert.setHeaderText("Ошибка");
			alert.setContentText("Ни одна строка не выбрана");
			alert.showAndWait();
		}
	}

}
