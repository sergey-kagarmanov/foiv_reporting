package application.models;

public class ErrorFile {

	public static int VALIDATE_EXCEPTION = 1;

	private String file;
	private Integer errorCode;
	private String msg;

	public ErrorFile(String file, Integer code, String msg) {
		this.file = file;
		this.errorCode = code;
		this.msg = msg;
	}

	public ErrorFile(String file, Integer code) {
		this(file, code, "");
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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
