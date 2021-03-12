package application.utils.skzi;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import application.models.ErrorFile;
import application.models.Key;

public abstract class SignaturaHandler implements Callable<ErrorFile>{

	protected File file;
	protected File out;

	public SignaturaHandler(Key key) {
		init(key);
	}

	public void setParameters(File file, String path) {
		this.file = file;
		this.out = new File(path+"\\"+file.getName());
		if (out.exists()) {
			out.delete();
		}
		try {
			out.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	abstract void init(Key key);
	abstract void unload();
}
