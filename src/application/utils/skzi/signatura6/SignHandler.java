package application.utils.skzi.signatura6;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaHandler;

public class SignHandler extends SignaturaHandler {

	public SignHandler(Key key) throws ReportError {
		super(key);
	}

	@Override
	public WorkingFile call() throws Exception {
		boolean flag = true;
		InputStream fis = getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = fis.read(buffer);
		executor.start(buffer, length);
		while ((length = fis.read(buffer)) > 0) {
			baos.write(executor.next(buffer, length));
			flag = false;
		}
		fis.close();
		byte[] bytes = executor.end();
		baos.write(bytes);
		file.setData(baos.toByteArray());
		baos.close();
		file.copyToSign();
		return file;
	}

	@Override
	protected void init(Key key) throws ReportError {
		executor = new SignSignatura(key);
	}

}
