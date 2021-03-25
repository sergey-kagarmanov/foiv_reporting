package application.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class ArchieveInputStreamHandler {

	private ISimpleInArchiveItem item;
	private ByteArrayInputStream arrayInputStream;

	public ArchieveInputStreamHandler(ISimpleInArchiveItem item) {
		this.item = item;
	}

	public InputStream getInputStream() throws SevenZipException {

		item.extractSlow(new ISequentialOutStream() {
			@Override
			public int write(byte[] data) throws SevenZipException {
				arrayInputStream = new ByteArrayInputStream(data);
				return data.length; // Return amount of consumed data
			}
		});
		return arrayInputStream;
	}

	// got from
	// http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
	public static File slurp(final InputStream is, File file) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			try {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
			} finally {
				out.flush();
				out.close();
				is.close();
			}
		} catch (UnsupportedEncodingException ex) {
			/* ... */
		} catch (IOException ex) {
			/* ... */
		}
		return file;
	}

	public static byte[] slurpByte(final InputStream is) {
		ByteArrayOutputStream out = null;
		out = new ByteArrayOutputStream();
		try {
			try {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
			} finally {
				out.flush();
				out.close();
				is.close();
			}
		} catch (UnsupportedEncodingException ex) {
			/* ... */
		} catch (IOException ex) {
			/* ... */
		}
		return out.toByteArray();
	}

}