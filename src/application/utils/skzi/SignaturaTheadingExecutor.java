package application.utils.skzi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.models.Key;
import application.models.WorkingFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class SignaturaTheadingExecutor {

	private List<WorkingFile> files;
	protected SignaturaHandler handler;
	
	public SignaturaTheadingExecutor(List<WorkingFile> files) {
		this.files = files;
	}
	
	public ObservableList<WorkingFile> execute(Key key) throws InterruptedException{
		ObservableList<WorkingFile> outFiles = FXCollections.observableArrayList();
		ExecutorService service = Executors.newWorkStealingPool();
		
		List<SignaturaHandler> handlers = new ArrayList<>();
		for (WorkingFile f : files) {
			handler = getHandler(key);
			handler.setParameters(f);
			handlers.add(handler);
		}
		
		List<Future<WorkingFile>> futures = service.invokeAll(handlers);
		futures.forEach(future -> {
			try {
				if (future.get() != null)
					outFiles.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} 

		});
		service.shutdown();
		return outFiles;
	}
	
	public abstract SignaturaHandler getHandler(Key key);
	
}
