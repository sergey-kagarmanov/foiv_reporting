package application.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.MainApp;
import application.errors.ReportError;
import application.models.FileType;
import application.models.Report;
import application.models.WorkingFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileChecker {

	public ObservableList<WorkingFile> execute(List<String> filenames, Report report) {
		try {
			ObservableList<WorkingFile> wFiles = FXCollections.observableArrayList();
			ExecutorService service = Executors.newWorkStealingPool();

			List<SingleFileCheck> checks = new ArrayList<>();
			List<FileType> types = MainApp.getDb().getFileTypes(report);
			filenames.forEach(name -> {
				checks.add(new SingleFileCheck(new File(name), null, types));
			});
			List<Future<WorkingFile>> futures = service.invokeAll(checks);
			futures.forEach(future -> {
				try {
					if (future.get() != null)
						wFiles.add(future.get());
				} catch (InterruptedException | ExecutionException e) {
					new ReportError(ReportError.UNHANDLE_SIGNATURA_ERROR,"Ошибка выполнения проверки входных файлов");
				}

			});
			service.shutdown();
			return wFiles;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ObservableList<WorkingFile> execute(ObservableList<WorkingFile> files, Report report) {
		try {
			ExecutorService service = Executors.newWorkStealingPool();

			List<SingleFileCheck> checks = new ArrayList<>();
			List<FileType> types = MainApp.getDb().getFileTypes(report);
			files.forEach(file -> {
				checks.add(new SingleFileCheck(null, file, types));
			});
			List<Future<WorkingFile>> futures = service.invokeAll(checks);
/*			futures.forEach(future -> {
				try {
					//if (future.get() != null)
						//wFiles.add(future.get());
				} catch (InterruptedException | ExecutionException e) {
					new ReportError("Ошибка выполнения проверки входных файлов");
				}

			});*/
			service.shutdown();
			return files;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
