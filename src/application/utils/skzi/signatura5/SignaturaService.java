package application.utils.skzi.signatura5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import application.models.ErrorFile;
import application.models.FileTransforming;
import application.models.Key;
import application.utils.FileUtils;
import javafx.collections.ObservableList;

public class SignaturaService {

	private Signatura signatura;
	private Key key;
	
	public SignaturaService(Key key) {
		this.signatura = new Signatura();
		initSignatura(key);
	}
	
	private int initSignatura(Key key) {
		int result = 0;
		if (this.key == null || !this.key.equals(key)) {
			result = signatura.initConfig(key.getData());
			this.key = key;
		} else {
			result = 0;
		}

		if (result == 0) {
			signatura.setParameters();
		}
		return result;
	}


	public ObservableList<ErrorFile> encrypt(Collection<FileTransforming> files) {
		SignaturaServiceAbstract service = new SignaturaServiceAbstract() {
			
			@Override
			public int action(String source, String target) {
				return signatura.encrypt(source, target);
			}
		};
		try {
			return service.execute(files);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ObservableList<ErrorFile> sign(Collection<FileTransforming> files) {
		SignaturaServiceAbstract service = new SignaturaServiceAbstract() {
			
			@Override
			public int action(String source, String target) {
				return signatura.signFile(source, target);
			}
		};
		try {
			return service.execute(files);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ObservableList<ErrorFile> decrypt(Collection<FileTransforming> files) {
		SignaturaServiceAbstract service = new SignaturaServiceAbstract() {
			
			@Override
			public int action(String source, String target) {
				File tmp = new File(target+"tmp");
				int result = -1;
				try {
					result = signatura.decrypt(new FileInputStream(new File(source)), new FileOutputStream(tmp));
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					FileUtils.decompressGzip(tmp, new File(target));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//int result = signatura.decrypt(source, tmp.getAbsolutePath());
				/*if (result == 0) {
						try {
							result = decompressGzip(tmp, new File(target));
						} catch (IOException e) {
							e.printStackTrace();
						}
				}*/
				return result;
			}
		};
		try {
			return service.execute(files);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ObservableList<ErrorFile> unsign(Collection<FileTransforming> files) {
		SignaturaServiceAbstract service = new SignaturaServiceAbstract() {
			
			@Override
			public int action(String source, String target) {
				return signatura.verifySign(source, target);
			}
		};
		try {
			return service.execute(files);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void unload() {
		signatura.unload();
	}
	

}
