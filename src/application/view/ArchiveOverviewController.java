package application.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import application.MainApp;
import application.errors.ReportError;
import application.models.AttributeDescr;
import application.models.FileAttribute;
import application.models.FileEntity;
import application.models.Report;
import application.models.ReportFile;
import application.models.TransportFile;
import application.utils.Constants;
import application.utils.DateUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

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
	TreeTableView<FileEntity> fileView;
	@FXML
	TreeTableColumn<FileEntity, String> fileNameColumn;
	@FXML
	TreeTableColumn<FileEntity, LocalDateTime> fileDateColumn;
	@FXML
	TreeTableColumn<FileEntity, String> fileDirectionColumn;
	@FXML
	TreeTableColumn<FileEntity, String> fileStatusColumn;
	@FXML
	TreeTableColumn<FileEntity, String> fileReportColumn;

	private MainApp mainApp;

	private Map<String, FileEntity> fileFiles;

	// private final Node inIcon = new ImageView(new
	// Image(getClass().getResourceAsStream("/img/in.png")));
	// private final Node outIcon = new ImageView(new
	// Image(getClass().getResourceAsStream("/img/out.png")));

	public ArchiveOverviewController() {
		// TODO Auto-generated constructor stub
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

		fileNameColumn
				.setCellValueFactory(new TreeItemPropertyValueFactory<FileEntity, String>("name"));
		fileDateColumn.setCellValueFactory(
				new TreeItemPropertyValueFactory<FileEntity, LocalDateTime>("datetime") {
					@Override
					public ObservableValue<LocalDateTime> call(
							CellDataFeatures<FileEntity, LocalDateTime> param) {
						return new SimpleObjectProperty<LocalDateTime>(
								param.getValue().getValue().getDatetime());
						/*
						 * return new SimpleStringProperty(
						 * DateUtils.formatGUI(param.getValue().getValue().
						 * getDatetime()));
						 */
					}
				});
		fileDateColumn.setCellFactory(column -> {
			TreeTableCell<FileEntity, LocalDateTime> cell = new TreeTableCell<FileEntity, LocalDateTime>() {

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

		fileReportColumn.setCellValueFactory(
				new TreeItemPropertyValueFactory<FileEntity, String>("report"));
		fileDirectionColumn.setCellValueFactory(
				new TreeItemPropertyValueFactory<FileEntity, String>("direction") {
					@Override
					public ObservableValue<String> call(
							CellDataFeatures<FileEntity, String> param) {
						return new SimpleStringProperty(
								param.getValue().getValue().getDirection() ? Constants.IN
										: Constants.OUT);
					}
				});
		fileStatusColumn.setCellValueFactory(
				new TreeItemPropertyValueFactory<FileEntity, String>("linkedFile") {
					@Override
					public ObservableValue<String> call(
							CellDataFeatures<FileEntity, String> param) {
						FileEntity fe = param.getValue().getValue().getLinkedFile();
						String tmp = "";
						if (fe == null) {
							if (param.getValue().getValue() instanceof ReportFile) {
								if (!((ReportFile) param.getValue().getValue()).getAttributes()
										.isEmpty()) {
									Map<String, FileAttribute> attr = ((ReportFile) param.getValue()
											.getValue()).getAttributes();
									if ((attr.get(AttributeDescr.PARENT) != null
											&& attr.get(AttributeDescr.PARENT).getValue() != null
											&& !"".equals(
													attr.get(AttributeDescr.PARENT).getValue()))
											|| (attr.get(AttributeDescr.PARENT_ID) != null
													&& attr.get(AttributeDescr.PARENT_ID)
															.getValue() != null
													&& !"".equals(attr.get(AttributeDescr.PARENT_ID)
															.getValue()))) {
										if (attr != null && attr.get(AttributeDescr.CODE) != null) {
											if (Constants.isPositive(
													attr.get(AttributeDescr.CODE).getValue()))
												tmp += Constants.POSITIVE + " ";
											else
												tmp += Constants.NEGATIVE + " ";
											if (attr.get(AttributeDescr.PARENT) != null)
												tmp += Constants.ANSWER_ON + attr
														.get(AttributeDescr.PARENT).getValue();
											else
												tmp += Constants.ANSWER_ON + attr
														.get(AttributeDescr.PARENT_ID).getValue();
										} else {
											tmp = "Ответ на ";
											if (attr.get(AttributeDescr.PARENT) != null
													&& attr.get(AttributeDescr.PARENT)
															.getValue() != null
													&& !"".equals(attr.get(AttributeDescr.PARENT)
															.getValue())) {
												tmp += attr.get(AttributeDescr.PARENT).getValue();
											} else {
												tmp += attr.get(AttributeDescr.PARENT_ID)
														.getValue();
											}
										}
									} else {
										tmp = "Исходный файл не найден";
									}
								} else {
									tmp = "Ответ не получен";
								}
							} else {

								if (param.getValue().getValue() != null) {
									ObservableList<ReportFile> list = mainApp.getDb()
											.getFilesByTransport(param.getValue().getValue());
									int status = 0;

									if (param.getValue().getValue().getDirection()) {
										for (ReportFile file : list) {// This is
																		// only
																		// for
																		// incoming
																		// transport
																		// files,
																		// because
																		// we
																		// don't
																		// do
																		// tickets
																		// for
																		// them,
																		// we
																		// need
																		// check
																		// all
																		// files
																		// in
																		// transport
																		// file,
																		// if
																		// they
																		// have
																		// linked
																		// files
																		// or it
																		// is
																		// ticket
																		// we
																		// mark
																		// transport
																		// as
																		// 'good'(green),
																		// if
																		// not
																		// all
																		// have
																		// answers
																		// or be
																		// tickets,
																		// we
																		// mark
																		// transport
																		// yellow,
																		// if no
																		// one
																		// have
																		// answers
																		// we
																		// mark
																		// transport
																		// pink
											if (file.getLinkedFile() != null
													|| file.getFileType().getTicket()) {
												status++;
											}
										}
										if (status == 0) {
											tmp = "Файлы не обработаны";
										} else if (status == list.size()) {
											tmp = "Все файлы обработаны";
										} else {
											tmp = "Файлы обработаны частично";
										}
									} else {
										tmp = "Файлы не обработаны";
									}
								}

							}
						} else if (fe instanceof ReportFile) {
							ReportFile rf = (ReportFile) fe;
							if (rf.getAttributes() != null) {
								FileAttribute fa = rf.getAttributes().get(AttributeDescr.CODE);
								FileAttribute commentAttr = rf.getAttributes()
										.get(AttributeDescr.COMMENT);
								if (((fa != null) && (Constants.isPositive(fa.getValue())))
										|| ((commentAttr != null) && (Constants.ACCEPT
												.equals(commentAttr.getValue())))) {
									tmp = "Успешный";
									if (rf.getAttributes().get(AttributeDescr.PARENT) != null) {
										tmp += " ответ " + rf.getName();
									} else if (rf.getAttributes()
											.get(AttributeDescr.PARENT_ID) != null) {
										tmp += " ответ " + rf.getName();
									}
								} else if (fa != null
										&& rf.getAttributes().get(AttributeDescr.COMMENT) != null) {
									tmp = rf.getAttributes().get(AttributeDescr.COMMENT).getValue();
								} else {
									// tmp = "Ответ обработан некорректно";
									if (rf.getAttributes().get(AttributeDescr.CODE) != null)
										tmp = mainApp.getDb().getMessageByCode(rf.getAttributes()
												.get(AttributeDescr.CODE).getValue());
									else
										tmp = "Ответ обработан некорректно";
								}

							} else {
								tmp = "Ответ некорректного формата";
							}
						}
						return new SimpleStringProperty(tmp);
					}
				});
		fileView.setRowFactory(new Callback<TreeTableView<FileEntity>, TreeTableRow<FileEntity>>() {
			@Override
			public TreeTableRow<FileEntity> call(TreeTableView<FileEntity> param) {

				TreeTableRow<FileEntity> row = new TreeTableRow<FileEntity>() {
					@Override
					protected void updateItem(FileEntity item, boolean empty) {
						super.updateItem(item, empty);

						this.setBackground(
								new Background(new BackgroundFill(Color.WHITE, null, null)));
						FileEntity fe = item;
						if (fe != null && fe.getLinkedFile() == null) {
							if (fe instanceof ReportFile) {
								ReportFile rf = ((ReportFile) fe);
								if (rf.getAttributes() == null) {
									this.setBackground(new Background(
											new BackgroundFill(Color.PINK, null, null)));
								} else {
									if ((rf.getAttributes().get(AttributeDescr.PARENT) != null
											&& rf.getAttributes().get(AttributeDescr.PARENT)
													.getValue() != null)
											|| (rf.getAttributes()
													.get(AttributeDescr.PARENT_ID) != null
													&& rf.getAttributes()
															.get(AttributeDescr.PARENT_ID)
															.getValue() != null)) {
										if ((rf.getAttributes().get(AttributeDescr.CODE) != null
												&& (Constants.isPositive(rf.getAttributes()
														.get(AttributeDescr.CODE).getValue()))
												|| (rf.getAttributes()
														.get(AttributeDescr.COMMENT) != null
														&& (Constants.ACCEPT
																.equals(rf.getAttributes()
																		.get(AttributeDescr.COMMENT)
																		.getValue()))))) {
											this.setBackground(new Background(new BackgroundFill(
													Color.SPRINGGREEN, null, null)));
										} else {
											this.setBackground(new Background(
													new BackgroundFill(Color.RED, null, null)));

										}
									} else if (rf.getAttributes().get(AttributeDescr.PARENT) != null
											&& rf.getAttributes().get(AttributeDescr.PARENT)
													.getValue() == null) {
										this.setBackground(new Background(
												new BackgroundFill(Color.YELLOW, null, null)));

									} else {
										this.setBackground(new Background(
												new BackgroundFill(Color.PINK, null, null)));
									}
								}
							} else {
								if (fe != null) {
									ObservableList<ReportFile> list = mainApp.getDb()
											.getFilesByTransport(fe);
									int status = 0;

									if (fe.getDirection()) {
										/**
										 * This is only for incoming transport
										 * files, because we don't do tickets
										 * for them, we need check all files in
										 * transport file, if they have linked
										 * files or it is ticket we mark
										 * transport as 'good'(green), if not
										 * all have answers or be tickets, we
										 * mark transport yellow, if no one have
										 * answers we mark transport pink
										 */
										for (ReportFile file : list) {
											if (file.getLinkedFile() != null
													|| file.getFileType().getTicket()) {
												status++;
											}
										}
										if (status == 0) {
											this.setBackground(new Background(
													new BackgroundFill(Color.PINK, null, null)));
										} else if (status == list.size()) {
											this.setBackground(new Background(new BackgroundFill(
													Color.SPRINGGREEN, null, null)));
										} else {
											this.setBackground(new Background(
													new BackgroundFill(Color.YELLOW, null, null)));
										}
									} else {
										this.setBackground(new Background(
												new BackgroundFill(Color.PINK, null, null)));
									}
								}
							}
						} else {
							if (fe != null && fe.getLinkedFile() instanceof ReportFile) {
								ReportFile lrf = (ReportFile) fe.getLinkedFile();
								if (lrf.getAttributes() != null) {
									if (lrf.getAttributes() != null)
										if (lrf.getAttributes().get(AttributeDescr.CODE) != null) {
											if (Constants.isPositive(lrf.getAttributes()
													.get(AttributeDescr.CODE).getValue())) {
												this.setBackground(
														new Background(new BackgroundFill(
																Color.SPRINGGREEN, null, null)));
											}
										} else if (lrf.getAttributes()
												.get(AttributeDescr.COMMENT) != null) {
											if (Constants.ACCEPT.equals(lrf.getAttributes()
													.get(AttributeDescr.COMMENT).getValue())) {
												this.setBackground(
														new Background(new BackgroundFill(
																Color.SPRINGGREEN, null, null)));

											} else {
												this.setBackground(new Background(
														new BackgroundFill(Color.RED, null, null)));

											}
										}
								} else {
									try {
										throw new ReportError("Не обработан файл " + fe.getName());
									} catch (ReportError e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				};
				return row;
			}
		});
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

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
		setData(mainApp.getDb().getArchiveFiles(startTime, endTime, reportChooser.getValue()));
		fillData();
	}

	public void fillData() {
		TreeItem<FileEntity> root = new TreeItem<>(new FileEntity());
		root.setExpanded(true);
		fileView.setShowRoot(false);
		for (FileEntity file : fileFiles.values()) {
			root.getChildren().add(createItem(file, 0));
		}
		fileView.setRoot(root);
	}

	private TreeItem<FileEntity> createItem(FileEntity entity, Integer padding) {
		TreeItem<FileEntity> item = null;
		if (entity != null && !entity.getDirection()) {
			Node outIcon = new ImageView(new Image(getClass().getResourceAsStream("out.png")));
			item = new TreeItem<FileEntity>(entity, outIcon);
		} else {
			Node inIcon = new ImageView(new Image(getClass().getResourceAsStream("in.png")));
			item = new TreeItem<FileEntity>(entity, inIcon);
		} // item.
		if (entity instanceof TransportFile) {
			TransportFile tFile = (TransportFile) entity;
			for (FileEntity cEntity : tFile.getListFiles().values()) {
				item.getChildren().add(createItem(cEntity, padding + 5));
			}
		}

		if (entity != null && entity.getLinkedFile() != null) {
			item.getChildren().add(createItem(entity.getLinkedFile(), padding + 5));
		}
		return item;
	}

	public void setData(Map<String, FileEntity> fileFiles) {
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
