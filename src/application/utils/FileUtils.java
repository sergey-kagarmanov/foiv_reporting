package application.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import application.errors.ReportError;
import application.models.FileType;
import application.models.Report;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class FileUtils {

	public static String tmpDir = "tmp\\";
	public static String exeDir = "exe\\";
	// public static String rarFullPath = "\"C:\\Program Files
	// (x86)\\WinRAR\\WinRAR.exe\"";

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
			if (directory == null) {
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

	public static void clearTmp_old(ObservableList<FileType> mask) throws ReportError {
		for (String filename : (mask != null ? getDirContentByMask(FileUtils.tmpDir, mask)
				: FXCollections.observableArrayList(new File(FileUtils.tmpDir).list()))) {
			File file = new File(FileUtils.tmpDir + filename);
			if (file.exists()) {
				file.delete();
			}
		}
	}

	public static void clearTmp() {
		File tmpDirHandler = new File(tmpDir);
		for (File f : tmpDirHandler.listFiles()) {
			if (f.isDirectory()) {
				for (File s : f.listFiles()) {
					s.delete();
				}
			}
			f.delete();
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
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}

	public static void compressGZIP(File input, File output) throws IOException {
		try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(output))) {
			try (FileInputStream in = new FileInputStream(input)) {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
			}
		}
	}

	public static ObservableList<File> getFromZip(File zipFile) throws IOException {
		byte[] buffer = new byte[1024];
		ObservableList<File> list = FXCollections.observableArrayList();
		File folder = new File("tmp");
		if (!folder.exists()) {
			folder.mkdir();
		}
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		folder.delete();

		ZipEntry ze = zis.getNextEntry();
		while (ze != null) {
			String fileName = ze.getName();
			File newFile = new File(folder + File.separator + fileName);
			new File(newFile.getParent()).mkdirs();

			FileOutputStream fos = new FileOutputStream(newFile);

			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			fos.close();

			if (fileName.endsWith(".xml")) {
				list.add(newFile);
			} else if (fileName.endsWith(".zip") || fileName.endsWith(".gz")) {
				new File(newFile.getParent()).mkdirs();
				list.addAll(getFromZip(newFile));
			} else if (fileName.endsWith(".7z") || fileName.endsWith(".arj")) {
				new File(newFile.getParent()).mkdirs();
				list.addAll(getFromZip(newFile));

			}
			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();

		return list;
	}

	public static ObservableList<File> getFrom7z(File file) throws IOException {
		ObservableList<File> tmp = FXCollections.observableArrayList();
		RandomAccessFile randomAccessFile = null;
		IInArchive inArchive = null;

		try {
			randomAccessFile = new RandomAccessFile(file, "r");
			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));

			// Getting simple interface of the archive inArchive
			ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

			for (final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
				if (!item.isFolder()) {
					ExtractOperationResult result;

					result = item.extractSlow(new ISequentialOutStream() {
						public int write(byte[] data) throws SevenZipException {

							// Write to file
							FileOutputStream fos;
							try {
								File file = new File(item.getPath());
								file.getParentFile().mkdirs();
								fos = new FileOutputStream(file);
								fos.write(data);
								fos.close();

							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							return data.length; // Return amount of consumed
												// data
						}
					});
					if (result == ExtractOperationResult.OK) {
						if (item.getPath().endsWith(".zip") || item.getPath().endsWith(".gz")) {
							tmp.addAll(getFromZip(new File(item.getPath())));
						} else if (item.getPath().endsWith(".zip")
								|| item.getPath().endsWith(".gz")) {
							tmp.addAll(getFromZip(new File(item.getPath())));
						} else if (item.getPath().endsWith(".xml")) {
							tmp.add(new File(item.getPath()));
						}
					} else {
						System.err.println("Error extracting item: " + result);
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error occurs: " + e);
			System.exit(1);
		} finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				} catch (SevenZipException e) {
					System.err.println("Error closing archive: " + e);
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					System.err.println("Error closing file: " + e);
				}
			}
		}
		return tmp;
	}

	public static void copyFile(File file, String where) {
		CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES };
		try {
			Files.copy(file.toPath(), Paths.get(where + "\\" + file.getName()), options);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void copyFiles(ObservableList<File> files, String where) {
		CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES };
		try {
			for (File f : files) {
				Files.copy(f.toPath(), Paths.get(where + "\\" + f.getName()), options);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void moveFiles(ObservableList<File> files, String where) {
		CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES };
		try {
			for (File f : files) {
				Files.move(f.toPath(), Paths.get(where + "\\" + f.getName()), options);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void compressFile(String sourceFile, String outFile) {
		try {
		FileOutputStream fos = new FileOutputStream(outFile);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		File fileToZip = new File(sourceFile);
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		zipOut.close();
		fis.close();
		fos.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
