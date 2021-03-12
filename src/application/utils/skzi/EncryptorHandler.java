package application.utils.skzi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import application.errors.ReportError;
import application.models.ErrorFile;
import application.models.Key;

public class EncryptorHandler extends SignaturaHandler {

	private EncryptorSignatura encryptor;

	public EncryptorHandler(Key key) {
		super(key);
	}

	@Override
	void init(Key key) {
		encryptor = new EncryptorSignatura(key);
	}
	
	@Override
	public ErrorFile call() throws Exception {
		try {
		ByteArrayInputStream bais = null;
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream(out);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(baos);

		byte[] bufferZip = new byte[1024];
		byte[] bufferEncrypt = new byte[1024];
		byte[] encrypted = null;
		int length = 0;
		encryptor.start();
		while ((length = fis.read(bufferZip)) > 0) {
			gzip.write(bufferZip, 0, length);
		}
		gzip.finish();
		bais = new ByteArrayInputStream(baos.toByteArray());
		while ((length = bais.read(bufferEncrypt)) > 0) {
			encrypted = encryptor.next(bufferEncrypt, length);
			if (encrypted!=null) {
				fos.write(bufferEncrypt);
			}
		}

		fos.write(encryptor.end());
		fos.flush();
		fos.close();
		bais.close();
		baos.close();
		fis.close();
		unload();
		return null;
		}catch (ReportError e) {
			return new ErrorFile(file.getName(), e.getErrorCode());
		}
	}

	@Override
	public void unload() {
		encryptor.unload();
	}
}
