package application.models;

import java.io.File;

import application.errors.ReportError;
import application.utils.FileUtils;
import application.utils.HashCodeUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileTransforming {

	private File original;
	private File current;
	private File signed;
	private ObservableList<String> transform;
	private String inPath;
	private Integer errorCode;

	public FileTransforming() {
		this(null, null);
	}

	public FileTransforming(String original, String inPath) {
		//this.current = new File(FileUtils.tmpDir + original);
		this.inPath = inPath;
		this.original = new File(inPath +"\\"+ original);
		this.current = this.original;
		this.transform = FXCollections.observableArrayList();
		transform.add(original);
		errorCode = 0;
	}

	public FileTransforming(String original, String current, String inPath) {
		this.current = new File(FileUtils.tmpDir + current);
		this.original = new File(inPath + original);
		this.transform = FXCollections.observableArrayList();
		transform.add(original);
		transform.add(current);
		errorCode = 0;
	}

	public File getSigned() {
		return signed;
	}
	
	public void setSigned(File file) {
		this.signed = file;
	}
	
	public boolean copySigned(String dir) {
		if (getCurrentFile().exists()) {
			File tmpFile = new File(dir + "\\" + original.getName());
			tmpFile.mkdirs();
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
			
			//if it use unattached signature, this block is to proceed copying signature
			if (signed!=null)
				FileUtils.copy(signed, tmpFile);
			else
				FileUtils.copy(original, tmpFile);
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return the errorCode
	 */
	public Integer getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 *            the errorCode to set
	 */
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the original
	 */
	public String getOriginal() {
		return original.getName();
	}

	/**
	 * @param original
	 *            the original to set
	 * @throws ReportError
	 */
	public void setOriginal(String original) throws ReportError {
		if (inPath != null && !"".equals(inPath)) {
			this.original = new File(inPath + "\\" + original);
			if (!this.original.exists()) {
				throw new ReportError(
						"Ошибка при установке оригинального файла, файл не существует");
			}
		} else
			throw new ReportError(
					"Ошибка при установке оригинального файла, входной каталог не задан");
	}

	/**
	 * @return the current
	 */
	public String getCurrent() {
		return current.getName();
	}

	/**
	 * @param current
	 *            the current to set
	 */
	public void setCurrent(String current) {
		File newFile = new File(current);
		this.current.renameTo(newFile);
		this.current = newFile;
		transform.add(current);
	}

	/**
	 * @return the transform
	 */
	public ObservableList<String> getTransform() {
		return transform;
	}

	/**
	 * @param transform
	 *            the transform to set
	 */
	public void setTransform(ObservableList<String> transform) {
		this.transform = transform;
	}

	public String getCommonOriginal() {
		return original.getName().toLowerCase();
	}

	public String getCommonCurrent() {
		return current.getName().toLowerCase();
	}

	public File getOriginalFile() {
		return original;
	}

	public File getCurrentFile() {
		return current;
	}

	/**
	 * Copy current file to dir, if changePointer is true, it also change current to that file
	 * @param dir
	 * @param changePointer
	 * @return
	 */
	public boolean copyCurrent(String dir, boolean changePointer) {
		if (getCurrentFile().exists()) {
			File tmpFile = new File(dir + "\\" + current.getName());
			tmpFile.mkdirs();
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
			FileUtils.copy(getCurrentFile(), tmpFile);
			if (changePointer) {
				setCurrent(tmpFile.getAbsolutePath());
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof FileTransforming)
			return original.getName().equals(((FileTransforming) obj).original.getName());
		else
			return false;
	}

	@Override
	public int hashCode() {
		return HashCodeUtil.hash(HashCodeUtil.SEED, original.getName());
	}

	@Override
	public String toString() {
		return original.getName();
	}
	
	public static ObservableList<FileTransforming> toFileTransforming(ObservableList<File> files){
		ObservableList<FileTransforming> result = FXCollections.observableArrayList();
		for(File f: files) {
			result.add(new FileTransforming(f.getName(), f.getParent()));
		}
		return result;
	}
}
