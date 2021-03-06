package application.models;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Deprecated
public class ReportFile extends FileEntity {

	private Map<String, FileAttribute> attributes;

	public ReportFile(UUID uuid, String name, LocalDateTime datetime, Report report,
			Boolean direction, ReportFile linkedFile, Map<String, FileAttribute> attributes, FileType fileType) {
		super(uuid, name, datetime, report, direction, linkedFile, fileType);
		this.attributes = attributes;
	}

	public ReportFile() {
		super();
		this.attributes = null;
	}

	/**
	 * @return the attributes
	 */
	public Map<String, FileAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map<String, FileAttribute> attributes) {
		this.attributes = attributes;
	}

}
