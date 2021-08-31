package application.utils.skzi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.signatura6.CommonSignatura;

public abstract class SignaturaHandler implements Callable<WorkingFile>, AutoCloseable {

	protected WorkingFile file;
	protected int result;
	protected CommonSignatura executor;

	public SignaturaHandler(Key key) throws ReportError {
		init(key);
	}

	public void setParameters(WorkingFile file) {
		this.file = file;
	}

	protected abstract void init(Key key) throws ReportError;


	protected InputStream getInputStream() throws ReportError {
		if (file != null) {
			return new ByteArrayInputStream(file.getData());
		}else {
			throw new ReportError(ReportError.FILE_ERROR, "Входные данные отсутствуют", file.getName());
		}
	}

	@Override
	public void close() throws Exception {
		//executor.unload();
	}
}
