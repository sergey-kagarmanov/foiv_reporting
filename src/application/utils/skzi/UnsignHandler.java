package application.utils.skzi;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import application.errors.ReportError;
import application.models.ErrorFile;
import application.models.Key;

public class UnsignHandler extends SignaturaHandler{

	private UnsignSignatura unsigner;
	
	public UnsignHandler(Key key) {
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
			fos.write(unsigner.next(baos.toByteArray(), baos.toByteArray().length));
			fis.close();
			fos.close();
			return null;
		} catch (ReportError e) {
			return new ErrorFile(file.getName(), e.getErrorCode());
		}
	}

	@Override
	void init(Key key) {
		unsigner = new UnsignSignatura(key);
	}

	@Override
	void unload() {
		unsigner.unload();
	}

}
