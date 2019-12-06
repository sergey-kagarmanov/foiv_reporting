package application.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import application.db.Dao;
import application.errors.ReportError;
import application.models.Chain;
import application.models.ErrorFile;
import application.models.FileAttribute;
import application.models.FileEntity;
import application.models.FileTransforming;
import application.models.FileType;
import application.models.Key;
import application.models.ProcessStep;
import application.models.Report;
import application.models.ReportFile;
import application.models.TransportFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class ProcessExecutor {

	private Report report;
	// private List<File> files;
	private List<String> filenames;

	private ProcessStep currentStep;
	private Chain chain;

	private Dao dao;

	private Signatura signatura;

	private String path = "";
	private String outputPath = "";
	private String archivePath = "";
	private Map<FileTransforming, TransportFile> transportFiles;
	private Map<FileTransforming, ReportFile> mapFiles;
	private Logger logger;
	private Boolean direction;
	// private Boolean parse = true;
	private FileParser parser;
	private String[] renameFiles = null;
	private Map<FileTransforming, ReportFile> ticketFiles;
	private String text = "";
	private boolean breakFlag = false;

	private ObservableList<FileTransforming> executedFiles;

	private ObservableList<ErrorFile> errorFiles;

	public ProcessExecutor(List<String> filenames, Report report, Dao dao, String path,
			String outputPath, String archivePath, Boolean direction) {
		this.report = report;
		this.filenames = filenames;
		this.path = path;
		this.outputPath = outputPath;
		this.dao = dao;
		transportFiles = new HashMap<FileTransforming, TransportFile>();
		this.archivePath = archivePath;
		logger = new Logger(dao);
		mapFiles = new HashMap<FileTransforming, ReportFile>();
		parser = new FileParser(dao, report, direction);
		ticketFiles = new HashMap<FileTransforming, ReportFile>();
		this.direction = direction;
		signatura = new Signatura();
		errorFiles = FXCollections.observableArrayList();
	}

	public void start() throws ReportError {
		FileUtils.clearTmp(null);
		executedFiles = FXCollections.observableArrayList();
		FileTransforming currentFile = null;
		for (String filename : filenames) {
			currentFile = new FileTransforming(filename,
					direction ? report.getPathIn() : report.getPathOut());
			if (currentFile.getOriginalFile().exists()) {

				// Copy files to tmp directory for work
				currentFile.copyCurrent(FileUtils.tmpDir, true);
				executedFiles.add(currentFile);
				/**
				 * Parse files if them would be encrypted or modified and
				 * collect into map key - name, otherwise collect transport
				 * files into map, key - name
				 */

				/**
				 * This cycle doesn't check equal files so be careful
				 */
				boolean flag = true;
				for (FileType tFile : report.getTickets()) {
					if (FileUtils.isType(currentFile.getCommonOriginal(), tFile)) {
						Map<String, FileAttribute> attr = null;
						try {
							attr = parser.parse(currentFile.getCurrentFile());
						} catch (ReportError e) {
							if (currentFile.getCurrentFile().exists()) {
								System.gc();
								if (!currentFile.getCurrentFile().delete()) {
									System.out.println(
											"Can't delete file - " + currentFile.getCurrent());
								}
							}
							break;
						}
						ticketFiles.put(currentFile, new ReportFile(0, currentFile.getOriginal(),
								LocalDateTime.now(), report, direction, null, attr));
						flag = false;

					}
				}

				if (flag)
					try {
						if (!direction) {
							ReportFile fileEntity = new ReportFile(0,
									currentFile.getCommonOriginal(), LocalDateTime.now(), report,
									direction, null, parser.parse(currentFile.getCurrentFile()));
							mapFiles.put(currentFile, fileEntity);
						} else {
							transportFiles.put(currentFile,
									new TransportFile(0, currentFile.getOriginal(),
											LocalDateTime.now(), report, direction, null, null));

						}
					} catch (ReportError e) {
						if (currentFile.getCurrentFile().exists()) {
							System.gc();
							if (!currentFile.getCurrentFile().delete()) {
								System.out
										.println("Can't delete file - " + currentFile.getCurrent());
							}
						}

					}
			} else {
				// TODO: Error, warning etc.
			}
		}

		// Check parser exceptions
		if (parser.getExceptions().size() > 0) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Ошибки валидации");
			alert.setHeaderText("При проверки валидации по схеме возникли ошибки");

			parser.getExceptions().forEach(error -> {
				text += error.getMessage() + "\r\n";
			});
			text += "Продолжить обработку?";
			alert.setContentText(text);
			breakFlag = alert.showAndWait().get() == ButtonType.OK;
		}

		if (breakFlag) {
			return;
		}

		// currentStep = dao.getActionForReport(report, direction);
		chain = dao.getChains(report, direction ? Constants.IN : Constants.OUT).get(0);
		if (chain.getSteps() != null) {
			currentStep = chain.getSteps().get(0);
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Пустой список обработки");
			alert.show();
			return;
		}

		/**
		 * List files that had errors
		 */
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		boolean breakFlag = true;
		while (currentStep != null && breakFlag) {
			errorFiles = executeStepSignatura(currentStep);
			if (errorFiles.size() == 0) {
				currentStep = currentStep.getNext();
			} else {
				String errorString = "";
				for (ErrorFile errorFile : errorFiles) {
					errorString += " Файл - " + errorFile.getFile() + " ошибка:"
							+ (errorFile.getErrorCode() == -1 ? " Проблема с доступом к файлу"
									: errorFile.getErrorCode());
				}
				Alert msg = new Alert(AlertType.CONFIRMATION);
				msg.setContentText("Имеются ошибки при обработке файлов: " + errorString
						+ " Продолжить обработку?");

				breakFlag = msg.showAndWait().get() == ButtonType.YES;
			}
		}
		if (breakFlag) {
			if (!direction) {
				for (FileTransforming tf : transportFiles.keySet()) {
					if (!tf.copyCurrent(outputPath, false)) {
						throw new ReportError("Не могу перенести транспортный файл "
								+ tf.getCurrent() + " в выходную директорию " + outputPath);
					}
				}
			} else {
				for (FileTransforming rf : mapFiles.keySet()) {
					if (!rf.copyCurrent(outputPath, false)) {
						throw new ReportError("не могу перенести файл отчетности " + rf.getCurrent()
								+ " в выходную директорию " + outputPath);
					}
				}

				try {
					// TODO: Check this!!!
					for (TransportFile tf : transportFiles.values()) {
						if (tf.getListFiles() != null) {
							for (String rfName : tf.getListFiles().keySet()) {

								((ReportFile) tf.getListFiles().get(rfName)).setAttributes(
										parser.parse(new File(FileUtils.tmpDir).listFiles(
												(dir, name) -> name.contains(rfName))[0]));
								FileUtils.copy(new File(FileUtils.tmpDir + rfName),
										new File(outputPath + "\\" + rfName));
							}
						} else {

						}
					}
				} catch (ReportError e) {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Ошибка");
					alert.setHeaderText("Ошибка при обработке файла - " + e.getMessage());
					alert.setContentText("Произошла ошибка при обработке файла - " + e.getMessage()
							+ "\n\rВы хотите прервать обработку?");
					if (alert.showAndWait().get() == ButtonType.OK) {
						throw new ReportError("Прервать обработку");
					} else {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}

			/**
			 * Copy tickets
			 */
			for (FileTransforming fe : ticketFiles.keySet()) {
				if (!fe.copyCurrent(outputPath, false)) {
					throw new ReportError("Не могу перенести файл квитанции " + fe.getCurrent()
							+ " в выходную директорию " + outputPath);
				}

				// Copy to archive
				if (!fe.copyCurrent(archivePath, false)) {
					throw new ReportError("Не могу перенести файл квитанции " + fe.getCurrent()
							+ " в архив " + archivePath);

				}
			}

			putFilesIntoArch();

			for (TransportFile tf : transportFiles.values()) {
				logger.log(tf);
			}
			for (ReportFile ticket : ticketFiles.values()) {
				logger.log(ticket);
			}

			/*
			 * if (direction) deleteSources(transportFiles.values()); else
			 * deleteSources(mapFiles.values());
			 */
			deleteSources(executedFiles);
			// TODO: Change to FileTransforming
			if (!ticketFiles.values().isEmpty()) {
				deleteSourcesTickets(ticketFiles.values());
			}
			ticketFiles = null;
			executedFiles = FXCollections.observableArrayList();// Clear
																// executed
																// files
		}
	}

	private void deleteSources(ObservableList<FileTransforming> col) {
		col.forEach((file) -> {
			/*
			 * if (direction) new File(path + "/" + ((TransportFile)
			 * file).getName()).delete(); else new File(path + "/" +
			 * ((ReportFile) file).getName()).delete();
			 */
			file.getOriginalFile().delete();
		});
	}

	private void deleteSourcesTickets(Collection col) {
		col.forEach((file) -> new File(path + "/" + ((FileEntity) file).getName()).delete());
	}

	public void putFilesIntoArch() throws ReportError {
		for (FileTransforming tf : transportFiles.keySet()) {
			if (tf.getCurrentFile().exists()) {

				// Copy files to tmp directory for work
				tf.copyCurrent(archivePath, false);

				// Map<String, ReportFile> mf = tf.getListFiles();
				// Collection<ReportFile> fc = mf.values();

				for (String rf : transportFiles.get(tf).getListFiles().keySet()) {
					int index = executedFiles.indexOf(new FileTransforming(rf,
							direction ? report.getPathIn() : report.getPathOut()));
					FileTransforming fileTransforming = null;
					if (index >= 0) {
						fileTransforming = executedFiles.get(index);
					} else {
						throw new ReportError(
								"Внутренняя ошибка, не могу найти изначальный файл " + rf);
					}
					fileTransforming.copySigned(archivePath);
				}
			} else {
				// TODO: Error, warning etc.
			}
		}
	}
	/*
	 * public void putFilesIntoArch() throws ReportError { try { File f = null;
	 * for (TransportFile tf : transportFiles.values()) { if ((f = new
	 * File(FileUtils.tmpDir + tf.getName())).exists()) {
	 * 
	 * // Copy files to tmp directory for work File tmpFile = new
	 * File(archivePath + "\\" + tf.getName()); tmpFile.mkdirs(); if
	 * (tmpFile.exists()) { tmpFile.delete(); } tmpFile.createNewFile();
	 * FileUtils.copy(f, tmpFile); f.delete(); Map<String, ReportFile> mf =
	 * tf.getListFiles(); Collection<ReportFile> fc = mf.values();
	 * 
	 * for (ReportFile rf : tf.getListFiles().values()) { String fileName =
	 * rf.getName(); if (renameFiles != null && renameFiles.length > 0) { if
	 * (FileFilter.maskFilter(renameFiles[0], fileName.toLowerCase())) {
	 * fileName = fileName.toLowerCase().replaceAll(renameFiles[1],
	 * renameFiles[2]); } } File rfFile = new File(report.getPathIn() + "\\" +
	 * fileName.replaceAll(renameFiles[2], renameFiles[1])); tmpFile = new
	 * File(archivePath + "\\" + fileName); tmpFile.mkdirs(); if
	 * (tmpFile.exists()) { tmpFile.delete(); } tmpFile.createNewFile();
	 * FileUtils.copy(rfFile, tmpFile); rfFile.delete(); } } else { // TODO:
	 * Error, warning etc. } }
	 * 
	 * } catch (IOException e) { e.printStackTrace(); throw new
	 * ReportError("Ошибка при копировании исходных файлов в архив"); } }
	 */

	private void loadKey(Key key) throws ReportError {
		Runtime r = Runtime.getRuntime();
		Process p = null;
		if (Settings.AUTO_KEY) {
			try {
				String q = Settings.LOAD_KEY + key.getData();
				p = r.exec(q);
				int i = p.waitFor();
				if (i != 0)
					throw new ReportError("Ключ " + key.getName() + " не был загружен");
			} catch (InterruptedException | IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	public void unloadKey() throws ReportError {
		Runtime r = Runtime.getRuntime();
		Process p = null;
		if (Settings.AUTO_KEY) {
			try {
				String q = Settings.UNLOAD_KEY;
				p = r.exec(q);
				int i = p.waitFor();
				if (i != 0)
					throw new ReportError("Ключи не были выгружены");

			} catch (InterruptedException | IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	public ObservableList<ErrorFile> executeStepSignatura(ProcessStep step) throws ReportError {
		int result = 0;
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		if (Constants.ACTIONS[0].equals(step.getAction().getName())) {
			// Do nothing
		} else if (Constants.ACTIONS[1].equals(step.getAction().getName())) {
			// encrypt
			result = signatura.initConfig(step.getKey().getData());
			if (result == 0) {
				signatura.setParameters();

				errorFiles = signatura.encryptFilesInPath(
						executedFiles.filtered(new Predicate<FileTransforming>() {

							@Override
							public boolean test(FileTransforming t) {
								if (t.getCurrent() != null)
									return t.getCurrent().matches(step.getData());
								else
									return t.getOriginal().matches(step.getData());
							}
						}), step.getKey().getData());
				signatura.unload();
			} else {
				throw new ReportError("Ошибка инициализации криптосистемы");
			}
		} else if (Constants.ACTIONS[2].equals(step.getAction().getName())) {
			// sign
			result = signatura.initConfig(step.getKey().getData());
			if (result == 0) {
				signatura.setSignParameters();
				errorFiles = signatura
						.signFilesInPath(executedFiles.filtered(new Predicate<FileTransforming>() {

							@Override
							public boolean test(FileTransforming t) {
								if (t.getCurrent() != null)
									return t.getCurrent().matches(step.getData());
								else
									return t.getOriginal().matches(step.getData());
							}
						}), step);
				signatura.unload();
			} else {
				throw new ReportError("Ошибка инициализации криптосистемы");
			}
		} else if (Constants.ACTIONS[3].equals(step.getAction().getName())) {
			// decrypt
			result = signatura.initConfig(step.getKey().getData());
			if (result == 0) {
				signatura.setParameters();
				errorFiles = signatura.decryptFilesInPath(
						executedFiles.filtered(new Predicate<FileTransforming>() {

							@Override
							public boolean test(FileTransforming t) {
								if (t.getCurrent() != null)
									return t.getCurrent().matches(step.getData());
								else
									return t.getOriginal().matches(step.getData());
							}
						}), step.getData());
				signatura.unload();
			} else {
				throw new ReportError("Ошибка инициализации криптосистемы");
			}
		} else if (Constants.ACTIONS[4].equals(step.getAction().getName())) {
			// verify and unsign
			result = signatura.initConfig(step.getKey().getData());
			if (result == 0) {
				signatura.setParameters();
				errorFiles = signatura.verifyAndUnsignFilesInPath(
						executedFiles.filtered(new Predicate<FileTransforming>() {

							@Override
							public boolean test(FileTransforming t) {
								if (t.getCurrent() != null)
									return t.getCurrent().matches(step.getData());
								else
									return t.getOriginal().matches(step.getData());
							}
						}), step.getData());
				signatura.unload();
			} else {
				throw new ReportError("Ошибка инициализации криптосистемы");
			}
		} else if (Constants.ACTIONS[5].equals(step.getAction().getName())) {
			// post??
		} else if (Constants.ACTIONS[6].equals(step.getAction().getName())) {
			// create archive - transport file
			Runtime r = Runtime.getRuntime();
			Process p = null;
			boolean loop = true;
			int i = 0;
			ObservableList<TransportFile> archiveTransportFiles = dao.getArchiveFiles(report,
					direction, LocalDate.now());
			if (archiveTransportFiles != null)
				for (TransportFile f : archiveTransportFiles) {
					if (f.getDatetime().toLocalDate().isEqual(LocalDate.now())) {
						i++;
					}
				}
			/*
			 * ObservableList<String> fileList =
			 * FileUtils.getDirContentByMask(FileUtils.tmpDir, step.getData());
			 */
			ObservableList<FileTransforming> doneList = FXCollections.observableArrayList();
			try {
				boolean multiVolumes = false;
				while (loop) {
					i++;
					ObservableList<FileTransforming> fileListTmp = FXCollections
							.observableArrayList();
					for (FileTransforming f : executedFiles) {
						if (f.getErrorCode() == 0 && !doneList.contains(f))
							fileListTmp.add(f);
					}
					long fileSize = 0;
					String command = FileUtils.exeDir + "arj.exe a -e ";// +
																		// FileUtils.tmpDir+step.getAction().getData();

					String multiCommand = FileUtils.exeDir + "arj.exe A -V5000k -Y -E ";
					String pattern = direction ? report.getTransportInPattern().getMask()
							: report.getTransportOutPattern().getMask();

					pattern = pattern.replaceAll("%date", DateUtils.formatReport(LocalDate.now()));
					pattern = pattern.replaceAll("%dd",
							LocalDate.now().format(DateTimeFormatter.ofPattern("dd")));
					pattern = pattern.replaceAll("%MM",
							LocalDate.now().format(DateTimeFormatter.ofPattern("MM")));
					pattern = pattern.replaceAll("%yy",
							LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));

					if (i < 10) {
						pattern = pattern.replaceAll("%n", i + "").replaceAll("%", "0");
					} else if (i < 100) {
						pattern = pattern.replaceAll("%%n", i + "").replaceAll("%", "0");
					} else {
						pattern = pattern.replaceAll("%%%n", i + "").replaceAll("%", "0");
					}
					command += FileUtils.tmpDir + pattern;
					multiCommand += FileUtils.tmpDir + pattern;

					// Add files to transport arch, if summary filesize doesn't
					// greater than constant
					int col = 0;

					// HashMap<FileTransforming, ReportFile> toLog = new
					// HashMap<FileTransforming, ReportFile>();
					// This map is needed if we have in one cycle of processing
					// several transport files, it is a part of global mapFiles
					HashMap<String, ReportFile> partialFileMap = new HashMap<String, ReportFile>();

					int checkCount = 0; // This check count needs to create one
										// large file in one multiple volume
					for (FileTransforming currentFile : fileListTmp) {

						fileSize += currentFile.getCurrentFile().length();
						if (fileSize < Settings.FILE_SIZE && col < Settings.FILE_COUNT) {
							command += " " + currentFile.getCurrentFile().getAbsolutePath();
							doneList.add(currentFile);
							partialFileMap.put(currentFile.getOriginal(),
									mapFiles.get(currentFile));
							loop = false;
							multiVolumes = false;
							col++;
						} else {
							loop = true;
							if (currentFile.getCurrentFile().length() > Settings.FILE_SIZE
									&& checkCount == 0) {
								multiVolumes = true;
								multiCommand += " "
										+ currentFile.getCurrentFile().getAbsolutePath();
								doneList.add(currentFile);
								partialFileMap.put(currentFile.getOriginal(),
										mapFiles.get(currentFile));
								col++;
							} else {
								multiVolumes = false;
							}
						}
						checkCount++;
					}
					transportFiles.put(new FileTransforming(pattern, FileUtils.tmpDir),
							new TransportFile(0, pattern, LocalDateTime.now(), report, direction,
									null, partialFileMap));

					try {
						if (multiVolumes) {
							p = r.exec(multiCommand);
						}else {
							p = r.exec(command);
						}

						InputStream is = p.getInputStream();
						int w = 0;
						while ((w = is.read()) != -1) {
							System.out.print((char) w);
						}
						System.out.println(p.waitFor());
						executedFiles.addAll(transportFiles.keySet());
					} catch (InterruptedException | IOException ie) {
						ie.printStackTrace();
						throw new ReportError("Ошибка создания транспортного файла");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error!!!");
				throw new ReportError("Ошибка создания транспортного файла");
			}
			System.out.println("ARJ returned " + p.exitValue());

		} else if (Constants.ACTIONS[7].equals(step.getAction().getName())) {
			// REPLACE 0 - ? , 1 - substring to replace, 2 - replacement
			renameFiles = new String[3];
			renameFiles[0] = step.getData().substring(0, step.getData().indexOf(" > "));
			String ReplaceText = step.getData().substring(step.getData().indexOf(" > (") + 4,
					step.getData().lastIndexOf(")"));
			renameFiles[1] = ReplaceText.substring(0, ReplaceText.indexOf("|"));
			renameFiles[2] = ReplaceText.substring(ReplaceText.indexOf("|") + 1,
					ReplaceText.length());

			FileFilter filter = new FileFilter(renameFiles[0]);

			for (FileTransforming file : executedFiles) {
				if (file.getErrorCode() == 0) {
					if (filter.accept(new File(FileUtils.tmpDir), file.getCurrent())) {
						file.setCurrent(file.getCurrentFile().getParentFile().getAbsolutePath()
								+ "\\" + file.getCommonCurrent().replaceAll(renameFiles[1],
										renameFiles[2]));// new
															// File(FileUtils.tmpDir
															// +
															// file.getName().toLowerCase()
						// .replaceAll(renameFiles[1], renameFiles[2]));
						// file.renameTo(rFile);
					}
				}
			}

			/*
			 * for (String f : filenames) { if (filter.accept(null, f)) {
			 * f.replaceAll(renameFiles[1], renameFiles[2]); } }
			 */

		} else if (Constants.ACTIONS[8].equals(step.getAction().getName())) {
			// UNPACK
			// Runtime r = Runtime.getRuntime();
			// Process p = null;
			for (String filename : FileUtils.getDirContentByMask(FileUtils.tmpDir,
					step.getData())) {

				List<File> tmpFiles = unrar(FileUtils.tmpDir + filename);
				for (File f : tmpFiles) {
					TransportFile tmpTransportFile = transportFiles
							.get(new FileTransforming(filename, FileUtils.tmpDir));
					if (tmpTransportFile.getListFiles() == null) {
						tmpTransportFile.setListFiles(new HashMap<String, ReportFile>());
					}
					tmpTransportFile.getListFiles().put(f.getName(), new ReportFile(0, f.getName(),
							LocalDateTime.now(), report, direction, null, null));
					executedFiles
							.add(new FileTransforming(f.getName(), f.getName(), FileUtils.tmpDir));
				}
			}

		}

		if (errorFiles.size() > 0) {
			excludeErrorFiles(errorFiles);
		}

		return errorFiles;
	}

	/*
	 * public void executeStep_old(ProcessStep step) throws
	 * FileNotFoundException, IOException { if
	 * (Constants.ACTIONS[0].equals(step.getAction().getName())) { script +=
	 * "TO "; script += step.getData(); script += "\r\n"; useScript = true; }
	 * else if (Constants.ACTIONS[1].equals(step.getAction().getName())) {
	 * script += "CRYPT "; script += FileUtils.tmpDir + step.getData(); script
	 * += "\r\n"; script += "START\r\n"; useScript = true; } else if
	 * (Constants.ACTIONS[2].equals(step.getAction().getName())) { script +=
	 * "SIGN "; script += FileUtils.tmpDir + step.getData(); script += "\r\n";
	 * useScript = true; script += "START\r\nEXIT\r\n"; } else if
	 * (Constants.ACTIONS[3].equals(step.getAction().getName())) { script +=
	 * "DECRYPT "; script += FileUtils.tmpDir + step.getData(); script +=
	 * "\r\n"; useScript = true; script += "START\r\nEXIT\r\n"; } else if
	 * (Constants.ACTIONS[4].equals(step.getAction().getName())) { script +=
	 * "UNSIGN "; script += FileUtils.tmpDir + step.getData(); script += "\r\n";
	 * useScript = true; script += "START\r\nEXIT\r\n"; } else if
	 * (Constants.ACTIONS[5].equals(step.getAction().getName())) {
	 * 
	 * } else if (Constants.ACTIONS[6].equals(step.getAction().getName())) {
	 * Runtime r = Runtime.getRuntime(); Process p = null; boolean loop = true;
	 * int i = 0; ObservableList<TransportFile> otf =
	 * dao.getArchiveFiles(report, direction); if (otf != null) for
	 * (TransportFile f : otf) { if
	 * (f.getDatetime().toLocalDate().isEqual(LocalDate.now())) { i++; } }
	 * ObservableList<String> fileList =
	 * FileUtils.getDirContentByMask(FileUtils.tmpDir, step.getData());
	 * ObservableList<String> doneList = FXCollections.observableArrayList();
	 * try { while (loop) { i++; ObservableList<String> fileListTmp =
	 * FXCollections.observableArrayList(); for (String f : fileList) { if
	 * (!doneList.contains(f)) fileListTmp.add(f); } long fileSize = 0; String
	 * command = FileUtils.exeDir + "arj.exe a -e ";// + //
	 * FileUtils.tmpDir+step.getAction().getData();
	 * 
	 * String pattern = direction ?
	 * report.getTransportInPattern().getMask().replaceAll("%date",
	 * DateUtils.formatReport(LocalDate.now())) :
	 * report.getTransportOutPattern().getMask().replaceAll("%date",
	 * DateUtils.formatReport(LocalDate.now())); if (i < 10) { pattern =
	 * pattern.replaceAll("%n", i + "").replaceAll("%", "0"); } else if (i <
	 * 100) { pattern = pattern.replaceAll("%%n", i + "").replaceAll("%", "0");
	 * } else if (i < 1000) { pattern = pattern.replaceAll("%%%%n", i +
	 * "").replaceAll("%", "0"); } command += FileUtils.tmpDir + pattern + " " +
	 * FileUtils.tmpDir;
	 * 
	 * // Add files to transport arch, if summary filesize doesn't // greater
	 * than constant int col = 0; HashMap<String, ReportFile> toLog = new
	 * HashMap<String, ReportFile>(); for (String filename : fileListTmp) { File
	 * tmpFile = new File(FileUtils.tmpDir + filename); fileSize +=
	 * tmpFile.length(); if (fileSize < Settings.FILE_SIZE && col <
	 * Settings.FILE_COUNT) { command += " " + filename; doneList.add(filename);
	 * if (renameFiles == null || FileFilter.maskFilter(renameFiles[0],
	 * filename.replaceAll(renameFiles[2], renameFiles[1]))) toLog.put(filename,
	 * mapFiles.get(filename)); else
	 * toLog.put(filename.replaceAll(renameFiles[2], renameFiles[1]),
	 * mapFiles.get(filename)); loop = false; col++; } else { loop = true; } }
	 * transportFiles.put(new FileTransforming(pattern, report.getPathIn()), new
	 * TransportFile(0, pattern, LocalDateTime.now(), report, direction, null,
	 * toLog));
	 * 
	 * try { p = r.exec(command);
	 * 
	 * InputStream is = p.getInputStream(); int w = 0; while ((w = is.read()) !=
	 * -1) { System.out.print((char) w); } System.out.println(p.waitFor()); }
	 * catch (InterruptedException | IOException ie) { ie.printStackTrace(); } }
	 * } catch (Exception e) { e.printStackTrace();
	 * System.out.println("Error!!!"); } System.out.println("ARJ returned " +
	 * p.exitValue());
	 * 
	 * } else if (Constants.ACTIONS[7].equals(step.getAction().getName())) {
	 * renameFiles = step.getData().split("|"); for (File file : files) {
	 * file.renameTo(new File(file.getName().replaceAll(renameFiles[0],
	 * renameFiles[1]))); }
	 * 
	 * } else if (Constants.ACTIONS[8].equals(step.getAction().getName())) { //
	 * Runtime r = Runtime.getRuntime(); // Process p = null; for (String
	 * filename : FileUtils.getDirContentByMask(FileUtils.tmpDir,
	 * step.getData())) {
	 * 
	 * List<File> tmpFiles = unrar(FileUtils.tmpDir + filename); for (File f :
	 * tmpFiles) { if (transportFiles.get(filename).getListFiles() == null) {
	 * transportFiles.get(filename) .setListFiles(new HashMap<String,
	 * ReportFile>()); }
	 * transportFiles.get(filename).getListFiles().put(f.getName(), new
	 * ReportFile(0, f.getName(), LocalDateTime.now(), report, direction, null,
	 * null)); } }
	 * 
	 * } }
	 */

	private List<File> unrar(String file) {
		List<File> files = new ArrayList<File>();
		RandomAccessFile randomAccessFile = null;
		IInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));
			ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
			for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
				if (!item.isFolder()) {
					files.add(ArchieveInputStreamHandler.slurp(
							new ArchieveInputStreamHandler(item).getInputStream(),
							new File(FileUtils.tmpDir + item.getPath())));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// System.exit(1);
		} finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				} catch (SevenZipException e) {
					System.err.println("Error closing archive: " + e.getMessage());
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					System.err.println("Error closing file: " + e.getMessage());
				}
			}
		}
		return files;
	}

	private void excludeErrorFiles(ObservableList<ErrorFile> errorFiles) {
		this.errorFiles.addAll(errorFiles);
		for (ErrorFile errorFile : errorFiles) {
			try {
				Files.move(Paths.get(FileUtils.tmpDir + errorFile.getFile()),
						Paths.get(Configuration.get("error_path").toString()),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
