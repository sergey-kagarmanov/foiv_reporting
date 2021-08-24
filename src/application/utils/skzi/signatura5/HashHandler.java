package application.utils.skzi.signatura5;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import application.models.Key;
import application.models.WorkingFile;

public class HashHandler extends SignaturaHandler {

	private HashSignatura hasher;
	
	public HashHandler(Key key) {
		super(key);
	}

	@Override
	public WorkingFile call() throws Exception {
			InputStream fis = getInputStream();
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = 0;
			hasher.start();
			while ((length = fis.read(buffer)) > 0) {
				hasher.next(buffer, length);
				baos.write(buffer, 0, length);
			}
			fos.write(hasher.end());
			fis.close();
			fos.close();
			file.setHashData(fos.toByteArray());
			return file;
	}

	@Override
	void init(Key key) {
		hasher = new HashSignatura(key);
	}

	@Override
	void unload() {
		//hasher.unload();
	}

}
