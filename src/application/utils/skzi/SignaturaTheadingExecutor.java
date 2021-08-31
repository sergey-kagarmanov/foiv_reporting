package application.utils.skzi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.MainApp;
import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.AlertWindow;
import application.utils.skzi.signatura6.LocalSignatura;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class SignaturaTheadingExecutor implements AutoCloseable {

	private List<WorkingFile> files;
	protected SignaturaHandler handler;

	public SignaturaTheadingExecutor(List<WorkingFile> files) {
		this.files = files;
	}

	public ObservableList<WorkingFile> execute(Key key) throws ReportError {
		ObservableList<WorkingFile> outFiles = FXCollections.observableArrayList();
		ExecutorService service = Executors.newWorkStealingPool();
		List<SignaturaHandler> handlers = new ArrayList<>();
		LocalSignatura.initSignatura(key.getData());
		for (WorkingFile f : files) {
			handler = getHandler(key);
			handler.setParameters(f);
			handlers.add(handler);
		}

		List<Future<WorkingFile>> futures;
		try {
			futures = service.invokeAll(handlers);
			for (Future<WorkingFile> future : futures) {
				if (future.get() != null)
					outFiles.add(future.get());

			}
			/*
			 * futures.forEach(future -> { if (future.get() != null)
			 * outFiles.add(future.get()); /* } catch (InterruptedException |
			 * ExecutionException e) { AlertWindow.show(new
			 * ReportError(ReportError.RUNTIME_ERROR, e.getLocalizedMessage(),
			 * null, e), MainApp.logger); }
			 */

		} catch (InterruptedException e1) {
			throw new ReportError(ReportError.RUNTIME_ERROR, e1.getLocalizedMessage(), null, e1);
		} catch (ExecutionException e) {
			throw new ReportError(ReportError.RUNTIME_ERROR, e.getLocalizedMessage(), null, e);
		} finally {
			LocalSignatura.uninitilize();
		}

		service.shutdown();
		return outFiles;
	}

	public abstract SignaturaHandler getHandler(Key key) throws ReportError;

	@Override
	public void close() throws Exception {
	}

}
