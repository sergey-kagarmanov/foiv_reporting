package application.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Chain{

	private IntegerProperty id;
	private StringProperty name;
	private ObjectProperty<Report> report;
	private BooleanProperty direction;
	private ObservableList<ProcessStep> steps;

	public Chain(){
		this(null, null, null, null, null);
	}
	
	public Chain(Integer id, String name, Report report, Boolean direction, ObservableList<ProcessStep> steps){
		this.id = new SimpleIntegerProperty();
		this.id.setValue(id);
		this.name = new SimpleStringProperty(name);
		this.report = new SimpleObjectProperty<Report>();
		this.report.set(report);
		this.direction = new SimpleBooleanProperty();
		this.direction.setValue(direction);
		this.steps = FXCollections.observableArrayList();
		if (steps!=null){
			this.steps.addAll(steps);
		}
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id.get();
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id.set(id);
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
	

	public ObservableList<ProcessStep> getSteps(){
		return steps;
	}
	
	public void setSteps(ObservableList<ProcessStep> steps){
		this.steps= steps;
	}
}
