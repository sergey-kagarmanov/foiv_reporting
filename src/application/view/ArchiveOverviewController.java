package application.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import application.MainApp;
import application.models.AttributeDescr;
import application.models.Report;
import application.models.WorkingFile;
import application.utils.Constants;
import application.utils.DateUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ArchiveOverviewController {

	@FXML
	DatePicker startDate;
	@FXML
	DatePicker endDate;

	@FXML
	ComboBox<Report> reportChooser;

	@FXML
	Label answerFile;
	@FXML
	Label answerCode;
	@FXML
	Label comment;
	@FXML
	Label dateTimeAnswer;
	@FXML
	Label dateTimeFile;

	private LocalDate startTime;
	private LocalDate endTime;

	@FXML
	TreeTableView<WorkingFile> fileView;
	@FXML
	TreeTableColumn<WorkingFile, String> fileNameColumn;
	@FXML
	TreeTableColumn<WorkingFile, LocalDateTime> fileDateColumn;
	@FXML
	TreeTableColumn<WorkingFile, String> fileDirectionColumn;
	@FXML
	TreeTableColumn<WorkingFile, String> fileStatusColumn;
	@FXML
	TreeTableColumn<WorkingFile, String> fileReportColumn;

	private List<WorkingFile> fileFiles;

	public ArchiveOverviewController() {
	}

	@FXML
	private void initialize() {

		reportChooser.setCellFactory(param -> {
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

		fileNameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<WorkingFile, String>("name"));

		fileDateColumn.setCellValueFactory(param -> new SimpleObjectProperty<LocalDateTime>(param.getValue().getValue().getDatetime()));

		fileDateColumn.setCellFactory(param -> {
			TreeTableCell<WorkingFile, LocalDateTime> cell = new TreeTableCell<WorkingFile, LocalDateTime>() {
				@Override
				protected void updateItem(LocalDateTime item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
					} else {
						this.setText(DateUtils.tableFormatter.format(item));
					}
				}
			};
			return cell;

		});

		fileReportColumn.setCellValueFactory(new TreeItemPropertyValueFactory<WorkingFile, String>("report"));

		fileDirectionColumn.setCellValueFactory(new TreeItemPropertyValueFactory<WorkingFile, String>("direction") {
			@Override
			public ObservableValue<String> call(CellDataFeatures<WorkingFile, String> param) {
				return new SimpleStringProperty(param.getValue().getValue().getDirection() ? Constants.IN : Constants.OUT);
			}
		});
		fileStatusColumn.setCellValueFactory(new TreeItemPropertyValueFactory<WorkingFile, String>("linkedFile") {

			@Override
			public ObservableValue<String> call(CellDataFeatures<WorkingFile, String> param) {
				String result = "";
				WorkingFile wFile = param.getValue().getValue();

				if (wFile.getChilds() == null) {
					// Not transport file
					if (wFile.getType() != null && wFile.getType().getTicket()) {
						// Is ticket file
						if (wFile.getAttributes() != null && wFile.getAttributes().size() > 0) {
							if (Constants.isPositive(wFile.getAttributes().get(AttributeDescr.CODE).getValue())) {
								result += Constants.POSITIVE;
							} else {
								result += Constants.NEGATIVE;
							}

							result += Constants.ANSWER_ON;

							if (wFile.getLinked() != null) {
								result += wFile.getLinked().getOriginalName();
							} else {
								result = Constants.UNKNOWN;
							}
						} else {
							result = Constants.UNKNOWN;
						}
					} else {
						// Common file
						if (wFile.getAnswer() != null) {
							if (Constants.isPositive(wFile.getAnswer().getAttributes().get(AttributeDescr.CODE).getValue())) {
								result += Constants.POSITIVE;
							} else {
								result += Constants.NEGATIVE;
							}

							result += wFile.getAnswer().getOriginalName();
						} else {
							result += Constants.NO_ANSWER;
						}
					}
				} else {
					// Transport File
					if (wFile.getAnswer() != null) {
						if (Constants.isPositive(wFile.getAnswer().getAttributes().get(AttributeDescr.CODE).getValue())) {
							result += Constants.POSITIVE;
						} else {
							result += Constants.NEGATIVE;
						}

						result += wFile.getAnswer().getOriginalName();
					} else {
						result += Constants.NO_ANSWER;
					}
				}

				if (wFile.getLinked() == null) {
					if (wFile.getDirection() == Constants.OUTPUT) {
						result += Constants.NO_ANSWER;
					} else if (wFile.getChilds() == null) {

					}
				}
				return new SimpleStringProperty(result);
			}
		});

		fileView.setRowFactory(param -> {
			TreeTableRow<WorkingFile> row = new TreeTableRow<WorkingFile>() {

				@Override
				protected void updateItem(WorkingFile item, boolean empty) {
					super.updateItem(item, empty);
					this.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

					if (item != null) {
						if (item.getChilds() == null) {
							// Not transport file
							if (item.getType() != null && item.getType().getTicket()) {
								// Is ticket file
								if (item.getAttributes() != null && item.getAttributes().size() > 0) {
									if (Constants.isPositive(item.getAttributes().get(AttributeDescr.CODE).getValue())) {
										this.setBackground(new Background(new BackgroundFill(Color.GREEN, null, getInsets())));
									} else {
										this.setBackground(new Background(new BackgroundFill(Color.RED, null, getInsets())));
									}
								} else {
									this.setBackground(new Background(new BackgroundFill(Color.PURPLE, null, getInsets())));
								}
							} else {
								// Common file
								if (item.getAnswer() != null) {
									if (Constants.isPositive(item.getAnswer().getAttributes().get(AttributeDescr.CODE).getValue())) {
										this.setBackground(new Background(new BackgroundFill(Color.GREEN, null, getInsets())));
									} else {
										this.setBackground(new Background(new BackgroundFill(Color.RED, null, getInsets())));
									}
								} else {
									this.setBackground(new Background(new BackgroundFill(Color.PURPLE, null, getInsets())));
								}
							}
						} else {
							// Transport File
							if (item.getAnswer() != null) {
								if (Constants.isPositive(item.getAnswer().getAttributes().get(AttributeDescr.CODE).getValue())) {
									this.setBackground(new Background(new BackgroundFill(Color.GREEN, null, getInsets())));
								} else {
									this.setBackground(new Background(new BackgroundFill(Color.RED, null, getInsets())));
								}
							} else {
								this.setBackground(new Background(new BackgroundFill(Color.PURPLE, null, getInsets())));
							}
						}
					}
				}
			};
			return row;
		});
	}

	private Stage dialogStage;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setReports(List reports) {
		reportChooser.getItems().clear();
		reportChooser.getItems().addAll(reports);
		reportChooser.getItems().add(null);

		// I don't understand why we added an empty report to the end of the
		// list
		// This is for all report filter and it should be null than new Report()
		// reportChooser.getItems().add(new Report());
	}

	@FXML
	public void handleUpdate() {
		List<WorkingFile> files = MainApp.getDb().getTransportFilesPerPeriod(startTime, endTime, reportChooser.getValue(), null);
		files.addAll(MainApp.getDb().getTicketFilesPerPeriod(startTime, endTime, reportChooser.getValue(), null));
		setData(files);
		fillData();
	}

	public void fillData() {
		TreeItem<WorkingFile> root = new TreeItem<>(new WorkingFile());
		root.setExpanded(true);
		fileView.setShowRoot(false);
		for (WorkingFile file : fileFiles) {
			root.getChildren().add(createItem(file, 0));
		}
		fileView.setRoot(root);
	}

	private TreeItem<WorkingFile> createItem(WorkingFile entity, Integer padding) {
		TreeItem<WorkingFile> item = null;
		if (entity != null && !entity.getDirection()) {
			Node outIcon = new ImageView(new Image(getClass().getResourceAsStream("out.png")));
			item = new TreeItem<WorkingFile>(entity, outIcon);
		} else {
			Node inIcon = new ImageView(new Image(getClass().getResourceAsStream("in.png")));
			item = new TreeItem<WorkingFile>(entity, inIcon);
		} // item.
		if (entity.getChilds() != null && entity.getChilds().size() > 0) {
			for (WorkingFile cEntity : entity.getChilds()) {
				item.getChildren().add(createItem(cEntity, padding + 5));
			}
		}

		if (entity != null && entity.getLinked() != null) {
			item.getChildren().add(createItem(entity.getLinked(), padding + 5));
		}
		return item;
	}

	public void setData(List<WorkingFile> fileFiles) {
		this.fileFiles = fileFiles;
		fillData();
	}

	public void setStartDate(LocalDate dateTime) {
		this.startTime = dateTime;
		startDate.setValue(startTime);
	}

	public void setEndTime(LocalDate dateTime) {
		this.endTime = dateTime;
		endDate.setValue(endTime);
	}

	@FXML
	public void handleStartDateSelected() {
		// checks that own date is correct
		if (endDate.getValue() != null)
			if (startDate.getValue().isAfter(endDate.getValue())) {
				startDate.setValue(endDate.getValue());
			}
		startTime = startDate.getValue();
	}

	@FXML
	public void handleEndDateSelected() {
		// checks that own date is correct
		if (startDate.getValue() != null)
			if (endDate.getValue().isBefore(startDate.getValue())) {
				endDate.setValue(startDate.getValue());
			}
		endTime = endDate.getValue();
	}

}
