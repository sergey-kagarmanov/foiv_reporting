package application.utils.skzi.signatura6;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import application.models.Key;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaHandler;

public class UnsignHandler extends SignaturaHandler {

	private UnsignSignatura unsigner;

	public UnsignHandler(Key key) {
		super(key);
	}

	@Override
	public WorkingFile call() throws Exception {
		boolean flag = true;
		file.copyToSign();
		InputStream fis = getInputStream();
		ByteArrayOutputStream fos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int length = 0;
		length = fis.read(buffer);
		unsigner.start(buffer, length);
		while ((length = fis.read(buffer)) > 0) {
			byte[] bytes = unsigner.next(buffer, length);
			if (bytes!=null)
				fos.write(bytes);
			flag = false;
		}
		/**
		 * This block uses to force call of next function, without it you get an error
		 */
		if (flag) {
			byte[] bytes = unsigner.next(new byte[0], 0);
			if (bytes!=null)
				fos.write(bytes);
		}
			
		fos.write(unsigner.end());
		file.setData(fos.toByteArray());
		fis.close();
		fos.close();
		return file;
	}

	@Override
	protected void init(Key key) {
		unsigner = new UnsignSignatura(key);
	}

}
