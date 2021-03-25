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
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.xml.sax.SAXParseException;

import application.MainApp;
import application.errors.ReportError;
import application.models.Chain;
import application.models.ErrorFile;
import application.models.FileAttribute;
import application.models.FileEntity;
import application.models.FileTransforming;
import application.models.FileType;
import application.models.ProcessStep;
import application.models.Report;
import application.models.ReportFile;
import application.models.TransportFile;
import application.models.WorkingFile;
import application.utils.skzi.SignaturaService;
import application.view.controls.ArchiveNameDialogBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class ProcessExecutor {

	private Report report;
	private List<String> filenames;
	private ProcessStep currentStep;
	private Chain chain;

	private String path = "";
	private String outputPath = "";
	private String archivePath = "";
	private Map<FileTransforming, TransportFile> transportFiles;
	private Map<FileTransforming, ReportFile> mapFiles;
	private Logger logger;
	private Boolean direction;
	private FileParser parser;
	private String[] renameFiles = null;
	private Map<FileTransforming, ReportFile> ticketFiles;
	private String text = "";
	private boolean breakFlag = false;

	private ObservableList<FileTransforming> executedFiles;

	private ObservableList<ErrorFile> errorFiles;

	public ProcessExecutor(List<String> filenames, Report report, String path,
			String outputPath, String archivePath, Boolean direction){
		this.report = report;
		
		//this should translate to absolute filenames
		this.filenames = new ArrayList<>();
		filenames.forEach(name->{
			this.filenames.add(path+"\\"+name);
		});
		
		this.path = path;
		this.outputPath = outputPath;
		transportFiles = new HashMap<FileTransforming, TransportFile>();
		this.archivePath = archivePath;
		logger = new Logger();
		mapFiles = new HashMap<FileTransforming, ReportFile>();
		parser = new FileParser(report, direction);
		ticketFiles = new HashMap<FileTransforming, ReportFile>();
		this.direction = direction;
		errorFiles = FXCollections.observableArrayList();

		// Create archive path
		File ap = new File(archivePath);
		ap.mkdirs();
		// Create output path
		File op = new File(outputPath);
		op.mkdirs();

	}

	public int start() throws ReportError {
		Integer result = 0;
		FileUtils.clearTmp();
		executedFiles = FXCollections.observableArrayList();
		FileTransforming currentFile = null;
		/**
		 * File parsing start
		 */
		for (String filename : filenames) {
			currentFile = new FileTransforming(filename,
					direction==Constants.INPUT ? report.getPathIn() : report.getPathOut());
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
				FileType fileType = null;
				for (FileType tFile : report.getTickets()) {
					fileType = tFile;
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
								LocalDateTime.now(), report, direction, null, attr, tFile));
						flag = false;
					}
				}

				if (flag)
					try {
						if (!direction) {
							ReportFile fileEntity = new ReportFile(0,
									currentFile.getCommonOriginal(), LocalDateTime.now(), report,
									direction, null, parser.parse(currentFile.getCurrentFile()),
									fileType);
							mapFiles.put(currentFile, fileEntity);
						} else {
							transportFiles.put(currentFile,
									new TransportFile(0, currentFile.getOriginal(),
											LocalDateTime.now(), report, direction, null, null,
											MainApp.getDb().getFileType(report.getId(), direction ? 1 : 0, 1)));

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
				result++;
			} else {
				// TODO: Error, warning etc.
			}
		}
		/**
		 * File parsing end
		 */

		// Check parser exceptions
		if (parser.getExceptions().size() > 0) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Ошибки валидации");
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.setHeaderText("При проверки валидации по схеме возникли ошибки");

			parser.getExceptions().forEach(error -> {
				SAXParseException e = (SAXParseException) error;
				text += "В файле " + e.getSystemId().substring(e.getSystemId().lastIndexOf("/") + 1)
						+ System.lineSeparator();
				text += "В строке " + e.getLineNumber();
				text += " колонка " + e.getColumnNumber() + System.lineSeparator();
				text += error.getMessage() + System.lineSeparator();
			});
			text += "Продолжить обработку?";
			alert.setContentText(text);
			result = result - parser.getExceptions().size();
			breakFlag = alert.showAndWait().get() == ButtonType.OK;
		}

		if (breakFlag) {
			return -1;
		}

		// currentStep = dao.getActionForReport(report, direction);
		ObservableList<Chain> chains = MainApp.getDb().getChains(report,
				direction ? Constants.IN : Constants.OUT);
		if (chains != null) {
			chain = chains.get(0);
		} else {
			throw new ReportError("Для отчетности " + report.getName() + " направление "
					+ (direction ? Constants.IN : Constants.OUT)
					+ " не определена последовательность действий");
		}
		if (chain.getSteps() != null) {
			currentStep = chain.getSteps().get(0);
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Пустой список обработки");
			alert.show();
			return 0;
		}

		/**
		 * List files that had errors
		 */
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		boolean breakFlag = true;
		while (currentStep != null && breakFlag) {

			try {
				errorFiles = executeStepSignatura(currentStep);
			} catch (ReportError | InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
								int index = executedFiles
										.indexOf(new FileTransforming(rfName, FileUtils.tmpDir));

								((ReportFile) tf.getListFiles().get(rfName)).setAttributes(
										parser.parse(executedFiles.get(index).getCurrentFile()));
								executedFiles.get(index).copyCurrent(outputPath, false);
								((ReportFile) tf.getListFiles().get(rfName)).setFileType(
										parser.getType(executedFiles.get(index).getCurrentFile()));

								((ReportFile) tf.getListFiles().get(rfName)).setLinkedFile(
										linkParent((ReportFile) tf.getListFiles().get(rfName)));
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
						MainApp.error(e.getLocalizedMessage());
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
		return result;
	}

	public int startStream() throws ReportError{
	
		FileChecker checker = new FileChecker();
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		
		ObservableList<WorkingFile> wFiles = checker.execute(filenames, report);
		wFiles.forEach(wFile -> {
			if (wFile.getExceptions()!=null && wFile.getExceptions().size()>0) {
				wFile.getExceptions().forEach(e -> {
					errorFiles.add(new ErrorFile(wFile.getOriginalName(), ErrorFile.VALIDATE_EXCEPTION, e.getMessage()));
				});
			}
		});
		
		if (errorFiles.size()>0) {
			//Ask to proceed
		}
		
		ObservableList<Chain> chains = MainApp.getDb().getChains(report,
				direction ? Constants.IN : Constants.OUT);
		if (chains != null) {
			chain = chains.get(0);
		} else {
			throw new ReportError("Для отчетности " + report.getName() + " направление "
					+ (direction ? Constants.IN : Constants.OUT)
					+ " не определена последовательность действий");
		}
		if (chain.getSteps() != null) {
			currentStep = chain.getSteps().get(0);
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Пустой список обработки");
			alert.show();
			return 0;
		}
		
		while (currentStep != null && !breakFlag) {

			try {
				StepExecutor stepExecutor = new StepExecutor(currentStep, report);
				
					wFiles = stepExecutor.execute(wFiles);
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
	
		wFiles.forEach(file -> {
			try {
				file.saveData(outputPath);
			} catch (ReportError e) {
				e.printStackTrace();
			}
		});
		
		return 0;
	}
	
	/*private void fileParsing(Report report, List<File> inFiles) {
		List<WorkingFile> files = new ArrayList<>();
		List<FileType> types = MainApp.getDb().getFileTypes(report);
		
		for (String filename : filenames) {
			currentFile = new FileTransforming(filename,
					direction==Constants.INPUT ? report.getPathIn() : report.getPathOut());
			if (currentFile.getOriginalFile().exists()) {

				// Copy files to tmp directory for work
				currentFile.copyCurrent(FileUtils.tmpDir, true);
				executedFiles.add(currentFile);

				boolean flag = true;
				FileType fileType = null;
				for (FileType tFile : report.getTickets()) {
					fileType = tFile;
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
								LocalDateTime.now(), report, direction, null, attr, tFile));
						flag = false;
					}
				}

				if (flag)
					try {
						if (!direction) {
							ReportFile fileEntity = new ReportFile(0,
									currentFile.getCommonOriginal(), LocalDateTime.now(), report,
									direction, null, parser.parse(currentFile.getCurrentFile()),
									fileType);
							mapFiles.put(currentFile, fileEntity);
						} else {
							transportFiles.put(currentFile,
									new TransportFile(0, currentFile.getOriginal(),
											LocalDateTime.now(), report, direction, null, null,
											MainApp.getDb().getFileType(report.getId(), direction ? 1 : 0, 1)));

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
				result++;
			} else {
				// TODO: Error, warning etc.
			}
		}

	}*/

	private ReportFile linkParent(ReportFile reportFile) {
		if (reportFile.getAttributes() != null) {
			for (String attributeName : reportFile.getAttributes().keySet()) {
				if (attributeName.equals(Constants.PARENT)) {
					return MainApp.getDb().getReportFileByName(
							reportFile.getAttributes().get(attributeName).getValue());
				}
			}
		}
		return null;
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
		String dateString = "\\"
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
		for (FileTransforming tf : transportFiles.keySet()) {
			if (tf.getCurrentFile().exists()) {

				// Copy files to tmp directory for work
				tf.copyCurrent(archivePath + dateString, false);

				// Map<String, ReportFile> mf = tf.getListFiles();
				// Collection<ReportFile> fc = mf.values();

				if (transportFiles == null || transportFiles.get(tf) == null
						|| transportFiles.get(tf).getListFiles() == null) {
					throw new ReportError("Неверные настройки отчетности!");
				}

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
					fileTransforming.copySigned(archivePath + dateString);
				}
			} else {
				// TODO: Error, warning etc.
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
				MainApp.error(ie.getLocalizedMessage());
				ie.printStackTrace();
			}
		}
	}

	/**
	 * create archive - transport file
	 * 
	 * @throws ReportError
	 */
	private void createArchive() throws ReportError {
		Runtime r = Runtime.getRuntime();
		Process p = null;
		/**
		 * Tmp dir for create archive, cause arj have only 64 params
		 */
		File tmpDir = new File(FileUtils.tmpDir + "arj\\");
		tmpDir.mkdirs();

		boolean loop = true;
		int i = 0;
		ObservableList<TransportFile> archiveTransportFiles = MainApp.getDb().getArchiveFiles(report, direction,
				LocalDate.now());
		if (archiveTransportFiles != null)
			for (TransportFile f : archiveTransportFiles) {
				if (f.getDatetime().toLocalDate().isEqual(LocalDate.now())) {
					i++;
				}
			}

		ObservableList<FileTransforming> doneList = FXCollections.observableArrayList();
		try {
			boolean multiVolumes = false;
			while (loop) {
				i++;
				ObservableList<FileTransforming> fileListTmp = FXCollections.observableArrayList();
				for (FileTransforming f : executedFiles) {
					if (f.getErrorCode() == 0 && !doneList.contains(f))
						fileListTmp.add(f);
				}
				long fileSize = 0;
				String command = FileUtils.exeDir + "arj.exe a -e ";

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
				
				pattern = ArchiveNameDialogBox.display(pattern);
				
				command += FileUtils.tmpDir + "arj\\" + pattern;
				multiCommand += FileUtils.tmpDir + "arj\\" + pattern;

				// Add files to transport arch, if summary filesize doesn't
				// greater than constant
				int col = 0;

				// This map is needed if we have in one cycle of processing
				// several transport files, it is a part of global mapFiles
				HashMap<String, ReportFile> partialFileMap = new HashMap<String, ReportFile>();

				int checkCount = 0; // This check count needs to create one
									// large file in one multiple volume
				int numberPart = 1; // If filesize more than limit, this is
									// numbers of multivolume archive
				if (fileListTmp.size() > 0)
					for (FileTransforming currentFile : fileListTmp) {

						fileSize += currentFile.getCurrentFile().length();
						if (fileSize < Settings.FILE_SIZE && col < Settings.FILE_COUNT) {
							FileUtils.copyFile(currentFile.getCurrentFile(),
									FileUtils.tmpDir + "arj\\");
							doneList.add(currentFile);
							partialFileMap.put(currentFile.getOriginal(),
									mapFiles.get(currentFile));
							loop = false;
							multiVolumes = false;
							col++;
						} else {
							if (currentFile.getCurrentFile().length() > Settings.FILE_SIZE
									&& checkCount == 0) {
								multiVolumes = true;
								FileUtils.copyFile(currentFile.getCurrentFile(),
										FileUtils.tmpDir + "arj\\");

								doneList.add(currentFile);
								partialFileMap.put(currentFile.getOriginal(),
										mapFiles.get(currentFile));
								col++;
								// each are int, so result would be int, but for
								// round we need float
								numberPart = (int) Math
										.ceil((currentFile.getCurrentFile().length() * 10)
												/ Settings.FILE_SIZE / 10.0);
							} else {
								loop = true;
							}
						}
						checkCount++;
						transportFiles.put(new FileTransforming(pattern, FileUtils.tmpDir),
								new TransportFile(0, pattern, LocalDateTime.now(), report,
										direction, null, partialFileMap,
										MainApp.getDb().getFileType(report.getId(), direction ? 1 : 0, 1)));
						doneList.add(new FileTransforming(pattern, FileUtils.tmpDir));
						if (multiVolumes) {
							for (int k = 1; k < numberPart; k++) {
								String localPattern = pattern.replaceAll("\\.(arj|ARJ)", ".a0" + k);
								FileTransforming tmpFile = new FileTransforming(localPattern,
										FileUtils.tmpDir);
								doneList.add(tmpFile);
								transportFiles.put(tmpFile, new TransportFile(0, localPattern,
										LocalDateTime.now(), report, direction, null,
										partialFileMap,
										MainApp.getDb().getFileType(report.getId(), direction ? 1 : 0, 1)));
							}
						}
					}
				else {
					loop = false;
				}
				command += " " + FileUtils.tmpDir + "arj\\*";
				multiCommand += " " + FileUtils.tmpDir + "arj\\*";
				try {
					if (multiVolumes) {
						p = r.exec(multiCommand);
					} else {
						p = r.exec(command);
					}

					InputStream is = p.getInputStream();
					int w = 0;
					while ((w = is.read()) != -1) {
						System.out.print((char) w);
					}
					System.out.println(p.waitFor());
					// Copy archive from arj dir to tmp, cause all handlers
					// are in this dir
					FileUtils.copyFile(new File(FileUtils.tmpDir + "arj\\" + pattern),
							FileUtils.tmpDir);
					for (File f : tmpDir.listFiles()) {
						f.delete();
					}
					executedFiles.addAll(transportFiles.keySet());
				} catch (InterruptedException | IOException ie) {
					MainApp.error(ie.getLocalizedMessage());
					ie.printStackTrace();
					throw new ReportError("Ошибка создания транспортного файла");
				}

			}
		} catch (Exception e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
			System.out.println("Error!!!");
			throw new ReportError("Ошибка создания транспортного файла");
		}
		MainApp.info("ARJ returned " + p.exitValue());
	}

	/**
	 * REPLACE 0 - ? , 1 - substring to replace, 2 - replacement
	 * 
	 * @param data
	 */
	private void replace(String data) {
		renameFiles = new String[3];
		renameFiles[0] = data.substring(0, data.indexOf(" > "));
		String ReplaceText = data.substring(data.indexOf(" > (") + 4, data.lastIndexOf(")"));
		renameFiles[1] = ReplaceText.substring(0, ReplaceText.indexOf("|"));
		renameFiles[2] = ReplaceText.substring(ReplaceText.indexOf("|") + 1, ReplaceText.length());

		for (FileTransforming file : executedFiles.filtered(new Predicate<FileTransforming>() {

			@Override
			public boolean test(FileTransforming t) {
				if (t.getCurrent() != null)
					return t.getCurrent().matches(renameFiles[0]);
				else
					return t.getOriginal().matches(renameFiles[0]);
			}
		})) {
			if (file.getErrorCode() == 0) {
				file.setCurrent(file.getCurrentFile().getParentFile().getAbsolutePath() + "\\"
						+ file.getCurrent().replaceAll(renameFiles[1], renameFiles[2]));
			}
		}

	}

	private void unpack(String data) {
		for (String filename : FileUtils.getDirContentByMask(FileUtils.tmpDir, data)) {

			List<File> tmpFiles = unrar(FileUtils.tmpDir + filename);
			for (File f : tmpFiles) {
				TransportFile tmpTransportFile = transportFiles
						.get(new FileTransforming(filename, FileUtils.tmpDir));
				if (tmpTransportFile.getListFiles() == null) {
					tmpTransportFile.setListFiles(new HashMap<String, ReportFile>());
				}
				tmpTransportFile.getListFiles().put(f.getName(), new ReportFile(0, f.getName(),
						LocalDateTime.now(), report, direction, null, null, null)); // is
																					// set
																					// below
																					// near
																					// attribute
																					// parse
				executedFiles.add(new FileTransforming(f.getName(), f.getName(), FileUtils.tmpDir));
			}
		}

	}

	private void copy(String data) {
		for (FileTransforming fTransforming : executedFiles) {
			fTransforming.copyCurrent(data, false);
		}

	}

	public ObservableList<ErrorFile> executeStepSignatura(ProcessStep step)
			throws ReportError, InterruptedException, ExecutionException {
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		SignaturaService service = new SignaturaService(step.getKey());
		switch (step.getAction().getName()) {
		case Constants.ENCRYPT:
			errorFiles = service.encrypt(executedFiles.filtered(t -> {
				if (t.getCurrent() != null)
					return t.getCurrent().matches(step.getData());
				else
					return t.getOriginal().matches(step.getData());
			}));

			break;
		case Constants.SIGN:
			errorFiles = service.sign(executedFiles.filtered(t -> {
				if (t.getCurrent() != null)
					return t.getCurrent().matches(step.getData());
				else
					return t.getOriginal().matches(step.getData());
			}));
			break;
		case Constants.DECRYPT:
			errorFiles = service.decrypt(executedFiles.filtered(t -> {
				if (t.getCurrent() != null)
					return t.getCurrent().matches(step.getData());
				else
					return t.getOriginal().matches(step.getData());
			}));
			break;
		case Constants.UNSIGN:
			errorFiles = service.unsign(executedFiles.filtered(t -> {
				if (t.getCurrent() != null)
					return t.getCurrent().matches(step.getData());
				else
					return t.getOriginal().matches(step.getData());
			}));
			break;
		case Constants.PACK:
			createArchive();
			break;
		case Constants.RENAME:
			replace(step.getData());
			break;
		case Constants.UNPACK:
			unpack(step.getData());
			break;
		case Constants.COPY:
			copy(step.getData());
			break;
		}

		if (errorFiles.size() > 0) {
			excludeErrorFiles(errorFiles);
		}

		return errorFiles;
	}

	
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
			MainApp.error(e.getLocalizedMessage());
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
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

}
