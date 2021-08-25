package application.utils.skzi.signatura5;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaHandler;

public class SignHandler extends SignaturaHandler {

	private SignSignatura sign;

	public SignHandler(Key key) {
		super(key);
	}

	@Override
	public WorkingFile call() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream fis = getInputStream();
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = fis.read(buffer)) > 0) {
			baos.write(buffer, 0, length);
		}
		fos.write(sign.next(baos.toByteArray(), baos.toByteArray().length));
		fis.close();
		fos.close();
		file.setData(fos.toByteArray());
		file.copyToSign();
		return file;
	}

	@Override
	protected void init(Key key) {
		sign = new SignSignatura(key);
	}

	@Override
	protected void unload() {
		// sign.unload();
	}

}
