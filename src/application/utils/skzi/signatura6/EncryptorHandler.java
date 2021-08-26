package application.utils.skzi.signatura6;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaHandler;

public class EncryptorHandler extends SignaturaHandler {

	public EncryptorHandler(Key key) throws ReportError {
		super(key);
	}

	@Override
	protected void init(Key key) throws ReportError {
		executor = new EncryptorSignatura(key);
	}

	@Override
	public WorkingFile call() throws Exception {

		ByteArrayInputStream bais = null;
		InputStream fis = getInputStream();
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(baos);

		byte[] bufferZip = new byte[1024];
		byte[] bufferEncrypt = new byte[1024];
		byte[] encrypted = null;
		int length = 0;

		while ((length = fis.read(bufferZip)) > 0) {
			gzip.write(bufferZip, 0, length);
		}
		gzip.finish();
		bais = new ByteArrayInputStream(baos.toByteArray());

		length = bais.read(bufferEncrypt);
		executor.start(bufferEncrypt, length);
		result = executor.getResult();
		if (result != 0) {
			throw new ReportError("Код ошибки - 0x" + Integer.toHexString(result));
		}
		while ((length = bais.read(bufferEncrypt)) > 0) {
			encrypted = executor.next(bufferEncrypt, length);
			result = executor.getResult();
			if (result==0 && encrypted != null) {
				fos.write(encrypted);
			}else if (result!=0)
				throw new ReportError("Код ошибки - 0x" + Integer.toHexString(result));
		}
		encrypted = executor.end();
		result = executor.getResult();
		if (result==0 && encrypted!=null)
			fos.write(encrypted);
		else if (result !=0) 
			throw new ReportError("Код ошибки - 0x" + Integer.toHexString(result));
		
		fos.flush();
		fos.close();
		bais.close();
		baos.close();
		fis.close();
		file.setData(fos.toByteArray());
		return file;
	}

}
