package application.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import application.MainApp;

public class ArchiverSteamExecutor {

	public static void compressFileStream(FileInputStream fis, FileOutputStream fos) throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
	}

	
	public static int decompressGzip(File input, File output) throws IOException {
		try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(input))) {
			try (FileOutputStream out = new FileOutputStream(output)) {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
			}
			MainApp.info("File " + input + " is decompressed to " + output);
		} catch (Exception e) {
			MainApp.error("File " + input + " isn't decompressed to" + output + " cause "
					+ e.getLocalizedMessage());
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

}
