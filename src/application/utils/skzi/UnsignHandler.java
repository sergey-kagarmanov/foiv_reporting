package application.utils.skzi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import application.models.Key;
import application.models.WorkingFile;

public class UnsignHandler extends SignaturaHandler{

	private UnsignSignatura unsigner;
	
	public UnsignHandler(Key key) {
		super(key);
	}

	@Override
	public WorkingFile call() throws Exception {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			file.copyToSign();
			InputStream fis = getInputStream();
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = fis.read(buffer)) > 0) {
				baos.write(buffer, 0, length);
			}
			fos.write(unsigner.next(baos.toByteArray(), baos.toByteArray().length));
			file.setData(fos.toByteArray());
			fis.close();
			fos.close();
			return file;
	}

	@Override
	void init(Key key) {
		unsigner = new UnsignSignatura(key);
	}

	@Override
	void unload() {
		unsigner.unload();
	}

}
