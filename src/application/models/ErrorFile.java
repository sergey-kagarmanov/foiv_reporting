package application.models;


public class ErrorFile {

	private String file;
	private Integer errorCode;

	
	public ErrorFile(String file, Integer code) {
		this.file = file;
		this.errorCode = code;
	}

	public ErrorFile() {
		this(null, 0);
	}

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @return the errorCode
	 */
	public Integer getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 *            the errorCode to set
	 */
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

}
