package application.models;

import java.time.LocalDateTime;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TransportFile extends FileEntity {

	private ObjectProperty<Map<String, ReportFile>> listFiles;

	public TransportFile(Integer id, String name, LocalDateTime datetime, Report report,
			Boolean direction, FileEntity linkedFile, Map<String, ReportFile> listFiles) {
		super(id, name, datetime, report, direction, linkedFile);
		this.listFiles = new SimpleObjectProperty<>(listFiles);
	}

	public TransportFile() {
		super();
		this.listFiles = null;
	}

	/**
	 * @return the listFiles
	 */
	public Map<String, ReportFile> getListFiles() {
		return listFiles.get();
	}

	/**
	 * @param listFiles
	 *            the listFiles to set
	 */
	public void setListFiles(Map<String, ReportFile> listFiles) {
		this.listFiles.set(listFiles);
	}

	/**
	 * @return the listFiles
	 */
	public ObjectProperty<Map<String, ReportFile>> getListFilesProperty() {
		return listFiles;
	}

	/**
	 * @param listFiles
	 *            the listFiles to set
	 */
	public void setListFilesProperty(ObjectProperty<Map<String, ReportFile>> listFiles) {
		this.listFiles = listFiles;
	}

}
