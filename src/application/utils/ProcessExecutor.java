package application.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.db.Dao;
import application.errors.ReportError;
import application.models.Chain;
import application.models.FileAttribute;
import application.models.FileEntity;
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
	private List<File> files;
	private List<String> filenames;

	private ProcessStep currentStep;
	private Chain chain;

	private Dao dao;

	private Signatura signatura;

	private boolean useScript = false;
	private String script = "";
	private String path = "";
	private String outputPath = "";
	private String archivePath = "";
	private Map<String, TransportFile> transportFiles;
	private Map<String, ReportFile> mapFiles;
	private Logger logger;
	private Boolean direction;
	// private Boolean parse = true;
	private FileParser parser;
	private String[] renameFiles = null;
	private Map<String, ReportFile> ticketFiles;
	private String text = "";
	private boolean breakFlag = false;

	public ProcessExecutor(List<String> filenames, Report report, Dao dao, String path,
			String outputPath, String archivePath, Boolean direction) {
		this.report = report;
		files = new ArrayList<>();
		this.filenames = filenames;
		this.path = path;
		this.outputPath = outputPath;
		this.dao = dao;
		transportFiles = new HashMap<String, TransportFile>();
		this.archivePath = archivePath;
		logger = new Logger(dao);
		mapFiles = new HashMap<String, ReportFile>();
		parser = new FileParser(dao, report, direction);
		ticketFiles = new HashMap<String, ReportFile>();
		this.direction = direction;
		signatura = new Signatura();
	}

	public void start() throws ReportError {
		FileUtils.clearTmp(null);

		try {
			for (String filename : filenames) {
				File file = new File(path + "\\" + filename);
				if (file.exists()) {

					// Copy files to tmp directory for work
					File tmpFile = new File(FileUtils.tmpDir + file.getName());
 					tmpFile.mkdirs();
					if (tmpFile.exists()) {
						tmpFile.delete();
					}
					tmpFile.createNewFile();
					FileUtils.copy(file, tmpFile);
					files.add(tmpFile);
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
						if (FileUtils.isType(filename, tFile)) {
							Map<String, FileAttribute> attr = null;
							try {
								attr = parser.parse(tmpFile);
							} catch (ReportError e) {
								if (tmpFile.exists()) {
									System.gc();
									if (!tmpFile.delete()) {
										System.out.println(
												"Can't delete file - " + tmpFile.getName());
									}
								}
								break;
							}
							ticketFiles.put(filename, new ReportFile(0, filename,
									LocalDateTime.now(), report, direction, null, attr));
							flag = false;

						}
					}

					if (flag)
						try {
							if (!direction) {
								ReportFile fileEntity = new ReportFile(0, filename,
										LocalDateTime.now(), report, direction, null,
										parser.parse(tmpFile));
								mapFiles.put(filename.toLowerCase(), fileEntity);
							} else {
								transportFiles.put(filename, new TransportFile(0, filename,
										LocalDateTime.now(), report, direction, null, null));

							}
						} catch (ReportError e) {
							if (tmpFile.exists()) {
								System.gc();
								if (!tmpFile.delete()) {
									System.out.println("Can't delete file - " + tmpFile.getName());
								}
							}

						}
				} else {
					// TODO: Error, warning etc.
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new ReportError() {
				{
					setMessage("Ошибка при копировании исходных файлов на обработку");
				}
			};
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
		currentStep = chain.getSteps().get(0);
		while (currentStep != null) {
			executeStepSignatura(currentStep);
			currentStep = currentStep.getNext();
			/*
			 * try { executeStepSignatura(currentStep); // Only for instruction
			 * TO we need to proceed next element if
			 * (Constants.ACTIONS[0].equals(currentStep.getAction().getName()))
			 * { currentStep = currentStep.getNext();
			 * executeStepSignatura(currentStep); } if (useScript) {
			 * loadKey(currentStep.getKey()); File batFile = new
			 * File(FileUtils.tmpDir + "batfile"); if (batFile.exists()) {
			 * batFile.delete(); } batFile.createNewFile(); FileWriter fw = new
			 * FileWriter(batFile);
			 * 
			 * fw.write(script); fw.flush(); fw.close(); Runtime r =
			 * Runtime.getRuntime(); Process p = null; try { String q =
			 * Settings.VERBA_PATH + " /@\"" + batFile.getAbsolutePath() + "\"";
			 * p = r.exec(q); System.out.println(p.waitFor());
			 * 
			 * } catch (InterruptedException | IOException ie) {
			 * ie.printStackTrace(); } unloadKey(); batFile.delete(); script =
			 * ""; useScript = false; } currentStep = currentStep.getNext(); }
			 * catch (FileNotFoundException e) { e.printStackTrace(); } catch
			 * (IOException e) { e.printStackTrace(); }
			 */
		}
		// Copy files from tmp dir to output dir
		try {
			if (!direction) {
				for (TransportFile tf : transportFiles.values()) {
					File tfile = new File(FileUtils.tmpDir + tf.getName());
					if (tfile.exists()) {
						File outTFile = new File(outputPath + "\\" + tfile.getName());
						outTFile.mkdirs();
						if (outTFile.exists())
							outTFile.delete();
						outTFile.createNewFile();
						FileUtils.copy(tfile, outTFile);
					}
				}
			} else {
				for (ReportFile rf : mapFiles.values()) {
					File rfile = new File(FileUtils.tmpDir + rf.getName());
					if (rfile.exists()) {
						File outrFile = new File(outputPath + "\\" + rfile.getName());
						outrFile.mkdirs();
						if (outrFile.exists())
							outrFile.delete();
						outrFile.createNewFile();
						FileUtils.copy(rfile, outrFile);
					}

				}
				try {
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
			for (ReportFile fe : ticketFiles.values()) {
				File tfile = new File(FileUtils.tmpDir + "\\" + fe.getName());
				if (tfile.exists()) {
					File outTFile = new File(outputPath + "\\" + fe.getName());
					outTFile.mkdirs();
					if (outTFile.exists())
						outTFile.delete();
					outTFile.createNewFile();
					FileUtils.copy(tfile, outTFile);
				}

				// Copy to archive
				File archiveFile = new File(archivePath + "\\" + fe.getName());
				archiveFile.mkdirs();
				if (archiveFile.exists()) {
					archiveFile.delete();
				}
				archiveFile.createNewFile();
				FileUtils.copy(tfile, archiveFile);

			}

			putFilesIntoArch();

			for (TransportFile tf : transportFiles.values()) {
				logger.log(tf);
			}
			for (ReportFile ticket : ticketFiles.values()) {
				logger.log(ticket);
			}

			if (direction)
				deleteSources(transportFiles.values());
			else
				deleteSources(mapFiles.values());

			if (!ticketFiles.values().isEmpty()) {
				deleteSourcesTickets(ticketFiles.values());
			}
			ticketFiles = null;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReportError("Ошибка при копировании подготовленных файлов");
		}
	}

	private void deleteSources(Collection col) {
		col.forEach((file) -> {
			if (direction)
				new File(path + "/" + ((TransportFile) file).getName()).delete();
			else
				new File(path + "/" + ((ReportFile) file).getName()).delete();

		});
	}

	private void deleteSourcesTickets(Collection col) {
		col.forEach((file) -> new File(path + "/" + ((FileEntity) file).getName()).delete());
	}

	public void putFilesIntoArch() throws ReportError {
		try {
			File f = null;
			for (TransportFile tf : transportFiles.values()) {
				if ((f = new File(FileUtils.tmpDir + tf.getName())).exists()) {

					// Copy files to tmp directory for work
					File tmpFile = new File(archivePath + "\\" + tf.getName());
					tmpFile.mkdirs();
					if (tmpFile.exists()) {
						tmpFile.delete();
					}
					tmpFile.createNewFile();
					FileUtils.copy(f, tmpFile);
					f.delete();
					Map<String, ReportFile> mf = tf.getListFiles();
					Collection<ReportFile> fc = mf.values();

					for (ReportFile rf : tf.getListFiles().values()) {
						String fileName = rf.getName();
						if (renameFiles != null && renameFiles.length > 0) {
							if (FileFilter.maskFilter(renameFiles[0], fileName.toLowerCase())) {
								fileName = fileName.toLowerCase().replaceAll(renameFiles[1], renameFiles[2]);
							}
						}
						File rfFile = new File(FileUtils.tmpDir + fileName);
						tmpFile = new File(archivePath + "\\" + fileName);
						tmpFile.mkdirs();
						if (tmpFile.exists()) {
							tmpFile.delete();
						}
						tmpFile.createNewFile();
						FileUtils.copy(rfFile, tmpFile);
						rfFile.delete();
					}
				} else {
					// TODO: Error, warning etc.
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new ReportError("Ошибка при копировании исходных файлов в архив");
		}
	}

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

	public void executeStepSignatura(ProcessStep step) {
		if (Constants.ACTIONS[0].equals(step.getAction().getName())) {
			// Do nothing
		} else if (Constants.ACTIONS[1].equals(step.getAction().getName())) {
			// encrypt
			signatura.initConfig(step.getKey().getData());
			signatura.setParameters();
			signatura.encryptFilesInPath(FileUtils.tmpDir, step.getKey().getData(), step);
			signatura.unload();
		} else if (Constants.ACTIONS[2].equals(step.getAction().getName())) {
			// sign
			signatura.initConfig(step.getKey().getData());
			signatura.setSignParameters();
			signatura.signFilesInPath(FileUtils.tmpDir, step);
			signatura.unload();
		} else if (Constants.ACTIONS[3].equals(step.getAction().getName())) {
			// decrypt
			signatura.initConfig(step.getKey().getData());
			signatura.setParameters();
			signatura.decryptFilesInPath(FileUtils.tmpDir, step.getData());
			signatura.unload();
		} else if (Constants.ACTIONS[4].equals(step.getAction().getName())) {
			// verify and unsign
			signatura.initConfig(step.getKey().getData());
			signatura.setParameters();
			signatura.verifyAndUnsignFilesInPath(FileUtils.tmpDir, step.getData());
			signatura.unload();
		} else if (Constants.ACTIONS[5].equals(step.getAction().getName())) {
			// post??
		} else if (Constants.ACTIONS[6].equals(step.getAction().getName())) {
			Runtime r = Runtime.getRuntime();
			Process p = null;
			boolean loop = true;
			int i = 0;
			ObservableList<TransportFile> otf = dao.getArchiveFiles(report, direction);
			if (otf != null)
				for (TransportFile f : otf) {
					if (f.getDatetime().toLocalDate().isEqual(LocalDate.now())) {
						i++;
					}
				}
			ObservableList<String> fileList = FileUtils.getDirContentByMask(FileUtils.tmpDir,
					step.getData());
			ObservableList<String> doneList = FXCollections.observableArrayList();
			try {
				while (loop) {
					i++;
					ObservableList<String> fileListTmp = FXCollections.observableArrayList();
					for (String f : fileList) {
						if (!doneList.contains(f.toLowerCase()))
							fileListTmp.add(f.toLowerCase());
					}
					long fileSize = 0;
					String command = FileUtils.exeDir + "arj.exe a -e ";// +
																		// FileUtils.tmpDir+step.getAction().getData();

					String pattern = direction
							? report.getTransportInPattern().getMask().replaceAll("%date",
									DateUtils.formatReport(LocalDate.now()))
							: report.getTransportOutPattern().getMask().replaceAll("%date",
									DateUtils.formatReport(LocalDate.now()));
					if (i < 10) {
						pattern = pattern.replaceAll("%n", i + "").replaceAll("%", "0");
					} else if (i < 100) {
						pattern = pattern.replaceAll("%%n", i + "").replaceAll("%", "0");
					} else {
						pattern = pattern.replaceAll("%%%n", i + "").replaceAll("%", "0");
					}
					command += FileUtils.tmpDir + pattern + " " + FileUtils.tmpDir;

					// Add files to transport arch, if summary filesize doesn't
					// greater than constant
					int col = 0;
					HashMap<String, ReportFile> toLog = new HashMap<String, ReportFile>();

					for (String filename : fileListTmp) {
						filename=filename.toLowerCase();
						File tmpFile = new File(FileUtils.tmpDir + filename);
						fileSize += tmpFile.length();
						if (fileSize < Settings.FILE_SIZE && col < Settings.FILE_COUNT) {
							command += " " + filename;
							doneList.add(filename);

							if (renameFiles == null || !FileFilter.maskFilter(renameFiles[0],
									filename.replaceAll(renameFiles[2], renameFiles[1]))){
								ReportFile rep=mapFiles.get(filename);
								toLog.put(filename, rep);
							}
							else {
								ReportFile rep=mapFiles
										.get(filename.replaceAll(renameFiles[2], renameFiles[1]));
								toLog.put(filename,rep);
							}
							loop = false;
							col++;
						} else {
							loop = true;
						}
					}
					transportFiles.put(pattern, new TransportFile(0, pattern, LocalDateTime.now(),
							report, direction, null, toLog));

					try {
						p = r.exec(command);

						InputStream is = p.getInputStream();
						int w = 0;
						while ((w = is.read()) != -1) {
							System.out.print((char) w);
						}
						System.out.println(p.waitFor());
					} catch (InterruptedException | IOException ie) {
						ie.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error!!!");
			}
			System.out.println("ARJ returned " + p.exitValue());

		} else if (Constants.ACTIONS[7].equals(step.getAction().getName())) {
			renameFiles = new String[3];
			renameFiles[0] = step.getData().substring(0, step.getData().indexOf(" > "));
			String ReplaceText = step.getData().substring(step.getData().indexOf(" > (") + 4,
					step.getData().lastIndexOf(")"));
			renameFiles[1] = ReplaceText.substring(0, ReplaceText.indexOf("|"));
			renameFiles[2] = ReplaceText.substring(ReplaceText.indexOf("|") + 1,
					ReplaceText.length());

			FileFilter filter = new FileFilter(renameFiles[0]);

			for (File file : files) {
				if (filter.accept(file, FileUtils.tmpDir + file.getName())) {
					File rFile = new File(FileUtils.tmpDir + file.getName().toLowerCase()
							.replaceAll(renameFiles[1], renameFiles[2]));
					file.renameTo(rFile);
				}
			}

			for (String f : filenames) {
				if (filter.accept(null, f)) {
					f.replaceAll(renameFiles[1], renameFiles[2]);
				}
			}

		} else if (Constants.ACTIONS[8].equals(step.getAction().getName())) {
			// Runtime r = Runtime.getRuntime();
			// Process p = null;
			for (String filename : FileUtils.getDirContentByMask(FileUtils.tmpDir,
					step.getData())) {

				/*
				 * String q = FileUtils.rarFullPath + " e " + FileUtils.tmpDir +
				 * filename + " " + FileUtils.tmpDir; p = r.exec(q);
				 * System.out.println(p.waitFor()); File file = new
				 * File(FileUtils.tmpDir + filename); if (file.exists()) {
				 * file.delete(); } File par = new File(FileUtils.tmpDir); for
				 * (String fname : par.list( (dir, name) ->
				 * !(name.contains(".arj") || name.contains(".ARJ")))) {
				 * transportFiles.get(filename).getListFiles().put(fname, new
				 * ReportFile(0, fname, LocalDateTime.now(), report, direction,
				 * null, null)); }
				 */
				List<File> tmpFiles = unrar(FileUtils.tmpDir + filename);
				for (File f : tmpFiles) {
					if (transportFiles.get(filename).getListFiles() == null) {
						transportFiles.get(filename)
								.setListFiles(new HashMap<String, ReportFile>());
					}
					transportFiles.get(filename).getListFiles().put(f.getName(), new ReportFile(0,
							f.getName(), LocalDateTime.now(), report, direction, null, null));

				}
			}

		}

	}

	public void executeStep_old(ProcessStep step) throws FileNotFoundException, IOException {
		if (Constants.ACTIONS[0].equals(step.getAction().getName())) {
			script += "TO ";
			script += step.getData();
			script += "\r\n";
			useScript = true;
		} else if (Constants.ACTIONS[1].equals(step.getAction().getName())) {
			script += "CRYPT ";
			script += FileUtils.tmpDir + step.getData();
			script += "\r\n";
			script += "START\r\n";
			useScript = true;
		} else if (Constants.ACTIONS[2].equals(step.getAction().getName())) {
			script += "SIGN ";
			script += FileUtils.tmpDir + step.getData();
			script += "\r\n";
			useScript = true;
			script += "START\r\nEXIT\r\n";
		} else if (Constants.ACTIONS[3].equals(step.getAction().getName())) {
			script += "DECRYPT ";
			script += FileUtils.tmpDir + step.getData();
			script += "\r\n";
			useScript = true;
			script += "START\r\nEXIT\r\n";
		} else if (Constants.ACTIONS[4].equals(step.getAction().getName())) {
			script += "UNSIGN ";
			script += FileUtils.tmpDir + step.getData();
			script += "\r\n";
			useScript = true;
			script += "START\r\nEXIT\r\n";
		} else if (Constants.ACTIONS[5].equals(step.getAction().getName())) {

		} else if (Constants.ACTIONS[6].equals(step.getAction().getName())) {
			Runtime r = Runtime.getRuntime();
			Process p = null;
			boolean loop = true;
			int i = 0;
			ObservableList<TransportFile> otf = dao.getArchiveFiles(report, direction);
			if (otf != null)
				for (TransportFile f : otf) {
					if (f.getDatetime().toLocalDate().isEqual(LocalDate.now())) {
						i++;
					}
				}
			ObservableList<String> fileList = FileUtils.getDirContentByMask(FileUtils.tmpDir,
					step.getData());
			ObservableList<String> doneList = FXCollections.observableArrayList();
			try {
				while (loop) {
					i++;
					ObservableList<String> fileListTmp = FXCollections.observableArrayList();
					for (String f : fileList) {
						if (!doneList.contains(f))
							fileListTmp.add(f);
					}
					long fileSize = 0;
					String command = FileUtils.exeDir + "arj.exe a -e ";// +
																		// FileUtils.tmpDir+step.getAction().getData();

					String pattern = direction
							? report.getTransportInPattern().getMask().replaceAll("%date",
									DateUtils.formatReport(LocalDate.now()))
							: report.getTransportOutPattern().getMask().replaceAll("%date",
									DateUtils.formatReport(LocalDate.now()));
					if (i < 10) {
						pattern = pattern.replaceAll("%n", i + "").replaceAll("%", "0");
					} else if (i < 100) {
						pattern = pattern.replaceAll("%%n", i + "").replaceAll("%", "0");
					} else if (i < 1000) {
						pattern = pattern.replaceAll("%%%%n", i + "").replaceAll("%", "0");
					}
					command += FileUtils.tmpDir + pattern + " " + FileUtils.tmpDir;

					// Add files to transport arch, if summary filesize doesn't
					// greater than constant
					int col = 0;
					HashMap<String, ReportFile> toLog = new HashMap<String, ReportFile>();
					for (String filename : fileListTmp) {
						File tmpFile = new File(FileUtils.tmpDir + filename);
						fileSize += tmpFile.length();
						if (fileSize < Settings.FILE_SIZE && col < Settings.FILE_COUNT) {
							command += " " + filename;
							doneList.add(filename);
							if (renameFiles == null || FileFilter.maskFilter(renameFiles[0],
									filename.replaceAll(renameFiles[2], renameFiles[1])))
								toLog.put(filename, mapFiles.get(filename));
							else
								toLog.put(filename.replaceAll(renameFiles[2], renameFiles[1]),
										mapFiles.get(filename));
							loop = false;
							col++;
						} else {
							loop = true;
						}
					}
					transportFiles.put(pattern, new TransportFile(0, pattern, LocalDateTime.now(),
							report, direction, null, toLog));

					try {
						p = r.exec(command);

						InputStream is = p.getInputStream();
						int w = 0;
						while ((w = is.read()) != -1) {
							System.out.print((char) w);
						}
						System.out.println(p.waitFor());
					} catch (InterruptedException | IOException ie) {
						ie.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error!!!");
			}
			System.out.println("ARJ returned " + p.exitValue());

		} else if (Constants.ACTIONS[7].equals(step.getAction().getName())) {
			renameFiles = step.getData().split("|");
			for (File file : files) {
				file.renameTo(new File(file.getName().replaceAll(renameFiles[0], renameFiles[1])));
			}

		} else if (Constants.ACTIONS[8].equals(step.getAction().getName())) {
			// Runtime r = Runtime.getRuntime();
			// Process p = null;
			for (String filename : FileUtils.getDirContentByMask(FileUtils.tmpDir,
					step.getData())) {

				/*
				 * String q = FileUtils.rarFullPath + " e " + FileUtils.tmpDir +
				 * filename + " " + FileUtils.tmpDir; p = r.exec(q);
				 * System.out.println(p.waitFor()); File file = new
				 * File(FileUtils.tmpDir + filename); if (file.exists()) {
				 * file.delete(); } File par = new File(FileUtils.tmpDir); for
				 * (String fname : par.list( (dir, name) ->
				 * !(name.contains(".arj") || name.contains(".ARJ")))) {
				 * transportFiles.get(filename).getListFiles().put(fname, new
				 * ReportFile(0, fname, LocalDateTime.now(), report, direction,
				 * null, null)); }
				 */
				List<File> tmpFiles = unrar(FileUtils.tmpDir + filename);
				for (File f : tmpFiles) {
					if (transportFiles.get(filename).getListFiles() == null) {
						transportFiles.get(filename)
								.setListFiles(new HashMap<String, ReportFile>());
					}
					transportFiles.get(filename).getListFiles().put(f.getName(), new ReportFile(0,
							f.getName(), LocalDateTime.now(), report, direction, null, null));
				}
			}

		}
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

}
