package application.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProcessStep {

	private IntegerProperty id;
	private ObjectProperty<Action> action;
	private ObjectProperty<ProcessStep> next;
	private ObjectProperty<Key> key;
	private StringProperty data;
	private IntegerProperty position;

	public ProcessStep(){
		this(null, null, null, null, null, null);
	}
	
	public ProcessStep(Integer id, Action action, Key key, ProcessStep next, String data, Integer position){
		this.id = new SimpleIntegerProperty();
		this.id.setValue(id);
		this.action = new SimpleObjectProperty<Action>();
		this.action.set(action);
		this.key = new SimpleObjectProperty<Key>();
		this.key.set(key);
		this.next = new SimpleObjectProperty<ProcessStep>();
		this.next.set(next);
		this.data = new SimpleStringProperty(data);
		this.position = new SimpleIntegerProperty();
		this.position.setValue(position);
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
	 * @return the action
	 */
	public Action getAction() {
		return action.get();
	}

	public ObjectProperty<Action> getActionProperty(){
		return action;
	}
	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(Action action) {
		this.action.set(action);
	}

	public void setActionProperty(ObjectProperty<Action> action){
		this.action = action;
	}
	/**
	 * @return the next
	 */
	public ProcessStep getNext() {
		return next.get();
	}
	
	public ObjectProperty<ProcessStep> getNextProperty(){
		return next;
	}

	/**
	 * @param next
	 *            the next to set
	 */
	public void setNext(ProcessStep next) {
		this.next.set(next);
	}
	
	public void setNextProperty(ObjectProperty<ProcessStep> next){
		this.next = next;
	}

	public Key getKey() {
		return key.get();
	}

	public void setKey(Key key) {
		this.key.set(key);
	}
	
	public void setKeyProperty(ObjectProperty<Key> key){
		this.key = key;
	}

	public String getData(){
		return data.get();
	}
	
	public StringProperty getDataProperty(){
		return data;
	}
	
	public Integer getPosition(){
		return position.get();
	}
	
	public IntegerProperty getPositionProperty(){
		return position;
	}
	
	public void setPosition(Integer position){
		this.position.set(position);
	}

	public void setPositionProperty(IntegerProperty position){
		this.position = position;
	}
	
	public void setData(String data){
		this.data.set(data);
	}
	
	public void setDataProperty(StringProperty data){
		this.data = data;
	}
	
	@Override
	public String toString() {
		return action.getValue()+" "+data.get();
	}
}
