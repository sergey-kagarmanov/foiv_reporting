package application.utils.skzi.signatura6;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import Pki1.LocalIface;
import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaHandler;

public class DecryptorHandler extends SignaturaHandler {

	private DecryptorSignatura decryptor;

	public DecryptorHandler(Key key) throws ReportError {
		super(key);
	}

	@Override
	public WorkingFile call() throws Exception {
		ByteArrayInputStream bais = null;
		InputStream fis = getInputStream();
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
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
		file.setData(fos.toByteArray());
		return file;
	}

	@Override
	protected void init(Key key) throws ReportError {
		decryptor = new DecryptorSignatura(key);
	}

	@Override
	public void close() throws Exception {
		decryptor.unload();
	}


}
