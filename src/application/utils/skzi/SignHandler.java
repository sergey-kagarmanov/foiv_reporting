package application.utils.skzi;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import application.errors.ReportError;
import application.models.ErrorFile;
import application.models.Key;

public class SignHandler extends SignaturaHandler {

	private SignSignatura sign;

	public SignHandler(Key key) {
		super(key);
	}

	@Override
	public ErrorFile call() throws Exception {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(out);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = fis.read(buffer)) > 0) {
				baos.write(buffer, 0, length);
			}
			fos.write(sign.next(baos.toByteArray(), baos.toByteArray().length));
			fis.close();
			fos.close();
			return null;
		} catch (ReportError e) {
			return new ErrorFile(file.getName(), e.getErrorCode());
		}
	}

	@Override
	void init(Key key) {
		sign = new SignSignatura(key);
	}

	@Override
	void unload() {
		sign.unload();
	}

}
