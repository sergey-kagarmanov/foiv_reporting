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
		InputStream fis = getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		byte[] bytes = null;
		int length = fis.read(buffer);
		executor.start(buffer, length);
		result = executor.getResult();
		if (result != 0) {
			throw new ReportError(ReportError.SIGN_ERROR, "Ошибка подписи 0x" + Integer.toHexString(result), file.getName());
		}
		while ((length = fis.read(buffer)) > 0) {
			bytes = executor.next(buffer, length);
			result = executor.getResult();
			if (result != 0) {
				throw new ReportError(ReportError.SIGN_ERROR, "Ошибка подписи 0x" + Integer.toHexString(result), file.getName());
			}
			if (bytes!=null)
				baos.write(bytes);
		}
		fis.close();
		bytes = executor.end();
		result = executor.getResult();
		if (result != 0) {
			throw new ReportError(ReportError.SIGN_ERROR, "Ошибка подписи 0x" + Integer.toHexString(result), file.getName());
		}
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
