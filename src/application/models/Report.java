package application.models;

import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Report {

	private IntegerProperty id;
	private StringProperty name;
	private ListProperty<FileType> patternOut;
	private ListProperty<FileType> patternIn;
	private ObjectProperty<FileType> transportOutPattern;
	private ObjectProperty<FileType> transportInPattern;
	private ListProperty<FileType> tickets;
	private StringProperty pathOut;
	private StringProperty pathIn;
	private StringProperty pathArchiveOut;
	private StringProperty pathArchiveIn;
	private StringProperty pathOutputOut;
	private StringProperty pathOutputIn;

	public Report() {
		this(null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public Report(Integer id, String name, List<FileType> patternOut, List<FileType> patternIn,
			FileType transportPatternOut, FileType transportPatternIn, String pathOut,
			String pathIn, String pathArchiveOut, String pathArchiveIn, String pathOutputOut,
			String pathOutputIn, ObservableList<FileType> tickets) {
		this.id = new SimpleIntegerProperty();
		this.id.setValue(id);
		this.name = new SimpleStringProperty(name);
		this.patternIn = new SimpleListProperty<FileType>();
		if (patternIn != null)
			this.patternIn.addAll(patternIn);
		this.patternOut = new SimpleListProperty<FileType>();
		if (patternOut != null) {
			this.patternOut.addAll(patternOut);
		}
		this.transportOutPattern = new SimpleObjectProperty<FileType>(transportPatternOut);
		this.transportInPattern = new SimpleObjectProperty<FileType>(transportPatternIn);
		this.pathOut = new SimpleStringProperty(pathOut);
		this.pathIn = new SimpleStringProperty(pathIn);
		this.pathArchiveIn = new SimpleStringProperty(pathArchiveIn);
		this.pathArchiveOut = new SimpleStringProperty(pathArchiveOut);
		this.pathOutputIn = new SimpleStringProperty(pathOutputIn);
		this.pathOutputOut = new SimpleStringProperty(pathOutputOut);
		this.tickets = new SimpleListProperty<FileType>();
		if (tickets != null)
			this.tickets.set(tickets);
	}

	/**
	 * @return the id
	 */
	public IntegerProperty getIdProperty() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setIdProperty(IntegerProperty id) {
		this.id = id;
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

	public Integer getId() {
		return id.get();
	}

	public void setId(Integer id) {
		this.id.set(id);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public ListProperty<FileType> getPatternInProperty() {
		return patternIn;
	}

	public void setPatternInProperty(ListProperty<FileType> patternIn) {
		this.patternIn = patternIn;
	}

	public ListProperty<FileType> getPatternOutProperty() {
		return patternOut;
	}

	public void setPatternOutProperty(ListProperty<FileType> patternOut) {
		this.patternOut = patternOut;
	}

	public ObservableList<FileType> getPatternIn() {
		return patternIn.get();
	}

	public void setPatternIn(ObservableList<FileType> patternIn) {
		this.patternIn.set(patternIn);
	}

	public ObservableList<FileType> getPatternOut() {
		return patternOut.get();
	}

	public void setPatternOut(ObservableList<FileType> patternOut) {
		this.patternOut.set(patternOut);
	}

	public FileType getTransportInPattern() {
		return transportInPattern.get();
	}

	public void setTransportInPattern(FileType patternIn) {
		this.transportInPattern.set(patternIn);
	}

	public ObjectProperty<FileType> getTransportInPatternProperty() {
		return transportInPattern;
	}

	public void setTransportInPetternProperty(ObjectProperty<FileType> patternInProperty) {
		this.transportInPattern = patternInProperty;
	}

	public FileType getTransportOutPattern() {
		return transportOutPattern.get();
	}

	public void setTransportOutPattern(FileType patternOut) {
		this.transportOutPattern.set(patternOut);
	}

	public ObjectProperty<FileType> getTransportOutPatternProperty() {
		return transportOutPattern;
	}

	public ObservableList<FileType> getTickets() {
		return tickets;
	}

	public void setTickets(ObservableList<FileType> tickets) {
		this.tickets.set(tickets);
	}

	public ListProperty<FileType> getTicketsProperty() {
		return tickets;
	}

	public void setTicketsProperty(ListProperty<FileType> tickets) {
		this.tickets = tickets;
	}

	public void setTransportOutPetternProperty(ObjectProperty<FileType> patternProperty) {
		this.transportOutPattern = patternProperty;
	}

	public StringProperty getPathOutProperty() {
		return pathOut;
	}

	public void setPathOutProperty(StringProperty pathOut) {
		this.pathOut = pathOut;
	}

	public String getPathOut() {
		return pathOut.get();
	}

	public void setPathOut(String pathOut) {
		this.pathOut.set(pathOut);
	}

	public StringProperty getPathInProperty() {
		return pathIn;
	}

	public void setPathInProperty(StringProperty pathIn) {
		this.pathIn = pathIn;
	}

	public String getPathIn() {
		return pathIn.get();
	}

	public void setPathIn(String pathIn) {
		this.pathIn.set(pathIn);
	}

	public StringProperty getPathArchiveOutProperty() {
		return pathArchiveOut;
	}

	public void setPathArchiveOutProperty(StringProperty pathArchiveOut) {
		this.pathArchiveOut = pathArchiveOut;
	}

	public String getPathArchiveOut() {
		return pathArchiveOut.get();
	}

	public void setPathArchiveOut(String pathArchiveOut) {
		this.pathArchiveOut.set(pathArchiveOut);
	}

	public StringProperty getPathArchiveInProperty() {
		return pathArchiveIn;
	}

	public void setPathArchiveInProperty(StringProperty pathArchiveIn) {
		this.pathArchiveIn = pathArchiveIn;
	}

	public String getPathArchiveIn() {
		return pathArchiveIn.get();
	}

	public void setPathArchiveIn(String pathArchiveIn) {
		this.pathArchiveIn.set(pathArchiveIn);
	}

	public StringProperty getPathOutputInProperty() {
		return pathOutputIn;
	}

	public void setPathOutputInProperty(StringProperty pathOutputIn) {
		this.pathOutputIn = pathOutputIn;
	}

	public String getPathOutputIn() {
		return pathOutputIn.get();
	}

	public void setPathOutputIn(String pathOutputIn) {
		this.pathOutputIn.set(pathOutputIn);
	}

	public StringProperty getPathOutputOutProperty() {
		return pathOutputOut;
	}

	public void setPathOutputOutProperty(StringProperty pathOutputOut) {
		this.pathOutputOut = pathOutputOut;
	}

	public String getPathOutputOut() {
		return pathOutputOut.get();
	}

	public void setPathOutputOut(String pathOutputOut) {
		this.pathOutputOut.set(pathOutputOut);
	}

	@Override
	public String toString() {
		return name.get();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Report)
			return ((Report) obj).getId() == id.get();
		else
			return false;
	}

}
