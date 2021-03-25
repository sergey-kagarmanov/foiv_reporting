package application.models;

import java.time.LocalDateTime;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileEntity {

	private ObjectProperty<UUID> uuid;
	private StringProperty name;
	private ObjectProperty<LocalDateTime> datetime;
	private ObjectProperty<Report> report;
	private BooleanProperty direction;
	private ObjectProperty<FileEntity> linkedFile;
	private ObjectProperty<FileType> fileType;


	public FileEntity(UUID uuid, String name, LocalDateTime datetime, Report report,
			Boolean direction, FileEntity linkedFile, FileType fileType) {
		this.uuid = new SimpleObjectProperty();
		this.uuid.setValue(uuid);
		this.name = new SimpleStringProperty(name);
		this.datetime = new SimpleObjectProperty<LocalDateTime>(datetime);
		this.report = new SimpleObjectProperty<Report>(report);
		this.direction = new SimpleBooleanProperty();
		this.direction.setValue(direction);
		this.linkedFile = new SimpleObjectProperty<FileEntity>(linkedFile);
		this.fileType = new SimpleObjectProperty<FileType>(fileType);
	}

	public FileEntity() {
		this(null, null, null, null, null, null, null);
	}

	/**
	 * @return the id
	 */
	public UUID getId() {
		return uuid.get();
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid.set(uuid);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name.get();
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return the datetime
	 */
	public LocalDateTime getDatetime() {
		return datetime.get();
	}

	/**
	 * @param datetime
	 *            the datetime to set
	 */
	public void setDatetime(LocalDateTime datetime) {
		this.datetime.set(datetime);
	}

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report.get();
	}

	/**
	 * @param report
	 *            the report to set
	 */
	public void setReport(Report report) {
		this.report.set(report);
	}

	/**
	 * @return the direction
	 */
	public Boolean getDirection() {
		return direction.get();
	}

	/**
	 * @param direction
	 *            the direction to set
	 */
	public void setDirection(Boolean direction) {
		this.direction.set(direction);
	}

	/**
	 * @return the linkedFile
	 */
	public FileEntity getLinkedFile() {
		return linkedFile.get();
	}

	/**
	 * @param linkedFile
	 *            the linkedFile to set
	 */
	public void setLinkedFile(FileEntity linkedFile) {
		this.linkedFile.set(linkedFile);
	}

	/**
	 * @return the id
	 */
	public ObjectProperty<UUID> getIdProperty() {
		return uuid;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setUuidProperty(ObjectProperty<UUID> uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the name
	 */
	public StringProperty getNameProperty() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setNameProperty(StringProperty name) {
		this.name = name;
	}

	/**
	 * @return the datetime
	 */
	public ObjectProperty<LocalDateTime> getDatetimeProperty() {
		return datetime;
	}

	/**
	 * @param datetime
	 *            the datetime to set
	 */
	public void setDatetimeProperty(ObjectProperty<LocalDateTime> datetime) {
		this.datetime = datetime;
	}

	/**
	 * @return the report
	 */
	public ObjectProperty<Report> getReportProperty() {
		return report;
	}

	/**
	 * @param report
	 *            the report to set
	 */
	public void setReportProperty(ObjectProperty<Report> report) {
		this.report = report;
	}

	/**
	 * @return the direction
	 */
	public BooleanProperty getDirectionProperty() {
		return direction;
	}

	/**
	 * @param direction
	 *            the direction to set
	 */
	public void setDirectionProperty(BooleanProperty direction) {
		this.direction = direction;
	}

	/**
	 * @return the linkedFile
	 */
	public ObjectProperty<FileEntity> getLinkedFileProperty() {
		return linkedFile;
	}

	/**
	 * @param linkedFile
	 *            the linkedFile to set
	 */
	public void setLinkedFileProperty(ObjectProperty<FileEntity> linkedFile) {
		this.linkedFile = linkedFile;
	}

	/**
	 * @return the fileType
	 */
	public FileType getFileType() {
		return fileType.get();
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(FileType fileType) {
		this.fileType = new SimpleObjectProperty<FileType>(fileType);
	}

	/**
	 * @return the fileType
	 */
	public ObjectProperty<FileType> getFileTypeProperty() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileTypeProperty(ObjectProperty<FileType> fileType) {
		this.fileType = fileType;
	}

}
