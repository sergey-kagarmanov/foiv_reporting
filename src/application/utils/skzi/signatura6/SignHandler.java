package application.utils.skzi.signatura6;

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
		boolean flag = true;
		InputStream fis = getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = fis.read(buffer);
		sign.start(buffer, length);
		while ((length = fis.read(buffer)) > 0) {
			baos.write(sign.next(buffer, length));
			flag = false;
		}
		/*if (flag) {
			baos.write(sign.next(buffer, buffer.length));
		}*/
		fis.close();
		byte[] bytes = sign.end();
		baos.write(bytes);
		file.setData(baos.toByteArray());
		baos.close();
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
