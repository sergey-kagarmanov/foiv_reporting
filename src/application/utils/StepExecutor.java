package application.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;

import application.MainApp;
import application.errors.ReportError;
import application.models.ProcessStep;
import application.models.Report;
import application.models.WorkingFile;
import application.utils.skzi.DecryptorHandler;
import application.utils.skzi.EncryptorHandler;
import application.utils.skzi.LocalSignatura;
import application.utils.skzi.SignHandler;
import application.utils.skzi.SignaturaHandler;
import application.utils.skzi.UnsignHandler;
import application.view.controls.ArchiveNameDialogBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class StepExecutor {

	private ProcessStep step;
	private Report report;

	public StepExecutor(ProcessStep step, Report report) {
		this.step = step;
		this.report = report;
	}

	public ObservableList<WorkingFile> execute(ObservableList<WorkingFile> files) throws IOException, ReportError {
		ObservableList<WorkingFile> result = FXCollections.observableArrayList();
		ObservableList<WorkingFile> stepFiles = FXCollections.observableArrayList();
		ObservableList<WorkingFile> nonWork = FXCollections.observableArrayList();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		boolean signaturaAction = false;
		List<SignaturaHandler> handlers = new ArrayList<>();
		
		LocalSignatura.initSignatura(step.getKey().getData());

		if (step.getData() != null && !Constants.RENAME.equals(step.getAction().getName()) && !Constants.COPY.equals(step.getAction().getName())
				&& !Constants.PACK.equals(step.getAction().getName())) {
			for (WorkingFile file : files) {
				if (Pattern.matches(step.getData(), file.getName())) {
					stepFiles.add(file);
				} else {
					nonWork.add(file);
				}
			}
		} else {
			stepFiles = files;
		}

		switch (step.getAction().getName()) {
		case Constants.SIGN:
			signaturaAction = true;
			stepFiles.forEach(file -> {
				SignaturaHandler handler = new SignHandler(step.getKey());
				handler.setParameters(file);
				handlers.add(handler);
			});
			break;
		case Constants.UNSIGN:
			signaturaAction = true;
			stepFiles.forEach(file -> {
				SignaturaHandler handler = new UnsignHandler(step.getKey());
				handler.setParameters(file);
				handlers.add(handler);
			});
			break;
		case Constants.ENCRYPT:
			signaturaAction = true;
			stepFiles.forEach(file -> {
				SignaturaHandler handler = new EncryptorHandler(step.getKey());
				handler.setParameters(file);
				handlers.add(handler);
			});
			break;
		case Constants.DECRYPT:
			signaturaAction = true;
			stepFiles.forEach(file -> {
				SignaturaHandler handler = new DecryptorHandler(step.getKey());
				handler.setParameters(file);
				handlers.add(handler);
			});
			break;
		case Constants.PACK:
			result = createArchive(report, files);
			break;
		case Constants.UNPACK:
			result = unpack(stepFiles);
			break;
		case Constants.RENAME:
			stepFiles.forEach(file -> {
				replace(step.getData(), file);
			});
			result = files;
			break;
		case Constants.COPY:
			stepFiles.forEach(file -> {
				File newfile = new File(step.getData() + "\\" + file.getName());
				if(newfile.exists()) {
					newfile.delete();
				}
				try {
					new File(step.getData()).mkdirs();
					newfile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				InputStream in = FileUtils.getStreamWithSaveData(file);
				try (FileOutputStream fos = new FileOutputStream(newfile)) {
					byte[] buffer = new byte[1024];
					int length = 0;
					while ((length = in.read(buffer)) > 0) {
						fos.write(buffer, 0, length);
					}
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			result = stepFiles;
		}
		if (signaturaAction) {
			try {
				List<Future<WorkingFile>> futures = executor.invokeAll(handlers);
				for (Future<WorkingFile> future : futures) {
					try {
						if (future != null)
							result.add(future.get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.shutdown();
		}
		
		if (nonWork.size() > 0) {
			result.addAll(nonWork);
		}

		return result;
	}

	private void replace(String data, WorkingFile file) {
		String[] renameFiles = new String[3];
		renameFiles[0] = data.substring(0, data.indexOf(" > "));
		String ReplaceText = data.substring(data.indexOf(" > (") + 4, data.lastIndexOf(")"));
		renameFiles[1] = ReplaceText.substring(0, ReplaceText.indexOf("|"));
		renameFiles[2] = ReplaceText.substring(ReplaceText.indexOf("|") + 1, ReplaceText.length());

		if (Pattern.compile(renameFiles[0]).matcher(file.getName()).matches()) {
			file.setName(file.getName().replaceAll(renameFiles[1], renameFiles[2]));
		}
	}

	/**
	 * create archive - transport file
	 * 
	 * @throws ReportError
	 */
	private ObservableList<WorkingFile> createArchive(Report report, ObservableList<WorkingFile> files) throws ReportError {
		ObservableList<WorkingFile> resultList = FXCollections.observableArrayList();
		Runtime r = Runtime.getRuntime();
		Process p = null;
		/**
		 * Tmp dir for create archive, cause arj have only 64 params
		 */
		File tmpDir = new File(FileUtils.tmpDir + "arj\\");
		tmpDir.mkdirs();

		boolean loop = true;
		int countperDay = 0;

		String lastName = MainApp.getDb().getLastArchiveToday(report);
		String pattern = report.getTransportOutPattern().getMask();
		pattern = pattern.replaceAll("%date", DateUtils.formatReport(LocalDate.now()));
		pattern = pattern.replaceAll("%dd", LocalDate.now().format(DateTimeFormatter.ofPattern("dd")));
		pattern = pattern.replaceAll("%MM", LocalDate.now().format(DateTimeFormatter.ofPattern("MM")));
		pattern = pattern.replaceAll("%yy", LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));

		if (lastName != null) {
			lastName = lastName.replaceFirst(pattern.substring(0, pattern.indexOf("%")), "");
			lastName = lastName.replace(lastName.substring(lastName.indexOf(".")), "");
			countperDay = Integer.parseInt(lastName);
		}
		ObservableList<WorkingFile> doneList = FXCollections.observableArrayList();
		Stack<WorkingFile> pool = new Stack<>();
		for (WorkingFile f : files) {
			if (f.getExceptions() == null || f.getExceptions()
					.size() == 0/* && !doneList.contains(f) */)
				pool.push(f);
		}
		try {
			boolean multiVolumes = false;

			while (!pool.isEmpty()) {
				countperDay++;
				String command = FileUtils.exeDir + "arj.exe a -e ";
				String multiCommand = FileUtils.exeDir + "arj.exe A -V5000k -Y -E ";

				// create new filename start

				if (countperDay < 10) {
					pattern = pattern.replaceAll("%n", countperDay + "").replaceAll("%", "0");
				} else if (countperDay < 100) {
					pattern = pattern.replaceAll("%%n", countperDay + "").replaceAll("%", "0");
				} else {
					pattern = pattern.replaceAll("%%%n", countperDay + "").replaceAll("%", "0");
				}
				pattern = ArchiveNameDialogBox.display(pattern);
				// end

				command += FileUtils.tmpDir + "arj\\" + pattern + " " + FileUtils.tmpDir + "arj\\*";
				multiCommand += FileUtils.tmpDir + "arj\\" + pattern + " " + FileUtils.tmpDir + "arj\\*";

				int numberPart = 1; // If filesize more than limit, this is
									// numbers of multivolume archive

				int col = 0;

				ObservableList<WorkingFile> transportFiles = FXCollections.observableArrayList();

				WorkingFile currentFile = null;
				long fileSize = 0;
				loop = true;
				while (loop && !pool.isEmpty()) {
					// for (WorkingFile currentFile : fileListTmp) {
					currentFile = pool.pop();
					fileSize += currentFile.getData().length;

					/**
					 * Check if one file more than file
					 */

					if (fileSize < Settings.FILE_SIZE && col < Settings.FILE_COUNT) {
						currentFile.saveData(FileUtils.tmpDir + "arj\\");
						doneList.add(currentFile);

						loop = true;
						multiVolumes = false;
						col++;

					} else if (currentFile.getData().length > Settings.FILE_SIZE && (fileSize - currentFile.getData().length == 0)) {
						multiVolumes = true;
						loop = false;
						currentFile.saveData(FileUtils.tmpDir + "arj\\");

						doneList.add(currentFile);
						col++;
						// calculate number archive parts of one big file
						numberPart = (int) Math.ceil((currentFile.getData().length * 10) / Settings.FILE_SIZE / 10.0);
					} else {
						loop = false;

					}

				}

				if (multiVolumes) {
					for (int k = 1; k < numberPart; k++) {
						String localPattern = pattern.replaceAll("\\.(arj|ARJ)", ".a0" + k);

						WorkingFile tmpFile = new WorkingFile(WorkingFile.NEW);
						tmpFile.setOriginalName(localPattern);
						tmpFile.setType(MainApp.getDb().getFileType(report.getId(), Constants.OUTPUT_INT, 1));
						tmpFile.setChilds(doneList);
						transportFiles.add(tmpFile);
					}
				} else {
					WorkingFile tmpFile = new WorkingFile(WorkingFile.NEW);
					tmpFile.setOriginalName(pattern);
					tmpFile.setType(MainApp.getDb().getFileType(report.getId(), Constants.OUTPUT_INT, 1));
					tmpFile.setChilds(doneList);
					transportFiles.add(tmpFile);
				}

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

					transportFiles.forEach(file -> {
						try {
							file.readData(FileUtils.tmpDir + "arj");
							new File(FileUtils.tmpDir + "arj//" + file.getOriginalName()).delete();
						} catch (ReportError e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});

					resultList.addAll(transportFiles);
					doneList.forEach(file -> {
						new File(FileUtils.tmpDir + "arj//" + file.getName()).delete();
					});
					doneList = FXCollections.observableArrayList();
					transportFiles.clear();

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
		return resultList;
	}

	private ObservableList<WorkingFile> unpack(ObservableList<WorkingFile> files) {
		ObservableList<WorkingFile> resultList = FXCollections.observableArrayList();

		files.forEach(file -> {
			IInArchive inArchive = null;
			try {
				file.saveData(FileUtils.tmpDir);
				inArchive = SevenZip.openInArchive(null, // autodetect archive
															// type
						new RandomAccessFileInStream(new RandomAccessFile(new File(FileUtils.tmpDir + "\\" + file.getName()), "r")));
				ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

				ObservableList<WorkingFile> localFiles = FXCollections.observableArrayList();

				for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
					if (!item.isFolder()) {
						WorkingFile tmp = new WorkingFile(WorkingFile.NEW);
						tmp.setOriginalName(item.getPath());
						tmp.setData(ArchieveInputStreamHandler.slurpByte(new ArchieveInputStreamHandler(item).getInputStream()));
						localFiles.add(tmp);
					}
				}
				file.setChilds(localFiles);
				resultList.addAll(localFiles);

			} catch (Exception e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			} finally {
				if (inArchive != null) {
					try {
						inArchive.close();
					} catch (SevenZipException e) {
						System.err.println("Error closing archive: " + e.getMessage());
					}
				}
				new File(FileUtils.tmpDir + "\\" + file.getName()).delete();
			}
		});

		return files;
	}

	private ObservableList<WorkingFile> unpackCommon(ObservableList<WorkingFile> files) {
		files.forEach(file -> {
			ObservableList<WorkingFile> children = FXCollections.observableArrayList();
			try (ArjArchiveInputStream i = new ArjArchiveInputStream(new ByteArrayInputStream(file.getData()))) {
				ArjArchiveEntry entry = null;
				while ((entry = i.getNextEntry()) != null) {
					if (!i.canReadEntryData(entry)) {
						// log something?

						continue;
					}
					WorkingFile wFile = new WorkingFile(WorkingFile.NEW);
					wFile.setOriginalName(entry.getName());
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					long last = entry.getSize();
					int len = (int) ((last - 1024) > 0 ? 1024 : (1024 - last));
					int lenRead = 0;
					while (len > 0) {
						lenRead = i.read(buffer, 0, len);
						if (len == lenRead)
							baos.write(buffer, 0, len);
					}
					wFile.setData(baos.toByteArray());
					wFile.setDatetime(LocalDateTime.now());
					file.setChilds(children);
				}
			} catch (IOException | ArchiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return files;
	}
}
