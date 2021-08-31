package application.utils.skzi.signatura6;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import application.errors.ReportError;
import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaHandler;

public class UnsignHandler extends SignaturaHandler {

	public UnsignHandler(Key key) throws ReportError {
		super(key);
	}

	@Override
	public WorkingFile call() throws Exception {
		boolean flag = true;
		byte[] bytes = null;
		file.copyToSign();
		InputStream fis = getInputStream();
		ByteArrayOutputStream fos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int length = 0;
		length = fis.read(buffer);
		executor.start(buffer, length);
		result = executor.getResult();
		if (result != 0) {
			throw new ReportError(ReportError.UNSIGN_ERROR, "Ошибка верификации и снятия подписи 0x" + Integer.toHexString(result), file.getName());
		}
		while ((length = fis.read(buffer)) > 0) {
			bytes = executor.next(buffer, length);
			if (result != 0) {
				throw new ReportError(ReportError.UNSIGN_ERROR, "Ошибка верификации и снятия подписи 0x" + Integer.toHexString(result),
						file.getName());
			}

			if (bytes != null)
				fos.write(bytes);
			flag = false;
		}
		/**
		 * This block uses to force call of next function, without it you get an
		 * error
		 */
		if (flag) {
			bytes = executor.next(new byte[0], 0);
			if (result != 0) {
				throw new ReportError(ReportError.UNSIGN_ERROR, "Ошибка верификации и снятия подписи 0x" + Integer.toHexString(result),
						file.getName());
			}
			if (bytes != null)
				fos.write(bytes);
		}

		bytes = executor.end();
		if (result!=0) {
			throw new ReportError(ReportError.UNSIGN_ERROR, "Ошибка верификации и снятия подписи 0x"+Integer.toHexString(result), file.getName());
		}
		fos.write(bytes);
		file.setData(fos.toByteArray());
		fis.close();
		fos.close();
		return file;
	}

	@Override
	protected void init(Key key) throws ReportError {
		executor = new UnsignSignatura(key);
	}

}
