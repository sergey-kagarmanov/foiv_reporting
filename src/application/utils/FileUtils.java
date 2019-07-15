package application.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import application.errors.ReportError;
import application.models.FileType;
import application.models.Report;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileUtils {

	public static String tmpDir = "c:\\sdm\\tmp\\";
	public static String exeDir = "c:\\sdm\\exe\\";
	//public static String rarFullPath = "\"C:\\Program Files (x86)\\WinRAR\\WinRAR.exe\"";

	/**
	 * Returns observableArrayList
	 * 
	 * @param directory
	 * @param observableList
	 * @return
	 * @throws ReportError
	 */
	public static ObservableList<String> getDirContentByMask(String directory,
			ObservableList<FileType> observableList) throws ReportError {
		try {
			if (directory==null) {
				directory = "c:\\";
			}
			File file = new File(directory);
			String[] files = file.list((dir, filename) -> {
				boolean flag = false;
				for (FileType tmp : observableList) {
					flag = Pattern.compile(tmp.getMask()).matcher(filename).matches();
					if (flag)
						return flag;
				}
				return false;
			});
			if (files != null)
				return FXCollections.observableArrayList(files);
			else
				return FXCollections.observableArrayList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ReportError("Ошибка при фильтрации файлов в каталоге - " + directory);
		}
	}

	public static Boolean isType(String fileName, FileType type) {
		return Pattern.compile(type.getMask()).matcher(fileName).matches();
	}

	public static ObservableList<String> getDirContentByMask(String directory, FileType fileType)
			throws ReportError {
		try {
			File file = new File(directory);
			String[] files = file.list((dir, filename) -> {
				return Pattern.compile(fileType.getMask()).matcher(filename).matches();
			});
			if (files != null)
				return FXCollections.observableArrayList(files);
			else
				return FXCollections.observableArrayList();
		} catch (Exception e) {
			throw new ReportError("Ошибка при фильтрации файлов в каталоге - " + directory);
		}
	}

	// TODO: Need to check uses and refactor to FileType or list
	/**
	 * Returns observableArrayList
	 * 
	 * @param directory
	 * @param observableList
	 * @return
	 */
	public static ObservableList<String> getDirContentByMask(String directory, String mask) {
		File file = new File(directory);
		String[] files = file
				.list((dir, filename) -> Pattern.compile(mask).matcher(filename).matches());
		if (files != null)
			return FXCollections.observableArrayList(files);
		else
			return FXCollections.observableArrayList();
	}

	public static void clearTmp(ObservableList<FileType> mask) throws ReportError {
		for (String filename : (mask != null ? getDirContentByMask(FileUtils.tmpDir, mask)
				: FXCollections.observableArrayList(new File(FileUtils.tmpDir).list()))) {
			File file = new File(FileUtils.tmpDir + filename);
			if (file.exists()) {
				file.delete();
			}
		}
	}

	/**
	 * Test directories for report if null change it to current
	 * 
	 * @param report
	 * @return
	 */
	public static Report testReport(Report report) {
		try {
			if (report.getPathArchiveIn() == null) {
				report.setPathArchiveIn(new File(".").getCanonicalPath());
			}
			if (report.getPathArchiveOut() == null) {
				report.setPathArchiveOut(new File(".").getCanonicalPath());
			}
			if (report.getPathIn() == null) {
				report.setPathIn(new File(".").getCanonicalPath());
			}
			if (report.getPathOut() == null) {
				report.setPathOut(new File(".").getCanonicalPath());
			}
			if (report.getPathOutputIn() == null) {
				report.setPathOutputIn(new File(".").getCanonicalPath());
			}
			if (report.getPathOutputOut() == null) {
				report.setPathOutputOut(new File(".").getCanonicalPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return report;
	}

	public static void copy(File from, File to) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(from));
			out = new BufferedOutputStream(new FileOutputStream(to));

			byte[] buffer = new byte[1024];
			int lengthRead;
			while ((lengthRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, lengthRead);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}
	
    public static void compressGZIP(File input, File output) throws IOException {
        try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(output))){
            try (FileInputStream in = new FileInputStream(input)){
                byte[] buffer = new byte[1024];
                int len;
                while((len=in.read(buffer)) != -1){
                    out.write(buffer, 0, len);
                }
            }
        }
    }

}
