package application.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Info for attributes stored as way how them could be searched in file. If
 * inName `true` `value` contains regular expression to get attribute value from
 * filename. If `inName` - `false` so value contain path to find value in xml or
 * another document. Delimiter would be `|` and last is attribute name in tag
 * 
 * @author admin
 *
 */
public class AttributeDescr {

	public static final String NAME = "name";
	public static final String CODE = "code";
	public static final String DATETIME = "datetime";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String COMMENT = "comment";
	public static final String PARENT = "parent";
	public static final String ID = "id";
	public static final String PARENT_ID = "parent_id";

	private IntegerProperty id;
	private BooleanProperty inName;
	private StringProperty etc;
	private ObjectProperty<FileAttribute> attr;
	private StringProperty location;
	private ObjectProperty<FileType> file;

	public AttributeDescr(Integer id, Boolean inName, String etc, FileAttribute attr,
			String location, FileType file) {
		this.id = new SimpleIntegerProperty();
		this.id.setValue(id);
		this.inName = new SimpleBooleanProperty();
		this.inName.setValue(inName);
		this.etc = new SimpleStringProperty(etc);
		this.attr = new SimpleObjectProperty<FileAttribute>(attr);
		this.location = new SimpleStringProperty(location);
		this.file = new SimpleObjectProperty<FileType>(file);

	}

	public AttributeDescr() {
		this(null, null, null, null, null, null);
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
		this.id.setValue(id);
	}

	public IntegerProperty getIdProperty(){
		return id;
	}
	
	public void setIdProperty(IntegerProperty id){
		this.id = id;
	}
	/**
	 * @return the inName
	 */
	public Boolean getInName() {
		return inName.get();
	}

	/**
	 * @param inName
	 *            the inName to set
	 */
	public void setInName(Boolean inName) {
		this.inName.setValue(inName);
	}
	
	public BooleanProperty getInNameProperty(){
		return inName;
	}
	
	public void setInNameProperty(BooleanProperty inName){
		this.inName = inName;
	}

	/**
	 * @return the etc
	 */
	public String getEtc() {
		return etc.get();
	}

	/**
	 * @param etc
	 *            the etc to set
	 */
	public void setEtc(String etc) {
		this.etc.set(etc);
	}

	public StringProperty getEtcProperty(){
		return etc;
	}
	
	public void setEtcProperty(StringProperty etc){
		this.etc = etc;
	}
	/**
	 * @return the attr
	 */
	public FileAttribute getAttr() {
		return attr.get();
	}

	/**
	 * @param attr
	 *            the attr to set
	 */
	public void setAttr(FileAttribute attr) {
		this.attr.set(attr);
	}

	public ObjectProperty<FileAttribute> getAttrProperty(){
		return attr;
	}
	
	public void setAttrProperty(ObjectProperty<FileAttribute> attr){
		this.attr = attr;
	}
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location.get();
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(String location) {
		this.location.set(location);
	}

	public StringProperty getLocationProperty(){
		return location;
	}
	
	public void setLocationProperty(StringProperty location){
		this.location = location;
	}
	/**
	 * @return the file
	 */
	public FileType getFile() {
		return file.get();
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(FileType file) {
		this.file.set(file);
	}
	
	public ObjectProperty<FileType> getFileProperty(){
		return file;
	}
	
	public void setFileProperty(ObjectProperty<FileType> file){
		this.file = file;
	}

	@Override
	public String toString() {
		return attr.toString();
	}
}
