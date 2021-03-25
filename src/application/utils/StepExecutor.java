package application.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import application.MainApp;
import application.errors.ReportError;
import application.models.ProcessStep;
import application.models.Report;
import application.models.TransportFile;
import application.models.WorkingFile;
import application.utils.skzi.DecryptorHandler;
import application.utils.skzi.EncryptorHandler;
import application.utils.skzi.SignHandler;
import application.utils.skzi.SignaturaHandler;
import application.utils.skzi.UnsignHandler;
import application.view.controls.ArchiveNameDialogBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

public class StepExecutor {

	private ProcessStep step;
	private Report report;

	public StepExecutor(ProcessStep step, Report report) {
		this.step = step;
		this.report = report;
	}

	public ObservableList<WorkingFile> execute(ObservableList<WorkingFile> files) throws IOException, ReportError {
		ObservableList<WorkingFile> result = FXCollections.observableArrayList();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		boolean signaturaAction = false;
		List<SignaturaHandler> handlers = new ArrayList<>();

		switch (step.getAction().getName()) {
		case Constants.SIGN:
			signaturaAction = true;
			files.forEach(file -> {
				SignaturaHandler handler = new SignHandler(step.getKey());
				handler.setParameters(file);
				handlers.add(handler);
			});
			break;
		case Constants.UNSIGN:
			signaturaAction = true;
			files.forEach(file -> {
				SignaturaHandler handler = new UnsignHandler(step.getKey());
				handler.setParameters(file);
				handlers.add(handler);
			});
			break;
		case Constants.ENCRYPT:
			signaturaAction = true;
			files.forEach(file -> {
				SignaturaHandler handler = new EncryptorHandler(step.getKey());
				handler.setParameters(file);
				handlers.add(handler);
			});
			break;
		case Constants.DECRYPT:
			signaturaAction = true;
			files.forEach(file -> {
				SignaturaHandler handler = new DecryptorHandler(step.getKey());
				handler.setParameters(file);
				handlers.add(handler);
			});
			break;
		case Constants.PACK:
			result = createArchive(report, files);
			break;
		case Constants.UNPACK:
			result = unpack(files);
			break;
		case Constants.RENAME:
			files.forEach(file -> {
				replace(step.getData(), file);
			});
			result = files;
			break;

		}
		if (signaturaAction) {
			try {
				List<Future<WorkingFile>> futures = executor.invokeAll(handlers);
				for(Future<WorkingFile> future : futures) {
					try {
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
		ObservableList<TransportFile> archiveTransportFiles = MainApp.getDb().getArchiveFiles(report, Constants.OUTPUT, LocalDate.now());
		if (archiveTransportFiles != null)
			for (TransportFile f : archiveTransportFiles) {
				if (f.getDatetime().toLocalDate().isEqual(LocalDate.now())) {
					countperDay++;
				}
			}

		ObservableList<WorkingFile> doneList = FXCollections.observableArrayList();
		Stack<WorkingFile> pool = new Stack<>();
		for (WorkingFile f : files) {
			if (f.getExceptions()==null || f.getExceptions()
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
				String pattern = report.getTransportOutPattern().getMask();
				pattern = pattern.replaceAll("%date", DateUtils.formatReport(LocalDate.now()));
				pattern = pattern.replaceAll("%dd", LocalDate.now().format(DateTimeFormatter.ofPattern("dd")));
				pattern = pattern.replaceAll("%MM", LocalDate.now().format(DateTimeFormatter.ofPattern("MM")));
				pattern = pattern.replaceAll("%yy", LocalDate.now().format(DateTimeFormatter.ofPattern("yy")));

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

						WorkingFile tmpFile = new WorkingFile();
						tmpFile.setOriginalName(localPattern);
						tmpFile.setType(MainApp.getDb().getFileType(report.getId(), Constants.OUTPUT_INT, 1));
						tmpFile.setChilds(doneList);
						transportFiles.add(tmpFile);
					}
				}else {
					WorkingFile tmpFile = new WorkingFile();
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
							file.readData(FileUtils.tmpDir + "arj\\");
						} catch (ReportError e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});

					resultList.addAll(transportFiles);
					doneList= FXCollections.observableArrayList();
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

	private ObservableList<WorkingFile> unpack(ObservableList<WorkingFile> files){
		ObservableList<WorkingFile> resultList = FXCollections.observableArrayList();
		
		files.forEach(file->{
			IInArchive inArchive = null;
			try {
				inArchive = SevenZip.openInArchive(null, // autodetect archive type
						new ByteArrayStream(file.getData(), false));
				ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

				ObservableList<WorkingFile> localFiles = FXCollections.observableArrayList();
				
				for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
					if (!item.isFolder()) {
						WorkingFile tmp = new WorkingFile();
						tmp.setOriginalName(item.getPath());
						tmp.setData(ArchieveInputStreamHandler.slurpByte(
								new ArchieveInputStreamHandler(item).getInputStream()));
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
			}
		});
		
		return resultList;
	}
}
