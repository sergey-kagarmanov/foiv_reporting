package application.utils.skzi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;

public abstract class SignaturaHandler implements Callable<WorkingFile> {

	protected WorkingFile file;

	public SignaturaHandler(Key key) {
		init(key);
	}

	public void setParameters(WorkingFile file) {
		this.file = file;
	}

	abstract void init(Key key);

	abstract void unload();

	InputStream getInputStream() throws ReportError {
		if (file != null) {
			return new ByteArrayInputStream(file.getData());
		}else {
			throw new ReportError("Входные данные отсутствуют");
		}
	}

}
