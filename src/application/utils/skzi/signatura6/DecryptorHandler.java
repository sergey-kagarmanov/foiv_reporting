package application.utils.skzi.signatura6;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaHandler;

public class DecryptorHandler extends SignaturaHandler {

	public DecryptorHandler(Key key) throws ReportError {
		super(key);
	}

	@Override
	public WorkingFile call() throws ReportError {
		ByteArrayInputStream bais = null;
		InputStream fis = null;
		try {
			fis = getInputStream();
		} catch (ReportError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] bufferZip = new byte[1024];
		byte[] bufferEncrypt = new byte[1024];
		byte[] buffer = null;
		int length = 0;
		try {
			if ((length = fis.read(bufferEncrypt)) > 0) {
				executor.start(bufferEncrypt, length);
				if (executor.getResult() != 0) {
					fos.close();
					baos.close();
					fis.close();

					throw new ReportError(ReportError.DECRYPT_ERROR, "Код ошибки - 0x" + Integer.toHexString(result), file.getName());
				}
				while ((length = fis.read(bufferEncrypt)) > 0) {
					buffer = executor.next(bufferEncrypt, length);
					if (executor.getResult() != 0) {
						fos.close();
						baos.close();
						fis.close();

						throw new ReportError(ReportError.DECRYPT_ERROR, "Код ошибки - 0x" + Integer.toHexString(result), file.getName());
					}
					if (buffer != null)
						baos.write(buffer);
				}
				executor.end();
				if (executor.getResult() != 0) {
					fos.close();
					baos.close();
					fis.close();

					throw new ReportError(ReportError.DECRYPT_ERROR, "Код ошибки - 0x" + Integer.toHexString(result), file.getName());
				}
				baos.flush();
				bais = new ByteArrayInputStream(baos.toByteArray());
				GZIPInputStream gzip = new GZIPInputStream(bais);
				while ((length = gzip.read(bufferZip)) > 0) {
					fos.write(bufferZip, 0, length);
				}
				bais.close();
			}
			fos.close();

			baos.close();
			fis.close();
		} catch (ReportError e) {
			e.setFilename(file.getName());
			throw e;
		} catch (Exception e) {
			throw new ReportError(ReportError.FILE_ERROR, "Ошибка чтения файла", file.getName(), e);
		}
		file.setData(fos.toByteArray());
		return file;
	}

	@Override
	protected void init(Key key) throws ReportError {
		executor = new DecryptorSignatura(key);
	}

	@Override
	public void close() throws Exception {
		//executor.unload();
	}

}
