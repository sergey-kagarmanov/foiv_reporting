package application.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileAttribute {

	private IntegerProperty id;
	private StringProperty name;
	private StringProperty value;

	public FileAttribute(Integer id, String name, String value) {
		this.id = new SimpleIntegerProperty(id);
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(value);
	}
	
	public FileAttribute() {
		this(null, null, null);
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

	public IntegerProperty getIdProperty(){
		return id;
	}
	
	public void setIdProperty(IntegerProperty id){
		this.id = id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name.get();
	}

	public StringProperty getNameProperty(){
		return name;
	}
	
	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name.set(name);
	}
	
	public void setNameProperty(StringProperty name){
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value.get();
	}
	
	public StringProperty getValueProperty(){
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value.set(value);
	}

	public void setValueProperty(StringProperty value){
		this.value = value;
	}
	
	@Override
	public String toString() {
		return name.get();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FileAttribute){
			return ((FileAttribute)obj).getId() == getId();
		}
		return false;
	}
}
