package application.utils.skzi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;

import application.errors.ReportError;
import application.models.ErrorFile;
import application.models.Key;

public class DecryptorHandler extends SignaturaHandler {

	private DecryptorSignatura decryptor;

	public DecryptorHandler(Key key) {
		super(key);
	}

	@Override
	public ErrorFile call() throws Exception {
		try {
			ByteArrayInputStream bais = null;
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(out);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] bufferZip = new byte[1024];
			byte[] bufferEncrypt = new byte[1024];
			byte[] buffer = null;
			int length = 0;
			if ((length = fis.read(bufferEncrypt)) > 0) {
				decryptor.start(bufferEncrypt, length);
				while ((length = fis.read(bufferEncrypt)) > 0) {
					buffer = decryptor.next(bufferEncrypt, length);
					if (buffer != null)
						baos.write(buffer);
				}
				decryptor.end();
				baos.flush();
				bais = new ByteArrayInputStream(baos.toByteArray());
				GZIPInputStream gzip = new GZIPInputStream(bais);
				while ((length = gzip.read(bufferZip)) > 0) {
					fos.write(bufferZip, 0, length);
				}
			}
			fos.close();
			bais.close();
			baos.close();
			fis.close();
			unload();
			return null;
		} catch (ReportError e) {
			return new ErrorFile(file.getName(), e.getErrorCode());
		}
	}

	@Override
	void init(Key key) {
		decryptor = new DecryptorSignatura(key);
	}

	@Override
	void unload() {
		decryptor.unload();
	}

}
