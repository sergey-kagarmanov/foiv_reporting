package application.utils.skzi;

import application.errors.ReportError;

public interface SignaturaExecutor {

	void init();
	
	void start()  throws ReportError;
	void start(byte[] buffer, int length)  throws ReportError;
	byte[] next(byte[] buffer, int length)  throws ReportError;
	byte[] end()  throws ReportError;
}
