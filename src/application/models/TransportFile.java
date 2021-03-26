package application.models;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

@Deprecated
public class TransportFile extends FileEntity {

	private ObjectProperty<Map<String, ReportFile>> listFiles;

	public TransportFile(UUID uuid, String name, LocalDateTime datetime, Report report,
			Boolean direction, FileEntity linkedFile, Map<String, ReportFile> listFiles, FileType fileType) {
		super(uuid, name, datetime, report, direction, linkedFile, fileType);
		this.listFiles = new SimpleObjectProperty<>(listFiles);
	}

	public TransportFile(UUID uuid, String name, LocalDateTime time) {
		super(uuid, name, time, null, null, null, null);
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
