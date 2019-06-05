package application.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TicketResults {

	private IntegerProperty id;
	private ObjectProperty<FileAttribute> attribute;
	private StringProperty value;
	private BooleanProperty accept;
	
	public TicketResults(){
		
	}
	
	public TicketResults(Integer id, FileAttribute attribute, String value, Boolean accept){
		this.id = new SimpleIntegerProperty();
		this.id.setValue(id);
		this.attribute = new SimpleObjectProperty<FileAttribute>(attribute);
		this.value = new SimpleStringProperty(value);
		this.accept = new SimpleBooleanProperty();
		this.accept.setValue(accept);
	}
	
	public Integer getId(){
		return id.get();
	}
	
	public IntegerProperty getIdProperty(){
		return id;
	}
	
	public void setId(Integer id){
		this.id.setValue(id);
	}
	
	public void setIdProperty(IntegerProperty id){
		this.id = id;
	}
	
	public FileAttribute getAttribute(){
		return attribute.get();
	}
	
	public ObjectProperty<FileAttribute> getAttributeProperty(){
		return attribute;
	}
	
	public void setAttribute(FileAttribute attribute){
		this.attribute.set(attribute);
	}
	
	public void setAttributeProperty(ObjectProperty<FileAttribute> attribute){
		this.attribute = attribute;
	}
	
	public String getValue(){
		return value.get();
	}
	
	public StringProperty getValueProperty(){
		return value;
	}
	
	public void setValue(String value){
		this.value.set(value);
	}
	
	public void setValueProperty(StringProperty value){
		this.value = value;
	}
	
	public Boolean getAccept(){
		return accept.get();
	}
	
	public BooleanProperty getAcceptProperty(){
		return accept;
	}
	
	public void setAccept(Boolean accept){
		this.accept.setValue(accept);
	}
	
	public void setAcceptProperty(BooleanProperty accept){
		this.accept = accept;
	}
	
}
