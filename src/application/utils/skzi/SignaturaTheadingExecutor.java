package application.utils.skzi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.models.ErrorFile;
import application.models.FileTransforming;
import application.models.Key;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class SignaturaTheadingExecutor {

	private List<FileTransforming> files;
	protected SignaturaHandler handler;
	
	public SignaturaTheadingExecutor(List<FileTransforming> files) {
		this.files = files;
	}
	
	public ObservableList<ErrorFile> execute(Key key, String path) throws InterruptedException{
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		ExecutorService service = Executors.newWorkStealingPool();
		
		List<SignaturaHandler> handlers = new ArrayList<>();
		for (FileTransforming f : files) {
			handler = getHandler(key);
			handler.setParameters(f.getCurrentFile(), path);
			handlers.add(handler);
		}
		List<Future<ErrorFile>> futures = service.invokeAll(handlers);
		futures.forEach(future -> {
			try {
				if (future.get() != null)
					errorFiles.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} 

		});
		service.shutdown();
		return errorFiles;
	}
	
	public abstract SignaturaHandler getHandler(Key key);
	
}
