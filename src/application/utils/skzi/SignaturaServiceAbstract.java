package application.utils.skzi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.models.ErrorFile;
import application.models.FileTransforming;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class SignaturaServiceAbstract {

	public ObservableList<ErrorFile> execute(Collection<FileTransforming> files) throws InterruptedException, ExecutionException {
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		//setParameters(parameters);
		ExecutorService service = Executors.newWorkStealingPool();

		OperationHandler handler = null;
		List<OperationHandler> handlers = new ArrayList<>();
		for (FileTransforming f : files) {
			handler = new OperationHandler(f) {

				@Override
				Integer operation(String source, String target) {
					return action(source, target);
				}
			};
			handlers.add(handler);
		}
		List<Future<ErrorFile>> futures = service.invokeAll(handlers);
		futures.forEach(future -> {
			try {
				if (future.get() != null)
					errorFiles.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		service.shutdown();
		return errorFiles;
	}

	public abstract int action(String source,String target);
}