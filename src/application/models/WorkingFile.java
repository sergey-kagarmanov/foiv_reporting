package application.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.MainApp;
import application.errors.ReportError;
import javafx.collections.ObservableList;

public class WorkingFile {
	private Integer id;
	private byte[] data;
	private String name;
	private String originalName;
	private FileType type;
	private List<Exception> exceptions;
	private Map<String, FileAttribute> attributes;
	private byte[] signData;
	private ObservableList<WorkingFile> childs;
	private byte[] hashData;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	
	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getOriginalName() {
		return originalName;
	}

	/**
	 * Setup original name and copy its value to name
	 * 
	 * @param originalName
	 */
	public void setOriginalName(String originalName) {
		this.originalName = originalName;
		this.name = originalName;
	}

	public FileType getType() {
		return type;
	}

	public void setType(FileType type) {
		this.type = type;
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<Exception> exceptions) {
		this.exceptions = exceptions;
	}

	public Map<String, FileAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, FileAttribute> attributes) {
		this.attributes = attributes;
	}

	public InputStream getStream() {
		return new ByteArrayInputStream(data);
	}

	public byte[] getSignData() {
		return signData;
	}

	public void setSignData(byte[] signData) {
		this.signData = signData;
	}

	public ObservableList<WorkingFile> getChilds() {
		return childs;
	}

	public void setChilds(ObservableList<WorkingFile> childs) {
		this.childs = childs;
	}

	public byte[] getHashData() {
		return hashData;
	}

	public void setHashData(byte[] hashData) {
		this.hashData = hashData;
	}

	public static List<WorkingFile> toWorking(List<File> files) {
		ExecutorService service = Executors.newWorkStealingPool();
		List<Callable<WorkingFile>> handlers = new ArrayList<>();
		files.forEach(file -> {
			handlers.add(new Callable<WorkingFile>() {

				@Override
				public WorkingFile call() throws Exception {
					FileInputStream fis = new FileInputStream(file);
					byte[] buffer = new byte[1024];
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int len = 0;
					while ((len = fis.read(buffer)) != -1) {
						baos.write(buffer, 0, len);
					}
					WorkingFile wFile = new WorkingFile();
					wFile.setData(baos.toByteArray());
					wFile.setName(file.getName());
					wFile.setOriginalName(file.getName());
					fis.close();
					return wFile;
				}
			});
		});
		List<Future<WorkingFile>> results = null;
		List<WorkingFile> out = new ArrayList<>();
		try {
			results = service.invokeAll(handlers);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		results.forEach(f -> {
			try {
				out.add(f.get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return out;
	}

	/**
	 * Flush data to disk in path with filename according to field name
	 * 
	 * @param path
	 * @throws ReportError
	 */
	public void saveData(String path) throws ReportError {
		FileOutputStream fis = null;
		ByteArrayInputStream bais = null;
		try {
			File f = new File(path + "\\" + name);
			f.mkdirs();
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			
			fis = new FileOutputStream(f);
			bais = new ByteArrayInputStream(data);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = bais.read(buffer)) > 0) {
				fis.write(buffer, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReportError(e.getMessage());
		} finally {
			try {
				fis.close();
				bais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Flush data to disk in path with filename according to field name
	 * 
	 * @param path
	 * @throws ReportError
	 */
	public void saveSignedData(String path) throws ReportError {
		FileOutputStream fis = null;
		ByteArrayInputStream bais = null;
		try {
			File f = new File(path + "\\" + name);
			f.mkdirs();
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			
			fis = new FileOutputStream(f);
			if (signData!=null && signData.length>0)
				bais = new ByteArrayInputStream(signData);
			else
				bais = new ByteArrayInputStream(data);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = bais.read(buffer)) > 0) {
				fis.write(buffer, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReportError(e.getMessage());
		} finally {
			try {
				fis.close();
				bais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read file to byte array from original name
	 * 
	 * @throws ReportError
	 */
	public void readData(String path) throws ReportError {
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		try {
			fis = new FileInputStream(new File(path + "\\" + originalName));
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = fis.read(buffer)) > 0) {
				baos.write(buffer, 0, length);
			}
			data = baos.toByteArray();
		} catch (IOException e) {
			MainApp.error(e.getMessage());
			throw new ReportError("Ошибка чтения файла " + path + "\\" + originalName);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void copyToSign() {
		signData = new byte[data.length];
		for(int i=0; i<data.length;i++) {
			signData[i] = data[i];
		}
		
	}

}
