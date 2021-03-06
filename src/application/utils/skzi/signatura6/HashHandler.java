package application.utils.skzi.signatura6;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaHandler;

public class HashHandler extends SignaturaHandler {

	private HashSignatura hasher;

	public HashHandler(Key key) throws ReportError {
		super(key);
	}

	@Override
	public WorkingFile call() throws Exception {
		InputStream fis = getInputStream();
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		hasher.start();
		while ((length = fis.read(buffer)) > 0) {
			hasher.next(buffer, length);
			baos.write(buffer, 0, length);
		}
		fos.write(hasher.end());
		fis.close();
		fos.close();
		file.setHashData(fos.toByteArray());
		return file;
	}

	@Override
	protected void init(Key key) throws ReportError {
		hasher = new HashSignatura(key);
	}

	@Override
	public void close() throws Exception {
		//hasher.unload();
	}


}
