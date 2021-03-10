package application.utils.skzi;

import java.io.File;
import java.util.concurrent.Callable;

import application.models.ErrorFile;
import application.models.FileTransforming;

abstract class OperationHandler implements Callable<ErrorFile> {

	private FileTransforming file;
	
	public OperationHandler(FileTransforming file) {
		this.file = file;
	}
	
	@Override
	public ErrorFile call() throws Exception {
		int result = -1;
		if (file.getErrorCode() == 0) {
			File tmp = new File(file.getCurrentFile().getAbsolutePath() + "_enc");
			result = operation(file.getCurrentFile().getAbsolutePath(),
					file.getCurrentFile().getAbsolutePath() + "_enc");
			if (result != 0) {
				file.setErrorCode(result);
			} else {
				if (file.getCurrentFile().delete())
					tmp.renameTo(file.getCurrentFile());
			}
		}
		if (result==0) {
			return null;
		}else {
			return new ErrorFile(file.getCurrent(), result);
		}
	}

	abstract Integer operation(String source, String target);
}
