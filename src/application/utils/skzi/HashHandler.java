package application.utils.skzi;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import application.errors.ReportError;
import application.models.ErrorFile;
import application.models.Key;

public class HashHandler extends SignaturaHandler {

	private HashSignatura hasher;
	
	public HashHandler(Key key) {
		super(key);
	}

	@Override
	public ErrorFile call() throws Exception {
		try {
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(out);
			byte[] buffer = new byte[1024];
			int length = 0;
			hasher.start();
			while ((length = fis.read(buffer)) > 0) {
				hasher.next(buffer, length);
			}
			fos.write(hasher.end());
			fis.close();
			fos.close();
			return null;
		} catch (ReportError e) {
			return new ErrorFile(file.getName(), e.getErrorCode());
		}
	}

	@Override
	void init(Key key) {
		hasher = new HashSignatura(key);
	}

	@Override
	void unload() {
		hasher.unload();
	}

}
