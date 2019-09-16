package application.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Key {

	private IntegerProperty id;
	private StringProperty name;
	private StringProperty data;

	public Key(){
		this(null, null, null);
	}
	
	public Key(Integer id, String name, String data){
		this.id = new SimpleIntegerProperty(id);
		this.name = new SimpleStringProperty(name);
		this.data = new SimpleStringProperty(data);
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id.get();
	}
	
	public IntegerProperty getIdProperty(){
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id.set(id);
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
	 * @return the data
	 */
	public String getData() {
		return data.get();
	}
	
	public StringProperty getDataProperty(){
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(String data) {
		this.data.set(data);
	}
	
	public void setDataProperty(StringProperty data){
		this.data = data;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Key){
			return ((Key)obj).getData().equals(data.get());
		}else{
			return false;
		}
	}
	
	@Override
	public String toString() {
		return name.get();
	}

}
