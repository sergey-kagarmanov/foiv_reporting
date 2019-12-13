package application.view;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import application.MainApp;
import application.models.FileType;
import application.models.Report;
import application.utils.Constants;
import application.utils.DateUtils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ArchiveNameDialogController {
	private Stage dialogStage;

	private MainApp mainApp;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	private String archiveName;
	private ObservableList<File> files;

	@FXML
	TextField reportField;

	@FXML
	TextField archiveField;

	@FXML
	private void initialize() {
	}

	public void setFiles(ObservableList<File> files) {
		this.files = files;
		Report currentReport = null;
		boolean breakFlag = false;
		List<Report> reports = mainApp.getDb().getReports();
		for(Report report : reports) {
			ObservableList<FileType> fTypes = mainApp.getDb().getFileTypeAsList(report.getId(), Constants.OUT_DB, 0);
			for(FileType fType : fTypes) {
				Pattern p = Pattern.compile(fType.getMask());
				for(File file : files) {
					if (p.matcher(file.getName()).matches()) {
						if (currentReport == null) {
							currentReport = report;
						}else if (report.getId()!=currentReport.getId()) {
							breakFlag = true;
						}
					}
					if (breakFlag) {
						break;
					}
				}
				if (breakFlag) {
					break;
				}
			}
		}
		
		if(breakFlag) {
			reportField.setText("Файлы соответствуют нескольким отчетам");
			String tmp = currentReport.getTransportOutPattern().getMask();
			tmp = tmp.replaceAll("%date", DateUtils.formatReport(LocalDate.now()));
			tmp = tmp.replaceAll("%dd",
					LocalDate.now().format(DateTimeFormatter.ofPattern("dd")));
			tmp = tmp.replaceAll("%MM",
					LocalDate.now().format(DateTimeFormatter.ofPattern("MM")));
			tmp = tmp.replaceAll("%yy",
					LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));

			tmp = tmp.replaceAll("%n","1").replaceAll("%", "0");

			archiveField.setText(tmp);
		}else if (currentReport!=null) {
			reportField.setText(currentReport.getName());
			String tmp = currentReport.getTransportOutPattern().getMask();
			tmp = tmp.replaceAll("%date", DateUtils.formatReport(LocalDate.now()));
			tmp = tmp.replaceAll("%dd",
					LocalDate.now().format(DateTimeFormatter.ofPattern("dd")));
			tmp = tmp.replaceAll("%MM",
					LocalDate.now().format(DateTimeFormatter.ofPattern("MM")));
			tmp = tmp.replaceAll("%yy",
					LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));

			tmp = tmp.replaceAll("%n","1").replaceAll("%", "0");

			archiveField.setText(tmp);
		}
	}

	@FXML
	public void saveName() {
		mainApp.setCurrentArchiveName(archiveField.getText());
		dialogStage.close();
	}
}
