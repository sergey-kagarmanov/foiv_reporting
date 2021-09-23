package application.errors;

public class ReportError extends Exception {

	public static final int INIT_SIGNATURA = 1;
	public static final int SIGN_ERROR = 2;
	public static final int UNSIGN_ERROR = 3;
	public static final int ENCRYPT_ERROR = 4;
	public static final int DECRYPT_ERROR = 5;
	public static final int UNHANDLE_SIGNATURA_ERROR = 6;
	public static final int FILE_ERROR = 7;
	public static final int SQL_ERROR = 8;
	public static final int UNKNOWN_FILETYPE = 9;
	public static final int XML_PARSE_ERROR = 10;
	public static final int SETUP_ERROR = 11;
	public static final int RUNTIME_ERROR = 12;
	
	private int errorCode;
	private String message;
	private String filename;
	private Exception e;
	
	public ReportError(int code){
		this(code, null, null, null);
	}
	
	public ReportError(int code, String text){
		this(code, text, null, null);
	}
	
	public ReportError(int code, String text, String filename){
		this(code, text, null, null);
	}
	
	public ReportError(int code, String text, String filename, Exception e){
		super();
		this.errorCode = code;
		this.message = "ВНИМАНИЕ!!!\n Обработка была прервана из-за следующих ошибок: "+ text;
		this.filename = filename;
	}
	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}
	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @return the message
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param message the message to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Exception getE() {
		return e;
	}

	public void setE(Exception e) {
		this.e = e;
	}
	
}
