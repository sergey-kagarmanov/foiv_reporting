package application.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class FileType {

	private IntegerProperty id;
	private StringProperty name;
	private StringProperty mask;
	private BooleanProperty direction;
	private BooleanProperty transport;
	private StringProperty vSchema;
	private BooleanProperty ticket;
	private IntegerProperty fileType;
	private ListProperty<TicketResults> results;

	public FileType(){
		this(null, null, null, null, null, null, null, null, null);
	}
	
	public FileType(Integer id, String name, String mask, String vSchema, Boolean direction, Boolean transport, Boolean ticket, Integer fileType, ObservableList<TicketResults> results){
		this.id = new SimpleIntegerProperty();
		this.id.setValue(id);
		this.name = new SimpleStringProperty(name);
		this.mask = new SimpleStringProperty(mask);
		this.direction = new SimpleBooleanProperty();
		this.direction.setValue(direction);
		this.transport = new SimpleBooleanProperty();
		this.transport.setValue(transport);
		this.vSchema = new SimpleStringProperty(vSchema);
		this.ticket = new SimpleBooleanProperty();
		this.ticket.setValue(ticket);
		this.fileType = new SimpleIntegerProperty();
		this.fileType.setValue(fileType);
		this.results = new SimpleListProperty<TicketResults>(results);
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
	 * @return the mask
	 */
	public String getMask() {
		return mask.get();
	}

	public StringProperty getMaskProperty(){
		return mask;
	}
	
	/**
	 * @param mask
	 *            the mask to set
	 */
	public void setMask(String mask) {
		this.mask.set(mask);
	}
	
	public void setMaskProperty(StringProperty mask){
		this.mask = mask;
	}


	/**
	 * @return the direction
	 */
	public Boolean getDirection() {
		return direction.get();
	}

	public BooleanProperty getDirectionProperty(){
		return direction;
	}
	
	/**
	 * @param direction
	 *            the direction to set
	 */
	public void setDirection(Boolean encrypt) {
		this.direction.set(encrypt);
	}

	public void setDirectionProperty(BooleanProperty encrypt){
		this.direction = encrypt;
	}
	
	public Boolean getTransport() {
		return transport.get();
	}
	
	public BooleanProperty getTranportProperty(){
		return transport;
	}

	public void setTransport(Boolean transport) {
		this.transport.set(transport);
	}
	
	public void setTransportProperty(BooleanProperty transport){
		this.transport = transport;
	}
	
	public String getValidationSchema(){
		return vSchema.get();
	}
	
	public StringProperty getValidationSchemaProperty(){
		return vSchema;
	}
	
	public void setValidationSchema(String validationSchema){
		vSchema.set(validationSchema);
	}
	
	public void setValidationSchemaProperty(StringProperty validationSchema){
		vSchema = validationSchema;
	}
	
	public boolean getTicket(){
		return ticket.get();
	}
	
	public void setTicket(Boolean ticket){
		this.ticket.set(ticket);
	}
	
	public BooleanProperty getTicketProperty(){
		return ticket;
	}
	
	public void setTicketProperty(BooleanProperty ticket){
		this.ticket = ticket;
	}
	
	public Integer getFileType(){
		return fileType.get();
	}
	
	public void setFileType(Integer fileType){
		this.fileType.setValue(fileType);
	}
	
	public IntegerProperty getFileTypeProperty(){
		return fileType;
	}
	
	public void setFiletypeProperty(IntegerProperty fileType){
		this.fileType = fileType;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof FileType)
			return ((FileType)arg0).getId().equals(id.get());
		else
			return false;
	}

	@Override
	public int hashCode() {
		return 14*id.get();
	}
	
	@Override
	public String toString() {
		return name.get();
	}
	
	public ObservableList<TicketResults> getResults(){
		return results.get();
	}
	
	public ListProperty<TicketResults> getResultsProperty(){
		return results;
	}
	
	public void setResults(ObservableList<TicketResults> results){
		this.results.set(results);
	}
	
	public void setResultProperty(ListProperty<TicketResults> results){
		this.results = results;
	}
}
